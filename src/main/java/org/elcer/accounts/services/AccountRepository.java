package org.elcer.accounts.services;


import com.google.common.annotations.VisibleForTesting;
import org.elcer.accounts.db.Transaction;
import org.elcer.accounts.exceptions.AccountNotFoundException;
import org.elcer.accounts.hk2.annotations.Component;
import org.elcer.accounts.hk2.annotations.PersistenceUnit;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.utils.CriteriaUtils;
import org.elcer.accounts.utils.ExceptionUtils;

import javax.inject.Inject;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
@SuppressWarnings("WeakerAccess")
public class AccountRepository {

    @PersistenceUnit(name = "accounts")
    @Inject
    private EntityManagerFactory efFactory;

    public Account createAccount(Account account) {
        return wrapInTran(tran -> {
            tran.getEm().persist(account);
            return account;
        });
    }

    public Account createAccount(Transaction tran, Account account) {
        tran.getEm().persist(account);
        return account;
    }


    @VisibleForTesting
    public Account createAccount(String name, BigDecimal amount) {
        return createAccount(new Account(name, amount));
    }


    Transaction beginTran() {
        return new Transaction(efFactory);
    }

    @VisibleForTesting
    public List<Account> getAllAccounts(int page, int size) {
        return wrapInTran((Function<Transaction, List<Account>>) tran ->
                getAllAccounts(tran, page, size));
    }


    List<Account> getAllAccounts(Transaction tran, int page, int size) {
        var builder = tran.getEm().getCriteriaBuilder();
        var q = builder.createQuery(Account.class);
        Root<Account> root = q.from(Account.class);
        q.select(root);
        var query = tran.getEm().createQuery(q);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    public Account retrieveAccountById(long id) {
        return wrapInTran((Function<Transaction, Account>)
                tran -> retrieveAccountById(tran, id));
    }

    public List<Account> retrieveAccountsByName(String name, int page, int size) {
        return wrapInTran((Function<Transaction, List<Account>>)
                tran -> retrieveAccountByName(tran, name, page, size));
    }

    private List<Account> retrieveAccountByName(Transaction tran, String name, int page, int size) {
        return _retrieveAccountsByName(tran.getEm(), name, page, size);
    }

    private List<Account> _retrieveAccountsByName(EntityManager em, String name, int page, int size) {
        return CriteriaUtils.createQuery(em, Account.class,
                (builder, root) -> builder.equal(root.get("name"), name))
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

    }

    Account retrieveAccountById(Transaction tran, long id) {
        return _retrieveAccountById(tran.getEm(), id);
    }

    private Account _retrieveAccountById(EntityManager em, long id) {
        try {
            return CriteriaUtils.createQuery(em, Account.class,
                    (builder, root) -> builder.equal(root.get("id"), id)).getSingleResult();
        } catch (NoResultException e) {
            throw new AccountNotFoundException(id);
        }
    }


    public void addBalance(@NotNull Account account, BigDecimal balance) {
        wrapInTran((Consumer<Transaction>) tran -> addBalance(tran, account, balance));
    }

    public void setBalance(Transaction tran, Account account, BigDecimal amount) {
        _setBalance(tran.getEm(), account, amount);
    }

    public void addBalance(Transaction tran, Account account, BigDecimal amount) {
        _addBalance(tran.getEm(), account, amount);
    }


    private void _setBalance(EntityManager em, Account account, BigDecimal amount) {
        var builder = em.getCriteriaBuilder();
        var update = builder.createCriteriaUpdate(Account.class);
        var root = update.from(Account.class);
        var balancePath = root.get("balance");
        update.set(balancePath, amount);
        update.where(builder.equal(root.get("id"), account.getId()));
        Query query = em.createQuery(update);
        query.executeUpdate();
    }


    private void _addBalance(EntityManager em, Account account, BigDecimal amount) {
        var builder = em.getCriteriaBuilder();
        var update = builder.createCriteriaUpdate(Account.class);
        var root = update.from(Account.class);
        var balancePath = root.<BigDecimal>get("balance");
        var eventualBalance = builder.sum(balancePath, amount);
        update.set(balancePath, eventualBalance);

        update.where(builder.equal(root.get("id"), account.getId()));
        Query query = em.createQuery(update);
        query.executeUpdate();
    }


    public void deleteAccount(long id) {
        wrapInTran((Consumer<Transaction>) tran -> deleteAccount(tran, id));
    }

    public void deleteAccount(Transaction tran, long id) {
        var builder = tran.getEm().getCriteriaBuilder();
        CriteriaDelete<Account> delete = builder.createCriteriaDelete(Account.class);
        Root<Account> root = delete.from(Account.class);
        delete.where(builder.equal(root.get("id"), id));
        tran.getEm().createQuery(delete).executeUpdate();

    }


    //Automatic resource disposing

    private <R> R wrapInTran(Function<Transaction, R> delegate) {
        var tran = beginTran();
        return ExceptionUtils.wrap(delegate, () -> {
            try {
                tran.commit();
            } catch (RollbackException e) {
                tran.rollback();
            }
            tran.getEm().close();

        }, tran);
    }

    private void wrapInTran(Consumer<Transaction> delegate) {
        var tran = beginTran();
        ExceptionUtils.wrap(delegate, () -> {
            try {
                tran.commit();
            } catch (RollbackException e) {
                tran.rollback();
            }
            tran.getEm().close();
        }, tran);
    }


}

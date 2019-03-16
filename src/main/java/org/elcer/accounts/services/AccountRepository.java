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
import javax.persistence.criteria.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class AccountRepository  {

    @PersistenceUnit(name = "accounts")
    @Inject
    private EntityManagerFactory efFactory;

    public Account createAccount(Account account) {
        return wrapInTran(tran -> {
            tran.getEm().persist(account);
            return account;
        });
    }

    @VisibleForTesting
    public Account createAccount(String name, BigDecimal amount) {
        Account account = new Account(name, amount);
        return wrapInTran(tran -> {
            tran.getEm().persist(account);
            return account;
        });
    }


    Transaction beginTran() {
        return new Transaction(efFactory);
    }

    @VisibleForTesting
    public List<Account> getAllAccounts() {
        return wrapInTran((Function<Transaction, List<Account>>) this::getAllAccounts);
    }

    @SuppressWarnings("WeakerAccess")
    List<Account> getAllAccounts(Transaction tran) {
        CriteriaBuilder builder = tran.getEm().getCriteriaBuilder();
        CriteriaQuery<Account> q = builder.createQuery(Account.class);
        TypedQuery<Account> query = tran.getEm().createQuery(q);
        return query.getResultList();
    }

    public Account retrieveAccountById(long id) {
        return wrapInTran((Function<Transaction, Account>)
                tran -> retrieveAccountById(tran, id));
    }

    public List<Account> retrieveAccountsByName(String name) {
        return wrapInTran((Function<Transaction, List<Account>>)
                tran -> retrieveAccountByName(tran, name));
    }

    private List<Account> retrieveAccountByName(Transaction tran, String name) {
        return _retrieveAccountsByName(tran.getEm(), name);
    }

    private List<Account> _retrieveAccountsByName(EntityManager em, String name) {
        return CriteriaUtils.createQuery(em, Account.class,
                (builder, root) -> builder.equal(root.get("name"), name))
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

    void setBalance(Transaction tran, Account account, BigDecimal amount) {
        _setBalance(tran.getEm(), account, amount);
    }

    void addBalance(Transaction tran, Account account, BigDecimal amount) {
        _addBalance(tran.getEm(), account, amount);
    }


    private void _setBalance(EntityManager em, Account account, BigDecimal amount) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaUpdate<Account> update = builder.createCriteriaUpdate(Account.class);
        Root<Account> root = update.from(Account.class);
        Path<BigDecimal> balancePath = root.get("balance");
        update.set(balancePath, amount);
        update.where(builder.equal(root.get("id"), account.getId()));
        Query query = em.createQuery(update);
        query.executeUpdate();
    }



    private void _addBalance(EntityManager em, Account account, BigDecimal amount) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaUpdate<Account> update = builder.createCriteriaUpdate(Account.class);
        Root<Account> root = update.from(Account.class);
        Path<BigDecimal> balancePath = root.get("balance");
        Expression<BigDecimal> eventualBalance = builder.sum(balancePath, amount);
        update.set(balancePath, eventualBalance);

        update.where(builder.equal(root.get("id"), account.getId()));
        Query query = em.createQuery(update);
        query.executeUpdate();
    }



    //Automatic resource disposing

    private <R> R wrapInTran(Function<Transaction, R> delegate) {
        Transaction tran = beginTran();
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
        Transaction tran = beginTran();
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

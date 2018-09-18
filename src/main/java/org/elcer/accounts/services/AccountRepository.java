package org.elcer.accounts.services;


import com.google.common.annotations.VisibleForTesting;
import org.elcer.accounts.db.Transaction;
import org.elcer.accounts.exceptions.NoAccountException;
import org.elcer.accounts.hk2.CustomInject;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.utils.ExceptionUtils;
import org.jvnet.hk2.annotations.Service;

import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class AccountRepository {

    @CustomInject(name = "accounts")
    private EntityManagerFactory efFactory;

    @VisibleForTesting
    public Account createAccount(long amount) {
        Account account = new Account(amount);
        return wrap(tran -> {
            tran.getEm().persist(account);
            return account;
        });
    }


    Transaction beginTran() {
        return new Transaction(efFactory);
    }

    @VisibleForTesting
    public List<Account> getAllAccounts() {
        return wrap(this::getAllAccountsWithTran);
    }

    @SuppressWarnings("WeakerAccess")
    List<Account> getAllAccountsWithTran(Transaction tran) {
        CriteriaBuilder builder = tran.getEm().getCriteriaBuilder();
        CriteriaQuery<Account> q = builder.createQuery(Account.class);
        TypedQuery<Account> query = tran.getEm().createQuery(q);
        return query.getResultList();
    }

    public Account retrieveAccountById(long id) {
        return wrap((Function<Transaction, Account>)
                tran -> retrieveAccountByIdWithTran(tran, id));
    }


    Account retrieveAccountByIdWithTran(Transaction tran, long id) {
        return _retrieveAccountById(tran.getEm(), id);
    }

    private Account _retrieveAccountById(EntityManager em, long id) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Account> q = builder.createQuery(Account.class);
        Root<Account> root = q.from(Account.class);
        q.select(root).where(builder.equal(root.get("id"), id));
        TypedQuery<Account> query = em.createQuery(q);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            throw new NoAccountException(id);
        }
    }




    public void updateAccount(@NotNull Account account, long balance) {
        wrap((Consumer<Transaction>) tran -> updateAccountWithTran(tran, account, balance));
    }

    void updateAccountWithTran(Transaction tran, Account account, long amount) {
        _updateAccount(tran.getEm(), account, amount);
    }

    private void _updateAccount(EntityManager em, Account account, long amount) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaUpdate<Account> update = builder.createCriteriaUpdate(Account.class);
        Root<Account> root = update.from(Account.class);
        Path<Long> balancePath = root.get("balance");
        Expression<Long> eventualBalance = builder.sum(balancePath, amount);
        update.set(balancePath, eventualBalance);

        update.where(builder.equal(root.get("id"), account.getId()));
        Query query = em.createQuery(update);
        query.executeUpdate();
    }


    //Resource disposing

    private <R> R wrap(Function<Transaction, R> delegate) {
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

    private void wrap(Consumer<Transaction> delegate) {
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

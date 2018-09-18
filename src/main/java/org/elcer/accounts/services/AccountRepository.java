package org.elcer.accounts.services;


import com.google.common.annotations.VisibleForTesting;
import org.elcer.accounts.eclipselink.Transaction;
import org.elcer.accounts.exceptions.NoAccountException;
import org.elcer.accounts.hk2.CustomInject;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.utils.ExceptionUtils;
import org.jvnet.hk2.annotations.Service;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
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
        return wrap(em -> {
            em.persist(account);
            return account;
        });
    }


    Transaction beginTran() {
        EntityManager em = efFactory.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        return new Transaction(transaction, em);
    }

    @VisibleForTesting
    public List<Account> getAllAccounts() {
        return wrap(this::getAllAccountsWithTran);
    }

    @SuppressWarnings("WeakerAccess")
    List<Account> getAllAccountsWithTran(EntityManager em) {
        checkTranActive(em, "getAllAccountsWithTran");

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Account> q = builder.createQuery(Account.class);
        TypedQuery<Account> query = em.createQuery(q);
        return query.getResultList();
    }

    public Account retrieveAccountById(long id) {
        return wrap((Function<EntityManager, Account>)
                em -> retrieveAccountByIdWithTran(em, id));
    }

    private void checkTranActive(EntityManager em, String method) {
        if (!em.getTransaction().isActive()) {
            throw new IllegalStateException(String.format("%s must be in transaction", method));
        }
    }

    Account retrieveAccountByIdWithTran(EntityManager em, long id) {
        checkTranActive(em, "retrieveAccountByIdWithTran");
        return _retrieveAccountById(em, id);
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


    void updateAccountWithTran(EntityManager em, long accountId, long amount) {
        checkTranActive(em, "updateAccount");

        em.createQuery("UPDATE Account a SET a.balance = a.balance + :amount where a.id = :accountId")
                .setParameter("amount", amount)
                .setParameter("accountId", accountId)
                .executeUpdate();

    }


    //Non atomic methods. Use only for tests
    @VisibleForTesting
    public void updateAccountNonAtomic(@NotNull Account account) {
        wrap((Consumer<EntityManager>) em -> updateAccountWithNonAtomicAtTran(em, account));
    }

    void updateAccountWithNonAtomicAtTran(EntityManager em, Account account) {
        checkTranActive(em, "updateAccountWithTran");
        _updateAccountNonAtomic(em, account);
    }

    private void _updateAccountNonAtomic(EntityManager em, Account account) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaUpdate<Account> update = builder.createCriteriaUpdate(Account.class);
        Root root = update.from(Account.class);
        update.set("balance", account.getBalance());
        update.where(builder.equal(root.get("id"), account.getId()));
        Query query = em.createQuery(update);
        query.executeUpdate();
    }



    //Resource disposing

    private <R> R wrap(Function<EntityManager, R> delegate) {
        EntityManager em = efFactory.createEntityManager();
        em.getTransaction().begin();
        return ExceptionUtils.wrap(delegate, () -> {
            try {
                em.getTransaction().commit();
            } catch (RollbackException e) {
                em.getTransaction().rollback();
            }
            em.close();

        }, em);
    }

    private void wrap(Consumer<EntityManager> delegate) {
        EntityManager em = efFactory.createEntityManager();
        em.getTransaction().begin();
        ExceptionUtils.wrap(delegate, () -> {
            try {
                em.getTransaction().commit();
            } catch (RollbackException e) {
                em.getTransaction().rollback();
            }
            em.close();
        }, em);
    }


}

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
        return wrap(this::_getAllAccounts);
    }

    @SuppressWarnings("WeakerAccess")
    List<Account> _getAllAccounts(EntityManager em) {
        if (!em.getTransaction().isActive()) {
            throw new IllegalStateException("_getAllAccounts must be in transaction");
        }

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Account> q = builder.createQuery(Account.class);
        TypedQuery<Account> query = em.createQuery(q);
        return query.getResultList();
    }

    public Account retrieveAccountById(long id) {
        return wrap((Function<EntityManager, Account>)
                em -> _retrieveAccountById(em, id));
    }

    Account _retrieveAccountById(EntityManager em, long id) {
        if (!em.getTransaction().isActive()) {
            throw new IllegalStateException("_retrieveAccountById must be in transaction");
        }

        return __retrieveAccountById(em, id);
    }

    private Account __retrieveAccountById(EntityManager em, long id) {
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

    void updateAccount(@NotNull Account account) {
        this.wrap((Consumer<EntityManager>) em -> _updateAccount(em, account));
    }

    void _updateAccount(EntityManager em, Account account) {
        if (!em.getTransaction().isActive()) {
            throw new IllegalStateException("_updateAccount must be in transaction");
        }
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaUpdate<Account> update = builder.createCriteriaUpdate(Account.class);
        Root root = update.from(Account.class);
        update.set("balance", account.getBalance());
        update.where(builder.equal(root.get("id"), account.getId()));
        Query query = em.createQuery(update);
        query.executeUpdate();
    }


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


    void addFunds(EntityManager em, long acc, long amount) {
        em.createQuery("UPDATE Account a SET a.balance = a.balance + :amount where a.id = :acc")
                .setParameter("amount", amount)
                .setParameter("acc", acc)
                .executeUpdate();

    }
}

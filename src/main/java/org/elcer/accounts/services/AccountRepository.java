package org.elcer.accounts.services;


import org.elcer.accounts.db.Transaction;
import org.elcer.accounts.exceptions.AccountNotFoundException;
import org.elcer.accounts.hk2.annotations.Component;
import org.elcer.accounts.hk2.annotations.PersistenceUnit;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.utils.CriteriaUtils;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

@Component
@SuppressWarnings("WeakerAccess")
public class AccountRepository {

    private static final String BALANCE_FIELD = "balance",
            NAME_FIELD = "name",
            ID_FIELD = "id";


    @PersistenceUnit(name = "accounts")
    @Inject
    private EntityManagerFactory efFactory;


    public Account save(Account account) {
        return wrapInTran(tran -> {
            tran.getEm().persist(account);
            return account;
        });
    }

    public Account save(Transaction tran, Account account) {
        EntityManager entityManager = tran.getEm();
        if (account.getId() == null) {
            entityManager.persist(account);
            return account;
        }
        return entityManager.merge(account);
    }


    Transaction beginTran() {
        return new Transaction(efFactory);
    }

    public List<Account> getAllAccounts(int page, int size) {
        return wrapInTran(tran -> getAllAccounts(tran, page, size));
    }


    List<Account> getAllAccounts(Transaction tran, int page, int size) {
        EntityManager entityManager = tran.getEm();
        var builder = entityManager.getCriteriaBuilder();
        var q = builder.createQuery(Account.class);
        Root<Account> root = q.from(Account.class);
        q.select(root);
        var query = entityManager.createQuery(q);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    public Account retrieveAccountById(long id) {
        return wrapInTran(tran -> retrieveAccountById(tran, id));
    }

    public List<Account> retrieveAccountsByName(String name, int page, int size) {
        return wrapInTran(tran -> retrieveAccountByName(tran, name, page, size));
    }

    private List<Account> retrieveAccountByName(Transaction tran, String name, int page, int size) {
        return _retrieveAccountsByName(tran.getEm(), name, page, size);
    }

    private List<Account> _retrieveAccountsByName(EntityManager em, String name, int page, int size) {
        return CriteriaUtils.createQuery(em, Account.class,
                (builder, root) -> builder.equal(root.get(NAME_FIELD), name))
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

    }


    public Account retrieveAccountById(Transaction tran, long id) {
        return _retrieveAccountById(tran.getEm(), id);
    }


    public long countAccounts(String name) {
        return wrapInTran(tran -> _countAccounts(tran.getEm(), name));
    }

    public long countAccounts() {
        return wrapInTran(tran -> _countAccounts(tran.getEm()));
    }


    private Account _retrieveAccountById(EntityManager em, long id) {
        Account account = em.find(Account.class, id);
        if (account == null) {
            throw new AccountNotFoundException(id);
        }
        return account;
    }


    public void deleteAccount(long id) {
        wrapInTran(tran -> {
            deleteAccount(tran, id);
            return null;
        });
    }

    public void deleteAccount(Account account) {
        wrapInTran(tran -> {
            deleteAccount(tran, account);
            return null;
        });
    }

    public void deleteAccount(Transaction tran, long id) {
        var builder = tran.getEm().getCriteriaBuilder();
        CriteriaDelete<Account> delete = builder.createCriteriaDelete(Account.class);
        Root<Account> root = delete.from(Account.class);
        delete.where(builder.equal(root.get(ID_FIELD), id));
        tran.getEm().createQuery(delete).executeUpdate();
    }

    public void deleteAccount(Transaction tran, Account account) {
        EntityManager entityManager = tran.getEm();
        entityManager.remove(entityManager.contains(account) ? account : entityManager.merge(account));
    }


    /* Deprecated.  Entity setters should be used */

    @Deprecated
    public void addBalance(@NotNull Account account, BigDecimal balance) {
        wrapInTran(tran -> {
            addBalance(tran, account, balance);
            return null;
        });
    }

    @Deprecated
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
        var balancePath = root.get(BALANCE_FIELD);
        update.set(balancePath, amount);
        update.where(builder.equal(root.get(ID_FIELD), account.getId()));
        Query query = em.createQuery(update);
        query.executeUpdate();
    }


    private void _addBalance(EntityManager em, Account account, BigDecimal amount) {
        var builder = em.getCriteriaBuilder();
        var update = builder.createCriteriaUpdate(Account.class);
        var root = update.from(Account.class);
        var balancePath = root.<BigDecimal>get(BALANCE_FIELD);
        var eventualBalance = builder.sum(balancePath, amount);
        update.set(balancePath, eventualBalance);

        update.where(builder.equal(root.get(ID_FIELD), account.getId()));
        Query query = em.createQuery(update);
        query.executeUpdate();
    }

    private long _countAccounts(EntityManager em, String name) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        var root = query.from(Account.class);
        query.select(builder.count(query.from(Account.class)));
        query.where(builder.equal(root.get(NAME_FIELD), name));
        return em.createQuery(query).getSingleResult();
    }

    private long _countAccounts(EntityManager em) {
        var builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        query.select(builder.count(query.from(Account.class)));
        return em.createQuery(query).getSingleResult();
    }

    //Automatic resource disposal

    private <R> R wrapInTran(Function<Transaction, R> delegate) {
        try (var tran = beginTran()) {
            return delegate.apply(tran);
        }
    }

}

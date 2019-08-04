package org.elcer.accounts.services;


import org.elcer.accounts.db.CriteriaUtils;
import org.elcer.accounts.db.Transaction;
import org.elcer.accounts.hk2.annotations.Component;
import org.elcer.accounts.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
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


    @PersistenceUnit(unitName = "accounts")
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


    public Transaction beginTran() {
        return new Transaction(efFactory);
    }

    public List<Account> findAll(int page, int size) {
        return wrapInTran(tran -> findAll(tran, page, size));
    }


    List<Account> findAll(Transaction tran, int page, int size) {
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

    public Account findById(long id) {
        return wrapInTran(tran -> findById(tran, id));
    }

    public List<Account> findByName(String name, int page, int size) {
        return wrapInTran(tran -> findByName(tran, name, page, size));
    }

    private List<Account> findByName(Transaction tran, String name, int page, int size) {
        return findByName(tran.getEm(), name, page, size);
    }

    private List<Account> findByName(EntityManager em, String name, int page, int size) {
        return CriteriaUtils.createQuery(em, Account.class,
                (builder, root) -> builder.equal(root.get(NAME_FIELD), name))
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

    }


    public Account findById(Transaction tran, long id) {
        return findById(tran.getEm(), id);
    }


    public long countAccounts(String name) {
        return wrapInTran(tran -> _countAccounts(tran.getEm(), name));
    }

    public long countAccounts() {
        return wrapInTran(tran -> _countAccounts(tran.getEm()));
    }


    private Account findById(EntityManager em, long id) {
        return em.find(Account.class, id);
    }


    /**
     * Delete account by id
     * @param id account id
     */
    public void deleteById(long id) {
        wrapInTran(tran -> {
            delete(tran, id);
            return null;
        });
    }

    /**
     * Delete account directly
     * @param account account to delete
     */

    public void delete(Account account) {
        wrapInTran(tran -> {
            delete(tran, account);
            return null;
        });
    }

    public void delete(Transaction tran, long id) {
        EntityManager entityManager = tran.getEm();
        var builder = entityManager.getCriteriaBuilder();
        CriteriaDelete<Account> delete = builder.createCriteriaDelete(Account.class);
        Root<Account> root = delete.from(Account.class);
        delete.where(builder.equal(root.get(ID_FIELD), id));
        entityManager.createQuery(delete).executeUpdate();
    }

    public void delete(Transaction tran, Account account) {
        EntityManager entityManager = tran.getEm();

        Account attachedAccount = entityManager.contains(account) ? account : entityManager.merge(account);
        entityManager.remove(attachedAccount);
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

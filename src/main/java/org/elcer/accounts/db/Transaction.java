package org.elcer.accounts.db;

import org.eclipse.persistence.sessions.UnitOfWork;
import org.elcer.accounts.utils.ExceptionUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

/*
 Wrapper over entity transaction to safely dispose it
 */

public class Transaction implements AutoCloseable {

    private final EntityTransaction delegate;
    private final EntityManager em;


    public Transaction(EntityManagerFactory entityManagerFactory) {
        em = entityManagerFactory.createEntityManager();
        delegate = em.getTransaction();
        delegate.begin();
       em.unwrap(UnitOfWork.class).beginEarlyTransaction();
    }

    @Override
    public void close() {
        ExceptionUtils.wrap(() -> {
            if (delegate.isActive()) {
                delegate.rollback();
            }
        }, em::close);
    }

    public EntityManager getEm() {
        return em;
    }

    public void commit() {
        delegate.commit();
    }

    public void rollback() {
        delegate.rollback();
    }
}

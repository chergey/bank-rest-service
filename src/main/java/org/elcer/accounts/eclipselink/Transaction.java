package org.elcer.accounts.eclipselink;

import org.elcer.accounts.utils.ExceptionUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/*
 Wrapper over entity transaction to safely dispose it
 */

public class Transaction implements AutoCloseable {

    private EntityTransaction delegate;
    private EntityManager em;

    public Transaction(EntityTransaction delegate, EntityManager em) {
        this.delegate = delegate;
        this.em = em;
    }

    @Override
    public void close() {
        ExceptionUtils.wrap(() -> {
            if (delegate.isActive()) {
                delegate.rollback();
            }
        }, () -> em.close());
    }

    public EntityManager getEm() {
        return em;
    }

    public void commit() {
        delegate.commit();
    }
}

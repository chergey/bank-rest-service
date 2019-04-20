package org.elcer.accounts.db;

import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.sessions.server.ClientSession;
import org.eclipse.persistence.sessions.server.ServerSession;
import org.elcer.accounts.utils.ExceptionUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

/**
 * Wrapper over entity transaction to safely dispose it
 * Need call commit() to commit it
 * Using ServerSession#acquireClientConnection to immediately start a transaction
 * @see ServerSession#acquireClientConnection(ClientSession)
 */

public class Transaction implements AutoCloseable {

    private final EntityTransaction delegate;
    private final EntityManager em;


    public Transaction(EntityManagerFactory entityManagerFactory) {
        em = entityManagerFactory.createEntityManager();
        delegate = em.getTransaction();
        delegate.begin();
        UnitOfWork uow = em.unwrap(UnitOfWork.class);
        uow.beginEarlyTransaction();

        AbstractSession clientSession = uow.getParent();
        if (clientSession instanceof ClientSession) {
            AbstractSession serverSession = clientSession.getParent();
            if (serverSession instanceof ServerSession)
                ((ServerSession) serverSession).acquireClientConnection((ClientSession) clientSession);
        }
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

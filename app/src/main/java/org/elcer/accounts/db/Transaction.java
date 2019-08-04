package org.elcer.accounts.db;

import io.vavr.control.Try;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.sessions.server.ClientSession;
import org.eclipse.persistence.sessions.server.ServerSession;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

/**
 * Wrapper over entity transaction to safely dispose it
 * Using ServerSession#acquireClientConnection to immediately start a transaction
 *
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
        Try.run(() -> {
            if (delegate.isActive()) {
                delegate.commit();
            }
        }).fold(e -> {
                    Try.run(em::close)
                            .onFailure(e::addSuppressed);
                    sneakyThrow(e);
                    return null;
                },
                e -> {
                    em.close();
                    return null;
                }
        );
    }

    public EntityManager getEm() {
        return em;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

}

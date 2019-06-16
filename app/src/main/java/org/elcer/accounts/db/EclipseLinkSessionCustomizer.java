package org.elcer.accounts.db;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.Session;


public class EclipseLinkSessionCustomizer implements SessionCustomizer {
    @Override
    public void customize(Session session) {
        var databaseLogin = (DatabaseLogin) session.getDatasourceLogin();
        databaseLogin.setTransactionIsolation(DatabaseLogin.TRANSACTION_SERIALIZABLE);
    }
}
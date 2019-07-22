package org.elcer.accounts.api;

import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;

public class MockInitialContextFactory implements InitialContextFactory {

    private static Context globalContext;

    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) {
        return globalContext;
    }

    public static void setGlobalContext(Context context) {
        globalContext = context;
    }

    static void clearCurrentContext() {
        globalContext = null;
    }

}


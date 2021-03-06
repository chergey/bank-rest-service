package org.elcer.accounts.db.cache;


import org.eclipse.persistence.internal.identitymaps.IdentityMap;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.elcer.accounts.cache.DefaultCacheInterceptor;
import org.elcer.accounts.model.Account;

public class AccountCacheInterceptor extends DefaultCacheInterceptor {

    private static AccountCacheSupport cacheSupport;

    private static final String CACHE_NAME = Account.class.getName();

    public AccountCacheInterceptor(IdentityMap targetIdentityMap, AbstractSession interceptedSession) {
        super(targetIdentityMap, interceptedSession, CACHE_NAME, cacheSupport);
    }

    static void setCacheSupport(AccountCacheSupport cacheSupport) {
        AccountCacheInterceptor.cacheSupport = cacheSupport;
    }
}
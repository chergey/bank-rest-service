package org.elcer.accounts.cache;

import org.elcer.accounts.hk2.annotations.Component;
import org.elcer.accounts.hk2.annotations.Eager;
import org.elcer.accounts.hk2.annotations.NoTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;


@Component
@Eager
@NoTest
public class AccountCacheSupport extends DefaultCacheSupport {

    private static final Logger logger = LoggerFactory.getLogger(AccountCacheSupport.class);

    private CacheCreator cacheCreator;

    private final String serverUrl = System.getProperty("hazelcast.url",
            "localhost:5701");

    @Override
    protected CacheCreator getCacheCreator() {
        return cacheCreator;
    }

    @PostConstruct
    private void init() {
        logger.info("AccountCacheSupport init");
        AccountCacheInterceptor.setCacheSupport(this);
        cacheCreator = new HazelcastCacheCreator(serverUrl);
    }
}

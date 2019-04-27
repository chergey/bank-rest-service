package org.elcer.accounts.app;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.elcer.accounts.hk2.annotations.Component;
import org.elcer.accounts.hk2.annotations.Eager;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.services.AccountRepository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;

@SuppressWarnings("unused")
@Component
@Eager
@Singleton
public class SampleDataInitializer {
    private static final int ACCOUNTS_TO_CREATE = 1000;

    @Inject
    private AccountRepository accountRepository;

    private boolean init;

    @PostConstruct
    public void init() {
        if (init) return;
        try {
            for (int i = 0; i < ACCOUNTS_TO_CREATE; i++) {
                accountRepository.save(new Account(RandomStringUtils.randomAlphabetic(5),
                        BigDecimal.valueOf(RandomUtils.nextLong(100, 1000))));
            }
        } finally {
            init = true;
        }
    }
}

package org.elcer.accounts.app;

import org.elcer.accounts.services.AccountRepository;
import org.elcer.accounts.utils.RandomUtils;
import org.jvnet.hk2.annotations.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Service
public class SampleDataInitializer {

    @Inject
    private AccountRepository accountRepository;

    private boolean init;

    private static final int ACCOUNS_TO_CREATE = 1000;

    @PostConstruct
    public void init() {
        if (init) return;
        try {
            for (int i = 1; i < ACCOUNS_TO_CREATE; i++) {
                accountRepository.createAccount(RandomUtils.getGtZeroRandom());
            }
        } finally {
            init = true;
        }
    }
}

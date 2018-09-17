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

    @PostConstruct
    public void init() {
        if (init) return;
        for (int i = 1; i < 1000; i++) {
            accountRepository.createAccount(RandomUtils.getGtZeroRandom());
        }
        init = true;
    }
}

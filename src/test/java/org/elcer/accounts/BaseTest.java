package org.elcer.accounts;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.jetty.server.Server;
import org.elcer.accounts.app.AppConfig;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.services.AccountRepository;
import org.elcer.accounts.services.AccountService;
import org.elcer.accounts.utils.RunnerUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
@Slf4j
public abstract class BaseTest {

    protected static List<Account> accounts = new ArrayList<>();

    protected static AccountService accountService;
    protected static AccountRepository accountRepository;
    protected static Server server;

    @BeforeClass
    @SneakyThrows
    public static void setUp() {

        server = RunnerUtils.startServer(RunnerUtils.DEFAULT_PORT, false);
        //wait
        Thread.sleep(2000);

        accountService = AppConfig.getServiceLocator().getService(AccountService.class);
        accountRepository = AppConfig.getServiceLocator().getService(AccountRepository.class);
        accounts = accountRepository.getAllAccounts(0, 1000);

        Assert.assertTrue("Accounts not initialized", CollectionUtils.isNotEmpty(accounts));
        System.out.println("Accounts created: " + accounts.size());

    }

    @AfterClass
    @SneakyThrows
    public static void tearDown() {
        if (server != null)
            server.stop();
    }

}

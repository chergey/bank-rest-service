package org.elcer.accounts;

import org.eclipse.jetty.server.Server;
import org.elcer.accounts.app.AppConfig;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.services.AccountRepository;
import org.elcer.accounts.services.AccountService;
import org.elcer.accounts.utils.ExceptionUtils;
import org.elcer.accounts.utils.RunnerUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public abstract class BaseTest {

    protected static final Logger logger = LoggerFactory.getLogger(AppTest.class);

    protected static List<Account> accounts = new ArrayList<>();

    protected static AccountService accountService;
    protected static AccountRepository accountRepository;
    protected static Server server;

    @BeforeClass
    public static void setUp() {

        server = RunnerUtils.startServer(RunnerUtils.DEFAULT_PORT, false);

        accountService = AppConfig.getServiceLocator().getService(AccountService.class);
        accountRepository = AppConfig.getServiceLocator().getService(AccountRepository.class);
        accounts = accountRepository.getAllAccounts();

    }

    @AfterClass
    public static void tearDown() {
        ExceptionUtils.sneakyThrow(() -> server.stop());
    }

}

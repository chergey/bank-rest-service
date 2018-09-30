package org.elcer.accounts;

import io.restassured.response.ResponseBody;
import org.eclipse.jetty.server.Server;
import org.elcer.accounts.app.AppConfig;
import org.elcer.accounts.exceptions.NotEnoughFundsException;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.model.AccountResponse;
import org.elcer.accounts.services.AccountRepository;
import org.elcer.accounts.services.AccountService;
import org.elcer.accounts.utils.ExceptionUtils;
import org.elcer.accounts.utils.RandomUtils;
import org.elcer.accounts.utils.RunnerUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static io.restassured.RestAssured.given;


@RunWith(RepeatableRunner.class)
public class AppTest {

    private static final Logger logger = LoggerFactory.getLogger(AppTest.class);

    private static List<Account> accounts = new ArrayList<>();

    private static AccountService accountService;
    private static AccountRepository accountRepository;
    private static Server server;

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



    @Repeat(2)
    @Test
    public void testConcurrencyAndDeadlocks() {
        final int times = 14000;

        Account first = accountRepository.createAccount(326000);
        Account second = accountRepository.createAccount(315000);
        Account third = accountRepository.createAccount(313000);
        Account fourth = accountRepository.createAccount(356000);

        long startingTotal = second.getBalance() + first.getBalance() + third.getBalance() + fourth.getBalance();

        ExecutorUtils.runConcurrently(
                () -> transfer(times, first, second),
                () -> transfer(times, second, first),
                () -> transfer(times, third, second),

                //  () -> transfer(times, second, fourth),
                () -> transfer(times, second, third),
                () -> transfer(times, first, third),
            //    () -> transfer(times, first, fourth),

                () -> transfer(times, third, second),
                () -> transfer(times, third, first)
            //    () -> transfer(times, third, fourth)

//                () -> transfer(times, fourth, first),
//                () -> transfer(times, fourth, second),
//                () -> transfer(times, fourth, third)

        );

        Account firstInTheEnd = accountRepository.retrieveAccountById(first.getId());
        Account secondInTheEnd = accountRepository.retrieveAccountById(second.getId());
        Account thirdInTheEnd = accountRepository.retrieveAccountById(third.getId());
        Account fourthInTheEnd = accountRepository.retrieveAccountById(fourth.getId());

        long endingTotal = firstInTheEnd.getBalance() + secondInTheEnd.getBalance() + thirdInTheEnd.getBalance() +
                fourthInTheEnd.getBalance();

        Assert.assertTrue("Balance can't be less than zero", firstInTheEnd.getBalance() >= 0);
        Assert.assertTrue("Balance can't be less than zero", secondInTheEnd.getBalance() >= 0);
        Assert.assertTrue("Balance can't be less than zero", thirdInTheEnd.getBalance() >= 0);
        Assert.assertTrue("Balance can't be less than zero", fourthInTheEnd.getBalance() >= 0);
        Assert.assertEquals(startingTotal, endingTotal);


    }

    private void transfer(final int times, Account debit, Account credit) {
        int i = times;
        while (i-- >= 0) {
            try {
                accountService.transfer(debit.getId(), credit.getId(), RandomUtils.getGtZeroRandomL());
            } catch (Exception e) {
                if (e instanceof NotEnoughFundsException) {
                    logger.info("Not enough money left in {}, stopping", debit.getId());
                    break;
                }
                throw e;
            }
        }
    }


    @Ignore
    @Test
    @Repeat(2)
    //TODO: remove
    public void testNonNegativeBalance() {
        final int times = 600;

        AtomicInteger i = new AtomicInteger(times);
        IntStream.range(0, times).parallel().forEach(ignored ->
        {
            while (i.get() >= 0) {
                Account from = getRandomAccount();
                Account to = getRandomAccount();
                if (from == to) {
                    continue;
                }

                try {
                    accountService.transfer(from.getId(), to.getId(), RandomUtils.getGtZeroRandom());
                } catch (Exception e) {
                    continue;
                }
                i.decrementAndGet();

            }
        });

        Assert.assertTrue("Oops, account must not end up with negative balance.",
                accountRepository.getAllAccounts().stream().noneMatch(a -> a.getBalance() < 0)
        );
    }

    @Test
    public void testRestCall() {
        Account from = getRandomAccountFromDb();
        Account to = getRandomAccountFromDb();
        while (true) {
            long fromBalance = from.getBalance();
            if (fromBalance > 0) {
                int amount = RandomUtils.getGtZeroRandom((int) fromBalance);
                if (from != to && amount > 0) {

                    logger.info("from {}, to {}", from, to);
                    String url = String.format("api/account/transfer?from=%d&to=%d&amount=%d",
                            from.getId(), to.getId(), amount);
                    logger.info(url);

                    ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();
                    Assert.assertEquals(body.as(AccountResponse.class), AccountResponse.SUCCESS);
                    break;
                }
            }
        }
    }

    private static Account getRandomAccount() {
        int acc = RandomUtils.getGtZeroRandom(accounts.size());
        Account account = accounts.get(acc);
        Objects.requireNonNull(account, "Account can't be null. Check your data!");
        return account;
    }

    private static Account getAccount(int acc) {
        Account account = accounts.get(acc);
        Objects.requireNonNull(account, "Account can't be null. Check your data!");
        return account;
    }

    private static Account getRandomAccountFromDb() {
        int acc = RandomUtils.getGtZeroRandom(accounts.size());
        Account account = accounts.get(acc);
        Objects.requireNonNull(account, "Account can't be null. Check your data!");
        Account updatedAccount = accountRepository.retrieveAccountById(account.getId());
        return updatedAccount;
    }

    private static Account getAccountFromDb(int acc) {
        Account account = accounts.get(acc);
        Objects.requireNonNull(account, "Account can't be null. Check your data!");
        Account updatedAccount = accountRepository.retrieveAccountById(account.getId());
        return updatedAccount;
    }


}
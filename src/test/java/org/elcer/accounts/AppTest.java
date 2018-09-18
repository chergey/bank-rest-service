package org.elcer.accounts;

import io.restassured.response.ResponseBody;
import org.elcer.accounts.app.AppConfig;
import org.elcer.accounts.exceptions.NotEnoughFundsException;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.model.AccountResponse;
import org.elcer.accounts.services.AccountRepository;
import org.elcer.accounts.services.AccountService;
import org.elcer.accounts.utils.RandomUtils;
import org.elcer.accounts.utils.RunnerUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
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

    @BeforeClass
    public static void setUp() {

        RunnerUtils.runServer(RunnerUtils.DEFAULT_PORT, false);

        accountService = AppConfig.getServiceLocator().getService(AccountService.class);
        accountRepository = AppConfig.getServiceLocator().getService(AccountRepository.class);
        accounts = accountRepository.getAllAccounts();

    }

    @Repeat(2)
    @Test
    public void testConcurrencyAndDeadlocks() {
        final int times = 100;
        AtomicInteger i = new AtomicInteger(times);

        Account from = accountRepository.createAccount(36000);
        Account to = accountRepository.createAccount(31000);

        long startingTotal = to.getBalance() + from.getBalance();

        ExecutorUtils.runConcurrently(
                () -> transfer(i, from, to),
                () -> transfer(i, to, from)
        );

        Account fromEnding = accountRepository.retrieveAccountById(from.getId());
        Account toEnding = accountRepository.retrieveAccountById(to.getId());

        long endingTotal = fromEnding.getBalance() + toEnding.getBalance();
        Assert.assertEquals(startingTotal, endingTotal);

    }

    private void transfer(AtomicInteger i, Account debit, Account credit) {
        while (i.get() >= 0) {
            try {
                accountService.transfer(debit.getId(), credit.getId(), RandomUtils.getGtZeroRandom());
            } catch (Exception e) {
                if (e instanceof NotEnoughFundsException) {
                    logger.info("Not enough money left in {}, stopping", debit.getId());
                    break;
                }
                throw e;
            }
            i.decrementAndGet();
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
                    String url = String.format("account/transfer?from=%d&to=%d&amount=%d",
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
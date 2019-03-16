package org.elcer.accounts;

import org.elcer.accounts.exceptions.NotEnoughFundsException;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.utils.RandomUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;


@RunWith(RepeatableRunner.class)
public class AppTest extends BaseTest {


    @Repeat(2)
    @Test
    public void testConcurrencyAndDeadlocks() {
        final int times = 14000;

        var first = accountRepository.createAccount("Mike", BigDecimal.valueOf(622600));
        var second = accountRepository.createAccount("Jenny", BigDecimal.valueOf(2315000));
        var third = accountRepository.createAccount("David", BigDecimal.valueOf(630000));
        var fourth = accountRepository.createAccount("Steve", BigDecimal.valueOf(356000));

        var startingTotal = second.getBalance().add(first.getBalance()).add(third.getBalance())
                .add(fourth.getBalance());

        ExecutorUtils.runConcurrently(
                () -> transfer(times, first, second),
                () -> transfer(times, second, first),
                () -> transfer(times, third, second),

                () -> transfer(times, second, fourth),
                () -> transfer(times, second, third),
                () -> transfer(times, first, third),
                () -> transfer(times, first, fourth),

                () -> transfer(times, third, second),
                () -> transfer(times, third, first),
                () -> transfer(times, third, fourth),

                () -> transfer(times, fourth, first),
                () -> transfer(times, fourth, second),
                () -> transfer(times, fourth, third)

        );

        var firstInTheEnd = accountRepository.retrieveAccountById(first.getId());
        var secondInTheEnd = accountRepository.retrieveAccountById(second.getId());
        var thirdInTheEnd = accountRepository.retrieveAccountById(third.getId());
        var fourthInTheEnd = accountRepository.retrieveAccountById(fourth.getId());

        var endingTotal = firstInTheEnd.getBalance().add(secondInTheEnd.getBalance()).add(thirdInTheEnd.getBalance())
                .add(fourthInTheEnd.getBalance());

        Assert.assertTrue("Balance can't be less than zero",
                firstInTheEnd.getBalance().compareTo(BigDecimal.ZERO) >= 0);
        Assert.assertTrue("Balance can't be less than zero",
                secondInTheEnd.getBalance().compareTo(BigDecimal.ZERO) >= 0);
        Assert.assertTrue("Balance can't be less than zero",
                thirdInTheEnd.getBalance().compareTo(BigDecimal.ZERO) >= 0);
        Assert.assertTrue("Balance can't be less than zero",
                fourthInTheEnd.getBalance().compareTo(BigDecimal.ZERO) >= 0);
        Assert.assertEquals(startingTotal, endingTotal);


    }

    private void transfer(final int times, Account debit, Account credit) {
        int i = times;
        while (i-- >= 0) {
            try {
                accountService.transfer(debit.getId(), credit.getId(), BigDecimal.valueOf(RandomUtils.getGtZeroRandom()));
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

        var i = new AtomicInteger(times);
        IntStream.range(0, times).parallel().forEach(ignored ->
        {
            while (i.get() >= 0) {
                Account from = getRandomAccount();
                Account to = getRandomAccount();
                if (from == to) {
                    continue;
                }

                try {
                    accountService.transfer(from.getId(), to.getId(), BigDecimal.valueOf(RandomUtils.getGtZeroRandom()));
                } catch (Exception e) {
                    continue;
                }
                i.decrementAndGet();

            }
        });

        Assert.assertTrue("Oops, account must not end up with negative balance.",
                accountRepository.getAllAccounts().stream().noneMatch(a -> a.getBalance().compareTo(BigDecimal.ZERO) < 0)
        );
    }


    private static Account getRandomAccount() {
        int acc = RandomUtils.getGtZeroRandom(3, accounts.size());
        var account = accounts.get(acc);
        Objects.requireNonNull(account, "Account can't be null. Check your data!");
        return account;
    }

    private static Account getAccount(int acc) {
        var account = accounts.get(acc);
        Objects.requireNonNull(account, "Account can't be null. Check your data!");
        return account;
    }


    private static Account getAccountFromDb(int acc) {
        var account = accounts.get(acc);
        Objects.requireNonNull(account, "Account can't be null. Check your data!");
        var updatedAccount = accountRepository.retrieveAccountById(account.getId());
        return updatedAccount;
    }


}
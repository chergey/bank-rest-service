package org.elcer.accounts;

import org.elcer.accounts.exceptions.NotEnoughFundsException;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.utils.RandomUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;


@RunWith(RepeatableRunner.class)
public class AppTest  extends BaseTest{


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



    private static Account getRandomAccount() {
        int acc = RandomUtils.getGtZeroRandom(3, accounts.size());
        Account account = accounts.get(acc);
        Objects.requireNonNull(account, "Account can't be null. Check your data!");
        return account;
    }

    private static Account getAccount(int acc) {
        Account account = accounts.get(acc);
        Objects.requireNonNull(account, "Account can't be null. Check your data!");
        return account;
    }



    private static Account getAccountFromDb(int acc) {
        Account account = accounts.get(acc);
        Objects.requireNonNull(account, "Account can't be null. Check your data!");
        Account updatedAccount = accountRepository.retrieveAccountById(account.getId());
        return updatedAccount;
    }


}
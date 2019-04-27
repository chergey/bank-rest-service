package org.elcer.accounts;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.elcer.accounts.exceptions.NotEnoughFundsException;
import org.elcer.accounts.model.Account;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;


@RunWith(RepeatableRunner.class)
@Slf4j
public class AppTest extends BaseTest {

    @Repeat(2)
    @Test
    public void testConcurrencyAndDeadlocks() {


        final int times = 14000;

        var first = accountRepository.save(new Account("Mike", BigDecimal.valueOf(622600)));
        var second = accountRepository.save(new Account("Jenny", BigDecimal.valueOf(2315000)));
        var third = accountRepository.save(new Account("David", BigDecimal.valueOf(630000)));
        var fourth = accountRepository.save(new Account("Steve", BigDecimal.valueOf(356000)));

        var startingTotal = second.getBalance().add(first.getBalance()).add(third.getBalance())
                .add(fourth.getBalance());

        ExecutorUtils.runConcurrentlyFJP(
                () -> transfer(times, first, second),
                () -> transfer(times, second, first),
                () -> transfer(times, third, second),

                () -> transfer(times, second, fourth),
                () -> transfer(times, second, third),
                () -> transfer(times, first, third),
                () -> transfer(times, first, fourth),

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
                accountService.transfer(debit.getId(), credit.getId(),
                        BigDecimal.valueOf(RandomUtils.nextInt(100, 10000)));
            } catch (Exception e) {
                if (e instanceof NotEnoughFundsException) {
                    log.info("Not enough money left in {}, stopping", debit.getId());
                    break;
                }
                throw e;
            }
        }
    }
}

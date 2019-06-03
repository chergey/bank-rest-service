package org.elcer.accounts;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.elcer.accounts.exceptions.NotEnoughFundsException;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.services.AccountService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;


@RunWith(RepeatableRunner.class)
@Slf4j
public class AppTest extends BaseTest {

    private static final int TIMES = 14000;


    @Repeat(2)
    @Test
    public void testConcurrencyAndDeadlocks() {
        AccountService accountService = getAccountService();

        var first = accountService.createAccount(new Account("Mike", BigDecimal.valueOf(622600)));
        var second = accountService.createAccount(new Account("Jenny", BigDecimal.valueOf(2315000)));
        var third = accountService.createAccount(new Account("David", BigDecimal.valueOf(630000)));
        var fourth = accountService.createAccount(new Account("Steve", BigDecimal.valueOf(356000)));

        var startingTotal = second.getBalance().add(first.getBalance()).add(third.getBalance())
                .add(fourth.getBalance());

        ExecutorUtils.runConcurrentlyFJP(
                () -> transfer(accountService, first, second),
                () -> transfer(accountService, second, first),
                () -> transfer(accountService, third, second),

                () -> transfer(accountService, second, fourth),
                () -> transfer(accountService, second, third),
                () -> transfer(accountService, first, third),
                () -> transfer(accountService, first, fourth),

                () -> transfer(accountService, third, first),
                () -> transfer(accountService, third, fourth),

                () -> transfer(accountService, fourth, first),
                () -> transfer(accountService, fourth, second),
                () -> transfer(accountService, fourth, third)

        );

        var firstInTheEnd = accountService.getAccount(first.getId());
        var secondInTheEnd = accountService.getAccount(second.getId());
        var thirdInTheEnd = accountService.getAccount(third.getId());
        var fourthInTheEnd = accountService.getAccount(fourth.getId());

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

    private void transfer(AccountService accountService, Account debit, Account credit) {
        int i = TIMES;
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

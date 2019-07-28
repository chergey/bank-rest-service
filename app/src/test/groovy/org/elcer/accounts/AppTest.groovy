package org.elcer.accounts

import org.apache.commons.lang3.RandomUtils
import org.elcer.accounts.api.ExecutorUtils
import org.elcer.accounts.exceptions.NotEnoughFundsException
import org.elcer.accounts.model.Account
import org.elcer.accounts.services.AccountService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.RepeatedTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AppTest extends BaseTest {

    private static final int TIMES = 14000

    private static Logger log = LoggerFactory.getLogger(AppTest.class)

    @RepeatedTest(1)
    void testConcurrencyAndDeadlocks() {
        def accountService = getAccountService()

        def first = accountService.createAccount(new Account("Mike", 622600 as BigDecimal))
        def second = accountService.createAccount(new Account("Jenny", 2315000 as BigDecimal))
        def third = accountService.createAccount(new Account("David", 630000 as BigDecimal))
        def fourth = accountService.createAccount(new Account("Steve", 356000 as BigDecimal))

        def startingTotal = second.getBalance() + first.getBalance() + third.getBalance() +
                fourth.getBalance()

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

        def firstInTheEnd = accountService.getAccount(first.getId())
        def secondInTheEnd = accountService.getAccount(second.getId())
        def thirdInTheEnd = accountService.getAccount(third.getId())
        def fourthInTheEnd = accountService.getAccount(fourth.getId())

        def endingTotal = firstInTheEnd.getBalance() + secondInTheEnd.getBalance() +
                thirdInTheEnd.getBalance() +
                fourthInTheEnd.getBalance()


        Assertions.assertTrue(firstInTheEnd.getBalance() >= 0g, "Balance can't be less than zero")
        Assertions.assertTrue(secondInTheEnd.getBalance() >= 0g, "Balance can't be less than zero")
        Assertions.assertTrue(thirdInTheEnd.getBalance() >= 0g, "Balance can't be less than zero")
        Assertions.assertTrue(fourthInTheEnd.getBalance() >= 0g, "Balance can't be less than zero")
        Assertions.assertEquals(startingTotal, endingTotal)


    }

    private static void transfer(AccountService accountService, Account debit, Account credit) {
        int i = TIMES
        while (i-- >= 0) {
            try {
                accountService.transfer(debit.getId(), credit.getId(), RandomUtils.nextInt(100, 10000) as BigDecimal)
            } catch (Exception e) {
                if (e instanceof NotEnoughFundsException) {
                    log.info("Not enough money left in ${debit.getId()}, stopping")
                    break;
                }
                throw e
            }
        }
    }

}
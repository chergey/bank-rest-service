package org.elcer.accounts

import org.apache.commons.lang3.RandomUtils
import org.elcer.accounts.api.ExecutorUtils
import org.elcer.accounts.api.MockInitialContextFactory
import org.elcer.accounts.app.AppConfig
import org.elcer.accounts.exceptions.NotEnoughFundsException
import org.elcer.accounts.model.Account
import org.elcer.accounts.services.AccountService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.mockito.Mock
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.naming.Context
import javax.naming.NamingException
import javax.persistence.Persistence

import static org.mockito.Mockito.when

class AppTest extends BaseTest {

    private static Logger log = LoggerFactory.getLogger(AppTest)

    private static final int TIMES = 14000

    private static final def entityManagerFactory =
            Persistence.createEntityManagerFactory(AppConfig.PU_NAME)

    @Mock
    private Context context


    @BeforeEach
    @Override
    void setUp() throws Exception {
        setupJndi()
        super.setUp()
    }


    private void setupJndi() throws NamingException {
        when(context.lookup("java:comp/env/" + AppConfig.PU_NAME)).thenReturn(entityManagerFactory)
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MockInitialContextFactory.class.getName())
        MockInitialContextFactory.setGlobalContext(context)
    }


    @RepeatedTest(1)
    void "run concurrency and deadlock test"() {
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

        def firstInTheEnd = accountService.findById(first.getId())
        def secondInTheEnd = accountService.findById(second.getId())
        def thirdInTheEnd = accountService.findById(third.getId())
        def fourthInTheEnd = accountService.findById(fourth.getId())

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
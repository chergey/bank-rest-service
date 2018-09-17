package org.elcer.accounts.services;


import org.elcer.accounts.eclipselink.Transaction;
import org.elcer.accounts.hk2.CustomInject;
import org.elcer.accounts.model.Account;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Service
@Singleton
public class AccountService {

    @Inject
    private AccountRepository accountRepository;

    @CustomInject
    private Logger logger;

    public boolean transfer(long from, long to, long amount) {
        logger.info("Begin transfer from {} to {} amount {}", from, to, amount);
        try (Transaction tran = accountRepository.beginTran()) {
            Account fromAccount = accountRepository._retrieveAccountById(tran.getEm(), from);
            Account toAccount = accountRepository._retrieveAccountById(tran.getEm(), to);
            if (fromAccount.getBalance() >= amount) {
                fromAccount.subtractBalance(amount);
                toAccount.increaseBalance(amount);

                accountRepository._updateAccount(tran.getEm(), fromAccount);
                accountRepository._updateAccount(tran.getEm(), toAccount);
                tran.commit();
            } else {
                logger.info("Not enough money in debit account {}", from);
                return false;
            }
        }
        logger.info("Successfully transferred from {} to {} amount {}", from, to, amount);
        return true;
    }
}
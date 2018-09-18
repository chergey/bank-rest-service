package org.elcer.accounts.services;


import org.elcer.accounts.db.Transaction;
import org.elcer.accounts.exceptions.NotEnoughFundsException;
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

    private boolean isFirst(long from, long to) {
        return from > to;
    }

    public void transfer(long from, long to, long amount) {
        logger.info("Begin transfer from {} to {} amount {}", from, to, amount);

        try (Transaction tran = accountRepository.beginTran()) {
            Account debitAccount;

            if (isFirst(from, to)) {
                debitAccount = accountRepository.retrieveAccountByIdWithTran(tran, from);
                accountRepository.retrieveAccountByIdWithTran(tran, to);
            } else {
                accountRepository.retrieveAccountByIdWithTran(tran, to);
                debitAccount = accountRepository.retrieveAccountByIdWithTran(tran, from);
            }

            if (debitAccount.getBalance() >= amount) {
                if (isFirst(from, to)) {
                    accountRepository.updateAccountWithTran(tran, from, -amount);
                    accountRepository.updateAccountWithTran(tran, to, amount);
                } else {
                    accountRepository.updateAccountWithTran(tran, to, amount);
                    accountRepository.updateAccountWithTran(tran, from, -amount);
                }

                tran.commit();
            } else {
                throw new NotEnoughFundsException(debitAccount.getId());
            }
        }

        logger.info("Successfully transferred from {} to {} amount {}", from, to, amount);
    }
}
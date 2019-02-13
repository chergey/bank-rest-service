package org.elcer.accounts.services;


import org.elcer.accounts.db.Transaction;
import org.elcer.accounts.exceptions.NotEnoughFundsException;
import org.elcer.accounts.hk2.annotations.Component;
import org.elcer.accounts.hk2.annotations.Raw;
import org.elcer.accounts.model.Account;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;


@Component
public class AccountService {

    @Inject
    private AccountRepository accountRepository;

    @Inject
    private Logger logger;

    @Inject
    @Raw
    private Synchronizer<Long> synchronizer;


    public void transfer(long from, long to, BigDecimal amount) {
        logger.info("Begin transfer from {} to {} amount {}", from, to, amount);


        try (Transaction tran = accountRepository.beginTran()) {
            synchronizer.withLock(from, to, () -> {

                Account debitAccount, creditAccount;
                debitAccount = accountRepository.retrieveAccountByIdWithTran(tran, from);
                creditAccount = accountRepository.retrieveAccountByIdWithTran(tran, to);

                if (debitAccount.getBalance().compareTo(amount) >= 0) {
                    accountRepository.updateAccountWithTran(tran, debitAccount, amount.negate());
                    accountRepository.updateAccountWithTran(tran, creditAccount, amount);
                    tran.commit();

                } else {
                    throw new NotEnoughFundsException(debitAccount.getId());
                }
            });

            logger.info("Successfully transferred from {} to {} amount {}", from, to, amount);
        }
    }

    public Account getAccounts(long id) {
        return accountRepository.retrieveAccountById(id);
    }

    public Account createAccount(Account account) {
        return accountRepository.createAccount(account);
    }

    public List<Account> getAccounts(String name) {
        return accountRepository.retrieveAccountsByName(name);
    }
}




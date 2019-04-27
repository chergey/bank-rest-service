package org.elcer.accounts.services;


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

        synchronizer.withLock(from, to, () -> {
            try (var tran = accountRepository.beginTran()) {
                Account debitAccount = accountRepository.retrieveAccountById(tran, from),
                        creditAccount = accountRepository.retrieveAccountById(tran, to);

                if (debitAccount.getBalance().compareTo(amount) >= 0) {
                    debitAccount.subtractBalance(amount);
                    creditAccount.increaseBalance(amount);

                    tran.commit();

                } else {
                    throw new NotEnoughFundsException(debitAccount.getId());
                }
            }

            logger.info("Successfully transferred from {} to {} amount {}", from, to, amount);
        });
    }


    public Account getAccount(long id) {
        return accountRepository.retrieveAccountById(id);
    }

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }


    public void deleteAccount(long id) {
        Account account = accountRepository.retrieveAccountById(id);
        accountRepository.deleteAccount(account);
    }

    public Account replaceAccount(long id, Account account) {
        try (var tran = accountRepository.beginTran()) {
            Account oldAccount = accountRepository.retrieveAccountById(tran, id);
            if (oldAccount != null) {
                oldAccount.setBalance(account.getBalance());
                oldAccount.setName(account.getName());
                return accountRepository.save(tran, oldAccount);
            } else {
                account.setId(id);
                return accountRepository.save(tran, account);
            }
        }
    }

    public List<Account> getAllAccounts(int page, int size) {
        return accountRepository.getAllAccounts(page, size);
    }

    public List<Account> getAccounts(String name, int page, int size) {
        return accountRepository.retrieveAccountsByName(name, page, size);
    }

    public long countAccounts(String name) {
        return accountRepository.countAccounts(name);
    }

    public long countAccounts() {
        return accountRepository.countAccounts();
    }
}




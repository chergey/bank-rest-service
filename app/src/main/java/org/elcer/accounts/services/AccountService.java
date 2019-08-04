package org.elcer.accounts.services;


import org.elcer.accounts.db.Transaction;
import org.elcer.accounts.exceptions.AccountNotFoundException;
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
                var debitAccount = findById(tran, from);
                var creditAccount = findById(tran, to);

                if (debitAccount.getBalance().compareTo(amount) >= 0) {
                    debitAccount.subtractBalance(amount);
                    creditAccount.increaseBalance(amount);

                } else {
                    throw new NotEnoughFundsException(debitAccount.getId());
                }
            }

            logger.info("Successfully transferred from {} to {} amount {}", from, to, amount);
        });
    }


    public Account findById(long id) {
        Account account = accountRepository.findById(id);
        throwIfNull(account, id);
        return account;
    }


    private Account findById(Transaction tran, long id) {
        Account account = accountRepository.findById(tran, id);
        throwIfNull(account, id);
        return account;
    }

    private void throwIfNull(Account account, long id) {
        if (account == null) {
            throw new AccountNotFoundException(id);
        }
    }

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }


    public void deleteAccount(long id) {
        Account account = findById(id);
        accountRepository.delete(account);
    }

    public void deleteAccount(Account account) {
        accountRepository.delete(account);
    }

    public Account replaceAccount(long id, Account account) {
        try (var tran = accountRepository.beginTran()) {
            Account oldAccount = accountRepository.findById(tran, id);
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
        return accountRepository.findAll(page, size);
    }

    public List<Account> getAccounts(String name, int page, int size) {
        return accountRepository.findByName(name, page, size);
    }

    public long countAccounts(String name) {
        return accountRepository.countAccounts(name);
    }

    public long countAccounts() {
        return accountRepository.countAccounts();
    }
}




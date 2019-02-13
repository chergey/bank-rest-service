package org.elcer.accounts.exceptions;

abstract class AccountException extends RuntimeException {

    private long accountId;

    public AccountException(long accountId) {
        this.accountId = accountId;
    }

    public long getAccountId() {
        return accountId;
    }
}

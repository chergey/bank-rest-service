package org.elcer.accounts.exceptions;

public class NotEnoughFundsException extends RuntimeException {
    private long accountId;

    public NotEnoughFundsException(long accountId) {
        this.accountId = accountId;
    }

    public long getAccountId() {
        return accountId;
    }
}

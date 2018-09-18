package org.elcer.accounts.exceptions;

public class NoAccountException extends RuntimeException {

    private long accountId;

    public NoAccountException(long accountId) {
        this.accountId = accountId;
    }

    public long getAccountId() {
        return accountId;
    }
}

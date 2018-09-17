package org.elcer.accounts.model;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountResponse {

    public static AccountResponse NEGATIVE_AMOUNT = new AccountResponse("Amount to transfer must be positive", 1);
    public static AccountResponse SUCCESS = new AccountResponse("Successfully transfered funds", 0);
    public static AccountResponse ERROR_UPDATING = new AccountResponse("Error while updating funds", 2);
    public static AccountResponse NO_SUCH_ACCOUNT = new AccountResponse("No account with id", 6);
    public static AccountResponse NOT_ENOUGH_MONEY = new AccountResponse("Not enough money ", 3);
    public static AccountResponse DEBIT_ACCOUNT_IS_CREDIT_ACCOUNT = new AccountResponse("Debit account can't be credit account", 4);
    private String message;
    private int code;


    public AccountResponse(String message, int code) {
        this.message = message;
        this.code = code;
    }


    public AccountResponse appendMessage(String message) {
        return appendMessage(":", message);
    }

    public AccountResponse appendMessage(String sep, String message) {
        AccountResponse response = new AccountResponse(this.message, this.code);
        response.message += sep + " " + message;
        return this;
    }
}

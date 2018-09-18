package org.elcer.accounts.model;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountResponse {


    //api responses
    public static AccountResponse NEGATIVE_AMOUNT = new AccountResponse("Amount to transfer must be positive", 1);
    public static AccountResponse SUCCESS = new AccountResponse("Successfully transferred funds", 0);
    public static AccountResponse ERROR_UPDATING = new AccountResponse("Error while updating funds", 2);
    public static AccountResponse NO_SUCH_ACCOUNT = new AccountResponse("No account with id", 6);
    public static AccountResponse NOT_ENOUGH_FUNDS = new AccountResponse("Not enough funds on account id", 3);
    public static AccountResponse DEBIT_ACCOUNT_IS_CREDIT_ACCOUNT = new AccountResponse("Debit account can't be credit account", 4);



    private String message;
    private int code;
    private static final String SPACE = " ";


    public AccountResponse(String message, int code) {
        this.message = message;
        this.code = code;
    }


    public <T> AccountResponse append(String sep, T data) {
        AccountResponse response = new AccountResponse(this.message, this.code);
        response.message += sep + SPACE + data;
        return this;
    }


    public <T> AccountResponse append(T data) {
        return append("", data);
    }
}

package org.elcer.accounts;

import io.restassured.internal.RestAssuredResponseImpl;
import io.restassured.response.ResponseBody;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.model.AccountResponse;
import org.elcer.accounts.utils.RandomUtils;
import org.elcer.accounts.utils.RunnerUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

import static io.restassured.RestAssured.given;

public class AccountResourceTest extends BaseTest {


    @Test
    public void testAccountTransferSuccessfully() {
        String url = "api/account/transfer?from=1&to=2&amount=10";
        logger.info(url);

        ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();
        Assert.assertEquals(body.as(AccountResponse.class), AccountResponse.success());

    }

    @Test
    public void testAccountTransfer400() {
        String url = "api/account/transfer?from=&to=&amount=";

        ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();
        assertHttpStatus(body, 400);


    }

    @Test
    public void testAccountTransferNotEnoughFunds() {
        String url = "api/account/transfer?from=2&to=1&amount=999999";

        ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();
        Assert.assertEquals(body.as(AccountResponse.class).getCode(), AccountResponse.notEnoughFunds().getCode());
    }

    @Test
    public void testAccountTransferSame() {
        String url = "api/account/transfer?from=2&to=2&amount=100";

        ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();
        Assert.assertEquals(body.as(AccountResponse.class).getCode(), AccountResponse.debitAccountIsCreditAccount().getCode());
        assertHttpStatus(body, 400);
    }


    @Test
    public void testAccountNegativeAmount() {
        String url = "api/account/transfer?from=2&to=1&amount=-100";

        ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();
        Assert.assertEquals(body.as(AccountResponse.class).getCode(), AccountResponse.negativeAmount().getCode());
        assertHttpStatus(body, 400);

    }


    @Test
    public void testGetAccountById() {
        String url = "api/account/1";

        ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();
        AccountResponse accountResponse = body.as(AccountResponse.class);

        Assert.assertNotNull(accountResponse.getAccount());
        Assert.assertEquals(accountResponse.getAccount().getId(), 1);
    }


    @Test
    public void testGetAccountById400() {
        String url = "api/account/";

        ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();

        assertHttpStatus(body, 404);


    }

    @Test
    public void testNoSuchAccount() {
        String url = "api/account/99999";

        ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();
        assertHttpStatus(body, 404);

        Assert.assertEquals(body.as(AccountResponse.class).getCode(), AccountResponse.noSuchAccount().getCode());


    }

    private static void assertHttpStatus(ResponseBody body, int code) {
        Assert.assertTrue(body instanceof RestAssuredResponseImpl &&
                ((RestAssuredResponseImpl) body).getGroovyResponse().getStatusCode() == code);

    }


    private static Account getRandomAccountFromDb() {
        int acc = RandomUtils.getGtZeroRandom(accounts.size());
        Account account = accounts.get(acc);
        Objects.requireNonNull(account, "Account can't be null. Check your data!");
        Account updatedAccount = accountRepository.retrieveAccountById(account.getId());
        return updatedAccount;
    }

}

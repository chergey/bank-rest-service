package org.elcer.accounts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.internal.RestAssuredResponseImpl;
import io.restassured.response.ResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.model.AccountListResponse;
import org.elcer.accounts.model.TransferResponse;
import org.elcer.accounts.utils.RunnerUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.io.IOException;
import java.math.BigDecimal;

import static io.restassured.RestAssured.given;


@Slf4j
public class AccountResourceTest extends BaseTest {

    @Test
    public void testDeleteAccount() {
        ResponseBody body = given()
                .contentType(ContentType.JSON)
                .when().port(RunnerUtils.DEFAULT_PORT)
                .delete("api/accounts/2").body();
        assertHttpStatus(body, 200);

    }


    @Test
    public void testCreateAccount() throws IOException {
        Account account = new Account("Daniel", BigDecimal.valueOf(10000));
        String serialized = serialize(account);
        ResponseBody body = given()
                .contentType(ContentType.JSON)
                .body(serialized)
                .when().port(RunnerUtils.DEFAULT_PORT)
                .post("api/accounts/").body();
        assertHttpStatus(body, 201);

    }

    @Test
    public void testAccountTransferSuccessfully() {
        ResponseBody body = given().when()
                .port(RunnerUtils.DEFAULT_PORT)
                .post("api/accounts/transfer?from=1&to=2&amount=10")
                .body();
        assertHttpStatus(body, Response.Status.OK.getStatusCode());

        Assert.assertEquals(TransferResponse.success(), body.as(TransferResponse.class));


    }

    @Test
    public void testAccountTransfer400() {
        ResponseBody body = given().when()
                .port(RunnerUtils.DEFAULT_PORT)
                .post("api/accounts/transfer?from=&to=&amount=")
                .body();
        assertHttpStatus(body, Response.Status.BAD_REQUEST.getStatusCode());


    }

    @Test
    public void testAccountTransferNotEnoughFunds() {
        ResponseBody body = given().when()
                .port(RunnerUtils.DEFAULT_PORT)
                .post("api/accounts/transfer?from=3&to=1&amount=999999")
                .body();
        assertHttpStatus(body, Response.Status.ACCEPTED.getStatusCode());

        Assert.assertEquals(TransferResponse.notEnoughFunds().getCode(), body.as(TransferResponse.class).getCode());

    }

    @Test
    public void testAccountTransferSame() {
        ResponseBody body = given().when()
                .port(RunnerUtils.DEFAULT_PORT)
                .post("api/accounts/transfer?from=2&to=2&amount=100")
                .body();
        assertHttpStatus(body, Response.Status.BAD_REQUEST.getStatusCode());

        Assert.assertEquals(TransferResponse.debitAccountIsCreditAccount().getCode(), body.as(TransferResponse.class).getCode());
    }


    @Test
    public void testAccountNegativeAmount() {
        ResponseBody body = given().when()
                .port(RunnerUtils.DEFAULT_PORT)
                .post("api/accounts/transfer?from=2&to=1&amount=-100").body();

        assertHttpStatus(body, Response.Status.BAD_REQUEST.getStatusCode());

        Assert.assertEquals(TransferResponse.negativeAmount().getCode(), body.as(TransferResponse.class).getCode());


    }


    @Test
    public void testGetAccountById() {
        ResponseBody body = given().when()
                .port(RunnerUtils.DEFAULT_PORT)
                .get("api/accounts/1")
                .body();

        assertHttpStatus(body, Response.Status.OK.getStatusCode());

        Account account = body.as(Account.class);

        Assert.assertNotNull("No account in reponse", account);
        Assert.assertEquals(1, account.getId());


    }

    @Test
    public void testGetAccountByName() {
        String name = accounts.get(0).getName();

        String url = String.format("api/accounts/%s?page=0&size=10", name);

        ResponseBody body = given().when()
                .port(RunnerUtils.DEFAULT_PORT)
                .get(url)
                .body();

        assertHttpStatus(body, Response.Status.OK.getStatusCode());

        AccountListResponse accountResponse = body.as(AccountListResponse.class);
        Assert.assertTrue("No accounts in response", CollectionUtils.isNotEmpty(accountResponse.getAccounts()));
        Assert.assertEquals(name, accountResponse.getAccounts().get(0).getName());
    }


    @Test
    public void testGetAllAccounts() {
        ResponseBody body = given().when()
                .port(RunnerUtils.DEFAULT_PORT)
                .get("api/accounts?page=0&size=10")
                .body();

        assertHttpStatus(body, Response.Status.OK.getStatusCode());

        AccountListResponse accountResponse = body.as(AccountListResponse.class);
        Assert.assertTrue("No accounts in response", CollectionUtils.isNotEmpty(accountResponse.getAccounts()));

    }

    @Test
    public void testNoSuchAccount() {
        ResponseBody body = given().when()
                .port(RunnerUtils.DEFAULT_PORT)
                .get("api/accounts/99999")
                .body();

        assertHttpStatus(body, Response.Status.NOT_FOUND.getStatusCode());

        Assert.assertEquals(TransferResponse.noSuchAccount().getCode(), body.as(TransferResponse.class).getCode());

    }

    private static void assertHttpStatus(ResponseBody body, int code) {
        Assert.assertTrue(body instanceof RestAssuredResponseImpl &&
                ((RestAssuredResponseImpl) body).getGroovyResponse().getStatusCode() == code);

    }


    private static <T> Object deserialize(String json, Class<T> objectClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper().
                setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper.readValue(json, objectClass);
    }


    private static String serialize(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper().
                setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper.writeValueAsString(object);
    }
}

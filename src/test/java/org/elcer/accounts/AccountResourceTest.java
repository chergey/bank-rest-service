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
import org.elcer.accounts.model.AccountResponse;
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
    public void testCreateAccount() throws IOException {
        String url = "api/account/create";
        Account account = new Account("Daniel", BigDecimal.valueOf(10000));
        String serialized = serialize(account);
        ResponseBody body = given()
                .contentType(ContentType.JSON)
                .body(serialized)
                .when().port(RunnerUtils.DEFAULT_PORT)
                .post(url).body();
        assertHttpStatus(body, 201);

    }

    @Test
    public void testAccountTransferSuccessfully() {
        String url = "api/account/transfer?from=1&to=2&amount=10";

        ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();
        Assert.assertEquals(AccountResponse.success(), body.as(AccountResponse.class));

        assertHttpStatus(body, Response.Status.OK.getStatusCode());

    }

    @Test
    public void testAccountTransfer400() {
        String url = "api/account/transfer?from=&to=&amount=";

        ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();
        assertHttpStatus(body, Response.Status.BAD_REQUEST.getStatusCode());


    }

    @Test
    public void testAccountTransferNotEnoughFunds() {
        String url = "api/account/transfer?from=2&to=1&amount=999999";

        ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();
        Assert.assertEquals(AccountResponse.notEnoughFunds().getCode(), body.as(AccountResponse.class).getCode());

        assertHttpStatus(body, Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    public void testAccountTransferSame() {
        String url = "api/account/transfer?from=2&to=2&amount=100";

        ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();
        Assert.assertEquals(AccountResponse.debitAccountIsCreditAccount().getCode(), body.as(AccountResponse.class).getCode());
        assertHttpStatus(body, Response.Status.BAD_REQUEST.getStatusCode());
    }


    @Test
    public void testAccountNegativeAmount() {
        String url = "api/account/transfer?from=2&to=1&amount=-100";

        ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();
        Assert.assertEquals(AccountResponse.negativeAmount().getCode(), body.as(AccountResponse.class).getCode());

        assertHttpStatus(body, Response.Status.BAD_REQUEST.getStatusCode());

    }


    @Test
    public void testGetAccountById() {
        String url = "api/account/1";

        ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();
        AccountResponse accountResponse = body.as(AccountResponse.class);

        Assert.assertNotNull("No account in reponse", accountResponse.getAccount());
        Assert.assertEquals(1, accountResponse.getAccount().getId());

        assertHttpStatus(body, Response.Status.OK.getStatusCode());

    }

    @Test
    public void testGetAccountByName() {

        String name = accounts.get(0).getName();
        String url = "api/account/" + name;

        ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();
        AccountListResponse accountResponse = body.as(AccountListResponse.class);
        Assert.assertTrue("No accounts in reponse", CollectionUtils.isNotEmpty(accountResponse.getAccounts()));
        Assert.assertEquals(name, accountResponse.getAccounts().get(0).getName());

        assertHttpStatus(body, Response.Status.OK.getStatusCode());

    }


    @Test
    public void testGetAccountByIdMissingNameOrIdParam() {
        String url = "api/account/";

        ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();

        assertHttpStatus(body, Response.Status.NOT_FOUND.getStatusCode());


    }

    @Test
    public void testNoSuchAccount() {
        String url = "api/account/99999";

        ResponseBody body = given().when().port(RunnerUtils.DEFAULT_PORT).get(url).body();

        assertHttpStatus(body, Response.Status.NOT_FOUND.getStatusCode());

        Assert.assertEquals(AccountResponse.noSuchAccount().getCode(), body.as(AccountResponse.class).getCode());


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

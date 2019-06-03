package org.elcer.accounts;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.model.PagedResponse;
import org.elcer.accounts.model.TransferResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;


@Slf4j
public class ResourceTest extends BaseTest {

    @After
    public void clean() {
        for (Account allAccount : getAccountService().getAllAccounts(0, Integer.MAX_VALUE)) {
            getAccountService().deleteAccount(allAccount.getId());
        }
    }


    @Test
    public void testDeleteAccount() {
        getAccountService().createAccount(new Account(1L, "Denis", BigDecimal.valueOf(40000)));

        Response response = target("api/accounts")
                .path(Integer.toString(1))
                .request()
                .delete();

        assertHttpStatus(response, Response.Status.OK);

    }


    @Test
    public void testReplaceAccount() {
        getAccountService().createAccount(new Account(2L, "Denis", BigDecimal.valueOf(40000)));

        Account account = new Account("Daniel", BigDecimal.valueOf(10000));
        Response response = target("api/accounts")
                .path(Integer.toString(2))
                .request()
                .put(Entity.json(account));

        Account replacedAccount = response.readEntity(Account.class);
        assertHttpStatus(response, Response.Status.CREATED);
        Assert.assertNotNull(replacedAccount);
        Assert.assertEquals("Account balance should be 10000", replacedAccount.getBalance(), BigDecimal.valueOf(10000));
        Assert.assertEquals("Account name should be Daniel", replacedAccount.getName(), "Daniel");

    }


    @Test
    public void testCreateAccount() {
        Account account = new Account("Daniel", BigDecimal.valueOf(10000));

        Response response = target("api/accounts")
                .request()
                .post(Entity.json(account));

        Account createdAccount = response.readEntity(Account.class);
        Assert.assertNotNull("Account can't be null", createdAccount);
        Assert.assertNotNull("Account id can't be null", createdAccount.getId());

        assertHttpStatus(response, Response.Status.CREATED);

    }

    @Test
    public void testAccountTransferSuccessfully() {
        getAccountService().createAccount(new Account(1L, "Daniel", BigDecimal.valueOf(10000)));
        getAccountService().createAccount(new Account(2L, "Mark", BigDecimal.valueOf(10000)));

        Response response = target("api/accounts/transfer")
                .queryParam("from", 1)
                .queryParam("to", 2)
                .queryParam("amount", 10)
                .request()
                .post(Entity.json(null));

        assertHttpStatus(response, Response.Status.OK);

        Assert.assertEquals(TransferResponse.success(), response.readEntity(TransferResponse.class));


    }

    @Test
    public void testAccountTransfer400() {
        getAccountService().createAccount(new Account(1L, "Daniel", BigDecimal.valueOf(10000)));
        getAccountService().createAccount(new Account(2L, "Mark", BigDecimal.valueOf(10000)));


        Response response = target("api/accounts/transfer")
                .queryParam("from")
                .queryParam("to")
                .queryParam("amount")
                .request()
                .post(Entity.json(null));

        assertHttpStatus(response, Response.Status.BAD_REQUEST);


    }

    @Test
    public void testAccountTransferNotEnoughFunds() {
        getAccountService().createAccount(new Account(1L, "Daniel", BigDecimal.valueOf(1000)));
        getAccountService().createAccount(new Account(2L, "Mark", BigDecimal.valueOf(1000)));

        Response response = target("api/accounts/transfer")
                .queryParam("from", 1)
                .queryParam("to", 2)
                .queryParam("amount", 999999)
                .request()
                .post(Entity.entity(null, MediaType.APPLICATION_JSON_TYPE));

        assertHttpStatus(response, Response.Status.ACCEPTED);

        Assert.assertEquals(TransferResponse.notEnoughFunds().getCode(),
                response.readEntity(TransferResponse.class).getCode());

    }

    @Test
    public void testAccountTransferSame() {
        getAccountService().createAccount(new Account(1L, "Daniel", BigDecimal.valueOf(10000)));
        getAccountService().createAccount(new Account(2L, "Mark", BigDecimal.valueOf(10000)));


        Response response = target("api/accounts/transfer")
                .queryParam("from", 2)
                .queryParam("to", 2)
                .queryParam("amount", 100)
                .request()
                .post(Entity.entity(null, MediaType.APPLICATION_JSON_TYPE));

        assertHttpStatus(response, Response.Status.BAD_REQUEST);

        Assert.assertEquals(TransferResponse.debitAccountIsCreditAccount().getCode(),
                response.readEntity(TransferResponse.class).getCode());
    }


    @Test
    public void testAccountNegativeAmount() {
        getAccountService().createAccount(new Account(1L, "Daniel", BigDecimal.valueOf(10000)));
        getAccountService().createAccount(new Account(2L, "Mark", BigDecimal.valueOf(10000)));

        Response response = target("api/accounts/transfer")
                .queryParam("from", 1)
                .queryParam("to", 2)
                .queryParam("amount", -100)
                .request()
                .post(Entity.entity(null, MediaType.APPLICATION_JSON_TYPE));
        assertHttpStatus(response, Response.Status.BAD_REQUEST);

        Assert.assertEquals(TransferResponse.negativeAmount().getCode(),
                response.readEntity(TransferResponse.class).getCode());


    }


    @Test
    public void testGetAccountById() {
        getAccountService().createAccount(new Account(1L, "Daniel", BigDecimal.valueOf(10000)));

        Response response = target("api/accounts")
                .path(Integer.toString(1))
                .request().get();

        assertHttpStatus(response, Response.Status.OK);
        Account account = response.readEntity(Account.class);

        Assert.assertNotNull("No account in response", account);
        Assert.assertEquals(Long.valueOf(1), account.getId());


    }

    @Test
    public void testGetAccountsByName() {
        String accountName = "Daniel";
        getAccountService().createAccount(new Account(1L, accountName, BigDecimal.valueOf(10000)));

        Response response = target("api/accounts")
                .path(accountName)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .request()
                .get();

        assertHttpStatus(response, Response.Status.OK);

        PagedResponse<Account> accountResponse = response.readEntity(new GenericType<>() {
        });

        Assert.assertTrue("No accounts in response",
                CollectionUtils.isNotEmpty(accountResponse.getContent()));

        Assert.assertEquals(accountName, accountResponse.getContent().get(0).getName());
    }


    @Test
    public void testGet10Accounts() {
        for (int i = 0; i < 10; i++)
            getAccountService().createAccount(new Account(i + 1L, Integer.toString(i), BigDecimal.valueOf(10000)));

        Response response = target("api/accounts")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .request()
                .get();

        assertHttpStatus(response, Response.Status.OK);

        PagedResponse<Account> accountResponse = response.readEntity(new GenericType<>() {
        });

        Assert.assertTrue("No accounts in response",
                CollectionUtils.isNotEmpty(accountResponse.getContent()));

        Assert.assertEquals(10, accountResponse.getContent().size());

    }

    @Test
    public void testNoSuchAccount() {
        Response response = target("api/accounts")
                .path(Integer.toString(1))
                .request()
                .get();

        assertHttpStatus(response, Response.Status.NOT_FOUND);

        Assert.assertEquals(TransferResponse.noSuchAccount().getCode(),
                response.readEntity(TransferResponse.class).getCode());

    }

    private static void assertHttpStatus(Response body, Response.Status status) {
        Assert.assertEquals(status.getStatusCode(), body.getStatus());
    }
}

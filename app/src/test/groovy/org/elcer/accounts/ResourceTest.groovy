package org.elcer.accounts

import lombok.extern.slf4j.Slf4j
import org.apache.commons.collections4.CollectionUtils
import org.elcer.accounts.model.Account
import org.elcer.accounts.model.PagedResponse
import org.elcer.accounts.model.TransferResponse
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import javax.ws.rs.client.Entity
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Slf4j
class ResourceTest extends BaseTest {

    @BeforeEach
    void clean() {
        for (Account account : getAccountService().getAllAccounts(0, Integer.MAX_VALUE)) {
            getAccountService().deleteAccount(account)
        }
    }

    @Test
    void testDeleteAccount() {
        getAccountService().createAccount(new Account(1L, "Denis", 40000 as BigDecimal))

        Response response = target("api/accounts")
                .path(Integer.toString(1))
                .request()
                .delete()

        assertHttpStatus(response, Response.Status.OK)

    }


    @Test
    void testReplaceAccount() {
        getAccountService().createAccount(new Account(1L, "Denis", 40000 as BigDecimal))

        Account account = new Account("Daniel",10000 as  BigDecimal)
        Response response = target("api/accounts")
                .path(Integer.toString(1))
                .request()
                .put(Entity.json(account))

        Account replacedAccount = response.readEntity(Account.class)
        assertHttpStatus(response, Response.Status.CREATED)
        Assert.assertNotNull(replacedAccount)

        Assert.assertEquals("Account balance should be 10000",
                replacedAccount.getBalance(), 10000 as BigDecimal)
        Assert.assertEquals("Account name should be Daniel",
                replacedAccount.getName(), "Daniel")

    }


    @Test
    void testCreateAccount() {

        Account account = new Account("Daniel", 10000 as BigDecimal);

        Response response = target("api/accounts")
                .request()
                .post(Entity.json(account));

        Account createdAccount = response.readEntity(Account.class)
        Assert.assertNotNull("Account can't be null", createdAccount)
        Assert.assertNotNull("Account id can't be null", createdAccount.getId())

        assertHttpStatus(response, Response.Status.CREATED)

    }

    @Test
    void testAccountTransferSuccessfully() {
        def accountService = getAccountService()
        accountService.createAccount(new Account(1L, "Daniel", 10000 as BigDecimal))
        accountService.createAccount(new Account(2L, "Mark",10000 as BigDecimal))

        Response response = target("api/accounts/transfer")
                .queryParam("from", 1)
                .queryParam("to", 2)
                .queryParam("amount", 10)
                .request()
                .post(Entity.json(null))

        assertHttpStatus(response, Response.Status.OK)

        Assert.assertEquals(TransferResponse.success(), response.readEntity(TransferResponse.class))


    }

    @Test
    void testAccountTransfer400() {
        def accountService = getAccountService()

        accountService.createAccount(new Account(1L, "Daniel",  10000 as BigDecimal))
        accountService.createAccount(new Account(2L, "Mark",  10000 as BigDecimal))


        Response response = target("api/accounts/transfer")
                .queryParam("from")
                .queryParam("to")
                .queryParam("amount")
                .request()
                .post(Entity.json(null))

        assertHttpStatus(response, Response.Status.BAD_REQUEST)


    }

    @Test
    void testAccountTransferNotEnoughFunds() {
        def accountService = getAccountService()
        accountService.createAccount(new Account(1L, "Daniel",1000 as BigDecimal))
        accountService.createAccount(new Account(2L, "Mark",1000 as BigDecimal))

        Response response = target("api/accounts/transfer")
                .queryParam("from", 1)
                .queryParam("to", 2)
                .queryParam("amount", 999999)
                .request()
                .post(Entity.entity(null, MediaType.APPLICATION_JSON_TYPE))

        assertHttpStatus(response, Response.Status.ACCEPTED)

        Assert.assertEquals(TransferResponse.notEnoughFunds().getCode(),
                response.readEntity(TransferResponse.class).getCode())

    }

    @Test
    void testAccountTransferSame() {
        getAccountService().createAccount(new Account(1L, "Daniel", 10000 as BigDecimal))
        getAccountService().createAccount(new Account(2L, "Mark", 10000 as BigDecimal))


        Response response = target("api/accounts/transfer")
                .queryParam("from", 2)
                .queryParam("to", 2)
                .queryParam("amount", 100)
                .request()
                .post(Entity.entity(null, MediaType.APPLICATION_JSON_TYPE))

        assertHttpStatus(response, Response.Status.BAD_REQUEST)

        Assert.assertEquals(TransferResponse.debitAccountIsCreditAccount().getCode(),
                response.readEntity(TransferResponse.class).getCode())
    }


    @Test
    void testAccountNegativeAmount() {
        getAccountService().createAccount(new Account(1L, "Daniel",10000 as BigDecimal))
        getAccountService().createAccount(new Account(2L, "Mark", 10000 as BigDecimal))

        Response response = target("api/accounts/transfer")
                .queryParam("from", 1)
                .queryParam("to", 2)
                .queryParam("amount", -100)
                .request()
                .post(Entity.entity(null, MediaType.APPLICATION_JSON_TYPE))

        assertHttpStatus(response, Response.Status.BAD_REQUEST)

        Assert.assertEquals(TransferResponse.negativeAmount().getCode(),
                response.readEntity(TransferResponse.class).getCode())


    }


    @Test
    void testGetAccountById() {
        getAccountService().createAccount(new Account(1L, "Daniel", 10000 as BigDecimal))

        Response response = target("api/accounts")
                .path(Integer.toString(1))
                .request().get();

        assertHttpStatus(response, Response.Status.OK)
        Account account = response.readEntity(Account.class)

        Assert.assertNotNull("No account in response", account)
        Assert.assertEquals(Long.valueOf(1), account.getId())


    }

    @Test
    void testGetAccountsByName() {
        String accountName = "Daniel";
        getAccountService().createAccount(new Account(1L, accountName,10000 as BigDecimal))

        Response response = target("api/accounts")
                .path(accountName)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .request()
                .get()

        assertHttpStatus(response, Response.Status.OK)

        PagedResponse<Account> accountResponse = response.readEntity(new GenericType<PagedResponse<Account>>() {
        });

        Assert.assertTrue("No accounts in response",
                CollectionUtils.isNotEmpty(accountResponse.getContent()))

        Assert.assertEquals(accountName, accountResponse.getContent().get(0).getName())
    }


    @Test
    void testGet10Accounts() {
        for (int i = 0; i < 10; i++)
            getAccountService().createAccount(new Account(i + 1L, Integer.toString(i), 10000 as BigDecimal))

        Response response = target("api/accounts")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .request()
                .get()

        assertHttpStatus(response, Response.Status.OK);

        PagedResponse<Account> accountResponse = response.readEntity(new GenericType<PagedResponse<Account>>() {
        })

        Assert.assertTrue("No accounts in response",
                CollectionUtils.isNotEmpty(accountResponse.getContent()))

        Assert.assertEquals(10, accountResponse.getContent().size())

    }

    @Test
    void testNoSuchAccount() {
        Response response = target("api/accounts")
                .path(Integer.toString(1))
                .request()
                .get()

        assertHttpStatus(response, Response.Status.NOT_FOUND);

        Assert.assertEquals(TransferResponse.noSuchAccount().getCode(),
                response.readEntity(TransferResponse.class).getCode())

    }


    private static void assertHttpStatus(Response body, Response.Status status) {
        Assert.assertEquals(status.getStatusCode(), body.getStatus())
    }
}

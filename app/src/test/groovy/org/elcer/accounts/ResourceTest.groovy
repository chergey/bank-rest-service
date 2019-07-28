package org.elcer.accounts

import org.apache.commons.collections4.CollectionUtils
import org.elcer.accounts.model.Account
import org.elcer.accounts.model.PagedResponse
import org.elcer.accounts.model.TransferResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import javax.ws.rs.client.Entity
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

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

        assertHttpStatus(Response.Status.OK, response)

    }


    @Test
    void testReplaceAccount() {
        getAccountService().createAccount(new Account(1L, "Denis", 40000 as BigDecimal))

        Account account = new Account("Daniel", 10000 as BigDecimal)
        Response response = target("api/accounts")
                .path(Integer.toString(1))
                .request()
                .put(Entity.json(account))

        Account replacedAccount = response.readEntity(Account.class)
        assertHttpStatus(Response.Status.CREATED, response)
        Assertions.assertNotNull(replacedAccount)

        Assertions.assertEquals(replacedAccount.getBalance(), 10000 as BigDecimal,
                "Account balance should be 10000")

        Assertions.assertEquals("Daniel", replacedAccount.getName(),
                "Account name should be Daniel")

    }


    @Test
    void testCreateAccount() {
        Account account = new Account("Daniel", 10000 as BigDecimal);

        Response response = target("api/accounts")
                .request()
                .post(Entity.json(account));

        Account createdAccount = response.readEntity(Account.class)
        Assertions.assertNotNull(createdAccount, "Account can't be null")
        Assertions.assertNotNull(createdAccount.getId(), "Account id can't be null")

        assertHttpStatus(Response.Status.CREATED, response)

    }

    @Test
    void testAccountTransferSuccessfully() {
        def accountService = getAccountService()
        accountService.createAccount(new Account(1L, "Daniel", 10000 as BigDecimal))
        accountService.createAccount(new Account(2L, "Mark", 10000 as BigDecimal))

        Response response = target("api/accounts/transfer")
                .queryParam("from", 1)
                .queryParam("to", 2)
                .queryParam("amount", 10)
                .request()
                .post(Entity.json(null))

        assertHttpStatus(Response.Status.OK, response)

        Assertions.assertEquals(TransferResponse.success(), response.readEntity(TransferResponse.class))


    }

    @Test
    void testAccountTransfer400() {
        def accountService = getAccountService()

        accountService.createAccount(new Account(1L, "Daniel", 10000 as BigDecimal))
        accountService.createAccount(new Account(2L, "Mark", 10000 as BigDecimal))


        Response response = target("api/accounts/transfer")
                .queryParam("from")
                .queryParam("to")
                .queryParam("amount")
                .request()
                .post(Entity.json(null))

        assertHttpStatus(Response.Status.BAD_REQUEST, response)


    }

    @Test
    void testAccountTransferNotEnoughFunds() {
        def accountService = getAccountService()
        accountService.createAccount(new Account(1L, "Daniel", 1000 as BigDecimal))
        accountService.createAccount(new Account(2L, "Mark", 1000 as BigDecimal))

        Response response = target("api/accounts/transfer")
                .queryParam("from", 1)
                .queryParam("to", 2)
                .queryParam("amount", 999999)
                .request()
                .post(Entity.entity(null, MediaType.APPLICATION_JSON_TYPE))

        assertHttpStatus(Response.Status.ACCEPTED, response)

        Assertions.assertEquals(TransferResponse.notEnoughFunds().getCode(),
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

        assertHttpStatus(Response.Status.BAD_REQUEST, response)

        Assertions.assertEquals(TransferResponse.debitAccountIsCreditAccount().getCode(),
                response.readEntity(TransferResponse.class).getCode())
    }


    @Test
    void testAccountNegativeAmount() {
        getAccountService().createAccount(new Account(1L, "Daniel", 10000 as BigDecimal))
        getAccountService().createAccount(new Account(2L, "Mark", 10000 as BigDecimal))

        Response response = target("api/accounts/transfer")
                .queryParam("from", 1)
                .queryParam("to", 2)
                .queryParam("amount", -100)
                .request()
                .post(Entity.entity(null, MediaType.APPLICATION_JSON_TYPE))

        assertHttpStatus(Response.Status.BAD_REQUEST, response)

        Assertions.assertEquals(TransferResponse.negativeAmount().getCode(),
                response.readEntity(TransferResponse.class).getCode())


    }


    @Test
    void testGetAccountById() {
        getAccountService().createAccount(new Account(1L, "Daniel", 10000 as BigDecimal))

        Response response = target("api/accounts")
                .path(Integer.toString(1))
                .request().get();

        assertHttpStatus(Response.Status.OK, response)
        Account account = response.readEntity(Account.class)

        Assertions.assertNotNull(account, "No account in response")
        Assertions.assertEquals(Long.valueOf(1), account.getId())


    }

    @Test
    void testGetAccountsByName() {
        String accountName = "Daniel"
        getAccountService().createAccount(new Account(1L, accountName, 10000 as BigDecimal))

        Response response = target("api/accounts")
                .path(accountName)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .request()
                .get()

        assertHttpStatus(Response.Status.OK, response)

        PagedResponse<Account> accountResponse = response.readEntity(new GenericType<PagedResponse<Account>>() {
        })

        Assertions.assertTrue(CollectionUtils.isNotEmpty(accountResponse.getContent()),
                "No accounts in response")

        Assertions.assertEquals(accountName, accountResponse.getContent().get(0).getName())
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

        assertHttpStatus(Response.Status.OK, response);

        PagedResponse<Account> accountResponse = response.readEntity(new GenericType<PagedResponse<Account>>() {
        })

        Assertions.assertTrue(CollectionUtils.isNotEmpty(accountResponse.getContent()),
                "No accounts in response")

        Assertions.assertEquals(10, accountResponse.getContent().size())

    }

    @Test
    void testNoSuchAccount() {
        Response response = target("api/accounts")
                .path(Integer.toString(1))
                .request()
                .get()

        assertHttpStatus(Response.Status.NOT_FOUND, response)

        Assertions.assertEquals(TransferResponse.noSuchAccount().getCode(),
                response.readEntity(TransferResponse.class).getCode())

    }


    private static void assertHttpStatus(Response.Status expectedStatus, Response body) {
        Assertions.assertEquals(expectedStatus.getStatusCode(), body.getStatus())
    }
}

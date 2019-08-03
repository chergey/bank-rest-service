package org.elcer.accounts

import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.reflect.FieldUtils
import org.elcer.accounts.model.Account
import org.elcer.accounts.model.PagedResponse
import org.elcer.accounts.model.TransferResponse
import org.glassfish.jersey.client.ClientResponse
import org.glassfish.jersey.uri.internal.JerseyUriBuilder
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
        for (def account : getAccountService().getAllAccounts(0, Integer.MAX_VALUE)) {
            getAccountService().deleteAccount(account)
        }
    }

    @Test
    void "delete account with met requirements returns OK"() {
        getAccountService().createAccount(new Account(1L, "Denis", 40000 as BigDecimal))

        def response = target("api/accounts")
                .path(Integer.toString(1))
                .request()
                .delete()

        Assertions.assertTrue(response.getLinks().isEmpty(), "Should not have links")

        assertHttpStatus(Response.Status.OK, response)

    }


    @Test
    void "replace account with met requirements returns OK"() {
        getAccountService().createAccount(new Account(1L, "Denis", 40000 as BigDecimal))

        Account account = new Account("Daniel", 10000 as BigDecimal)
        def response = target("api/accounts")
                .path(Integer.toString(1))
                .request()
                .put(Entity.json(account))

        Account replacedAccount = response.readEntity(Account.class)
        assertHttpStatus(Response.Status.CREATED, response)
        Assertions.assertNotNull(replacedAccount)

        Assertions.assertEquals(10000 as BigDecimal, replacedAccount.getBalance(),
                "Account balance should be 10000")

        Assertions.assertEquals("Daniel", replacedAccount.getName(),
                "Account name should be Daniel")


        URI link = createLink(response, 1 as String)
        Assertions.assertFalse(response.getLinks().isEmpty(), "Should have links")
        Assertions.assertEquals("self", response.getLinks().first().rel, "Should have self link")
        Assertions.assertEquals(link, response.getLinks().first().uri, "Self link uri should point to account uri")

    }


    @Test
    void "create account with met requirements returns OK"() {
        Account account = new Account("Daniel", 10000 as BigDecimal);

        def response = target("api/accounts")
                .request()
                .post(Entity.json(account))

        Account createdAccount = response.readEntity(Account.class)
        Assertions.assertNotNull(createdAccount, "Account can't be null")
        Assertions.assertNotNull(createdAccount.getId(), "Account id can't be null")

        assertHttpStatus(Response.Status.CREATED, response)

        URI link = createLink(response, createdAccount.getId() as String)
        Assertions.assertFalse(response.getLinks().isEmpty(), "Should have links")
        Assertions.assertEquals("self", response.getLinks().first().rel)
        Assertions.assertEquals(link, response.getLinks().first().uri, "Self link uri should point to account uri")

    }


    @Test
    void "transfer with with met requirements returns OK"() {
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

        Assertions.assertEquals(TransferResponse.success(),
                response.readEntity(TransferResponse.class), "Should be success code")


    }

    @Test
    void "transfer without body returns 400"() {
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
    void "transfer not enough funds returns CONFLICT"() {
        def accountService = getAccountService()
        accountService.createAccount(new Account(1L, "Daniel", 1000 as BigDecimal))
        accountService.createAccount(new Account(2L, "Mark", 1000 as BigDecimal))

        Response response = target("api/accounts/transfer")
                .queryParam("from", 1)
                .queryParam("to", 2)
                .queryParam("amount", 999999)
                .request()
                .post(Entity.entity(null, MediaType.APPLICATION_JSON_TYPE))

        assertHttpStatus(Response.Status.CONFLICT, response)

        Assertions.assertEquals(TransferResponse.notEnoughFunds().getCode(),
                response.readEntity(TransferResponse.class).getCode(),
                "Response should have notEnoughFunds code")

    }

    @Test
    void "transfer to the same account returns 400"() {
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
                response.readEntity(TransferResponse.class).getCode(),
                "Response should have debitAccountIsCreditAccount code")
    }


    @Test
    void "transfer negative amount returns 400"() {
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
                response.readEntity(TransferResponse.class).getCode(), "Response should have negativeAmount code")


    }


    @Test
    void "get account by id returns OK"() {
        getAccountService().createAccount(new Account(1L, "Daniel", 10000 as BigDecimal))

        Response response = target("api/accounts")
                .path(Integer.toString(1))
                .request().get();

        assertHttpStatus(Response.Status.OK, response)
        Account account = response.readEntity(Account.class)

        Assertions.assertNotNull(account, "No account in response")
        Assertions.assertEquals(1L, account.getId())


        Assertions.assertFalse(response.getLinks().isEmpty(), "Should have links")
        Assertions.assertTrue(response.hasLink("self"), "Should have self link")
        Assertions.assertTrue(response.hasLink("accounts"), "Should have all accounts link")


    }

    @Test
    void "get accounts by name returns OK"() {
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

        Assertions.assertEquals(accountName, accountResponse.getContent().first().getName())

        def links = accountResponse.getLinks()*.rel
        Assertions.assertNotNull(links.find(s -> s == "self"), "Should have self page link")
        Assertions.assertNotNull(links.find(s -> s == "next"), "Should have next page link")
        Assertions.assertNotNull(links.find(s -> s == "last"), "Should have last page link" )
        Assertions.assertNull(links.find(s -> s == "prev"), "Should not have prev page link")
    }


    @Test
    void "get 10 accounts returns OK"() {
        for (int i = 0; i < 10; i++)
            getAccountService().createAccount(new Account(i + 1L, Integer.toString(i), 10000 as BigDecimal))

        Response response = target("api/accounts")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .request()
                .get()


        Assertions.assertTrue(response.getLinks().isEmpty(), "Should not have links")

        assertHttpStatus(Response.Status.OK, response);

        PagedResponse<Account> accountResponse = response.readEntity(new GenericType<PagedResponse<Account>>() {
        })

        Assertions.assertTrue(CollectionUtils.isNotEmpty(accountResponse.getContent()),
                "No accounts in response")

        Assertions.assertEquals(10, accountResponse.getContent().size())

        def links = accountResponse.getLinks()*.rel
        Assertions.assertNotNull(links.find(s -> s == "self"), "Should have self page link")
        Assertions.assertNotNull(links.find(s -> s == "next"), "Should have next page link")
        Assertions.assertNotNull(links.find(s -> s == "last"), "Should have last page link" )
        Assertions.assertNull(links.find(s -> s == "prev"), "Should not have prev page link")

    }

    @Test
    void "get account that doesn't exist returns 404"() {
        Response response = target("api/accounts")
                .path(Integer.toString(1))
                .request()
                .get()

        assertHttpStatus(Response.Status.NOT_FOUND, response)

        Assertions.assertEquals(TransferResponse.noSuchAccount().getCode(),
                response.readEntity(TransferResponse.class).getCode(),
        "Response should have noSuchAccount code")

    }


    private static void assertHttpStatus(Response.Status expectedStatus, Response body) {
        Assertions.assertEquals(expectedStatus.getStatusCode(), body.getStatus(),
                "HTTP status should be ${expectedStatus.getStatusCode()}")
    }

    private static URI createLink(Response response, String... values) {
        def context = FieldUtils.readDeclaredField(response, "context", true)
        def uri = (context as ClientResponse)?.getResolvedRequestUri()

        Assertions.assertNotNull(uri, "Request uri can't be null")
        def builder = new JerseyUriBuilder().uri(uri)
        for (String value : values) {
            builder.path(value)
        }

        return builder.build()
    }
}

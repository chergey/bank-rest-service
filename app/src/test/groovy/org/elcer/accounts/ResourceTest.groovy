package org.elcer.accounts


import org.apache.commons.lang3.reflect.FieldUtils
import org.elcer.accounts.db.Transaction
import org.elcer.accounts.model.Account
import org.elcer.accounts.model.PagedResponse
import org.elcer.accounts.model.TransferResponse
import org.elcer.accounts.resource.Page
import org.elcer.accounts.services.AccountRepository
import org.glassfish.jersey.client.ClientResponse
import org.glassfish.jersey.uri.internal.JerseyUriBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito

import javax.ws.rs.client.Entity
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class ResourceTest extends BaseTest {


    @Mock
    public AccountRepository accountRepository

    @Mock
    private Transaction transaction

    @Override
    protected Set<Object> getMockBeans() {
        return Collections.singleton(accountRepository)
    }


    //workaround for java.lang.AbstractMethodError while invoking in getProperty(accountRepository)
    def getAccountRepository() {
        return accountRepository
    }

    @Test
    void "delete account with met requirements returns OK"() {
        def account = new Account(1L, "Mandy", 10000 as BigDecimal)
        def url = "api/accounts"

        Mockito.when(accountRepository.findById(1L))
                .thenReturn(account)


        Mockito.doAnswer(invocation -> {
            Mockito.when(getAccountRepository().findById(1L))
                    .thenReturn(null)
        }).when(accountRepository).delete(account)


        //check for existence
        Response response = target(url)
                .path(Integer.toString(1))
                .request().get()

        assertHttpStatus(Response.Status.OK, response)
        account = response.readEntity(Account.class)

        Assertions.assertNotNull(account, "No account in response")
        Assertions.assertEquals(1L, account.getId())

        //delete
        response = target(url)
                .path(Integer.toString(1))
                .request()
                .delete()

        Assertions.assertTrue(response.getLinks().isEmpty(), "Should not have links")

        assertHttpStatus(Response.Status.OK, response)


        // check for deletion

        response = target(url)
                .path(Integer.toString(1))
                .request()
                .get()

        assertHttpStatus(Response.Status.NOT_FOUND, response)

        Assertions.assertEquals(TransferResponse.noSuchAccount().getCode(),
                response.readEntity(TransferResponse.class).getCode(),
                "Response should have noSuchAccount code")

    }


    @Test
    void "replace account with met requirements returns OK"() {
        def accountName = "Daniel"
        def url = "api/accounts"

        Mockito.when(accountRepository.findById(transaction, 1))
                .thenReturn(new Account(1L, "Mandy", 10000 as BigDecimal))

        def mockAccount = new Account(1, accountName, 10000 as BigDecimal)
        Mockito.when(accountRepository.save(transaction, mockAccount)).thenReturn(mockAccount)
        Mockito.when(accountRepository.beginTran()).thenReturn(transaction)

        def bodyAccount = new Account(accountName, 10000 as BigDecimal)
        def response = target(url)
                .path(Integer.toString(1))
                .request()
                .put(Entity.json(bodyAccount))

        Account replacedAccount = response.readEntity(Account.class)
        assertHttpStatus(Response.Status.CREATED, response)
        Assertions.assertNotNull(replacedAccount)

        Assertions.assertEquals(10000 as BigDecimal, replacedAccount.getBalance(),
                "Account balance should be 10000")

        Assertions.assertEquals(accountName, replacedAccount.getName(),
                "Account name should be $accountName")

        URI link = createLink(response, 1 as String)
        Assertions.assertFalse(response.getLinks().isEmpty(), "Should have links")
        Assertions.assertEquals("self", response.getLinks().first().rel, "Should have self link")
        Assertions.assertEquals(link, response.getLinks().first().uri, "Self link uri should point to account uri")

    }


    @Test
    void "create account with met requirements returns OK"() {
        def accountName = "Daniel"

        def mockAccount = new Account(1, accountName, 1000 as BigDecimal)
        def account = new Account(accountName, 1000 as BigDecimal)

        Mockito.when(accountRepository.save(account)).thenReturn(mockAccount)

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
        Mockito.when(accountRepository.findById(transaction, 1))
                .thenReturn(new Account(1L, "Daniel", 1000 as BigDecimal))

        Mockito.when(accountRepository.findById(transaction, 2))
                .thenReturn(new Account(2L, "Mark", 1000 as BigDecimal))

        Mockito.when(accountRepository.beginTran()).thenReturn(transaction)

        Response response = target("api/accounts/transfer")
                .queryParam("from", 1)
                .queryParam("to", 2)
                .queryParam("amount", 10)
                .request()
                .post(Entity.json(null))

        assertHttpStatus(Response.Status.OK, response)

        Assertions.assertEquals(TransferResponse.success(),
                response.readEntity(TransferResponse.class), "Response should have success code")


    }

    @Test
    void "transfer without body returns 400"() {
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
        Mockito.when(accountRepository.findById(transaction, 1))
                .thenReturn(new Account(1L, "Daniel", 1000 as BigDecimal))
        Mockito.when(accountRepository.findById(transaction, 2))
                .thenReturn(new Account(2L, "Mark", 10 as BigDecimal))

        Mockito.when(accountRepository.beginTran()).thenReturn(transaction)

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
        def response = target("api/accounts/transfer")
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
        Response response = target("api/accounts/transfer")
                .queryParam("from", 1)
                .queryParam("to", 2)
                .queryParam("amount", -100)
                .request()
                .post(Entity.entity(null, MediaType.APPLICATION_JSON_TYPE))

        assertHttpStatus(Response.Status.BAD_REQUEST, response)

        Assertions.assertEquals(TransferResponse.negativeAmount().getCode(),
                response.readEntity(TransferResponse.class).getCode(),
                "Response should have negativeAmount code")


    }


    @Test
    void "get account by id returns OK"() {
        def accountName = "Daniel"

        Mockito.when(accountRepository.findById(1))
                .thenReturn(new Account(1L, accountName, 10000 as BigDecimal))

        Response response = target("api/accounts")
                .path(Integer.toString(1))
                .request().get();

        assertHttpStatus(Response.Status.OK, response)
        Account account = response.readEntity(Account.class)

        Assertions.assertNotNull(account, "No account in response")
        Assertions.assertEquals(1L, account.getId())
        Assertions.assertEquals(accountName, account.getName())

        Assertions.assertFalse(response.getLinks().isEmpty(), "Should have links")
        Assertions.assertTrue(response.hasLink("self"), "Should have self link")
        Assertions.assertTrue(response.hasLink("accounts"), "Should have all accounts link")


    }

    @Test
    void "get accounts by name returns OK"() {
        def accountName = "Daniel"

        Mockito.when(accountRepository.findByName(accountName, 0, 10))
                .thenReturn(List.of(
                        new Account(1L, accountName, 100 as BigDecimal),
                        new Account(2L, accountName, 10000 as BigDecimal),
                        new Account(3L, accountName, 0 as BigDecimal)
                ))

        Mockito.when(accountRepository.countAccounts(accountName))
                .thenReturn(1L)

        Response response = target("api/accounts")
                .path(accountName)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .request()
                .get()

        assertHttpStatus(Response.Status.OK, response)

        def accountResponse = response.readEntity(new GenericType<PagedResponse<Account>>() {
        })

        Assertions.assertTrue(accountResponse.getContent() as boolean, "No accounts in response")
        Assertions.assertTrue(accountResponse.getContent().size() == 3, "Number of accounts should be 3")
        Assertions.assertEquals(accountName, accountResponse.getContent().first().getName())

        def links = accountResponse.getLinks()*.rel
        Assertions.assertNotNull(links.find(s -> s == "self"), "Should have self page link")
        Assertions.assertNull(links.find(s -> s == "next"), "Should not have next page link")
        Assertions.assertNotNull(links.find(s -> s == "last"), "Should have last page link")
        Assertions.assertNull(links.find(s -> s == "prev"), "Should not have prev page link")
    }


    @Test
    void "get 10 accounts returns OK"() {
        def url = "api/accounts"

        def subSet1 = []
        for (int i = 0; i < 10; i++) {
            subSet1 << new Account(i as long, i as String, 10000 as BigDecimal)
        }

        def subSet2 = []
        for (int i = 10; i < 20; i++) {
            subSet2 << new Account(i as long, i as String, 10000 as BigDecimal)
        }

        def subSet3 = []
        for (int i = 20; i < 30; i++) {
            subSet3 << new Account(i as long, i as String, 10000 as BigDecimal)
        }

        def allAccounts = subSet1 + subSet2 + subSet3 as List<Account>

        Mockito.when(accountRepository.findAll(0, 30))
                .thenReturn(allAccounts)

        Mockito.when(accountRepository.findAll(0, 10))
                .thenReturn(subSet1)

        Mockito.when(accountRepository.findAll(1, 10))
                .thenReturn(subSet2)

        Mockito.when(accountRepository.findAll(2, 10))
                .thenReturn(subSet3)

        Mockito.when(accountRepository.countAccounts())
                .thenReturn(30L)

        //0-10
        Response response = target(url)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .request()
                .get()

        assertPages(response, 10, Page.SELF, Page.NEXT, Page.LAST)

        //10-20
        response = target(url)
                .queryParam("page", 1)
                .queryParam("size", 10)
                .request()
                .get()

        assertPages(response, 10, Page.SELF, Page.NEXT, Page.LAST, Page.PREV)

        //20-30
        response = target(url)
                .queryParam("page", 2)
                .queryParam("size", 10)
                .request()
                .get()

        assertPages(response, 10, Page.SELF, Page.LAST, Page.PREV)

        //0-30
        response = target(url)
                .queryParam("page", 0)
                .queryParam("size", 30)
                .request()
                .get()

        assertPages(response, 30, Page.SELF, Page.LAST)

    }


    def static assertPages(Response response, long size, Page... requiredPages) {
        Assertions.assertTrue(response.getLinks().isEmpty(), "Should not have links")
        assertHttpStatus(Response.Status.OK, response);
        def accountResponse = response.readEntity(new GenericType<PagedResponse<Account>>() {
        })

        Assertions.assertTrue(accountResponse.getContent() as boolean, "No accounts in response")
        Assertions.assertEquals(size, accountResponse.getContent().size())

        def links = accountResponse.getLinks()*.rel

        for (def page : Page.values()) {
            if (requiredPages.contains(page))
                Assertions.assertNotNull(links.find(s -> s == page.toString()), "Should have $page page link")
            else
                Assertions.assertNull(links.find(s -> s == page.toString()), "Should have $page page link")
        }

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

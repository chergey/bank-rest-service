package org.elcer.accounts.resource;


import org.elcer.accounts.app.AppConfig;
import org.elcer.accounts.hk2.annotations.Required;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.model.PagedResponse;
import org.elcer.accounts.model.TransferResponse;
import org.elcer.accounts.services.AccountService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.math.BigDecimal;

/**
 * Application endpoint
 */

@Path("/api")
@Produces({MediaType.APPLICATION_JSON})
@Singleton
public class AccountResource {

    @Inject
    private AccountService accountService;

    @Context
    private UriInfo uriInfo;

    @POST
    @Path("/accounts")
    public Response createAccount(Account account) {
        var builder = uriInfo.getAbsolutePathBuilder();
        Account createdAccount = accountService.createAccount(account);
        builder.path(Long.toString(createdAccount.getId()));

        Link self = Link.fromUriBuilder(uriInfo.getAbsolutePathBuilder())
                .rel("self").build();

        return Response.created(builder.build())
                .entity(createdAccount)
                .links(self)
                .build();
    }

    @PUT
    @Path("/accounts/{id:\\d+}")
    public Response replaceAccount(@PathParam("id") long id, Account account) {
        var builder = uriInfo.getAbsolutePathBuilder();
        Account replacedAccount = accountService.replaceAccount(id, account);
        builder.path(Long.toString(replacedAccount.getId()));
        return Response.created(builder.build()).build();
    }

    @DELETE
    @Path("/accounts/{id:\\d+}")
    public Response deleteAccount(@PathParam("id") long id) {
        var builder = uriInfo.getAbsolutePathBuilder();
        accountService.deleteAccount(id);
        return Response.ok(builder.build()).build();
    }


    @GET
    @Path("/accounts/{name:[a-zA-Z]+}")
    public PagedResponse<Account> getAccountByName(@PathParam("name") String name,
                                                   @DefaultValue("0") @QueryParam(AppConfig.PAGE_PARAM_NAME) int page,
                                                   @DefaultValue("20") @QueryParam(AppConfig.SIZE_PARAM_NAME) int size) {
        var accounts = accountService.getAccounts(name, page, size);
        long total = accountService.countAccounts(name);
        var accountWithLink = new PagedResponse<Account>();

        if (total > 0) {
            UriBuilder startBuilder = uriInfo.getRequestUriBuilder().path(name);
            PagedResourceSupport pagedResourceSupport = new PagedResourceSupport(startBuilder);
            accountWithLink.setLinks(pagedResourceSupport.createLinks(page, size, total));
        }
        accountWithLink.setContent(accounts);
        return accountWithLink;
    }

    @GET
    @Path("/accounts/")
    public PagedResponse<Account> getAllAccounts(
            @DefaultValue("0") @QueryParam(AppConfig.PAGE_PARAM_NAME)  int page,
            @DefaultValue("20") @QueryParam(AppConfig.SIZE_PARAM_NAME) int size) {
        var accounts = accountService.getAllAccounts(page, size);

        long total = accountService.countAccounts();
        var accountWithLink = new PagedResponse<Account>();

        if (total > 0) {
            UriBuilder startBuilder = uriInfo.getRequestUriBuilder();
            PagedResourceSupport pagedResourceSupport = new PagedResourceSupport(startBuilder);
            accountWithLink.setLinks(pagedResourceSupport.createLinks(page, size, total));
        }
        accountWithLink.setContent(accounts);
        return accountWithLink;

    }

    @GET
    @Path("/accounts/{id:\\d+}")
    public Response getAccount(@PathParam("id") Long id) {

        UriBuilder startBuilder = uriInfo.getAbsolutePathBuilder();
        Link self = Link.fromUriBuilder(startBuilder)
                .rel("self").build();

        PagedResourceSupport pagedResourceSupport = new PagedResourceSupport(startBuilder);

        var account = accountService.getAccount(id);
        return Response.ok(account)
                .links(self, pagedResourceSupport.getAllAccountsLink())
                .build();

    }


    @POST
    @Path("/accounts/transfer")
    @Required({"from", "to", "amount"})
    public Response transfer(@QueryParam("from") long from, @QueryParam("to") long to,
                             @QueryParam("amount") BigDecimal amount) {

        if (from == to) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(TransferResponse.debitAccountIsCreditAccount()).build();
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(TransferResponse.negativeAmount()).build();
        }
        accountService.transfer(from, to, amount);
        return Response.ok(TransferResponse.success()).build();
    }

}

package org.elcer.accounts.resource;


import org.apache.shiro.authz.annotation.RequiresPermissions;
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
    @RequiresPermissions("users:create")
    public Response createAccount(Account account) {
        Account createdAccount = accountService.createAccount(account);

        var builder = uriInfo.getAbsolutePathBuilder();
        builder.path(Long.toString(createdAccount.getId()));
        Link self = Link.fromUriBuilder(builder).rel("self").build();

        return Response.created(builder.build())
                .entity(createdAccount)
                .links(self)
                .build();
    }

    @PUT
    @Path("/accounts/{id:\\d+}")
    @RequiresPermissions("users:update")
    public Response replaceAccount(@PathParam("id") long id, Account account) {
        var replacedAccount = accountService.replaceAccount(id, account);

        var builder = uriInfo.getAbsolutePathBuilder();
        builder.path(Long.toString(replacedAccount.getId()));
        Link self = Link.fromUriBuilder(builder).rel("self").build();
        return Response.created(builder.build())
                .entity(replacedAccount)
                .links(self)
                .build();
    }

    @DELETE
    @Path("/accounts/{id:\\d+}")
    @RequiresPermissions("users:update")
    public Response deleteAccount(@PathParam("id") long id) {
        accountService.deleteAccount(id);

        var builder = uriInfo.getAbsolutePathBuilder();
        return Response.ok(builder.build()).build();
    }


    @GET
    @Path("/accounts/{name:[a-zA-Z]+}")
    public PagedResponse<Account> getAccountByName(@PathParam("name") String name,
                                                   @DefaultValue("0") @QueryParam(AppConfig.PAGE_PARAM_NAME) int page,
                                                   @DefaultValue(AppConfig.DEFAULT_PAGESIZE) @QueryParam(AppConfig.SIZE_PARAM_NAME) int size) {
        var accounts = accountService.getAccounts(name, page, size);
        long total = accountService.countAccounts(name);
        var pagedAccounts = new PagedResponse<Account>();

        if (total > 0) {
            var startBuilder = uriInfo.getRequestUriBuilder().path(name);
            var pagedResourceSupport = new PagedResourceSupport(startBuilder);
            pagedAccounts.setLinks(pagedResourceSupport.createLinks(page, size, total));
        }
        pagedAccounts.setContent(accounts);
        return pagedAccounts;
    }

    @GET
    @Path("/accounts/")
    public PagedResponse<Account> getAllAccounts(
            @DefaultValue("0") @QueryParam(AppConfig.PAGE_PARAM_NAME) int page,
            @DefaultValue(AppConfig.DEFAULT_PAGESIZE) @QueryParam(AppConfig.SIZE_PARAM_NAME) int size) {
        var accounts = accountService.getAllAccounts(page, size);

        long total = accountService.countAccounts();
        var pagedAccounts = new PagedResponse<Account>();

        if (total > 0) {
            var startBuilder = uriInfo.getRequestUriBuilder();
            var pagedResourceSupport = new PagedResourceSupport(startBuilder);
            pagedAccounts.setLinks(pagedResourceSupport.createLinks(page, size, total));
        }
        pagedAccounts.setContent(accounts);
        return pagedAccounts;

    }

    @GET
    @Path("/accounts/{id:\\d+}")
    public Response getAccount(@PathParam("id") Long id) {
        var account = accountService.getAccount(id);

        var startBuilder = uriInfo.getAbsolutePathBuilder();
        var pagedResourceSupport = new PagedResourceSupport(startBuilder);
        Link self = Link.fromUriBuilder(startBuilder)
                .rel("self").build();

        return Response.ok(account)
                .links(self, pagedResourceSupport.getAllAccountsLink())
                .build();

    }


    @POST
    @Path("/accounts/transfer")
    @RequiresPermissions("users:update")
    public Response transfer(@Required @QueryParam("from") long from, @Required @QueryParam("to") long to,
                             @Required @QueryParam("amount") BigDecimal amount) {

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

package org.elcer.accounts.resource;


import org.elcer.accounts.hk2.annotations.Required;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.model.AccountListResponse;
import org.elcer.accounts.model.TransferResponse;
import org.elcer.accounts.services.AccountService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
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
        accountService.createAccount(account);
        builder.path(Long.toString(account.getId()));
        return Response.created(builder.build()).build();
    }

    @PUT
    @Path("/accounts/{id:\\d+}")
    public Response replaceAccount(@PathParam("id") long id, Account account) {
        var builder = uriInfo.getAbsolutePathBuilder();
        accountService.replaceAccount(id, account);
        builder.path(Long.toString(account.getId()));
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
    @Required({"page", "size"})
    public Response getAccountByName(@PathParam("name") String name,
                                     @QueryParam("page") int page,
                                     @QueryParam("size") int size) {
        var accounts = accountService.getAccounts(name, page, size);
        AccountListResponse accountListResponse = new AccountListResponse()
                .setAccounts(accounts)
                .setNoMore(accounts.size() < size);

        return Response.ok(accountListResponse).build();
    }

    @GET
    @Path("/accounts/")
    @Required({"page", "size"})
    public Response getAllAccounts(@QueryParam("page") int page,
                                   @QueryParam("size") int size) {
        var accounts = accountService.getAllAccounts(page, size);
        AccountListResponse accountListResponse = new AccountListResponse()
                .setAccounts(accounts)
                .setNoMore(accounts.size() < size);
        return Response.ok(accountListResponse).build();
    }

    @GET
    @Path("/accounts/{id:\\d+}")
    public Response getAccount(@PathParam("id") Long id) {
        var account = accountService.getAccount(id);
        return Response.ok(account).build();

    }

    @GET
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

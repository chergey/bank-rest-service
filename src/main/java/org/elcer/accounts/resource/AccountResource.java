package org.elcer.accounts.resource;


import org.elcer.accounts.hk2.annotations.Required;
import org.elcer.accounts.model.Account;
import org.elcer.accounts.model.AccountListResponse;
import org.elcer.accounts.model.AccountResponse;
import org.elcer.accounts.services.AccountService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;

@Path("/api/account")
@Produces({MediaType.APPLICATION_JSON})
@Singleton
public class AccountResource {

    @Inject
    private AccountService accountService;

    @Context
    private UriInfo uriInfo;

    @POST
    @Path("/create")
    public Response createAccount(Account account) {
        var builder = uriInfo.getAbsolutePathBuilder();
        accountService.createAccount(account);
        builder.path(Long.toString(account.getId()));
        return Response.created(builder.build()).build();
    }

    @GET
    @Path("/{name:[a-zA-Z]+}")
    public Response getAccountByName(@PathParam("name") String name) {
        var accounts = accountService.getAccounts(name);
        var accountResponse = new AccountListResponse().setAccounts(accounts);
        return Response.ok(accountResponse).build();
    }

    @GET
    @Path("/{id:\\d+}")
    public Response getAccount(@PathParam("id") Long id) {
        var account = accountService.getAccounts(id);
        AccountResponse accountResponse = new AccountResponse()
                .setAccount(account);
        return Response.ok(accountResponse).build();

    }

    @GET
    @Path("/transfer")
    @Required({"from", "to", "amount"})
    public Response transfer(@QueryParam("from") long from, @QueryParam("to") long to,
                             @QueryParam("amount") BigDecimal amount) {

        if (from == to) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(AccountResponse.debitAccountIsCreditAccount()).build();
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(AccountResponse.negativeAmount()).build();
        }
        accountService.transfer(from, to, amount);
        return Response.ok(AccountResponse.success()).build();
    }

}

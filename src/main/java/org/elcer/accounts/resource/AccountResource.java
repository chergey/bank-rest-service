package org.elcer.accounts.resource;


import org.elcer.accounts.model.Account;
import org.elcer.accounts.model.AccountResponse;
import org.elcer.accounts.services.AccountService;
import org.elcer.accounts.hk2.Required;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/account")
@Produces({MediaType.APPLICATION_JSON})
@Singleton
public class AccountResource {

    @Inject
    private AccountService accountService;

    @GET
    @Path("/{id}")

    public Response getAccount(@PathParam("id") Long id) {
        if (id == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Account account = accountService.getAccount(id);
        AccountResponse accountResponse=new AccountResponse("", 0);
        accountResponse.setAccount(account);
        return Response.ok(accountResponse).build();

    }

    @GET
    @Path("/transfer")
    @Required({"from", "to", "amount"})
    public Response transfer(@QueryParam("from") long from, @QueryParam("to") long to,
                             @QueryParam("amount") long amount) {

        if (from == to) {
            return Response.ok(AccountResponse.DEBIT_ACCOUNT_IS_CREDIT_ACCOUNT).build();
        }
        if (amount < 0) {
            return Response.ok(AccountResponse.NEGATIVE_AMOUNT).build();
        }
        accountService.transfer(from, to, amount);
        return Response.ok(AccountResponse.SUCCESS).build();
    }

}

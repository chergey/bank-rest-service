package org.elcer.accounts.resource;


import org.elcer.accounts.model.AccountResponse;
import org.elcer.accounts.services.AccountService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/account")
@Produces({MediaType.APPLICATION_JSON})
@Singleton
public class AccountResource {

    @Inject
    private AccountService accountService;


    @GET
    @Path("/transfer")
    public Response transfer(@QueryParam("from") long from, @QueryParam("to") long to,
                             @QueryParam("amount") int amount) {

        if (from == to) {
            return Response.ok(AccountResponse.DEBIT_ACCOUNT_IS_CREDIT_ACCOUNT).build();
        }
        if (amount < 0) {
            return Response.ok(AccountResponse.NEGATIVE_AMOUNT).build();
        }
        if (!accountService.transfer(from, to, amount)) {
            return Response.ok(AccountResponse.NOT_ENOUGH_MONEY).build();
        }

        return Response.ok(AccountResponse.SUCCESS).build();
    }

}

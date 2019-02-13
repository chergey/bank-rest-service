package org.elcer.accounts.exceptions.mappers;

import org.elcer.accounts.exceptions.AccountNotFoundException;
import org.elcer.accounts.model.AccountResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class NoAccountExceptionMapper implements ExceptionMapper<AccountNotFoundException> {
    @Override
    public Response toResponse(AccountNotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(AccountResponse.noSuchAccount().addAccountId(exception.getAccountId()))
                .build();
    }
}

package org.elcer.accounts.exceptions.mappers;

import org.elcer.accounts.exceptions.NotEnoughFundsException;
import org.elcer.accounts.model.AccountResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotEnoughFundsExceptionMapper implements ExceptionMapper<NotEnoughFundsException> {
    @Override
    public Response toResponse(NotEnoughFundsException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(AccountResponse.notEnoughFunds().addAccountId(exception.getAccountId()))
                .build();
    }
}
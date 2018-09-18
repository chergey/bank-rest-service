package org.elcer.accounts.exceptions;

import org.elcer.accounts.model.AccountResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotEnoughFundsExceptionMapper implements ExceptionMapper<NotEnoughFundsException> {
    @Override
    public Response toResponse(NotEnoughFundsException exception) {
        return Response.status(404)
                .entity(AccountResponse.NOT_ENOUGH_FUNDS.append(Long.toString(exception.getAccountId())))
                .build();
    }
}
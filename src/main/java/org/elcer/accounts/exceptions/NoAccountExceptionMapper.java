package org.elcer.accounts.exceptions;

import org.elcer.accounts.model.AccountResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class NoAccountExceptionMapper implements ExceptionMapper<NoAccountException> {
    @Override
    public Response toResponse(NoAccountException exception) {
        return Response.status(404)
                .entity(AccountResponse.NO_SUCH_ACCOUNT.addAccountId(exception.getAccountId()))
                .build();
    }
}

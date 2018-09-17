package org.elcer.accounts.eclipselink;

import org.elcer.accounts.model.AccountResponse;

import javax.persistence.NoResultException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class NoResultExceptionMapper implements ExceptionMapper<NoResultException> {
    @Override
    public Response toResponse(NoResultException exception) {
        return Response.status(404)
                .entity(AccountResponse.NO_SUCH_ACCOUNT)
                .build();
    }
}

package org.elcer.accounts.hk2;

import org.elcer.accounts.hk2.annotations.Required;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Provider validating required parameters at the request time
 *
 * @see org.elcer.accounts.hk2.annotations.Required
 */
@Provider
public class RequiredParamResourceFilterFactory implements DynamicFeature {

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        final Method resourceMethod = resourceInfo.getResourceMethod();
        Arrays.stream(resourceMethod.getParameters())
                .map(p -> p.getAnnotation(Required.class))
                .filter(Objects::nonNull)
                .findAny().ifPresent(p -> context.register(new RequiredParamFilter(resourceMethod)));


    }

    private class RequiredParamFilter implements ContainerRequestFilter {

        private final Method resourceMethod;

        private RequiredParamFilter(Method resourceMethod) {
            this.resourceMethod = resourceMethod;
        }

        @Override
        public void filter(ContainerRequestContext containerRequest) {
            var requiredParametersValueMissing = new ArrayList<String>();
            var queryParameters = containerRequest.getUriInfo().getQueryParameters();


            for (Parameter parameter : resourceMethod.getParameters()) {
                Required requiredAnn = parameter.getAnnotation(Required.class);
                if (requiredAnn == null) continue;

                String value = null;

                PathParam pathParam = parameter.getAnnotation(PathParam.class);
                QueryParam queryParam = parameter.getAnnotation(QueryParam.class);

                if (queryParam != null) {
                    value = queryParam.value();
                }
                if (pathParam != null) {
                    value = pathParam.value();
                }

                if (value == null) {
                    throw new RuntimeException("No @PathParam or @QueryParam defined!");
                }

                if (queryParameters.get(value).isEmpty()) {
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                            .entity(String.format("Parameter %s missing", String.join(",", requiredParametersValueMissing)))
                            .build());
                }

            }

        }

    }

}
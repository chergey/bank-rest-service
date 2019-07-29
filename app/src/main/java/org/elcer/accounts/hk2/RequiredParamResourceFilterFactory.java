package org.elcer.accounts.hk2;

import org.apache.commons.collections4.CollectionUtils;
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
                .filter(p -> p.getAnnotation(Required.class) != null)
                .peek(p -> {
                    PathParam pathParam = p.getAnnotation(PathParam.class);
                    QueryParam queryParam = p.getAnnotation(QueryParam.class);
                    if (pathParam != null && queryParam != null) {
                        throw new RuntimeException("Both PathParam and QueryParam are defined. Choose only one!");
                    }

                    if (pathParam == null && queryParam == null) {
                        throw new RuntimeException("No @PathParam or @QueryParam defined!");
                    }
                })
                .findAny()
                .ifPresent(p -> context.register(new RequiredParamFilter(resourceMethod)));
    }

    public static class RequiredParamFilter implements ContainerRequestFilter {

        private final Method resourceMethod;

        public RequiredParamFilter(Method resourceMethod) {
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
                if (pathParam != null) {
                    value = pathParam.value();
                }

                if (value == null) {
                    QueryParam queryParam = parameter.getAnnotation(QueryParam.class);
                    if (queryParam != null) {
                        value = queryParam.value();
                    }
                }

                if (CollectionUtils.isEmpty(queryParameters.get(value))) {
                    requiredParametersValueMissing.add(value);
                }
            }

            if (!requiredParametersValueMissing.isEmpty())
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(String.format("Parameters are missing: %s", String.join(",", requiredParametersValueMissing)))
                        .build());

        }

    }

}
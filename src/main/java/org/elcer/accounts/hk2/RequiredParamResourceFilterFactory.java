package org.elcer.accounts.hk2;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Provider
public class RequiredParamResourceFilterFactory implements DynamicFeature {


    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        final Method resourceMethod = resourceInfo.getResourceMethod();
        Required ann = resourceMethod.getAnnotation(Required.class);
        if (ann != null)
            context.register(new RequiredParamFilter(ann.value()));
    }

    private class RequiredParamFilter implements ContainerRequestFilter {

        private final List<String> requiredParams;

        private RequiredParamFilter(String[] requiredParams) {
            this.requiredParams = Arrays.stream(requiredParams).collect(Collectors.toList());
        }

        @Override
        public void filter(ContainerRequestContext containerRequest) {
            List<String> requiredParametersValueMissing = new ArrayList<>();
            MultivaluedMap<String, String> queryParameters = containerRequest.getUriInfo().getQueryParameters();

            Set<String> urlParms = queryParameters.keySet();
            for (Map.Entry<String, List<String>> param : queryParameters.entrySet()) {
                if (param.getValue().stream().allMatch(f -> f == null || f.isEmpty()) && requiredParams.contains(param.getKey()))
                    requiredParametersValueMissing.add(param.getKey());
            }

            for (String requiredParam : requiredParams) {
                if (!urlParms.contains(requiredParam)) {
                    requiredParametersValueMissing.add(requiredParam);
                }
            }

            if (!requiredParametersValueMissing.isEmpty()) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                        .entity(String.format("%s missing", String.join(",", requiredParametersValueMissing)))
                        .build());
            }
        }

    }

}
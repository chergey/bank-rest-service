package org.elcer.accounts.app;


import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.web.jaxrs.ShiroFeature;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Inject;

@Slf4j
public class AppConfig extends ResourceConfig {

    public static final String PAGE_PARAM_NAME = "page", SIZE_PARAM_NAME = "size";
    public static final String DEFAULT_PAGESIZE = "20";

    @Inject
    public AppConfig(ServiceLocator serviceLocator) {
        bootstrap(serviceLocator);
    }

    private void bootstrap(ServiceLocator serviceLocator) {
        packages(LocatorUtils.PACKAGE_NAME);
        register(ShiroFeature.class);
        register(JacksonFeature.class);
        addServices(serviceLocator);
    }

    private void addServices(ServiceLocator serviceLocator) {
        AbstractBinder binder = LocatorUtils.bindServices(serviceLocator, true);
        register(binder);
    }
}

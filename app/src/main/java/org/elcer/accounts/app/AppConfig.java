package org.elcer.accounts.app;


import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.web.jaxrs.ShiroFeature;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Inject;
import java.util.Collections;

@Slf4j
public class AppConfig extends ResourceConfig {

    //property names common to the whole application
    public static final String PAGE_PARAM_NAME = "page", SIZE_PARAM_NAME = "size";
    public static final String DEFAULT_PAGESIZE = "20";

    public static final String PU_NAME = "accounts";

    @Inject
    public AppConfig(ServiceLocator serviceLocator) {
        bootstrap(serviceLocator);
    }

    private void bootstrap(ServiceLocator serviceLocator) {
        packages(LocatorUtils.PACKAGE_NAMES);
        register(ShiroFeature.class);
        register(JacksonFeature.class);
        addServices(serviceLocator);
    }

    private void addServices(ServiceLocator serviceLocator) {
        AbstractBinder binder = LocatorUtils.bindServices(serviceLocator, Collections.emptySet());
        register(binder);
    }
}

package org.elcer.accounts.app;


import com.google.common.annotations.VisibleForTesting;
import eu.infomas.annotation.AnnotationDetector;
import gov.va.oia.HK2Utilities.AnnotatedClasses;
import gov.va.oia.HK2Utilities.AnnotationReporter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.elcer.accounts.hk2.annotations.Component;
import org.elcer.accounts.hk2.annotations.Eager;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

@ApplicationPath("/")
@Slf4j
public class AppConfig extends ResourceConfig {

    public static final String PAGE_PARAM_NAME = "page", SIZE_PARAM_NAME = "size";
    private static final String PACKAGE_NAME = "org.elcer.accounts";


    private static ServiceLocator SERVICE_LOCATOR;
    private static CountDownLatch initialized = new CountDownLatch(1);




    @Inject
    public AppConfig(ServiceLocator serviceLocator) {
        packages(PACKAGE_NAME);
        addServices(serviceLocator);
        register(JacksonFeature.class);

    }


    @VisibleForTesting
    @SneakyThrows
    public static ServiceLocator getServiceLocator() {
        initialized.await();
        return SERVICE_LOCATOR;
    }


    private void addServices(ServiceLocator serviceLocator) {
        var ac = new AnnotatedClasses();

        @SuppressWarnings("unchecked")
        var cf = new AnnotationDetector(new AnnotationReporter(ac, new Class[]{Component.class}));
        Class<?>[] annotatedClasses = new Class[0];

        try {
            cf.detect(PACKAGE_NAME);
            annotatedClasses = ac.getAnnotatedClasses();
        } catch (Exception e) {
            log.error("Error while scanning packages", e);
        }

        Class[] classes = Arrays.stream(annotatedClasses)
                .filter(c -> !c.isInterface()).toArray(Class[]::new);
        ServiceLocatorUtilities.addClasses(serviceLocator, classes);
        var bindings = new HashMap<Class<?>, Class<?>>();
        for (var annotatedClass : annotatedClasses) {
            if (annotatedClass.isInterface()) {
                var annotation = annotatedClass.getAnnotation(Component.class);
                Class<?> impl = annotation.value();
                if (impl == Class.class)
                    throw new RuntimeException("Component implementation is not defined!");

                bindings.put(annotatedClass, impl);

            }
        }

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindings.forEach((intf, imp) -> bind(imp).to(intf));
            }
        });

        for (var annotatedClass : annotatedClasses) {
            if (annotatedClass.isAnnotationPresent(Eager.class)) {
                serviceLocator.getService(annotatedClass);
            }
        }
        SERVICE_LOCATOR = serviceLocator;
        initialized.countDown();
    }
}

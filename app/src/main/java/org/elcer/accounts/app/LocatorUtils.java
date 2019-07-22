package org.elcer.accounts.app;

import eu.infomas.annotation.AnnotationDetector;
import gov.va.oia.HK2Utilities.AnnotatedClasses;
import gov.va.oia.HK2Utilities.AnnotationReporter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.elcer.accounts.hk2.annotations.Component;
import org.elcer.accounts.hk2.annotations.Eager;
import org.elcer.accounts.hk2.annotations.NoTest;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

@Slf4j
@UtilityClass
public class LocatorUtils {
    public static final String PACKAGE_NAME = "org.elcer.accounts";


    public static AbstractBinder bindServices(ServiceLocator serviceLocator, boolean register) {
        var ac = new AnnotatedClasses();

        @SuppressWarnings("unchecked")
        var cf = new AnnotationDetector(new AnnotationReporter(ac, new Class[]{Component.class}));
        Class<?>[] annotatedClasses = new Class[0];

        try {
            cf.detect(PACKAGE_NAME);
            annotatedClasses = ac.getAnnotatedClasses();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error while scanning packages", e);
        }

        Class[] classes = Arrays.stream(annotatedClasses)
                .filter(c -> !c.isInterface()).toArray(Class[]::new);
        ServiceLocatorUtilities.addClasses(serviceLocator, classes);
        var bindings = new HashMap<Class<?>, Class<?>>();
        for (var annotatedClass : annotatedClasses) {
            if (!TestUtils.TEST || annotatedClass.getAnnotation(NoTest.class) == null) {
                if (annotatedClass.isInterface()) {
                    var annotation = annotatedClass.getAnnotation(Component.class);
                    Class<?> impl = annotation.value();
                    if (impl == Class.class)
                        throw new RuntimeException("Component implementation is not defined!");

                    bindings.put(annotatedClass, impl);
                }
            }
        }

        if (register) {
            for (var annotatedClass : annotatedClasses) {
                if (!TestUtils.TEST || annotatedClass.getAnnotation(NoTest.class) == null) {
                    if (annotatedClass.isAnnotationPresent(Eager.class)) {
                        serviceLocator.getService(annotatedClass);
                    }
                }
            }
        }
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bindings.forEach((intf, imp) -> bind(imp).to(intf));
            }
        };
    }
}

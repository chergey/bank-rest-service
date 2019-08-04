package org.elcer.accounts.app;

import eu.infomas.annotation.AnnotationDetector;
import gov.va.oia.HK2Utilities.AnnotatedClasses;
import gov.va.oia.HK2Utilities.AnnotationReporter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.elcer.accounts.hk2.annotations.Component;
import org.elcer.accounts.hk2.annotations.Eager;
import org.elcer.accounts.hk2.annotations.NotUsedInTest;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

@Slf4j
@UtilityClass
public class LocatorUtils {

    //packages to scan for injection
    private static final String APP_NAME = "org.elcer.accounts";

    public static final String[] PACKAGE_NAMES = {
            APP_NAME + ".app",
            APP_NAME + ".db",
            APP_NAME + ".cache",
            APP_NAME + ".services",
            APP_NAME + ".hk2",
            APP_NAME + ".resource",
            APP_NAME + ".exceptions.mappers"
    };


    public static AbstractBinder bindServices(ServiceLocator serviceLocator, Set<Object> overridingBeans) {
        var ac = new AnnotatedClasses();

        @SuppressWarnings("unchecked")
        Class<? extends Annotation>[] annotations = new Class[]{Component.class};
        var detector = new AnnotationDetector(new AnnotationReporter(ac, annotations));

        Class<?>[] annotatedClasses = new Class[0];
        try {
            detector.detect(PACKAGE_NAMES);
            annotatedClasses = ac.getAnnotatedClasses();
            if (annotatedClasses.length == 0) {
                log.warn("Something is wrong. No components found!");
            } else {
                log.info("Found components {}", annotatedClasses.length);
            }
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error while scanning packages", e);
        }

        //add concrete classes
        Class[] classes = Arrays.stream(annotatedClasses)
                .filter(c -> !c.isInterface() && isMarkedAsNotUsedInTest(c) &&
                        overridingBeans.stream()
                                .map(Object::getClass)
                                .noneMatch(c::isAssignableFrom)
                )
                .toArray(Class[]::new);
        ServiceLocatorUtilities.addClasses(serviceLocator, classes);

        var bindings = new HashMap<Class<?>, Object>();
        for (var annotatedClass : annotatedClasses) {
            overridingBeans.stream()
                    .filter(overriding -> annotatedClass.isAssignableFrom(overriding.getClass()))
                    .findAny()
                    .ifPresentOrElse(bean -> bindings.put(annotatedClass, bean),
                            () -> {
                                if (isMarkedAsNotUsedInTest(annotatedClass) && annotatedClass.isInterface()) {
                                    var annotation = annotatedClass.getAnnotation(Component.class);
                                    Class<?> impl = annotation.value();
                                    if (impl == Class.class)
                                        throw new ApplicationStartupException("Component implementation is not defined!");

                                    bindings.put(annotatedClass, impl);
                                }
                            });
        }

        for (var annotatedClass : annotatedClasses) {
            if (isMarkedAsNotUsedInTest(annotatedClass) && annotatedClass.isAnnotationPresent(Eager.class)) {
                serviceLocator.getService(annotatedClass);
            }
        }
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bindings.forEach((iface, implOrClass) -> {
                    if (implOrClass instanceof Class<?>)
                        bind((Class<?>) implOrClass).to(iface);
                    else {
                        @SuppressWarnings("unchecked")
                        Class<Object> objectClass = (Class<Object>) iface;
                        bind(implOrClass).to(objectClass);
                    }

                });
            }
        };
    }

    private static boolean isMarkedAsNotUsedInTest(Class<?> clazz) {
        return !TestUtils.TEST || clazz.getAnnotation(NotUsedInTest.class) == null;
    }
}

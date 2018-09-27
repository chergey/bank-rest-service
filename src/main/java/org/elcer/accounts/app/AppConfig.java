package org.elcer.accounts.app;


import com.google.common.annotations.VisibleForTesting;
import eu.infomas.annotation.AnnotationDetector;
import gov.va.oia.HK2Utilities.AnnotatedClasses;
import gov.va.oia.HK2Utilities.AnnotationReporter;
import org.elcer.accounts.utils.ExceptionUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import java.util.concurrent.CountDownLatch;

@ApplicationPath("/")
public class AppConfig extends ResourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @VisibleForTesting
    public static ServiceLocator getServiceLocator() {
        ExceptionUtils.sneakyThrow(()-> initialized.await() );

        return SERVICE_LOCATOR;
    }

    private static ServiceLocator SERVICE_LOCATOR;
    private static CountDownLatch initialized = new CountDownLatch(1);
    private static final String PACKAGE_NAME = "org.elcer.accounts";

    @Inject
    public AppConfig(ServiceLocator serviceLocator) {
        packages(PACKAGE_NAME);
        addServices(serviceLocator);
    }

    private void addServices(ServiceLocator serviceLocator) {
        AnnotatedClasses ac = new AnnotatedClasses();

        @SuppressWarnings("unchecked")
        AnnotationDetector cf = new AnnotationDetector(new AnnotationReporter(ac, new Class[]{Service.class}));
        Class<?>[] annotatedClasses = new Class[0];

        try {
            cf.detect(PACKAGE_NAME);
            annotatedClasses = ac.getAnnotatedClasses();
        } catch (Exception e) {
            logger.error("Error while scanning packages", e);
        }

        ServiceLocatorUtilities.addClasses(serviceLocator, annotatedClasses);

        //run initializer
        serviceLocator.getService(SampleDataInitializer.class);

        SERVICE_LOCATOR = serviceLocator;
        initialized.countDown();
    }
}

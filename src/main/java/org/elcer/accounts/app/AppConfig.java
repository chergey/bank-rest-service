package org.elcer.accounts.app;


import com.google.common.annotations.VisibleForTesting;
import eu.infomas.annotation.AnnotationDetector;
import gov.va.oia.HK2Utilities.AnnotatedClasses;
import gov.va.oia.HK2Utilities.AnnotationReporter;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import java.util.concurrent.CountDownLatch;

@ApplicationPath("/")
public class AppConfig extends ResourceConfig {

    @VisibleForTesting
    public static ServiceLocator getServiceLocator() {
        try {
            initialized.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

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
            e.printStackTrace();
        }

        ServiceLocatorUtilities.addClasses(serviceLocator, annotatedClasses);

        //run initializer
        serviceLocator.getService(SampleDataInitializer.class);

        SERVICE_LOCATOR = serviceLocator;
        initialized.countDown();
    }
}

package org.elcer.accounts;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.elcer.accounts.app.AppConfig;
import org.elcer.accounts.app.ApplicationStartupException;
import org.elcer.accounts.app.LocatorUtils;
import org.elcer.accounts.app.ObjectMapperProvider;
import org.elcer.accounts.services.AccountService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.internal.PersistenceUnitBinder;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Slf4j
@ExtendWith(MockitoExtension.class)
public abstract class BaseTest extends JerseyTest {

    private ServiceLocator locator;


    static {
        System.setProperty(TestProperties.RECORD_LOG_LEVEL, Integer.toString(Level.FINE.intValue()));
    }


    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }


    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        var jacksonProvider = new JacksonJaxbJsonProvider();
        jacksonProvider.setMapper(ObjectMapperProvider.OBJECT_MAPPER);
        config.register(jacksonProvider);
    }

    @Override
    protected Application configure() {
        addTestClassPath();

        var appConfig = new ResourceConfig();
        appConfig.packages(LocatorUtils.PACKAGE_NAMES);

        var persistenceUnitBinder = new PersistenceUnitBinder(new HttpServlet() {
            @Override
            public String getInitParameter(String name) {
                return AppConfig.PU_NAME;
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return Collections.enumeration(Set.of("unit:" + AppConfig.PU_NAME));
            }
        });

        appConfig.register(persistenceUnitBinder);
        injectAppBeans(appConfig);
        return appConfig;
    }

    //add additional beans for tests
    protected Set<Object> getMockBeans() {
        return Collections.emptySet();
    }

    private void injectAppBeans(ResourceConfig appConfig) {
        appConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                locator = getServiceLocator(this);
                install(LocatorUtils.bindServices(locator, getMockBeans()));
            }

        });
    }

    /*
    Ugly hack to add app classes (not test) to forked vm classpath
     */
    private void addTestClassPath() {
        var contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            var ucp = FieldUtils.readDeclaredField(contextClassLoader, "ucp", true);
            URL[] urls = (URL[]) MethodUtils.invokeMethod(ucp, "getURLs");
            var paths = Arrays.stream(urls).map(URL::toString)
                    .filter(s -> s.contains("/app/target/"))
                    .collect(Collectors.toList());
            if (paths.size() == 0) {
                throw new ApplicationStartupException("Application build dir is not found");
            }

            if (!paths.contains("/classes")) {
                String testClassPath = paths.stream().filter(s -> s.contains("test-classes")).findAny()
                        .orElseThrow(() -> new RuntimeException("test-classes path should be present on classpath"));
                String appClassPath = testClassPath.replace("test-classes", "classes");
                MethodUtils.invokeMethod(ucp, "addURL", new URL("file:/" + appClassPath));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | MalformedURLException e) {
            throw new ApplicationStartupException("Could not set up test classpath", e);
        }
    }

    /*
    Get service locator out of binder
     */
    private ServiceLocator getServiceLocator(AbstractBinder binder) {
        ServiceLocator locator;
        try {
            Object dynamicConfiguration = MethodUtils.invokeMethod(binder, true, "configuration");
            locator = (ServiceLocator) FieldUtils.readDeclaredField(dynamicConfiguration, "locator", true);

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new ApplicationStartupException("Could not get service locator", e);
        }
        return locator;
    }


    AccountService getAccountService() {
        return locator.getService(AccountService.class);
    }

}

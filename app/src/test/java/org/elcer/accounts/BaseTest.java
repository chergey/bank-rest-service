package org.elcer.accounts;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.elcer.accounts.api.MockInitialContextFactory;
import org.elcer.accounts.app.AppConfig;
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
import org.jvnet.hk2.internal.DynamicConfigurationImpl;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public abstract class BaseTest extends JerseyTest {

    private ServiceLocator locator;

    private final Context context = mock(Context.class);

    private static final EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory(AppConfig.PU_NAME);

    static {
        System.setProperty(TestProperties.RECORD_LOG_LEVEL, Integer.toString(Level.FINE.intValue()));
    }

    private void setupJndi() throws NamingException {
        when(context.lookup("java:comp/env/" + AppConfig.PU_NAME)).thenReturn(entityManagerFactory);
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MockInitialContextFactory.class.getName());
        MockInitialContextFactory.setGlobalContext(context);
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        setupJndi();
        super.setUp();
    }


    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
        jacksonProvider.setMapper(ObjectMapperProvider.OBJECT_MAPPER);
        config.register(jacksonProvider);
    }

    @Override
    protected Application configure() {
        rectifyAppClassPath();

        ResourceConfig appConfig = new ResourceConfig();
        appConfig.packages(LocatorUtils.PACKAGE_NAMES);

        PersistenceUnitBinder persistenceUnitBinder = new PersistenceUnitBinder(new HttpServlet() {
            @Override
            public String getInitParameter(String name) {
                return "accounts";
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return Collections.enumeration(Set.of("unit:" + AppConfig.PU_NAME));
            }
        });

        appConfig.register(persistenceUnitBinder);

        appConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                locator = getServiceLocator(this);
                install(LocatorUtils.bindServices(locator, false));
            }


        });

        return appConfig;
    }

    /*
    Ugly hack to add app classes (not test) to forked vm classpath
     */
    private void rectifyAppClassPath() {
        var contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            var ucp = FieldUtils.readDeclaredField(contextClassLoader, "ucp", true);
            URL[] urls = (URL[]) MethodUtils.invokeMethod(ucp, "getURLs");
            List<String> paths = Arrays.stream(urls).map(URL::toString)
                    .filter(s -> s.contains("/app/target/"))
                    .collect(Collectors.toList());
            if (paths.size() == 0) {
                throw new RuntimeException("Application build dir is not found");
            }

            if (!paths.contains("/classes")) {
                String testClassPath = paths.stream().filter(s -> s.contains("test-classes")).findAny().orElseThrow(
                        () -> new RuntimeException("test-classes path should be present on classpath"));
                String appClassPath = testClassPath.replace("test-classes", "classes");
                MethodUtils.invokeMethod(ucp, "addURL", new URL("file:/" + appClassPath));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    Get service locator out of binder
     */
    private ServiceLocator getServiceLocator(AbstractBinder binder) {
        ServiceLocator locator;
        try {
            var dynamicConfiguration = (DynamicConfigurationImpl)
                    MethodUtils.invokeMethod(binder, true, "configuration");
            locator = (ServiceLocator) FieldUtils.readDeclaredField(dynamicConfiguration, "locator", true);

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return locator;
    }


    AccountService getAccountService() {
        return locator.getService(AccountService.class);
    }

}

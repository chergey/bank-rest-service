package org.elcer.accounts;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.elcer.accounts.api.MockInitialContextRule;
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
import org.junit.Rule;
import org.jvnet.hk2.internal.DynamicConfigurationImpl;

import javax.naming.Context;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public abstract class BaseTest extends JerseyTest {

    private ServiceLocator locator;

    private final Context context = mock(Context.class);

    @Rule
    public MockInitialContextRule mockInitialContextRule = new MockInitialContextRule(context);

    private static final EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory("accounts");


    @Override
    public void setUp() throws Exception {
        when(context.lookup("java:comp/env/accounts")).thenReturn(entityManagerFactory);
        super.setUp();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
        jacksonProvider.setMapper(ObjectMapperProvider.OBJECT_MAPPER);
        config.register(jacksonProvider);
    }

    @Override
    protected Application configure() {
        ResourceConfig appConfig = new ResourceConfig();
        appConfig.packages(LocatorUtils.PACKAGE_NAME);

        PersistenceUnitBinder persistenceUnitBinder = new PersistenceUnitBinder(new HttpServlet() {
            @Override
            public String getInitParameter(String name) {
                return "accounts";
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return Collections.enumeration(Set.of("unit:accounts"));
            }
        });

        appConfig.register(persistenceUnitBinder);

        appConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                locator = getServiceLocator();
                install(LocatorUtils.bindServices(locator, false));
            }

            private ServiceLocator getServiceLocator() {
                ServiceLocator locator;
                try {
                    DynamicConfigurationImpl dynamicConfiguration = (DynamicConfigurationImpl)
                            MethodUtils.invokeMethod(this, true, "configuration");
                    locator = (ServiceLocator) FieldUtils.readDeclaredField(dynamicConfiguration, "locator", true);

                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                return locator;
            }
        });

        return appConfig;
    }

    AccountService getAccountService() {
        return locator.getService(AccountService.class);
    }

}

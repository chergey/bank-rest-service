package org.elcer.accounts.hk2;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.api.ServiceHandle;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Arrays;
import java.util.Optional;


/*
 * Catch-all resolver for custom services
 */


@Singleton
@Service
@SuppressWarnings("unused")
@Rank(1)
public class CustomInjectionResolver implements InjectionResolver<CustomInject> {
    @Inject
    @Named(InjectionResolver.SYSTEM_RESOLVER_NAME)
    private InjectionResolver<Inject> systemResolver;


    @Override
    public Object resolve(Injectee injectee, ServiceHandle<?> root) {
         return Arrays.stream(Beans.values()).filter(f -> f.clazz == injectee.getRequiredType())
                .findAny().map(f -> f.create(injectee)).orElseGet(()->
                        systemResolver.resolve(injectee, root)
                );
    }

    @Override
    public boolean isConstructorParameterIndicator() {
        return false;
    }

    @Override
    public boolean isMethodParameterIndicator() {
        return false;
    }

    @SuppressWarnings("unchecked")
    private enum Beans {
        LOGGER(Logger.class) {
            @Override
            <T> T create(Injectee injectee) {
                return (T) LoggerFactory.getLogger(injectee.getInjecteeClass());
            }
        },

        ENTITY_MANAGER(EntityManagerFactory.class) {
            @Override
            <T> T create(Injectee injectee) {
                CustomInject annotation = injectee.getParent().getAnnotation(CustomInject.class);
                EntityManagerFactory factory = Persistence.createEntityManagerFactory(annotation.name());
                return (T) factory;
            }
        };

        Beans(Class<?> clazz) {
            this.clazz = clazz;
        }

        abstract <T> T create(Injectee injectee);

        private Class<?> clazz;

    }
}
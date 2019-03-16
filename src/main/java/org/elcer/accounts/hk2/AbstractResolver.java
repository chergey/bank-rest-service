package org.elcer.accounts.hk2;

import org.apache.commons.lang3.StringUtils;
import org.elcer.accounts.hk2.annotations.PersistenceContext;
import org.elcer.accounts.hk2.annotations.PersistenceUnit;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Arrays;

public abstract class AbstractResolver<T> implements InjectionResolver<T> {

    @Inject
    @Named(InjectionResolver.SYSTEM_RESOLVER_NAME)
    protected InjectionResolver<Inject> systemResolver;


    @Override
    public Object resolve(Injectee injectee, ServiceHandle<?> root) {
        return Arrays.stream(Beans.values())
                .filter(f -> f.clazz == injectee.getRequiredType())
                .findAny().map(f -> f.create(injectee))
                .orElseGet(() -> systemResolver.resolve(injectee, root));
    }


    @Override
    public boolean isConstructorParameterIndicator() {
        return true;
    }

    @Override
    public boolean isMethodParameterIndicator() {
        return true;
    }

    private enum Beans {
        LOGGER(Logger.class) {
            @Override
            @SuppressWarnings("unchecked")
            <T> T create(Injectee injectee) {
                return (T) LoggerFactory.getLogger(injectee.getInjecteeClass());
            }
        },
        ENTITY_MANAGER_FACTORY(EntityManagerFactory.class) {
            @Override
            @SuppressWarnings("unchecked")
            <T> T create(Injectee injectee) {
                var annotation = injectee.getParent().getAnnotation(PersistenceUnit.class);
                if (annotation == null || StringUtils.isEmpty(annotation.name())) {
                    throw new RuntimeException("@PersistenceUnit (and name) should be specified on EntityManagerFactory");
                }
                var factory = Persistence.createEntityManagerFactory(annotation.name());
                return (T) factory;
            }
        },

        ENTITY_MANAGER(EntityManager.class) {
            @Override
            @SuppressWarnings("unchecked")
            <T> T create(Injectee injectee) {
                var annotation = injectee.getParent().getAnnotation(PersistenceContext.class);
                if (annotation == null || StringUtils.isEmpty(annotation.name())) {
                    throw new RuntimeException("@PersistenceContext (and name) should be specified on EntityManager");
                }
                var factory = Persistence.createEntityManagerFactory(annotation.name());
                return (T) factory.createEntityManager();
            }
        };

        Beans(Class<?> clazz) {
            this.clazz = clazz;
        }

        abstract <T> T create(Injectee injectee);

        private Class<?> clazz;

    }
}

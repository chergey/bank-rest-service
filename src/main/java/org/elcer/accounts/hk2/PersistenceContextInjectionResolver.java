package org.elcer.accounts.hk2;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceHandle;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Arrays;


/*
 * Injection of persistence contexts
 */
@Service
@SuppressWarnings("unused")
public class PersistenceContextInjectionResolver implements InjectionResolver<PersistenceContext> {
    @Inject
    @Named(InjectionResolver.SYSTEM_RESOLVER_NAME)
    private InjectionResolver<Inject> systemResolver;


    @Override
    public Object resolve(Injectee injectee, ServiceHandle<?> root) {
        return Arrays.stream(Beans.values()).filter(f -> f.clazz == injectee.getRequiredType())
                .findAny().map(f -> f.create(injectee)).orElseGet(() ->
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

    private enum Beans {
        ENTITY_MANAGER_FACTORY(EntityManagerFactory.class) {
            @Override
            @SuppressWarnings("unchecked")
            <T> T create(Injectee injectee) {
                PersistenceContext annotation = injectee.getParent().getAnnotation(PersistenceContext.class);
                EntityManagerFactory factory = Persistence.createEntityManagerFactory(annotation.name());
                return (T) factory;
            }
        },

        ENTITY_MANAGER(EntityManager.class) {
            @Override
            @SuppressWarnings("unchecked")
            <T> T create(Injectee injectee) {
                PersistenceContext annotation = injectee.getParent().getAnnotation(PersistenceContext.class);
                EntityManagerFactory factory = Persistence.createEntityManagerFactory(annotation.name());
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
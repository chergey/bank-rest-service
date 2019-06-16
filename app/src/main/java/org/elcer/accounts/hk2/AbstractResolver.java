package org.elcer.accounts.hk2;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceHandle;

import javax.inject.Inject;
import javax.inject.Named;
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

}

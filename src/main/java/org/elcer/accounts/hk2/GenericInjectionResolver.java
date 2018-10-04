package org.elcer.accounts.hk2;

import org.elcer.accounts.utils.ExceptionUtils;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.utilities.InjecteeImpl;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.internal.SystemInjecteeImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


@Service
@Rank(1)
@SuppressWarnings("unused")
public class GenericInjectionResolver implements InjectionResolver<Inject> {

    @Inject
    @Named(InjectionResolver.SYSTEM_RESOLVER_NAME)
    private InjectionResolver<Inject> systemResolver;


    @Override
    public Object resolve(Injectee injectee, ServiceHandle<?> root) {
        if (injectee.getRequiredType() instanceof ParameterizedType &&
                injectee.getParent().getAnnotation(Raw.class) != null) {
            Type rawType = ((ParameterizedType) injectee.getRequiredType()).getRawType();
            if (injectee instanceof SystemInjecteeImpl) {
                Field requiredType;
                try {
                    requiredType = SystemInjecteeImpl.class.getDeclaredField("requiredType");

                    requiredType.setAccessible(true);
                    requiredType.set(injectee, rawType);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    ExceptionUtils.sneakyThrow(e);
                }
                return systemResolver.resolve(injectee, root);
            } else if (injectee instanceof InjecteeImpl) {
                ((InjecteeImpl) injectee).setRequiredType(
                        rawType);
            }
        }

        return systemResolver.resolve(injectee, root);
    }

    @Override
    public boolean isConstructorParameterIndicator() {
        return false;
    }

    @Override
    public boolean isMethodParameterIndicator() {
        return false;
    }
}

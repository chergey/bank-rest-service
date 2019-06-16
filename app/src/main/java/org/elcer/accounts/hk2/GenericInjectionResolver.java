package org.elcer.accounts.hk2;

import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.elcer.accounts.hk2.annotations.Component;
import org.elcer.accounts.hk2.annotations.Raw;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.utilities.InjecteeImpl;
import org.jvnet.hk2.internal.SystemInjecteeImpl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Injector resolving raw types and injecting them
 */


@Rank(1)
@SuppressWarnings("unused")
@Component
@Singleton
public class GenericInjectionResolver extends AbstractResolver<Inject> {

    @Override
    @SneakyThrows
    public Object resolve(Injectee injectee, ServiceHandle<?> root) {
        if (injectee.getRequiredType() instanceof ParameterizedType &&
                injectee.getParent().getAnnotation(Raw.class) != null) {
            Type rawType = ((ParameterizedType) injectee.getRequiredType()).getRawType();
            if (injectee instanceof SystemInjecteeImpl) {
                FieldUtils.writeDeclaredField(injectee, "requiredType", rawType, true);
                return systemResolver.resolve(injectee, root);
            } else if (injectee instanceof InjecteeImpl) {
                ((InjecteeImpl) injectee).setRequiredType(rawType);
            }
        }

        return super.resolve(injectee, root);
    }
}

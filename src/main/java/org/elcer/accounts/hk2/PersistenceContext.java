package org.elcer.accounts.hk2;

import org.glassfish.hk2.api.InjectionPointIndicator;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({METHOD, FIELD})
@InjectionPointIndicator
public @interface PersistenceContext {
    String name() default "";
}
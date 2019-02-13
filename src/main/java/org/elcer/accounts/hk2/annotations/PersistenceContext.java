package org.elcer.accounts.hk2.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * JPA persistence context
 * @see javax.persistence.EntityManager
 */
@Retention(RUNTIME)
@Target({METHOD, FIELD})
public @interface PersistenceContext {
    String name() default "";
}
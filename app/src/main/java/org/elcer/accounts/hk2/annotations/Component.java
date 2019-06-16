package org.elcer.accounts.hk2.annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to annotate beans to be added to context
 *
 */
@Retention(RUNTIME)
@Target({TYPE})
public @interface Component {
    /**
     * @return implementation of interface if it is an interface
     */
    Class<?> value() default Class.class;
}

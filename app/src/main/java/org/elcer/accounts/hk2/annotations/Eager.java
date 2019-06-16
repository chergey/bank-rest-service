package org.elcer.accounts.hk2.annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to make an early resolution of the component
 * @see Component
 */
@Retention(RUNTIME)
@Target({TYPE})
public @interface Eager {
}

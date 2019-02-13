package org.elcer.accounts.hk2.annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to inject generified signatures as raw ones
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface Raw {
}

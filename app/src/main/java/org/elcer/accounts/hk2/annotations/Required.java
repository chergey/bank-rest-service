package org.elcer.accounts.hk2.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to specify required params for the request method
 * @see org.elcer.accounts.hk2.RequiredParamResourceFilterFactory
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Required {
}
package org.elcer.accounts.hk2.annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used to indicate that the class is not scanned when running tests
 */
@Retention(RUNTIME)
@Target({TYPE})
public @interface NotUsedInTest {
}

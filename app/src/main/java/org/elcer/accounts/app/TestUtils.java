package org.elcer.accounts.app;


import io.vavr.control.Try;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class TestUtils {

    public static final boolean TEST = isTest();

    private static boolean isTest() {
       return Try.sequence(List.of( Try.of(() -> {
                   Class.forName("org.junit.Test");
                   return true;
               }),
               Try.of(() -> {
                   Class.forName("org.junit.jupiter.api.Test");
                   return true;
               })
       )).flatMap(f-> f.toTry()).getOrElse(false);

    }

}

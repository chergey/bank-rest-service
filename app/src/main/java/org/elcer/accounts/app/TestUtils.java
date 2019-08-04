package org.elcer.accounts.app;


import io.vavr.Value;
import io.vavr.control.Try;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
@SuppressWarnings("deprecation")
public class TestUtils {

    public static final boolean TEST = isTest();

    private static boolean isTest() {
        return Try.sequence(List.of(Try.run(() -> Class.forName("org.junit.Test")),
                Try.run(() -> Class.forName("org.junit.jupiter.api.Test"))
        )).flatMap(Value::toTry).map(f -> true).getOrElse(false);

    }

}

package org.elcer.accounts.api;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.Arrays;


@UtilityClass
public class ExecutorUtils {

    @SneakyThrows
    public static void runConcurrentlyFJP(Runnable... tasks) {
        var oldClLdr = Thread.currentThread().getContextClassLoader();
        Arrays.stream(tasks).parallel().forEach(r -> {
            ClassLoader currentClLdr = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(oldClLdr);
            try {
                r.run();
            } finally {
                Thread.currentThread().setContextClassLoader(currentClLdr);
            }

        });
    }
}

package org.elcer.accounts.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.function.Function;


@UtilityClass
@SuppressWarnings("unused")
public class ExceptionUtils {

    public static <T> void wrap(Runnable delegate, Runnable cleanup) {
        wrap((Function<T, Object>) ignored -> {
            delegate.run();
            return null;
        }, cleanup, null);
    }

    @SneakyThrows
    public static <T, R> R wrap(Function<T, R> delegate, Runnable cleanup, T arg) {
        Exception saved = null;
        R value = null;
        try {
            value = delegate.apply(arg);
        } catch (Exception e) {
            saved = e;
        } finally {
            try {
                cleanup.run();
            } catch (Exception x) {
                if (saved != null) {
                    saved.addSuppressed(x);
                }
            }
        }
        if (saved != null) {
            throw saved;
        }
        return value;
    }

}

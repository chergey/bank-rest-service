package org.elcer.accounts.utils;

import java.util.function.Consumer;
import java.util.function.Function;

public class ExceptionUtils {
    public static <T> void wrap(Consumer<T> delegate, Runnable cleanup) {
        wrap((Function<T, Object>) t -> {
            delegate.accept(t);
            return null;
        }, cleanup, null);
    }

    public static <T> void wrap(Runnable delegate, Runnable cleanup) {
        wrap((Function<T, Object>) ignored -> {
            delegate.run();
            return null;
        }, cleanup, null);
    }

    public static <T> void wrap(Consumer<T> delegate, Runnable cleanup, T arg) {
        wrap(t -> {
            delegate.accept(t);
            return null;
        }, cleanup, arg);
    }

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
            sneakyThrow(saved);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }
}

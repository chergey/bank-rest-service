package org.elcer.accounts.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Synchronization mechanism on primitives
 * @param <T>
 */
public class SyncManager<T extends Comparable<T>> {
    private final Map<T, Object> slots = new ConcurrentHashMap<>();

    private DeadlockStrategy<T> deadlockStrategy =
            (candidate1, candidate2) -> candidate1.compareTo(candidate2) > 0;

    public void withLock(final T one, final T second, Runnable action) {
        Object o1 = slots.computeIfAbsent(one, (k) -> new Object()),
                o2 = slots.computeIfAbsent(second, (k) -> new Object()),
                fistToTake, secondToTake;
        if (deadlockStrategy.resolve(one, second)) {
            fistToTake = o1;
            secondToTake = o2;
        } else {
            fistToTake = o2;
            secondToTake = o1;
        }
        synchronized (fistToTake) {
            synchronized (secondToTake) {
                action.run();
            }
        }
    }

}

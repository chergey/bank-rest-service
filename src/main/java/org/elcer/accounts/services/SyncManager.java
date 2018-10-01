package org.elcer.accounts.services;

import org.jvnet.hk2.annotations.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Synchronization mechanism on primitives
 *
 * @param <T>
 */

@Service
public class SyncManager<T extends Comparable<T>> {
    private final Map<T, Object> slots = new ConcurrentHashMap<>();

    private DeadlockStrategy<T> deadlockStrategy =
            (candidate1, candidate2) -> candidate1.compareTo(candidate2) > 0;

    @SuppressWarnings("SynchronizeOnNonFinalField")
    public void withLock(final T one, final T second, Runnable action) {
        Object o1 = slots.computeIfAbsent(one, (k) -> new Object()),
                o2 = slots.computeIfAbsent(second, (k) -> new Object()),
                firstToTake, secondToTake;
        if (deadlockStrategy.resolve(one, second)) {
            firstToTake = o1;
            secondToTake = o2;
        } else {
            firstToTake = o2;
            secondToTake = o1;
        }
        synchronized (firstToTake) {
            synchronized (secondToTake) {
                action.run();
            }
        }
    }

}

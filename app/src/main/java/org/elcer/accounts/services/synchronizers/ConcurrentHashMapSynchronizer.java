package org.elcer.accounts.services.synchronizers;

import org.elcer.accounts.services.CompareStrategy;
import org.elcer.accounts.services.Synchronizer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Alternative synchronization, making use of internal structure of CHM
 * @see Synchronizer
 */
@SuppressWarnings("unused")
public class ConcurrentHashMapSynchronizer<T extends Comparable<T>> implements Synchronizer<T> {

    private final Map<T, Object> slots = new ConcurrentHashMap<>();

    private static final Object DUMMY_OBJECT = new Object();

    private CompareStrategy<T> compareStrategy =
            (candidate1, candidate2) -> candidate1.compareTo(candidate2) > 0;


    private void lock(T firstToTake, T secondToTake, Runnable action) {
        slots.compute(firstToTake, (t, o) -> {
            slots.compute(secondToTake, (t1, o1) -> {
                action.run();
                return DUMMY_OBJECT;
            });
            return DUMMY_OBJECT;
        });
    }

    @Override
    public void withLock(T one, T second, Runnable action) {
        if (compareStrategy.compare(one, second)) {
            lock(one, second, action);
        } else {
            lock(second, one, action);
        }
    }
}

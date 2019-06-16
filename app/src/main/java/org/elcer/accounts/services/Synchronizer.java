package org.elcer.accounts.services;


import org.elcer.accounts.hk2.annotations.Component;
import org.elcer.accounts.services.synchronizers.ReentrantlockSynchronizer;

/**
 * Synchcronizer used to manage concurrent operations
 * @param <T>
 */
@Component(ReentrantlockSynchronizer.class)
public interface Synchronizer<T extends Comparable<T>> {
    void withLock(T one, T second, Runnable action);
}

package org.elcer.accounts.services;


import org.elcer.accounts.hk2.annotations.Component;

@Component(ReentrantlockSynchronizer.class)
public interface Synchronizer<T extends Comparable<T>> {
    void withLock(T one, T second, Runnable action);
}

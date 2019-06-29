package org.elcer.accounts.cache;

public interface CacheCreator {

    /**
     * Create new or return previously created cache
     * @param name cache name
     * @param <K> key
     * @param <V> value
     * @return javax.Cache instance
     */
    <K, V> Cache<K, V> getOrCreateCache(String name);
}

package org.elcer.accounts.cache;

import com.hazelcast.core.IMap;

public interface CacheCreator {

    /**
     * Create new or return previously created cache
     * @param name cache name
     * @param <K> key
     * @param <V> value
     * @return javax.Cache instance
     */
    <K, V> IMap<K, V> getOrCreateCache(String name);
}

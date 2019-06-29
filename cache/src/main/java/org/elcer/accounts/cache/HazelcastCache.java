package org.elcer.accounts.cache;

import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicates;

import java.util.Map;
import java.util.Set;

public class HazelcastCache<K, V> implements Cache<K, V> {

    private final IMap<K, V> delegate;

    public HazelcastCache(IMap<K, V> delegate) {
        this.delegate = delegate;
    }

    @Override
    public V get(K key) {
        return delegate.get(key);
    }

    @Override
    public void put(K key, V val) {
        delegate.put(key, val);
    }

    @Override
    public boolean containsKey(K key) {
        return delegate.containsKey(key);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        @SuppressWarnings("unchecked")
        Set<K> kSet = (Set<K>) keys;
        return delegate.getAll(kSet);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        delegate.putAll(map);
    }

    @Override
    public boolean replace(K key, V oldVal, V newVal) {
        return delegate.replace(key, oldVal, newVal);
    }

    @Override
    public boolean replace(K key, V val) {
        return delegate.replace(key, val) != null;
    }

    @Override
    public boolean remove(K key) {
        return delegate.remove(key) != null;
    }

    @Override
    public boolean remove(K key, V oldVal) {
        return delegate.remove(key, oldVal);
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        keys.forEach(delegate::remove);
    }

    @Override
    public void removeAll() {
        delegate.removeAll(Predicates.alwaysTrue());
    }

    @Override
    public V getAndPut(K key, V val) {
        V v = delegate.get(key);
        if (v != null)
            delegate.put(key, val);
        return v;
    }

    @Override
    public V getAndRemove(K key) {
        return delegate.remove(key);
    }

    @Override
    public V getAndReplace(K key, V val) {
        return delegate.replace(key, val);
    }

    @Override
    public boolean putIfAbsent(K key, V val) {
        return delegate.putIfAbsent(key, val) != null;
    }

    @Override
    public void clear() {
        delegate.clear();
    }
}

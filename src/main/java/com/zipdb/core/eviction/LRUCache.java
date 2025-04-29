package com.zipdb.core.eviction;

import com.zipdb.core.datatype.DataType;

import java.util.LinkedHashMap;

public class LRUCache implements EvictionCache {
    private final int capacity;
    private final LinkedHashMap<String, DataType> cache;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true);
    }

    @Override
    public DataType get(String key) {
        return cache.get(key);
    }

    @Override
    public void set(String key, DataType value) {
        if (cache.size() >= capacity) {
            evict();
        }
        cache.put(key, value);
    }

    @Override
    public void remove(String key) {
        cache.remove(key);
    }

    @Override
    public void evict() {
        String eldest = cache.entrySet().iterator().next().getKey();
        cache.remove(eldest);
    }
}

package com.zipdb.core;

import com.zipdb.core.datatype.DataType;
import com.zipdb.core.eviction.EvictionCache;
import com.zipdb.core.eviction.LRUCache;
import com.zipdb.core.eviction.LFUCache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private final ConcurrentHashMap<String, DataType> store = new ConcurrentHashMap<>();
    private final Map<String, Long> expirationTimes = new HashMap<>();
    private final EvictionCache evictionCache;  // Use interface for eviction cache

    // Constructor allows selecting eviction policy
    public DataStore(int cacheSize, boolean useLFU) {
        if (useLFU) {
            this.evictionCache = new LFUCache(cacheSize);  // Use LFU cache
        } else {
            this.evictionCache = new LRUCache(cacheSize);  // Default to LRU cache
        }
    }

    public void set(String key, DataType value) {
        store.put(key, value);
        evictionCache.set(key, value);  // Add to eviction cache
        expirationTimes.remove(key);
    }

    public DataType get(String key) {
        if (isExpired(key)) {
            remove(key);
            return null;
        }

        return evictionCache.get(key);  // Retrieve from eviction cache
    }

    public boolean exists(String key) {
        return store.containsKey(key);
    }

    public void delete(String key) {
        store.remove(key);
        evictionCache.remove(key);  // Remove from eviction cache
    }

    public Map<String, DataType> getAllEntries() {
        return store;
    }

    public Set<String> getAllKeys() {
        return store.keySet();
    }

    public void remove(String key) {
        store.remove(key);
        expirationTimes.remove(key);
        evictionCache.remove(key);
    }

    public void setExpiration(String key, long expirationTime) {
        expirationTimes.put(key, expirationTime);
    }

    public boolean isExpired(String key) {
        Long expirationTime = expirationTimes.get(key);
        return expirationTime != null && System.currentTimeMillis() > expirationTime;
    }
}

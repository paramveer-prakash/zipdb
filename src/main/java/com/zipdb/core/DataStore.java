package com.zipdb.core;

import com.zipdb.core.datatype.DataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the in-memory data store.
 */
public class DataStore {

    private final ConcurrentHashMap<String, DataType> store = new ConcurrentHashMap<>();
    private Map<String, Long> expirationTimes = new HashMap<>();  // To store expiration times

    public void set(String key, DataType value) {
        store.put(key, value);
        expirationTimes.remove(key);
    }

    public DataType get(String key) {
        if (isExpired(key)) {
            remove(key);  // Automatically remove expired keys
            return null;
        }
        return store.get(key);
    }

    public boolean exists(String key) {
        return store.containsKey(key);
    }

    public void delete(String key) {
        store.remove(key);
    }

    public Map<String, DataType> getAllEntries() {
        return store;
    }

    public void remove(String key) {
        store.remove(key);
        expirationTimes.remove(key);
    }

    public void setExpiration(String key, long expirationTime) {
        expirationTimes.put(key, expirationTime);
    }

    public boolean isExpired(String key) {
        Long expirationTime = expirationTimes.get(key);
        return expirationTime != null && System.currentTimeMillis() > expirationTime;
    }

    public Set<String> getAllKeys() {
        return store.keySet();  // Assuming 'store' is a map holding the data
    }

}

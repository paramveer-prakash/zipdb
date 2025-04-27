package com.zipdb.core;

import com.zipdb.core.datatype.DataType;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the in-memory data store.
 */
public class DataStore {

    private final ConcurrentHashMap<String, DataType> store = new ConcurrentHashMap<>();

    public void set(String key, DataType value) {
        store.put(key, value);
    }

    public DataType get(String key) {
        return store.get(key);
    }

    public boolean exists(String key) {
        return store.containsKey(key);
    }

    public void delete(String key) {
        store.remove(key);
    }
}

package com.zipdb.core.eviction;

import com.zipdb.core.datatype.DataType;

public interface EvictionCache {

    // Get data from cache
    DataType get(String key);

    // Set data in cache
    void set(String key, DataType value);

    // Remove data from cache
    void remove(String key);

    // Evict the least used item based on the eviction policy
    void evict();
}

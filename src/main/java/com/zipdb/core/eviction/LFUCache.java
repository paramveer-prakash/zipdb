package com.zipdb.core.eviction;

import com.zipdb.core.datatype.DataType;

import java.util.*;

public class LFUCache implements EvictionCache {
    private final int capacity;
    private final Map<String, DataType> cache;
    private final Map<String, Integer> frequencies;
    private final PriorityQueue<KeyFrequency> frequencyQueue;

    public LFUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>(capacity);
        this.frequencies = new HashMap<>(capacity);
        this.frequencyQueue = new PriorityQueue<>(Comparator.comparingInt(KeyFrequency::getFrequency));
    }

    @Override
    public DataType get(String key) {
        if (cache.containsKey(key)) {
            frequencies.put(key, frequencies.get(key) + 1);
            return cache.get(key);
        }
        return null;
    }

    @Override
    public void set(String key, DataType value) {
        if (cache.size() >= capacity) {
            evict();
        }
        cache.put(key, value);
        frequencies.put(key, 1);
        frequencyQueue.add(new KeyFrequency(key, 1));
    }

    @Override
    public void remove(String key) {
        cache.remove(key);
        frequencies.remove(key);
        frequencyQueue.removeIf(kf -> kf.getKey().equals(key));
    }

    @Override
    public void evict() {
        KeyFrequency leastFrequent = frequencyQueue.poll();
        if (leastFrequent != null) {
            String keyToEvict = leastFrequent.getKey();
            cache.remove(keyToEvict);
            frequencies.remove(keyToEvict);
        }
    }

    // Utility class to track frequency of keys
    private static class KeyFrequency {
        private final String key;
        private final int frequency;

        public KeyFrequency(String key, int frequency) {
            this.key = key;
            this.frequency = frequency;
        }

        public String getKey() {
            return key;
        }

        public int getFrequency() {
            return frequency;
        }
    }
}

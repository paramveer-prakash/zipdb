package com.zipdb.core.datatype;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a Redis-like hash type (a map of fields and values).
 */
public class HashType implements DataType {

    private final ConcurrentHashMap<String, String> hash;

    public HashType() {
        this.hash = new ConcurrentHashMap<>();
    }

    @Override
    public String getType() {
        return "hash";
    }

    public void putField(String field, String value) {
        hash.put(field, value);
    }

    public String getField(String field) {
        return hash.get(field);
    }

    public boolean deleteField(String field) {
        return hash.remove(field) != null;
    }

    public boolean hasField(String field) {
        return hash.containsKey(field);
    }

    public int size() {
        return hash.size();
    }

    public Map<String, String> getFields() {
        return hash;
    }
}

package com.zipdb.core;

import com.zipdb.core.datatype.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataStoreTest {

    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = new DataStore(2, false);  // LRU eviction strategy with a cache size of 2
    }

    @Test
    void testSetAndGet() {
        dataStore.set("key1", new StringType("value1"));
        assertEquals("value1", dataStore.get("key1").getValue());
    }

    @Test
    void testEvictionLRU() {
        dataStore.set("key1", new StringType("value1"));
        dataStore.set("key2", new StringType("value2"));
        dataStore.set("key3", new StringType("value3"));

        // "key1" should be evicted because it was least recently used.
        assertNull(dataStore.get("key1"));
        assertEquals("value2", dataStore.get("key2").getValue());
        assertEquals("value3", dataStore.get("key3").getValue());
    }

    @Test
    void testDelete() {
        dataStore.set("key1", new StringType("value1"));
        dataStore.delete("key1");
        assertNull(dataStore.get("key1"));
    }

    @Test
    void testExpiry() {
        dataStore.set("key1", new StringType("value1"));
        dataStore.setExpiration("key1", System.currentTimeMillis() + 1000); // 1 second expiration
        try {
            Thread.sleep(1100); // Sleep for more than 1 second to ensure expiration
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertNull(dataStore.get("key1"));
    }
}

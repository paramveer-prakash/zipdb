package com.zipdb.persistence;

import com.zipdb.core.DataStore;

/**
 * Snapshot Manager interface (for RDB-style persistence).
 */
public interface SnapshotManager {

    /**
     * Saves the current state of the DataStore.
     * @param dataStore the current in-memory datastore
     * @throws Exception if saving fails
     */
    void saveSnapshot(DataStore dataStore) throws Exception;

    /**
     * Loads the snapshot (if available) into the given DataStore.
     * @param dataStore the in-memory datastore to populate
     * @throws Exception if loading fails
     */
    void loadSnapshot(DataStore dataStore) throws Exception;
}

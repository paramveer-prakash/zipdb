package com.zipdb.core.command;

import com.zipdb.core.DataStore;
import com.zipdb.persistence.SnapshotManager;

public class SnapshotCommand implements Command {

    private final DataStore dataStore;
    private final SnapshotManager snapshotManager;

    public SnapshotCommand(DataStore dataStore, SnapshotManager snapshotManager) {
        this.dataStore = dataStore;
        this.snapshotManager = snapshotManager;
    }

    @Override
    public String execute(String[] args) {
        try {
            snapshotManager.saveSnapshot(dataStore);
            return "SNAPSHOT SAVED";
        } catch (Exception e) {
            return "-ERR snapshot failed\r\n";
        }
    }
}

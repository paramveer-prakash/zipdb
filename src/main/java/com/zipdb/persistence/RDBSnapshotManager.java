package com.zipdb.persistence;

import com.zipdb.core.DataStore;
import com.zipdb.core.datatype.DataType;

import java.io.*;
import java.util.Map;

public class RDBSnapshotManager implements SnapshotManager{

    private final String snapshotFile;

    public RDBSnapshotManager(String snapshotFile) {
        this.snapshotFile = snapshotFile;
    }

    // Save the snapshot of the current state of DataStore
    public void saveSnapshot(DataStore dataStore) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(snapshotFile))) {
            Map<String, DataType> allEntries = dataStore.getAllEntries();
            out.writeObject(allEntries);  // Serialize the data store entries
            out.flush();
        }
    }

    // Load the snapshot from the RDB file into DataStore
    public void loadSnapshot(DataStore dataStore) throws IOException, ClassNotFoundException {
        File snapshot = new File(snapshotFile);
        if (snapshot.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(snapshot))) {
                @SuppressWarnings("unchecked")
                Map<String, DataType> loadedData = (Map<String, DataType>) in.readObject();
                for (Map.Entry<String, DataType> entry : loadedData.entrySet()) {
                    dataStore.set(entry.getKey(), entry.getValue());
                }
            }
        }
    }
}

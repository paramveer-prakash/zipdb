package com.zipdb.persistence;

import com.zipdb.core.DataStore;
import com.zipdb.core.datatype.*;

import java.io.*;
import java.util.Map;
import java.util.Set;

public class FileSnapshotManager implements SnapshotManager {

    private final File snapshotFile;

    public FileSnapshotManager(String filePath) {
        this.snapshotFile = new File(filePath);
    }

    @Override
    public synchronized void saveSnapshot(DataStore dataStore) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(snapshotFile))) {
            for (Map.Entry<String, DataType> entry : dataStore.getAllEntries().entrySet()) {
                String key = entry.getKey();
                DataType value = entry.getValue();
                String serialized = serializeEntry(key, value);
                writer.write(serialized);
                writer.newLine();
            }
        }
    }

    @Override
    public synchronized void loadSnapshot(DataStore dataStore) throws IOException {
        if (!snapshotFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(snapshotFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                deserializeEntry(line, dataStore);
            }
        }
    }

    private String serializeEntry(String key, DataType value) {
        StringBuilder sb = new StringBuilder();
        sb.append(value.getType()).append(" ").append(key).append(" ");
        switch (value.getType()) {
            case "string" -> sb.append(((StringType) value).getValue());
            case "hash" -> {
                HashType hash = (HashType) value;
                hash.getFields().forEach((field, val) ->
                        sb.append(field).append("=").append(val).append(";"));
            }
            case "sortedset" -> {
                SortedSetType sortedSet = (SortedSetType) value;
                sortedSet.getAllMembers().forEach((member, score) ->
                        sb.append(member).append(":").append(score).append(";"));
            }
            case "bloomfilter" -> {
                BloomFilterType bloom = (BloomFilterType) value;
                sb.append(bloom.getSize()).append(",").append(bloom.getNumHashes()).append(",");
                sb.append(bloom.getBitSetString());  // Serialize bitset
            }
            case "countminsketch" -> {
                CountMinSketchType cms = (CountMinSketchType) value;
                sb.append(cms.getWidth()).append(",").append(cms.getDepth()).append(",");
                sb.append(cms.getBitSetString());  // Serialize bitset
            }


        }
        return sb.toString();
    }

    private void deserializeEntry(String line, DataStore dataStore) {
        String[] tokens = line.split(" ", 3);
        String type = tokens[0];
        String key = tokens[1];
        String payload = tokens[2];

        switch (type) {
            case "string" -> dataStore.set(key, new StringType(payload));
            case "hash" -> {
                HashType hash = new HashType();
                String[] fields = payload.split(";");
                for (String field : fields) {
                    if (!field.isEmpty()) {
                        String[] parts = field.split("=");
                        hash.putField(parts[0], parts[1]);
                    }
                }
                dataStore.set(key, hash);
            }
            case "sortedset" -> {
                SortedSetType sortedSet = new SortedSetType();
                String[] members = payload.split(";");
                for (String member : members) {
                    if (!member.isEmpty()) {
                        String[] parts = member.split(":");
                        sortedSet.add(parts[0], Double.parseDouble(parts[1]));
                    }
                }
                dataStore.set(key, sortedSet);
            }
            case "bloomfilter" -> {
                String[] parts = payload.split(",", 3);
                int size = Integer.parseInt(parts[0]);
                int numHashes = Integer.parseInt(parts[1]);
                String bitSetStr = parts[2];
                BloomFilterType bloom = new BloomFilterType(size, numHashes);
                bloom.loadBitSetFromString(bitSetStr);
                dataStore.set(key, bloom);
            }
            case "countminsketch" -> {
                String[] parts = payload.split(",", 3);
                int width = Integer.parseInt(parts[0]);
                int depth = Integer.parseInt(parts[1]);
                String bitSetStr = parts[2];
                CountMinSketchType cms = new CountMinSketchType(width, depth);
                cms.loadBitSetFromString(bitSetStr);
                dataStore.set(key, cms);
            }
        }
    }
}

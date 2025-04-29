package com.zipdb.persistence;

import com.zipdb.core.DataStore;
import com.zipdb.core.datatype.DataType;
import com.zipdb.core.datatype.StringType;

import java.io.*;

public class AOFLogManager {

    private final String aofFile;

    public AOFLogManager(String aofFile) {
        this.aofFile = aofFile;
    }

    // Append a write operation to the AOF file
    public void appendToLog(String command) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(aofFile, true))) {
            writer.write(command + "\n");
            writer.flush();
        }
    }

    // Replay the AOF file to restore commands
    public void replayAOF(DataStore dataStore) throws IOException {
        File aof = new File(aofFile);
        if (aof.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(aof))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Process each command in AOF file (just `SET` for simplicity)
                    String[] commandParts = line.split(" ");
                    if ("SET".equals(commandParts[0])) {
                        String key = commandParts[1];
                        String value = commandParts[2];
                        dataStore.set(key, new StringType(value));
                    }
                }
            }
        }
    }
}

package com.zipdb.persistence;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileWAL implements WAL {

    private BufferedWriter writer;
    private final File logFile;

    public FileWAL(String filePath) throws IOException {
        this.logFile = Paths.get(filePath).toFile();
        this.writer = new BufferedWriter(new FileWriter(logFile, true)); // append mode
    }

    @Override
    public synchronized void append(String entry) throws IOException {
        writer.write(entry);
        writer.newLine();
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    /**
     * Reads all existing WAL entries (for recovery).
     */
    public List<String> readAllEntries() throws IOException {
        List<String> entries = new ArrayList<>();
        if (!logFile.exists()) return entries;

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                entries.add(line);
            }
        }
        return entries;
    }

    public synchronized void truncate() throws IOException {
        writer.close();
        // Overwrite (truncate) the WAL file
        BufferedWriter newWriter = new BufferedWriter(new FileWriter(logFile, false));  // false = overwrite
        newWriter.close();
        // Reopen writer for future appends
        this.writer = new BufferedWriter(new FileWriter(logFile, true));
    }

}

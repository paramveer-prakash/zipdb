package com.zipdb.persistence;

/**
 * WAL (Write-Ahead Log) interface for durability.
 */
public interface WAL {

    /**
     * Appends an entry (command) to the log.
     * @param entry the command to be logged
     * @throws Exception if writing fails
     */
    void append(String entry) throws Exception;

    /**
     * Closes the WAL (flushes and releases resources).
     * @throws Exception if closing fails
     */
    void close() throws Exception;
}

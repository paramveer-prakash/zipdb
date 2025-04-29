package com.zipdb.core;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class AuditLog {

    // Log command actions with timestamp
    public static void logAction(String user, String command) {
        try (FileWriter writer = new FileWriter("audit.log", true)) {
            String logEntry = LocalDateTime.now() + " - User: " + user + " executed command: " + command + "\n";
            writer.append(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

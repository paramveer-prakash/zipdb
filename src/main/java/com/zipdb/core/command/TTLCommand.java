package com.zipdb.core.command;

import com.zipdb.core.DataStore;
import com.zipdb.core.datatype.DataType;

public class TTLCommand implements Command {

    private final DataStore dataStore;

    public TTLCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 2) {
            return "-ERR wrong number of arguments for 'ttl' command\r\n";
        }
        String key = args[0];
        int ttl;
        try {
            ttl = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return "-ERR invalid ttl value\r\n";
        }

        DataType existing = dataStore.get(key);
        if (existing == null) {
            return ":0\r\n";  // Key does not exist
        }

        // Set the expiration time
        long expirationTime = System.currentTimeMillis() + ttl * 1000L;  // ttl in seconds
        dataStore.setExpiration(key, expirationTime);  // Set expiration in DataStore

        return ":1\r\n";  // Successfully set TTL
    }
}

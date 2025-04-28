package com.zipdb.core.command;

import com.zipdb.core.DataStore;
import com.zipdb.core.datatype.DataType;

public class DELCommand implements Command {

    private final DataStore dataStore;

    public DELCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 1) {
            return "-ERR wrong number of arguments for 'del' command";
        }
        String key = args[0];

        DataType existing = dataStore.get(key);
        if (existing == null) {
            return ":0";  // Key doesn't exist
        }

        dataStore.remove(key);  // Remove the key from datastore
        return ":1";  // Successfully deleted
    }
}

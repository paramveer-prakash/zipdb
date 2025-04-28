package com.zipdb.core.command;

import com.zipdb.core.DataStore;
import com.zipdb.core.datatype.DataType;

public class EXISTSCommand implements Command {

    private final DataStore dataStore;

    public EXISTSCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 1) {
            return "-ERR wrong number of arguments for 'exists' command\r\n";
        }
        String key = args[0];

        DataType existing = dataStore.get(key);
        return existing != null ? ":1\r\n" : ":0\r\n";  // Return 1 if exists, 0 if not
    }
}

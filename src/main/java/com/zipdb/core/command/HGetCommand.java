package com.zipdb.core.command;

import com.zipdb.core.DataStore;
import com.zipdb.core.datatype.DataType;
import com.zipdb.core.datatype.HashType;

public class HGetCommand implements Command {

    private final DataStore dataStore;

    public HGetCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 2) {
            return "-ERR wrong number of arguments for 'hget' command\r\n";
        }
        String key = args[0];
        String field = args[1];

        DataType existing = dataStore.get(key);
        if (existing == null) {
            return "$-1\r\n";  // Null bulk reply
        }
        if (!(existing instanceof HashType)) {
            return "-ERR wrong type for key\r\n";
        }

        String value = ((HashType) existing).getField(field);
        if (value == null) {
            return "$-1\r\n";
        }
        return "$" + value.length() + "\r\n" + value + "\r\n";
    }
}

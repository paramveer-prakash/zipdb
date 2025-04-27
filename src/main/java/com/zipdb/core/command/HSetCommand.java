package com.zipdb.core.command;

import com.zipdb.core.DataStore;
import com.zipdb.core.datatype.DataType;
import com.zipdb.core.datatype.HashType;

public class HSetCommand implements Command {

    private final DataStore dataStore;

    public HSetCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 3) {
            return "-ERR wrong number of arguments for 'hset' command\r\n";
        }
        String key = args[0];
        String field = args[1];
        String value = args[2];

        DataType existing = dataStore.get(key);
        HashType hash;

        if (existing == null) {
            hash = new HashType();
            dataStore.set(key, hash);
        } else if (existing instanceof HashType) {
            hash = (HashType) existing;
        } else {
            return "-ERR wrong type for key\r\n";
        }

        hash.putField(field, value);
        return ":1\r\n";  // Returns number of fields added
    }
}

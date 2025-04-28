package com.zipdb.core.command.bloom;

import com.zipdb.core.DataStore;
import com.zipdb.core.datatype.BloomFilterType;
import com.zipdb.core.datatype.DataType;

public class BFExistsCommand implements com.zipdb.core.command.Command {

    private final DataStore dataStore;

    public BFExistsCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 2) {
            return "-ERR wrong number of arguments for 'bf.exists' command\r\n";
        }
        String key = args[0];
        String item = args[1];

        DataType existing = dataStore.get(key);
        if (!(existing instanceof BloomFilterType)) {
            return ":0\r\n";  // Bloom filter doesn't exist → return 0
        }

        boolean exists = ((BloomFilterType) existing).mightContain(item);
        return ":" + (exists ? "1" : "0");
    }
}

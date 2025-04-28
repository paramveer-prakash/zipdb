package com.zipdb.core.command.bloom;

import com.zipdb.core.DataStore;
import com.zipdb.core.datatype.BloomFilterType;
import com.zipdb.core.datatype.DataType;

public class BFAddCommand implements com.zipdb.core.command.Command {

    private final DataStore dataStore;

    public BFAddCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 2) {
            return "-ERR wrong number of arguments for 'bf.add' command\r\n";
        }
        String key = args[0];
        String item = args[1];

        DataType existing = dataStore.get(key);
        BloomFilterType bloom;

        if (existing == null) {
            bloom = new BloomFilterType(1024, 3);  // default size and hash count
            dataStore.set(key, bloom);
        } else if (existing instanceof BloomFilterType) {
            bloom = (BloomFilterType) existing;
        } else {
            return "-ERR wrong type for key\r\n";
        }

        bloom.add(item);
        return ":1";  // Return 1 to indicate success
    }
}

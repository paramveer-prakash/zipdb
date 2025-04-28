package com.zipdb.core.command;

import com.zipdb.core.DataStore;
import com.zipdb.core.datatype.DataType;
import com.zipdb.core.datatype.SortedSetType;

public class ZAddCommand implements Command {

    private final DataStore dataStore;

    public ZAddCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 3) {
            return "-ERR wrong number of arguments for 'zadd' command\r\n";
        }
        String key = args[0];
        double score;
        try {
            score = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            return "-ERR invalid score value\r\n";
        }
        String member = args[2];

        DataType existing = dataStore.get(key);
        SortedSetType sortedSet;

        if (existing == null) {
            sortedSet = new SortedSetType();
            dataStore.set(key, sortedSet);
        } else if (existing instanceof SortedSetType) {
            sortedSet = (SortedSetType) existing;
        } else {
            return "-ERR wrong type for key\r\n";
        }

        sortedSet.add(member, score);
        return ":1";  // Returns number of elements added (simplified)
    }
}

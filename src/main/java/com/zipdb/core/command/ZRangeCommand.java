package com.zipdb.core.command;

import com.zipdb.core.DataStore;
import com.zipdb.core.datatype.DataType;
import com.zipdb.core.datatype.SortedSetType;

import java.util.List;

public class ZRangeCommand implements Command {

    private final DataStore dataStore;

    public ZRangeCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 3) {
            return "-ERR wrong number of arguments for 'zrange' command\r\n";
        }
        String key = args[0];
        double min, max;
        try {
            min = Double.parseDouble(args[1]);
            max = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            return "-ERR invalid score range\r\n";
        }

        DataType existing = dataStore.get(key);
        if (existing == null) {
            return "*0\r\n";  // Empty array
        }
        if (!(existing instanceof SortedSetType)) {
            return "-ERR wrong type for key\r\n";
        }

        List<String> members = ((SortedSetType) existing).rangeByScore(min, max);

        StringBuilder response = new StringBuilder();
        response.append("*").append(members.size()).append("\r\n");
        for (String member : members) {
            response.append("$").append(member.length()).append("\r\n").append(member).append("\r\n");
        }
        return response.toString();
    }
}

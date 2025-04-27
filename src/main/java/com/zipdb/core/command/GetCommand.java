package com.zipdb.core.command;

import com.zipdb.core.DataStore;
import com.zipdb.core.datatype.DataType;
import com.zipdb.core.datatype.StringType;

public class GetCommand implements Command {

    private final DataStore dataStore;

    public GetCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 1) {
            return "-ERR wrong number of arguments for 'get' command\r\n";
        }
        String key = args[0];
        DataType value = dataStore.get(key);
        if (value == null) {
            return "$-1\r\n"; // Null bulk reply
        }
        if (value instanceof StringType) {
            String str = ((StringType) value).getValue();
            return "$" + str.length() + "\r\n" + str + "\r\n";
        }
        return "-ERR wrong type of value\r\n";
    }
}

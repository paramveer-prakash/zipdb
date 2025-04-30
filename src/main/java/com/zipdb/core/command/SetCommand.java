package com.zipdb.core.command;

import com.zipdb.core.DataStore;
import com.zipdb.core.datatype.StringType;

public class SetCommand implements Command {

    private final DataStore dataStore;

    public SetCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 2) {
            return "ERR wrong number of arguments for 'set' command";
        }
        String key = args[0];
        String value = args[1];
        dataStore.set(key, new StringType(value));
        return "OK";
    }
}

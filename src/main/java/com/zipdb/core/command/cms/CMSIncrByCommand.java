package com.zipdb.core.command.cms;

import com.zipdb.core.DataStore;
import com.zipdb.core.command.Command;
import com.zipdb.core.datatype.CountMinSketchType;
import com.zipdb.core.datatype.DataType;
import com.zipdb.network.resp.RespError;

public class CMSIncrByCommand implements Command {

    private final DataStore dataStore;

    public CMSIncrByCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public Object execute(String[] args) {
        if (args.length < 3) {
            return new RespError("wrong number of arguments for 'cms.incrby' command");
        }
        String key = args[0];
        String item = args[1];
        int count;
        try {
            count = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            return new RespError("invalid count value");
        }

        DataType existing = dataStore.get(key);
        CountMinSketchType cms;

        if (existing == null) {
            cms = new CountMinSketchType(1024, 5);  // Default width and depth
            dataStore.set(key, cms);
        } else if (existing instanceof CountMinSketchType) {
            cms = (CountMinSketchType) existing;
        } else {
            return new RespError("wrong type for key");
        }

        cms.increment(item, count);
        return 1;  // Success
    }
}

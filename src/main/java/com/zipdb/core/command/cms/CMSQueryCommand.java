package com.zipdb.core.command.cms;

import com.zipdb.core.DataStore;
import com.zipdb.core.command.Command;
import com.zipdb.core.datatype.CountMinSketchType;
import com.zipdb.core.datatype.DataType;
import com.zipdb.network.resp.RespError;

public class CMSQueryCommand implements Command {

    private final DataStore dataStore;

    public CMSQueryCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public Object execute(String[] args) {
        if (args.length < 2) {
            return new RespError("wrong number of arguments for 'cms.query' command");
        }
        String key = args[0];
        String item = args[1];

        DataType existing = dataStore.get(key);
        if (!(existing instanceof CountMinSketchType)) {
            return 0;  // Return 0 if the key is missing or not a CountMinSketch
        }

        CountMinSketchType cms = (CountMinSketchType) existing;
        int frequency = cms.query(item);
        return frequency;
    }
}

package com.zipdb.core.command;

import com.zipdb.core.DataStore;
import com.zipdb.core.command.bloom.BFAddCommand;
import com.zipdb.core.command.bloom.BFExistsCommand;
import com.zipdb.core.command.cms.CMSIncrByCommand;
import com.zipdb.core.command.cms.CMSQueryCommand;
import com.zipdb.network.resp.RespError;
import com.zipdb.observability.MetricsCollector;
import com.zipdb.persistence.SnapshotManager;
import com.zipdb.persistence.WAL;

import java.util.HashMap;
import java.util.Map;

public class CommandProcessor {

    private final DataStore dataStore;
    private final WAL wal;
    private final Map<String, Command> commands = new HashMap<>();
    private final MetricsCollector metricsCollector;


    public CommandProcessor(DataStore dataStore, WAL wal, SnapshotManager snapshotManager,MetricsCollector metricsCollector) {
        this.dataStore = dataStore;
        this.wal = wal;
        this.metricsCollector = metricsCollector;
        commands.put("set", new SetCommand(dataStore));
        commands.put("get", new GetCommand(dataStore));
        commands.put("hset", new HSetCommand(dataStore));
        commands.put("hget", new HGetCommand(dataStore));
        commands.put("zadd", new ZAddCommand(dataStore));
        commands.put("zrange", new ZRangeCommand(dataStore));
        commands.put("snapshot", new SnapshotCommand(dataStore, snapshotManager));
        commands.put("bf.add", new BFAddCommand(dataStore));
        commands.put("bf.exists", new BFExistsCommand(dataStore));
        commands.put("cms.incrby", new CMSIncrByCommand(dataStore));
        commands.put("cms.query", new CMSQueryCommand(dataStore));
        commands.put("del", new DELCommand(dataStore));
        commands.put("exists", new EXISTSCommand(dataStore));
        commands.put("ttl", new TTLCommand(dataStore));
        commands.put("keys", new KEYSCommand(dataStore));


        // More commands will be added here.
    }

    public Object process(String inputLine) {
        String[] tokens = inputLine.trim().split("\\s+");
        if (tokens.length == 0) {
            return new RespError("empty command");
        }
        String commandName = tokens[0].toLowerCase();
        Command command = commands.get(commandName);
        if (command == null) {
            return new RespError("unknown command");
        }
        String[] args = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, args, 0, args.length);

        try {
            wal.append(inputLine);  // WAL write BEFORE executing
        } catch (Exception e) {
            return new RespError("WAL failure");
        }

        metricsCollector.incrementCommandCount();

        return command.execute(args);
    }

    public Object processWithoutWAL(String inputLine) {
        String[] tokens = inputLine.trim().split("\\s+");
        if (tokens.length == 0) {
            return new RespError("empty command");
        }
        String commandName = tokens[0].toLowerCase();
        Command command = commands.get(commandName);
        if (command == null) {
            return new RespError("unknown command");
        }
        String[] args = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, args, 0, args.length);
        metricsCollector.incrementCommandCount();
        return command.execute(args);  // No WAL logging here
    }


}

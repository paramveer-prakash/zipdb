package com.zipdb.core.command;

import com.zipdb.core.DataStore;
import java.util.HashMap;
import java.util.Map;

public class CommandProcessor {

    private final Map<String, Command> commands = new HashMap<>();

    public CommandProcessor(DataStore dataStore) {
        commands.put("set", new SetCommand(dataStore));
        commands.put("get", new GetCommand(dataStore));
        commands.put("hset", new HSetCommand(dataStore));
        commands.put("hget", new HGetCommand(dataStore));
        commands.put("zadd", new ZAddCommand(dataStore));
        commands.put("zrange", new ZRangeCommand(dataStore));
        // More commands will be added here.
    }

    public String process(String inputLine) {
        String[] tokens = inputLine.trim().split("\\s+");
        if (tokens.length == 0) {
            return "-ERR empty command\r\n";
        }
        String commandName = tokens[0].toLowerCase();
        Command command = commands.get(commandName);
        if (command == null) {
            return "-ERR unknown command\r\n";
        }
        String[] args = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, args, 0, args.length);
        return command.execute(args);
    }
}

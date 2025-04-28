package com.zipdb.core.command;

import com.zipdb.core.DataStore;

import java.util.List;
import java.util.stream.Collectors;

public class KEYSCommand implements Command {

    private final DataStore dataStore;

    public KEYSCommand(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public String execute(String[] args) {
        if (args.length < 1) {
            return "-ERR wrong number of arguments for 'keys' command\r\n";
        }
        String pattern = args[0];

        List<String> matchingKeys = dataStore.getAllKeys().stream()
                .filter(key -> key.matches(pattern.replace("*", ".*")))  // Match pattern using regex
                .collect(Collectors.toList());

        StringBuilder response = new StringBuilder();
        response.append("*").append(matchingKeys.size()).append("\r\n");
        for (String key : matchingKeys) {
            response.append("$").append(key.length()).append("\r\n").append(key).append("\r\n");
        }
        return response.toString();
    }
}

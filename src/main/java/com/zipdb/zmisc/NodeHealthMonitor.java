package com.zipdb.zmisc;

import java.util.*;
import java.util.concurrent.*;

public class NodeHealthMonitor {

    private final NodeInfo self;
    private final List<NodeInfo> peers;
    private final Map<NodeInfo, Boolean> liveNodes = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public NodeHealthMonitor(NodeInfo self, List<NodeInfo> cluster) {
        this.self = self;
        this.peers = new ArrayList<>(cluster);
        this.peers.remove(self);
        for (NodeInfo node : peers) {
            liveNodes.put(node, false); // assume down initially
        }
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::pingAll, 2, 5, TimeUnit.SECONDS);
    }

    private void pingAll() {
        for (NodeInfo node : peers) {
            boolean isAlive = pingNode(node);
            liveNodes.put(node, isAlive);
            System.out.println("Ping " + node + ": " + (isAlive ? "alive" : "down"));
        }
    }

    private boolean pingNode(NodeInfo node) {
        String response = KVStoreClient.sendCommand(node.getHost(), node.getPort(), "PING");
        return response != null && response.equalsIgnoreCase("PONG");
    }

    public Map<NodeInfo, Boolean> getLiveNodes() {
        return liveNodes;
    }

    public boolean isNodeAlive(NodeInfo node) {
        return node.equals(self) || liveNodes.getOrDefault(node, false);
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}

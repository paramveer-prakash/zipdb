package com.zipdb.zmisc;

import java.util.*;
import java.util.*;

public class KVStoreNode {

    private final NodeInfo self;
    private final List<NodeInfo> cluster;
    private final ConsistentHashRing ring;
    private final KVStoreServer server;
    private final int replicationFactor = 2; // You can increase this as needed

    public KVStoreNode(String host, int port, List<NodeInfo> cluster) {
        this.self = new NodeInfo(host, port);
        this.cluster = cluster;
        this.ring = new ConsistentHashRing(cluster);
        this.server = new KVStoreServer(port);
    }

    public void start() {
        // Start the TCP server to handle incoming set/get requests
        new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Simple CLI loop for testing commands interactively
        Scanner scanner = new Scanner(System.in);
        System.out.println("KVStoreNode started. Type commands (e.g., set key value / get key):");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if (line == null || line.isEmpty()) continue;

            String[] parts = line.split(" ", 3);
            String command = parts[0].toLowerCase();

            if ("PING".equalsIgnoreCase(command)) {
                out.write("PONG\n");
            }else if ("set".equals(command) && parts.length == 3) {
                String key = parts[1];
                String value = parts[2];

                List<NodeInfo> replicas = ring.getReplicasForKey(key, replicationFactor);
                boolean storedLocally = false;

                for (NodeInfo node : replicas) {
                    if (node.equals(self)) {
                        server.getStore().put(key, value);
                        storedLocally = true;
                    } else {
                        String resp = KVStoreClient.set(node.getHost(), node.getPort(), key, value);
                        System.out.println("Forwarded to replica " + node + ": " + resp);
                    }
                }

                if (storedLocally) {
                    System.out.println("Stored locally and replicated to " + (replicas.size() - 1) + " other nodes.");
                }

            } else if ("get".equals(command) && parts.length == 2) {
                String key = parts[1];
                NodeInfo primary = ring.getNodeForKey(key);

                if (primary.equals(self)) {
                    String val = server.getStore().get(key);
                    System.out.println(val != null ? "VALUE " + val : "NOT_FOUND");
                } else {
                    String resp = KVStoreClient.get(primary.getHost(), primary.getPort(), key);
                    System.out.println("Forwarded to primary " + primary + ": " + resp);
                }

            } else {
                System.out.println("Invalid command. Use: set key value | get key");
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java KVStoreNode <port> <peer1Host:port> <peer2Host:port> ...");
            System.exit(1);
        }

        int selfPort = Integer.parseInt(args[0]);
        String selfHost = "localhost";

        List<NodeInfo> cluster = new ArrayList<>();
        cluster.add(new NodeInfo(selfHost, selfPort)); // add self

        for (int i = 1; i < args.length; i++) {
            String[] hostPort = args[i].split(":");
            cluster.add(new NodeInfo(hostPort[0], Integer.parseInt(hostPort[1])));
        }

        KVStoreNode node = new KVStoreNode(selfHost, selfPort, cluster);
        node.start();
    }
}

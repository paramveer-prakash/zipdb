package com.zipdb.zmisc;

import com.zipdb.zmisc.NodeInfo;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class ConsistentHashRing {
    private final SortedMap<Integer, NodeInfo> ring = new TreeMap<>();
    private final List<NodeInfo> nodes;

    public ConsistentHashRing(List<NodeInfo> nodes) {
        this.nodes = nodes;
        for (NodeInfo node : nodes) {
            addNode(node);
        }
    }

    private void addNode(NodeInfo node) {
        int hash = hash(node.getAddress());
        ring.put(hash, node);
    }

    public NodeInfo getNodeForKey(String key) {
        if (ring.isEmpty()) return null;
        int hash = hash(key);
        SortedMap<Integer, NodeInfo> tailMap = ring.tailMap(hash);
        return tailMap.isEmpty() ? ring.get(ring.firstKey()) : tailMap.get(tailMap.firstKey());
    }

    private int hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(digest).getInt();  // 32-bit hash
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Set<NodeInfo> getAllNodes() {
        return new HashSet<>(ring.values());
    }

    public List<NodeInfo> getReplicasForKey(String key, int replicationFactor) {
        List<NodeInfo> replicas = new ArrayList<>();
        int keyHash = hash(key);

        SortedMap<Integer, NodeInfo> tail = ring.tailMap(keyHash);
        Iterator<NodeInfo> iterator = tail.values().iterator();

        while (replicas.size() < replicationFactor && iterator.hasNext()) {
            NodeInfo node = iterator.next();
            if (!replicas.contains(node)) replicas.add(node);
        }

        // wrap around the ring if needed
        if (replicas.size() < replicationFactor) {
            for (NodeInfo node : ring.values()) {
                if (!replicas.contains(node)) replicas.add(node);
                if (replicas.size() == replicationFactor) break;
            }
        }

        return replicas;
    }

}

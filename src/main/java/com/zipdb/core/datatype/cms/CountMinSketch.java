package com.zipdb.core.datatype.cms;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.Random;

public class CountMinSketch {

    private final int[][] table;
    private final int width;
    private final int depth;
    private final int[] hashSeeds;

    public CountMinSketch(int width, int depth) {
        this.width = width;
        this.depth = depth;
        this.table = new int[depth][width];
        this.hashSeeds = new int[depth];
        Random random = new Random();

        // Initialize random seeds for each hash function
        for (int i = 0; i < depth; i++) {
            hashSeeds[i] = random.nextInt();
        }
    }

    public void increment(String item, int count) {
        byte[] bytes = item.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < depth; i++) {
            int hash = hash(bytes, hashSeeds[i]);
            int idx = Math.abs(hash % width);
            table[i][idx] += count;
        }
    }

    public int query(String item) {
        byte[] bytes = item.getBytes(StandardCharsets.UTF_8);
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < depth; i++) {
            int hash = hash(bytes, hashSeeds[i]);
            int idx = Math.abs(hash % width);
            min = Math.min(min, table[i][idx]);
        }
        return min;
    }

    private int hash(byte[] data, int seed) {
        int hash = seed;
        for (byte b : data) {
            hash ^= b;
            hash *= 0x5bd1e995;
            hash ^= hash >>> 15;
        }
        return hash;
    }

    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return depth;
    }

    public String getBitSetString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < width; j++) {
                if (table[i][j] != 0) {
                    sb.append(j).append(" ");  // Save non-zero indices as part of the bitset
                }
            }
        }
        return sb.toString();
    }

    public void loadBitSetFromString(String bitSetStr) {
        BitSet loaded = new BitSet();
        String[] indices = bitSetStr.split(" ");
        for (String idx : indices) {
            if (!idx.isEmpty()) {
                loaded.set(Integer.parseInt(idx));
            }
        }

        // Load bitset back into table (this is simplified for the example)
        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < width; j++) {
                if (loaded.get(j)) {
                    table[i][j] = 1;  // Restore the bitset into the table
                }
            }
        }
    }
}

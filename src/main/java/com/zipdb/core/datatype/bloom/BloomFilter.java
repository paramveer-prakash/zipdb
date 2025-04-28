package com.zipdb.core.datatype.bloom;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;

public class BloomFilter {

    private final BitSet bitSet;
    private final int size;
    private final int numHashes;

    public BloomFilter(int size, int numHashes) {
        this.size = size;
        this.numHashes = numHashes;
        this.bitSet = new BitSet(size);
    }

    public void add(String item) {
        for (int hash : getHashes(item)) {
            bitSet.set(Math.abs(hash % size));
        }
    }

    public boolean mightContain(String item) {
        for (int hash : getHashes(item)) {
            if (!bitSet.get(Math.abs(hash % size))) {
                return false;
            }
        }
        return true;
    }

    private int[] getHashes(String item) {
        int[] hashes = new int[numHashes];
        byte[] bytes = item.getBytes(StandardCharsets.UTF_8);
        int hash1 = murmurHash(bytes, 0);
        int hash2 = murmurHash(bytes, hash1);
        for (int i = 0; i < numHashes; i++) {
            hashes[i] = hash1 + i * hash2;
        }
        return hashes;
    }

    private int murmurHash(byte[] data, int seed) {
        int hash = seed;
        for (byte b : data) {
            hash ^= b;
            hash *= 0x5bd1e995;
            hash ^= hash >>> 15;
        }
        return hash;
    }

    public int getSize() {
        return size;
    }

    public int getNumHashes() {
        return numHashes;
    }

    public String getBitSetString() {
        return bitSet.toString();  // Serialize bitset as string
    }

    public void loadBitSetFromString(String bitSetStr) {
        BitSet loaded = new BitSet();
        String[] indices = bitSetStr.replace("{", "").replace("}", "").split(", ");
        for (String idx : indices) {
            if (!idx.isEmpty()) {
                loaded.set(Integer.parseInt(idx));
            }
        }
        bitSet.clear();
        bitSet.or(loaded);
    }

}

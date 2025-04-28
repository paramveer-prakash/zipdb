package com.zipdb.core.datatype;

import com.zipdb.core.datatype.bloom.BloomFilter;

/**
 * Bloom Filter type for ZipDB.
 */
public class BloomFilterType implements DataType {

    private final BloomFilter bloomFilter;

    public BloomFilterType(int size, int numHashes) {
        this.bloomFilter = new BloomFilter(size, numHashes);
    }

    @Override
    public String getType() {
        return "bloomfilter";
    }

    public void add(String item) {
        bloomFilter.add(item);
    }

    public boolean mightContain(String item) {
        return bloomFilter.mightContain(item);
    }

    public int getSize() {
        return bloomFilter.getSize();
    }

    public int getNumHashes() {
        return bloomFilter.getNumHashes();
    }

    public String getBitSetString() {
        return bloomFilter.getBitSetString();
    }

    public void loadBitSetFromString(String bitSetStr) {
        bloomFilter.loadBitSetFromString(bitSetStr);
    }

}

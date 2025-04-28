package com.zipdb.core.datatype;

import com.zipdb.core.datatype.cms.CountMinSketch;

/**
 * Count-Min Sketch type for ZipDB.
 */
public class CountMinSketchType implements DataType {

    private final CountMinSketch cms;

    public CountMinSketchType(int width, int depth) {
        this.cms = new CountMinSketch(width, depth);
    }

    @Override
    public String getType() {
        return "countminsketch";
    }

    public void increment(String item, int count) {
        cms.increment(item, count);
    }

    public int query(String item) {
        return cms.query(item);
    }

    public int getWidth() {
        return cms.getWidth();
    }

    public int getDepth() {
        return cms.getDepth();
    }

    public String getBitSetString() {
        return cms.getBitSetString();
    }

    public void loadBitSetFromString(String bitSetStr) {
        cms.loadBitSetFromString(bitSetStr);
    }

}

package com.zipdb.core.datatype;

/**
 * Represents a simple key-value string type.
 */
public class StringType implements DataType {

    private String value;

    public StringType(String value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return "string";
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

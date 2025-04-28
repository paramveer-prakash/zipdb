package com.zipdb.network.resp;

public class RespError {
    private final String message;

    public RespError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

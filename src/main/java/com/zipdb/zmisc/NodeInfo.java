package com.zipdb.zmisc;

public class NodeInfo {
    private final String host;
    private final int port;

    public NodeInfo(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getAddress() {
        return host + ":" + port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return getAddress();
    }

    @Override
    public int hashCode() {
        return getAddress().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeInfo) {
            return getAddress().equals(((NodeInfo) obj).getAddress());
        }
        return false;
    }
}

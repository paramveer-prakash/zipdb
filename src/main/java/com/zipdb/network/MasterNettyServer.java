package com.zipdb.network;

import com.zipdb.core.DataStore;
import com.zipdb.core.command.CommandProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MasterNettyServer extends NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(MasterNettyServer.class);

    private Set<SlaveNettyServer> slaveNodes = new HashSet<>();

    public MasterNettyServer(int port) throws IOException {
        super(port);  // Start the server with the provided port
    }

    // Add a slave node to the master node
    public void addSlave(SlaveNettyServer slave) {
        slaveNodes.add(slave);
    }

    @Override
    public void start() throws InterruptedException, IOException {
        // Start the base Netty server to handle read/write operations
        super.start();

        // After starting the server, replicate writes to all Slave nodes
        replicateToSlaves();
    }

    private void replicateToSlaves() {
        // Logic to replicate write operations to all Slave nodes
        logger.info("Replicating writes to slaves...");
        for (SlaveNettyServer slave : slaveNodes) {
            // Replicate write operations (e.g., send AOF log, custom replication logic)
            slave.syncWithMaster();
        }
    }

    // Get the slave nodes for replication (or failure detection)
    public Set<SlaveNettyServer> getSlaveNodes() {
        return slaveNodes;
    }
}

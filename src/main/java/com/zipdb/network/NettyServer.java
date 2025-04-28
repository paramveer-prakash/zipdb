package com.zipdb.network;

import com.zipdb.common.StartupBanner;
import com.zipdb.core.DataStore;
import com.zipdb.core.command.CommandProcessor;
import com.zipdb.network.http.HttpMetricsServer;
import com.zipdb.network.resp.RESPDecoder;
import com.zipdb.network.resp.RESPEncoder;
import com.zipdb.observability.MetricsCollector;
import com.zipdb.persistence.FileSnapshotManager;
import com.zipdb.persistence.FileWAL;
import com.zipdb.persistence.SnapshotManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private final int port;
    private final DataStore dataStore;
    private final FileWAL wal ;
    private final CommandProcessor commandProcessor ;
    private final SnapshotManager snapshotManager = new FileSnapshotManager("dump.rdb");
    private final ScheduledExecutorService snapshotScheduler = Executors.newSingleThreadScheduledExecutor();
    private final MetricsCollector metricsCollector = new MetricsCollector();
    private final ScheduledExecutorService metricsScheduler = Executors.newSingleThreadScheduledExecutor();
    private final HttpMetricsServer httpMetricsServer;


    public NettyServer(int port) throws IOException {
        this.port = port;
        this.wal = new FileWAL("wal.log");  // Handle IOException here
        this.dataStore = new DataStore();
        this.commandProcessor = new CommandProcessor(dataStore, wal,snapshotManager,metricsCollector);
        this.httpMetricsServer = new HttpMetricsServer(8080, metricsCollector);  // Metrics on port 8080

    }

    public void start() throws InterruptedException, IOException {
        StartupBanner.printBanner();

        // 1️⃣ Recovery: Load snapshot + replay WAL
        try {
            snapshotManager.loadSnapshot(dataStore);
            logger.info("Snapshot loaded successfully.");
            replayWAL();
        } catch (Exception e) {
            logger.error("Error during recovery: ", e);
            throw new RuntimeException(e);
        }

        // 2️⃣ Start HTTP metrics server (non-blocking)
        new Thread(() -> {
            try {
                httpMetricsServer.start();  // Runs on port 8080
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // 3️⃣ Start snapshot and metrics schedulers
        startSnapshotScheduler();
        startMetricsScheduler();

        // 4️⃣ Start RESP server (Netty)
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new RESPDecoder());
                            ch.pipeline().addLast(new RESPEncoder());
                            ch.pipeline().addLast(new RESPHandler(commandProcessor));
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            logger.info("ZipDB RESP server started on port {}", port);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            snapshotScheduler.shutdown();
            metricsScheduler.shutdown();
            wal.close();
        }
    }


    private void startMetricsScheduler() {
        metricsScheduler.scheduleAtFixedRate(() -> {
            String snapshot = metricsCollector.getMetricsSnapshot();
            logger.info("Metrics: {}", snapshot);
        }, 10, 10, TimeUnit.SECONDS);  // Logs every 10 seconds
    }


    private void startSnapshotScheduler() {
        snapshotScheduler.scheduleAtFixedRate(() -> {
            try {
                snapshotManager.saveSnapshot(dataStore);
                wal.truncate();  // Compact WAL after snapshot
                logger.info("Periodic snapshot saved and WAL truncated.");
                logger.info("Periodic snapshot saved.");
            } catch (Exception e) {
                logger.error("Failed to save periodic snapshot: ", e);
            }
        }, 30, 30, TimeUnit.SECONDS);  // First run after 30s, then every 30s
    }


    public static void main(String[] args) throws InterruptedException, IOException {
        int port = 6379;
        new NettyServer(port).start();
    }

    static class ServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

        private final CommandProcessor commandProcessor;

        public ServerHandler(CommandProcessor commandProcessor) {
            this.commandProcessor = commandProcessor;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
            byte[] bytes = new byte[msg.readableBytes()];
            msg.readBytes(bytes);
            String input = new String(bytes).trim();


            //String response = commandProcessor.process(input);
            //ctx.writeAndFlush(Unpooled.copiedBuffer(response.getBytes()));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }

    private void replayWAL() {
        try {
            List<String> entries = wal.readAllEntries();
            for (String entry : entries) {
                commandProcessor.processWithoutWAL(entry);  // Avoid re-logging during replay
            }
            logger.info("WAL replay completed. {} entries restored.", entries.size());
        } catch (Exception e) {
            logger.error("Error during WAL replay: ", e);
        }
    }

}

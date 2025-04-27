package com.zipdb.network;

import com.zipdb.common.StartupBanner;
import com.zipdb.core.DataStore;
import com.zipdb.core.command.CommandProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private final int port;
    private final DataStore dataStore = new DataStore();
    private final CommandProcessor commandProcessor = new CommandProcessor(dataStore);

    public NettyServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        StartupBanner.printBanner();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ServerHandler(commandProcessor));
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            logger.info("ZipDB Server started on port {}", port);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
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
            String response = commandProcessor.process(input);
            ctx.writeAndFlush(Unpooled.copiedBuffer(response.getBytes()));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}

package com.zipdb.network.http;

import com.zipdb.observability.MetricsCollector;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

public class HttpMetricsServer {

    private final int port;
    private final MetricsCollector metricsCollector;

    public HttpMetricsServer(int port, MetricsCollector metricsCollector) {
        this.port = port;
        this.metricsCollector = metricsCollector;
    }

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(512 * 1024));
                            ch.pipeline().addLast(new MetricsHandler(metricsCollector));
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("HTTP Metrics server started on port " + port);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    static class MetricsHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        private final MetricsCollector metricsCollector;

        public MetricsHandler(MetricsCollector metricsCollector) {
            this.metricsCollector = metricsCollector;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
            if (request.uri().equals("/metrics")) {
                String metrics = "commands_processed_total " + metricsCollector.getCommandCount() + "\n" +
                        "heap_memory_used_bytes " + metricsCollector.getHeapMemoryUsed();
                byte[] content = metrics.getBytes(StandardCharsets.UTF_8);

                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.copiedBuffer(metrics, StandardCharsets.UTF_8));
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);  // 👈 Ensure Content-Length is set

                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);  // 👈 Ensure connection closes
            } else {
                ctx.writeAndFlush(new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.NOT_FOUND));
            }
        }
    }
}

package com.zipdb.network;

import com.zipdb.core.command.CommandProcessor;
import com.zipdb.network.resp.RespError;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RESPHandler extends SimpleChannelInboundHandler<String[]> {

    private final CommandProcessor commandProcessor;

    public RESPHandler(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String[] msg) {
        StringBuilder input = new StringBuilder();
        for (String arg : msg) {
            input.append(arg).append(" ");
        }

        Object result = commandProcessor.process(input.toString().trim());
        String respFormatted = formatResp(result);
        ctx.writeAndFlush(respFormatted);


    }

    private String formatResp(Object result) {
        if (result instanceof RespError) {
            return "-" + ((RespError) result).getMessage() + "\r\n";
        } else if (result instanceof Integer) {
            return ":" + result + "\r\n";
        } else if (result instanceof String) {
            return "+" + result + "\r\n";
        } else {
            return "ERR unknown response type\r\n";
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

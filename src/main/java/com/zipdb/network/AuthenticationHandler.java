package com.zipdb.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import com.zipdb.core.command.CommandProcessor;
import org.mindrot.jbcrypt.BCrypt;

public class AuthenticationHandler extends SimpleChannelInboundHandler<String> {

    // Store the hashed password securely
    private static final String STORED_HASHED_PASSWORD = BCrypt.hashpw("your-secure-password", BCrypt.gensalt());

    private final CommandProcessor commandProcessor;

    public AuthenticationHandler(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (msg.startsWith("AUTH")) {
            String password = msg.substring(5).trim(); // Extract password after "AUTH "

            if (BCrypt.checkpw(password, STORED_HASHED_PASSWORD)) {
                ctx.writeAndFlush("OK\r\n");  // Password is correct
                // Proceed with normal command handling
                commandProcessor.process(msg);
            } else {
                ctx.writeAndFlush("ERR invalid password\r\n");
                ctx.close();  // Close the connection on failure
            }
        }
    }
}

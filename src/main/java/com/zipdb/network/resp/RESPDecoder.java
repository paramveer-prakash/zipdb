package com.zipdb.network.resp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RESPDecoder extends ReplayingDecoder<Void> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte prefix = in.readByte();

        if (prefix == '*') {  // RESP Array
            int numArgs = readInteger(in);
            List<String> args = new ArrayList<>();
            for (int i = 0; i < numArgs; i++) {
                byte type = in.readByte();
                if (type != '$') throw new IllegalArgumentException("Invalid bulk string");
                int length = readInteger(in);
                ByteBuf strBuf = in.readBytes(length);
                args.add(strBuf.toString(StandardCharsets.UTF_8));
                in.readBytes(2);  // Skip \r\n
            }
            out.add(args.toArray(new String[0]));  // Pass String[] to next handler
        } else {
            throw new IllegalArgumentException("Unsupported RESP type: " + (char) prefix);
        }
    }

    private int readInteger(ByteBuf in) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            byte b = in.readByte();
            if (b == '\r') {
                in.readByte();  // skip \n
                break;
            }
            sb.append((char) b);
        }
        return Integer.parseInt(sb.toString());
    }
}

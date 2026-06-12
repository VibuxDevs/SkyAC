package com.skyac.network;

import com.skyac.check.PlayerData;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class PacketInterceptor extends ChannelDuplexHandler {
    private final PlayerData data;

    public PacketInterceptor(PlayerData data) {
        this.data = data;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!data.handlePacket(msg, true)) {
            return; // Cancel packet
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!data.handlePacket(msg, false)) {
            return; // Cancel packet
        }
        super.write(ctx, msg, promise);
    }
}

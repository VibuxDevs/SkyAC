package com.skyac.network;

import com.skyac.SkyAC;
import com.skyac.check.PlayerData;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NettyInjector {
    private final SkyAC plugin;
    private final Map<UUID, PlayerData> dataMap = new ConcurrentHashMap<>();
    
    private String version;

    public NettyInjector(SkyAC plugin) {
        this.plugin = plugin;
        try {
            String packageName = plugin.getServer().getClass().getPackage().getName();
            version = packageName.substring(packageName.lastIndexOf('.') + 1);
        } catch (Exception e) {
            version = "v1_8_R3";
        }
    }

    public void inject(Player player) {
        try {
            Channel channel = getChannel(player);
            if (channel == null) return;
            
            if (channel.pipeline().get("SkyAC") != null) {
                channel.pipeline().remove("SkyAC");
            }
            
            PlayerData data = new PlayerData(player, plugin);
            dataMap.put(player.getUniqueId(), data);
            
            PacketInterceptor interceptor = new PacketInterceptor(data);
            channel.pipeline().addBefore("packet_handler", "SkyAC", interceptor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uninject(Player player) {
        try {
            Channel channel = getChannel(player);
            if (channel != null && channel.pipeline().get("SkyAC") != null) {
                channel.pipeline().remove("SkyAC");
            }
            dataMap.remove(player.getUniqueId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Channel getChannel(Player player) throws Exception {
        Object craftPlayer = player.getClass().getMethod("getHandle").invoke(player);
        Field playerConnectionField = craftPlayer.getClass().getField("playerConnection");
        Object playerConnection = playerConnectionField.get(craftPlayer);
        Field networkManagerField = playerConnection.getClass().getField("networkManager");
        Object networkManager = networkManagerField.get(playerConnection);
        Field channelField = networkManager.getClass().getField("channel");
        return (Channel) channelField.get(networkManager);
    }
}

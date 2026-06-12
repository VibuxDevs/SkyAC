package com.skyac;

import com.skyac.network.NettyInjector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SkyAC extends JavaPlugin {
    private NettyInjector nettyInjector;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.nettyInjector = new NettyInjector(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            nettyInjector.inject(player);
        }
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            nettyInjector.uninject(player);
        }
    }

    public NettyInjector getNettyInjector() {
        return nettyInjector;
    }
}

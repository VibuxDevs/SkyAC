package com.skyac;

import com.skyac.network.NettyInjector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SkyAC extends JavaPlugin implements Listener {
    private NettyInjector nettyInjector;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.nettyInjector = new NettyInjector(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            nettyInjector.inject(player);
        }

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            nettyInjector.uninject(player);
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (nettyInjector != null) {
            com.skyac.check.PlayerData data = nettyInjector.getPlayerData(player);
            if (data != null) {
                com.skyac.check.impl.FastEatCheck check = data.getCheck(com.skyac.check.impl.FastEatCheck.class);
                if (check != null) {
                    long duration = System.currentTimeMillis() - data.getLastUseItemTime();
                    check.verify(duration);
                }
            }
        }
    }

    public NettyInjector getNettyInjector() {
        return nettyInjector;
    }
}

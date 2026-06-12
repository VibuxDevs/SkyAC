package com.skyac;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.util.function.Consumer;

public class SchedulerUtils {
    private static boolean isFolia = false;

    static {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            isFolia = true;
        } catch (ClassNotFoundException e) {
            isFolia = false;
        }
    }

    public static void runTask(Plugin plugin, Player player, Runnable runnable) {
        if (isFolia) {
            try {
                Object scheduler = player.getClass().getMethod("getScheduler").invoke(player);
                scheduler.getClass()
                        .getMethod("run", Plugin.class, Consumer.class, Runnable.class)
                        .invoke(scheduler, plugin, (Consumer<Object>) task -> runnable.run(), null);
            } catch (Exception e) {
                // Fallback on failure
                org.bukkit.Bukkit.getScheduler().runTask(plugin, runnable);
            }
        } else {
            org.bukkit.Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }
}

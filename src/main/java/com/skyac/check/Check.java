package com.skyac.check;

import com.skyac.SkyAC;
import org.bukkit.entity.Player;

public abstract class Check {
    protected final PlayerData data;
    protected final Player player;
    protected final SkyAC plugin;
    
    private int vl = 0;
    protected final String name;

    public Check(PlayerData data, String name) {
        this.data = data;
        this.player = data.getPlayer();
        this.plugin = data.getPlugin();
        this.name = name;
    }

    public abstract void handle(Object packet, boolean isIncoming);

    protected void flag(String info) {
        vl++;
        int maxVl = plugin.getConfig().getInt("checks." + name.toLowerCase() + ".max_vl", 20);
        boolean enabled = plugin.getConfig().getBoolean("checks." + name.toLowerCase() + ".enabled", true);
        
        if (!enabled) return;

        plugin.getLogger().warning("[SkyAC] " + player.getName() + " flagged " + name + " (VL: " + vl + ") -> " + info);
        
        if (plugin.getConfig().getBoolean("rubberband_on_flag", true)) {
            data.flagSetback();
        }
        
        if (vl >= maxVl) {
            String cmd = plugin.getConfig().getString("punishment_cmd", "kick %player% %check%");
            cmd = cmd.replace("%player%", player.getName()).replace("%check%", name);
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
        }
    }
}

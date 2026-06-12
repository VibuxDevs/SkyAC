package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import java.lang.reflect.Field;

public class NoFallCheck extends Check {

    public NoFallCheck(PlayerData data) {
        super(data, "NoFall");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
        if (packetName.equals("PacketPlayInFlying") || packetName.equals("PacketPlayInPosition") || packetName.equals("PacketPlayInPositionLook")) {
            try {
                Field onGroundField = getField(packet.getClass(), "f");
                if (onGroundField == null) return;

                boolean onGround = onGroundField.getBoolean(packet);

                if (onGround) {
                    Location loc = player.getLocation();
                    
                    // Check if block directly under player's feet (and slightly offset) is air
                    boolean isAirBelow = isAir(loc.clone().add(0, -0.01, 0).getBlock().getType()) 
                            && isAir(loc.clone().add(0, -0.1, 0).getBlock().getType())
                            && isAir(loc.clone().add(0, -0.5, 0).getBlock().getType());

                    if (isAirBelow && player.getFallDistance() > 1.5 && !player.isFlying()) {
                        flag(String.format("Spoofed ground state (NoFall). Fall distance: %.2f", player.getFallDistance()));
                    }
                }
            } catch (Exception e) {}
        }
    }

    private boolean isAir(Material material) {
        return material == Material.AIR 
            || material.name().contains("CAVE_AIR") 
            || material.name().contains("VOID_AIR");
    }

    private Field getField(Class<?> clazz, String fieldName) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f;
        } catch (Exception e) {
            Class<?> superCls = clazz.getSuperclass();
            if (superCls != null && superCls != Object.class) {
                return getField(superCls, fieldName);
            }
            return null;
        }
    }
}

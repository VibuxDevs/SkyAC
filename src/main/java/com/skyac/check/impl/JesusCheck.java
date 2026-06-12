package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import java.lang.reflect.Field;

public class JesusCheck extends Check {

    public JesusCheck(PlayerData data) {
        super(data, "Jesus");
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
                if (onGround && !player.isInsideVehicle()) {
                    Location loc = player.getLocation();
                    
                    // Check blocks below and at player
                    Material standBlock = loc.getBlock().getType();
                    Material underBlock = loc.clone().add(0, -0.1, 0).getBlock().getType();

                    boolean onLiquid = isLiquid(underBlock) && (standBlock == Material.AIR || isLiquid(standBlock));
                    boolean standingOnSolid = underBlock.isSolid() || underBlock == Material.LILY_PAD;

                    if (onLiquid && !standingOnSolid) {
                        flag("Walking on liquid (Jesus).");
                    }
                }
            } catch (Exception e) {}
        }
    }

    private boolean isLiquid(Material material) {
        return material.name().contains("WATER") || material.name().contains("LAVA");
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

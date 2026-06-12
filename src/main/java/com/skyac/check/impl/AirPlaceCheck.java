package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import org.bukkit.Material;
import java.lang.reflect.Field;

public class AirPlaceCheck extends Check {

    public AirPlaceCheck(PlayerData data) {
        super(data, "AirPlace");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
        if (packetName.equals("PacketPlayInBlockPlace")) {
            try {
                Field blockPosField = getField(packet.getClass(), "a");
                if (blockPosField == null) return;
                Object blockPos = blockPosField.get(packet);
                if (blockPos == null) return;

                Field xF = getField(blockPos.getClass(), "a");
                Field yF = getField(blockPos.getClass(), "b");
                Field zF = getField(blockPos.getClass(), "c");
                if (xF == null || yF == null || zF == null) return;

                int bx = xF.getInt(blockPos);
                int by = yF.getInt(blockPos);
                int bz = zF.getInt(blockPos);

                if (bx == -1 || by == -1 || bz == -1) return;
                // Special check for overflow/null location representations
                if (by < 0 || by > 256) return;

                org.bukkit.block.Block block = player.getWorld().getBlockAt(bx, by, bz);
                Material material = block.getType();

                if (material == Material.AIR || material.name().contains("CAVE_AIR") || material.name().contains("VOID_AIR")) {
                    if (!material.name().contains("WATER") && !material.name().contains("LAVA")) {
                        flag(String.format("Attempted to place block against air block at X:%d Y:%d Z:%d", bx, by, bz));
                    }
                }
            } catch (Exception e) {}
        }
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

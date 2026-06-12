package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import org.bukkit.Material;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class FastBreakCheck extends Check {
    private final Map<org.bukkit.util.Vector, Long> breakStartTimes = new HashMap<>();

    public FastBreakCheck(PlayerData data) {
        super(data, "FastBreak");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
        if (packetName.equals("PacketPlayInPlayerDigging")) {
            try {
                Field actionField = getField(packet.getClass(), "c");
                if (actionField == null) {
                    actionField = getField(packet.getClass(), "action");
                }
                Field blockPosField = getField(packet.getClass(), "a");
                if (actionField == null || blockPosField == null) return;

                Object action = actionField.get(packet);
                Object blockPos = blockPosField.get(packet);
                if (action == null || blockPos == null) return;

                Field xF = getField(blockPos.getClass(), "a");
                Field yF = getField(blockPos.getClass(), "b");
                Field zF = getField(blockPos.getClass(), "c");
                if (xF == null || yF == null || zF == null) return;

                int bx = xF.getInt(blockPos);
                int by = yF.getInt(blockPos);
                int bz = zF.getInt(blockPos);

                org.bukkit.util.Vector pos = new org.bukkit.util.Vector(bx, by, bz);
                String actionStr = action.toString();

                if (actionStr.equals("START_DESTROY_BLOCK")) {
                    breakStartTimes.put(pos, System.currentTimeMillis());
                } else if (actionStr.equals("STOP_DESTROY_BLOCK")) {
                    if (breakStartTimes.containsKey(pos)) {
                        long duration = System.currentTimeMillis() - breakStartTimes.remove(pos);
                        org.bukkit.block.Block block = player.getWorld().getBlockAt(bx, by, bz);
                        Material material = block.getType();

                        if (duration < 50 && !player.getGameMode().name().equals("CREATIVE") && !isInstantlyBreakable(material)) {
                            flag(String.format("Broke block too fast. Break duration: %dms", duration));
                        }
                    }
                }
            } catch (Exception e) {}
        }
    }

    private boolean isInstantlyBreakable(Material m) {
        String name = m.name();
        return m == Material.AIR
            || name.contains("SAPLING")
            || name.contains("FLOWER")
            || name.contains("GRASS")
            || name.contains("MUSHROOM")
            || name.contains("TORCH")
            || name.contains("LEAVES")
            || m == Material.SNOW;
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

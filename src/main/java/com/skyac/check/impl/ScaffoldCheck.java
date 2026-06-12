package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import java.lang.reflect.Field;

public class ScaffoldCheck extends Check {
    private float lastPitch = 0;

    public ScaffoldCheck(PlayerData data) {
        super(data, "Scaffold");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
        
        if (packetName.equals("PacketPlayInLook") || packetName.equals("PacketPlayInPositionLook")) {
            try {
                Field pitchField = getField(packet.getClass(), "pitch");
                if (pitchField != null) {
                    this.lastPitch = pitchField.getFloat(packet);
                }
            } catch (Exception e) {}
        }

        if (packetName.equals("PacketPlayInBlockPlace")) {
            try {
                Field blockPosField = getField(packet.getClass(), "a");
                if (blockPosField == null) return;
                Object blockPos = blockPosField.get(packet);
                if (blockPos == null) return;

                Field yF = getField(blockPos.getClass(), "b");
                if (yF == null) return;
                int by = yF.getInt(blockPos);

                int playerY = (int) Math.round(player.getLocation().getY());

                if (by < playerY && lastPitch < 70.0 && !player.isFlying()) {
                    flag(String.format("Placed block below feet with illegal pitch. Pitch: %.1f", lastPitch));
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

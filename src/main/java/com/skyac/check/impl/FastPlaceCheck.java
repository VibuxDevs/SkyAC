package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import java.lang.reflect.Field;

public class FastPlaceCheck extends Check {
    private long lastPlaceTime = 0;

    public FastPlaceCheck(PlayerData data) {
        super(data, "FastPlace");
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

                Field yF = getField(blockPos.getClass(), "b");
                if (yF == null) return;
                int by = yF.getInt(blockPos);

                if (by == -1) return;

                long now = System.currentTimeMillis();
                if (lastPlaceTime != 0) {
                    long delay = now - lastPlaceTime;
                    if (delay < 150) {
                        flag(String.format("Placed block too fast. Delay: %dms, Limit: 150ms", delay));
                    }
                }
                lastPlaceTime = now;
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

package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import java.lang.reflect.Field;

public class BadPacketsCheck extends Check {

    public BadPacketsCheck(PlayerData data) {
        super(data, "BadPackets");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
        if (packetName.equals("PacketPlayInLook") || packetName.equals("PacketPlayInPositionLook")) {
            try {
                Field yawField = getField(packet.getClass(), "yaw");
                Field pitchField = getField(packet.getClass(), "pitch");

                if (yawField != null && pitchField != null) {
                    float yaw = yawField.getFloat(packet);
                    float pitch = pitchField.getFloat(packet);

                    if (Float.isNaN(yaw) || Float.isInfinite(yaw) || Float.isNaN(pitch) || Float.isInfinite(pitch)) {
                        flag("NaN or Infinite rotation parameters");
                        return;
                    }

                    if (Math.abs(pitch) > 90.0F) {
                        flag("Pitch limit exceeded: " + pitch);
                    }
                }
            } catch (Exception e) {}
        }

        if (packetName.equals("PacketPlayInPosition") || packetName.equals("PacketPlayInPositionLook")) {
            try {
                Field xField = getField(packet.getClass(), "x");
                Field yField = getField(packet.getClass(), "y");
                Field zField = getField(packet.getClass(), "z");

                if (xField != null && yField != null && zField != null) {
                    double x = xField.getDouble(packet);
                    double y = yField.getDouble(packet);
                    double z = zField.getDouble(packet);

                    if (Double.isNaN(x) || Double.isInfinite(x) || 
                        Double.isNaN(y) || Double.isInfinite(y) || 
                        Double.isNaN(z) || Double.isInfinite(z)) {
                        flag("NaN or Infinite position coordinates");
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

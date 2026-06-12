package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import java.lang.reflect.Field;

public class NoSlowCheck extends Check {
    private double lastX = 0;
    private double lastZ = 0;

    public NoSlowCheck(PlayerData data) {
        super(data, "NoSlow");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
        if (packetName.equals("PacketPlayInFlying") || packetName.equals("PacketPlayInPosition") || packetName.equals("PacketPlayInPositionLook")) {
            try {
                Field xField = getField(packet.getClass(), "x");
                Field zField = getField(packet.getClass(), "z");
                if (xField == null || zField == null) return;

                double x = xField.getDouble(packet);
                double z = zField.getDouble(packet);
                if (x == 0 && z == 0) return;

                if (lastX != 0 && lastZ != 0) {
                    double deltaX = x - lastX;
                    double deltaZ = z - lastZ;
                    double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

                    boolean isUsingItem = player.isBlocking();
                    
                    try {
                        isUsingItem |= (boolean) player.getClass().getMethod("isHandRaised").invoke(player);
                    } catch (Exception ignored) {}

                    if (isUsingItem) {
                        double maxNoSlowSpeed = plugin.getConfig().getDouble("checks.noslow.max_speed", 0.18);
                        
                        if (distanceXZ > maxNoSlowSpeed && data.getVelocityTicks() == 0) {
                            flag(String.format("Moving too fast while using item. Speed: %.3f, Limit: %.3f", distanceXZ, maxNoSlowSpeed));
                        }
                    }
                }
                this.lastX = x;
                this.lastZ = z;
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

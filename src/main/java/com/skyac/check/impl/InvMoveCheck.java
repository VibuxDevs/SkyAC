package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import java.lang.reflect.Field;

public class InvMoveCheck extends Check {
    private double lastX = 0;
    private double lastZ = 0;
    private double speedXZ = 0;

    public InvMoveCheck(PlayerData data) {
        super(data, "InvMove");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
        if (packetName.equals("PacketPlayInFlying") || packetName.equals("PacketPlayInPosition") || packetName.equals("PacketPlayInPositionLook")) {
            try {
                Field xField = getField(packet.getClass(), "x");
                Field zField = getField(packet.getClass(), "z");
                if (xField != null && zField != null) {
                    double x = xField.getDouble(packet);
                    double z = zField.getDouble(packet);
                    if (x != 0 && z != 0) {
                        if (lastX != 0 && lastZ != 0) {
                            double dX = x - lastX;
                            double dZ = z - lastZ;
                            this.speedXZ = Math.sqrt(dX * dX + dZ * dZ);
                        }
                        this.lastX = x;
                        this.lastZ = z;
                    }
                }
            } catch (Exception e) {}
        }

        if (packetName.equals("PacketPlayInWindowClick")) {
            if (speedXZ > 0.15 && data.getVelocityTicks() == 0 && !player.isInsideVehicle()) {
                flag(String.format("Interacted with inventory while moving. Speed: %.3f", speedXZ));
            }
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

package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import java.lang.reflect.Field;

public class EntityFlyCheck extends Check {
    private double lastX = 0;
    private double lastY = 0;
    private double lastZ = 0;

    public EntityFlyCheck(PlayerData data) {
        super(data, "EntityFly");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
        if (packetName.equals("PacketPlayInVehicleMove")) {
            try {
                Field xField = getField(packet.getClass(), "x");
                Field yField = getField(packet.getClass(), "y");
                Field zField = getField(packet.getClass(), "z");
                if (xField == null || yField == null || zField == null) return;

                double x = xField.getDouble(packet);
                double y = yField.getDouble(packet);
                double z = zField.getDouble(packet);

                if (lastX != 0 && lastY != 0 && lastZ != 0) {
                    double deltaX = x - lastX;
                    double deltaY = y - lastY;
                    double deltaZ = z - lastZ;
                    double distXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

                    boolean onSolidBlock = player.getLocation().clone().add(0, -0.5, 0).getBlock().getType().isSolid();

                    if (deltaY > 0.4 && !onSolidBlock) {
                        flag(String.format("Vehicle moved up illegally. deltaY: %.3f", deltaY));
                    } else if (distXZ > 2.0) {
                        flag(String.format("Vehicle moved too fast horizontally. speed: %.3f", distXZ));
                    }
                }

                this.lastX = x;
                this.lastY = y;
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

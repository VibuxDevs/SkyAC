package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import java.lang.reflect.Field;

public class VelocityCheck extends Check {
    private double lastX = 0;
    private double lastY = 0;
    private double lastZ = 0;

    public VelocityCheck(PlayerData data) {
        super(data, "Velocity");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
        if (packetName.equals("PacketPlayInFlying") || packetName.equals("PacketPlayInPosition") || packetName.equals("PacketPlayInPositionLook")) {
            try {
                Field xField = getField(packet.getClass(), "x");
                Field yField = getField(packet.getClass(), "y");
                Field zField = getField(packet.getClass(), "z");
                if (xField == null || yField == null || zField == null) return;

                double x = xField.getDouble(packet);
                double y = yField.getDouble(packet);
                double z = zField.getDouble(packet);
                if (x == 0 && y == 0 && z == 0) return;

                if (lastX != 0 && lastY != 0 && lastZ != 0) {
                    int ticks = data.getVelocityTicks();
                    if (ticks == 19 || ticks == 18) {
                        double deltaX = x - lastX;
                        double deltaY = y - lastY;
                        double deltaZ = z - lastZ;
                        double distXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

                        double expectedXZ = Math.sqrt(data.getVelocityX() * data.getVelocityX() + data.getVelocityZ() * data.getVelocityZ());
                        double expectedY = data.getVelocityY();

                        if (expectedXZ > 0.1 || expectedY > 0.1) {
                            double ratioXZ = distXZ / expectedXZ;
                            double ratioY = deltaY / expectedY;
                            double minRatio = plugin.getConfig().getDouble("checks.velocity.min_ratio", 0.4);

                            boolean colliding = player.getLocation().getBlock().getType().isSolid() ||
                                                player.getLocation().clone().add(0, 1.8, 0).getBlock().getType().isSolid();

                            if (!colliding && (ratioXZ < minRatio || (expectedY > 0.0 && ratioY < minRatio))) {
                                flag(String.format("Reduced knockback. Horizontal Ratio: %.2f, Vertical Ratio: %.2f", ratioXZ, ratioY));
                            }
                        }
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

package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import java.lang.reflect.Field;

public class StepCheck extends Check {
    private double lastY = 0;

    public StepCheck(PlayerData data) {
        super(data, "Step");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
        if (packetName.equals("PacketPlayInFlying") || packetName.equals("PacketPlayInPosition") || packetName.equals("PacketPlayInPositionLook")) {
            try {
                Field yField = getField(packet.getClass(), "y");
                Field onGroundField = getField(packet.getClass(), "f");
                if (yField == null || onGroundField == null) return;

                double y = yField.getDouble(packet);
                boolean onGround = onGroundField.getBoolean(packet);

                if (y != 0) {
                    if (lastY != 0) {
                        double deltaY = y - lastY;
                        if (deltaY > 0.6 && onGround && !player.isFlying() && data.getVelocityTicks() == 0) {
                            flag(String.format("Instantly stepped up blocks. Height: %.3f, Limit: 0.6", deltaY));
                        }
                    }
                    this.lastY = y;
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

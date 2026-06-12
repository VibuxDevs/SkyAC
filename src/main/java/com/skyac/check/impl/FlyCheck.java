package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import java.lang.reflect.Field;

public class FlyCheck extends Check {

    private double lastY = 0;

    public FlyCheck(PlayerData data) {
        super(data, "Fly");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;
        
        String packetName = packet.getClass().getSimpleName();
        if (packetName.equals("PacketPlayInFlying") || packetName.equals("PacketPlayInPosition") || packetName.equals("PacketPlayInPositionLook")) {
            try {
                // Heuristic Fly check over NMS payload
                Field yField = getField(packet.getClass(), "y");
                Field onGroundField = getField(packet.getClass(), "f");
                
                if (yField == null || onGroundField == null) return;
                
                double y = yField.getDouble(packet);
                boolean onGround = onGroundField.getBoolean(packet);
                
                if (!onGround && y == lastY && data.getVelocityTicks() == 0) {
                    flag("Hovering in air. Y=" + y);
                }
                
                this.lastY = y;

            } catch (Exception e) {
                // Silent
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

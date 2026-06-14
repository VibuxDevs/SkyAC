package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import java.lang.reflect.Field;

public class CriticalsCheck extends Check {
    private double lastY = 0;
    private boolean lastOnGround = true;

    public CriticalsCheck(PlayerData data) {
        super(data, "Criticals");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
        if (packetName.equals("PacketPlayInFlying") || packetName.equals("PacketPlayInPosition") || packetName.equals("PacketPlayInPositionLook")) {
            try {
                Field yField = getField(packet.getClass(), "y");
                Field onGroundField = getField(packet.getClass(), "f");
                if (yField != null && onGroundField != null) {
                    this.lastY = yField.getDouble(packet);
                    this.lastOnGround = onGroundField.getBoolean(packet);
                }
            } catch (Exception e) {}
        }

        if (packetName.equals("PacketPlayInUseEntity")) {
            try {
                Field actionField = getField(packet.getClass(), "action");
                if (actionField == null) return;
                Object actionEnum = actionField.get(packet);
                if (actionEnum == null || !actionEnum.toString().equals("ATTACK")) return;

                double yOffset = lastY % 1.0;
                if (!lastOnGround && (yOffset == 0.0 || yOffset == 0.5) && !player.isFlying() && !player.isInsideVehicle()) {
                    flag("Slight offset critical spoof.");
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

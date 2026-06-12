package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import java.lang.reflect.Field;

public class NoWebCheck extends Check {
    private double lastX = 0;
    private double lastZ = 0;

    public NoWebCheck(PlayerData data) {
        super(data, "NoWeb");
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
                    double speedXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

                    Location loc = player.getLocation();
                    boolean inWeb = loc.getBlock().getType() == Material.COBWEB ||
                                    loc.clone().add(0, 1, 0).getBlock().getType() == Material.COBWEB;

                    if (inWeb && speedXZ > 0.1 && data.getVelocityTicks() == 0 && !player.isFlying()) {
                        flag(String.format("Moved too fast inside cobweb. Speed: %.3f, Limit: 0.1", speedXZ));
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

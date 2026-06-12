package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;

public class SpeedCheck extends Check {
    private double lastX = 0;
    private double lastZ = 0;
    private boolean lastOnGround = true;

    public SpeedCheck(PlayerData data) {
        super(data, "Speed");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
        if (packetName.equals("PacketPlayInFlying") || packetName.equals("PacketPlayInPosition") || packetName.equals("PacketPlayInPositionLook")) {
            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR || player.isFlying()) {
                return;
            }

            try {
                Field xField = getField(packet.getClass(), "x");
                Field zField = getField(packet.getClass(), "z");
                Field onGroundField = getField(packet.getClass(), "f");

                if (xField == null || zField == null || onGroundField == null) return;

                double x = xField.getDouble(packet);
                double z = zField.getDouble(packet);
                boolean onGround = onGroundField.getBoolean(packet);

                // If position fields are set to 0 (sometimes PacketPlayInFlying only updates rotation), skip speed check
                if (x == 0 && z == 0) return;

                if (lastX != 0 && lastZ != 0) {
                    double deltaX = x - lastX;
                    double deltaZ = z - lastZ;
                    double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

                    double maxSpeed = lastOnGround ? 0.34 : 0.36;

                    // Sprinting offset
                    if (player.isSprinting()) {
                        maxSpeed += 0.08;
                    }

                    // Potion speed amplifier (20% per level)
                    if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                        int amp = player.getPotionEffect(PotionEffectType.SPEED).getAmplifier() + 1;
                        maxSpeed += maxSpeed * 0.2 * amp;
                    }

                    // Velocity (knockback) compensation
                    if (data.getVelocityTicks() > 0) {
                        double velXZ = Math.sqrt(data.getVelocityX() * data.getVelocityX() + data.getVelocityZ() * data.getVelocityZ());
                        maxSpeed += velXZ;
                    }

                    // Configurable buffer/offset from config.yml
                    double threshold = plugin.getConfig().getDouble("checks.speed.threshold", 0.05);
                    maxSpeed += threshold;

                    if (distanceXZ > maxSpeed) {
                        flag(String.format("Moved too fast XZ. Speed: %.3f, Limit: %.3f", distanceXZ, maxSpeed));
                    }
                }

                this.lastX = x;
                this.lastZ = z;
                this.lastOnGround = onGround;

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

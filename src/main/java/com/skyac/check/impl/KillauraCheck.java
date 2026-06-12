package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;

public class KillauraCheck extends Check {
    private float lastYaw = 0;
    private float lastPitch = 0;

    public KillauraCheck(PlayerData data) {
        super(data, "Killaura");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
        
        // Track client rotation
        if (packetName.equals("PacketPlayInLook") || packetName.equals("PacketPlayInPositionLook")) {
            try {
                Field yawField = getField(packet.getClass(), "yaw");
                Field pitchField = getField(packet.getClass(), "pitch");
                if (yawField != null && pitchField != null) {
                    this.lastYaw = yawField.getFloat(packet);
                    this.lastPitch = pitchField.getFloat(packet);
                }
            } catch (Exception e) {}
        }
        
        // Check alignment on attack
        if (packetName.equals("PacketPlayInUseEntity")) {
            try {
                Field actionField = getField(packet.getClass(), "action");
                if (actionField == null) return;
                
                Object actionEnum = actionField.get(packet);
                if (actionEnum == null || !actionEnum.toString().equals("ATTACK")) return;

                Field idField = getField(packet.getClass(), "a");
                if (idField == null) return;

                int targetId = idField.getInt(packet);
                Entity target = getEntityById(targetId);
                
                if (target != null) {
                    Vector eyeLoc = player.getEyeLocation().toVector();
                    double height = 1.8;
                    try {
                        height = (double) target.getClass().getMethod("getHeight").invoke(target);
                    } catch (Exception ignored) {}
                    
                    Vector targetCenter = target.getLocation().toVector().add(new Vector(0, height / 2.0, 0));
                    Vector playerLook = getLookVector(lastYaw, lastPitch);
                    Vector targetDir = targetCenter.subtract(eyeLoc).normalize();
                    
                    double dot = playerLook.dot(targetDir);
                    double minDot = plugin.getConfig().getDouble("checks.killaura.min_dot", 0.7);
                    
                    if (dot < minDot) {
                        flag(String.format("Attacked outside of field of view. Dot product: %.3f", dot));
                    }
                }
            } catch (Exception e) {}
        }
    }

    private Vector getLookVector(float yaw, float pitch) {
        double rotX = (double) yaw;
        double rotY = (double) pitch;
        double y = -Math.sin(Math.toRadians(rotY));
        double xz = Math.cos(Math.toRadians(rotY));
        double x = -xz * Math.sin(Math.toRadians(rotX));
        double z = xz * Math.cos(Math.toRadians(rotX));
        return new Vector(x, y, z).normalize();
    }

    private Entity getEntityById(int id) {
        try {
            Object craftWorld = player.getWorld();
            Object worldServer = craftWorld.getClass().getMethod("getHandle").invoke(craftWorld);
            Object nmsEntity = worldServer.getClass().getMethod("getEntity", int.class).invoke(worldServer, id);
            if (nmsEntity != null) {
                return (Entity) nmsEntity.getClass().getMethod("getBukkitEntity").invoke(nmsEntity);
            }
        } catch (Exception e) {
            for (Entity entity : player.getWorld().getEntities()) {
                if (entity.getEntityId() == id) return entity;
            }
        }
        return null;
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

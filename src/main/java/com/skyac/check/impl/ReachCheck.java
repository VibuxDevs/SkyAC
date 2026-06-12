package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import org.bukkit.entity.Entity;
import java.lang.reflect.Field;

public class ReachCheck extends Check {

    public ReachCheck(PlayerData data) {
        super(data, "Reach");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
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
                    double dist = getDistanceToBox(player.getEyeLocation(), target);
                    double maxReach = plugin.getConfig().getDouble("checks.reach.max_reach", 3.0);
                    
                    double pingBuffer = (player.getPing() / 100.0) * 0.1;
                    maxReach += pingBuffer;

                    if (dist > maxReach) {
                        flag(String.format("Attacked entity too far. Reach: %.3f, Limit: %.3f", dist, maxReach));
                    }
                }

            } catch (Exception e) {
                // Silent
            }
        }
    }

    private double getDistanceToBox(org.bukkit.Location eye, Entity target) {
        double eyeX = eye.getX();
        double eyeY = eye.getY();
        double eyeZ = eye.getZ();

        org.bukkit.Location tLoc = target.getLocation();
        double width = 0.6;
        double height = 1.8;
        
        try {
            width = (double) target.getClass().getMethod("getWidth").invoke(target);
            height = (double) target.getClass().getMethod("getHeight").invoke(target);
        } catch (Exception ignored) {}

        double minX = tLoc.getX() - (width / 2.0);
        double maxX = tLoc.getX() + (width / 2.0);
        double minY = tLoc.getY();
        double maxY = tLoc.getY() + height;
        double minZ = tLoc.getZ() - (width / 2.0);
        double maxZ = tLoc.getZ() + (width / 2.0);

        double closestX = Math.max(minX, Math.min(eyeX, maxX));
        double closestY = Math.max(minY, Math.min(eyeY, maxY));
        double closestZ = Math.max(minZ, Math.min(eyeZ, maxZ));

        double dX = eyeX - closestX;
        double dY = eyeY - closestY;
        double dZ = eyeZ - closestZ;

        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
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

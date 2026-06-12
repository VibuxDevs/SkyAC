package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import org.bukkit.Material;
import java.lang.reflect.Field;

public class FastBowCheck extends Check {
    private long lastInteractTime = 0;

    public FastBowCheck(PlayerData data) {
        super(data, "FastBow");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
        
        if (packetName.equals("PacketPlayInBlockPlace")) {
            if (player.getItemInHand() != null && player.getItemInHand().getType() == Material.BOW) {
                this.lastInteractTime = System.currentTimeMillis();
            }
        }
        
        if (packetName.equals("PacketPlayInPlayerDigging")) {
            try {
                Field actionField = getField(packet.getClass(), "c");
                if (actionField == null) {
                    actionField = getField(packet.getClass(), "action");
                }
                
                if (actionField != null) {
                    Object actionEnum = actionField.get(packet);
                    if (actionEnum != null && actionEnum.toString().equals("RELEASE_USE_ITEM")) {
                        if (player.getItemInHand() != null && player.getItemInHand().getType() == Material.BOW) {
                            if (lastInteractTime != 0) {
                                long chargeTime = System.currentTimeMillis() - lastInteractTime;
                                double minChargeTime = plugin.getConfig().getDouble("checks.fastbow.min_charge_time", 150.0);
                                
                                if (chargeTime < minChargeTime) {
                                    flag(String.format("Fired bow too fast. Charge: %dms, Min: %.0fms", chargeTime, minChargeTime));
                                }
                            }
                        }
                    }
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

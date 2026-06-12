package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;

public class PhaseCheck extends Check {
    private int solidTicks = 0;

    public PhaseCheck(PlayerData data) {
        super(data, "Phase");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
        if (packetName.equals("PacketPlayInFlying") || packetName.equals("PacketPlayInPosition") || packetName.equals("PacketPlayInPositionLook")) {
            Location loc = player.getLocation();
            
            Material feetMaterial = loc.getBlock().getType();
            Material headMaterial = player.getEyeLocation().getBlock().getType();

            if (isSolidBlock(feetMaterial) && isSolidBlock(headMaterial) && !player.isFlying() && player.getGameMode().name().equals("SURVIVAL")) {
                solidTicks++;
                if (solidTicks > 3) {
                    flag("Moving through solid blocks (Phase). Block: " + feetMaterial.name());
                }
            } else {
                solidTicks = 0;
            }
        }
    }

    private boolean isSolidBlock(Material material) {
        return material.isSolid() 
            && !material.name().contains("PLATE")
            && !material.name().contains("SIGN")
            && !material.name().contains("DOOR")
            && !material.name().contains("FENCE")
            && !material.name().contains("WALL")
            && !material.name().contains("GATE")
            && !material.name().contains("STAIRS")
            && !material.name().contains("SLAB")
            && !material.name().contains("TRAPDOOR")
            && material != Material.LADDER
            && material != Material.VINE;
    }
}

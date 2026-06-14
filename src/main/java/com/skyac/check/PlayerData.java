package com.skyac.check;

import com.skyac.SkyAC;
import com.skyac.check.impl.FlyCheck;
import com.skyac.check.impl.SpeedCheck;
import com.skyac.check.impl.TimerCheck;
import com.skyac.check.impl.ReachCheck;
import com.skyac.check.impl.KillauraCheck;
import com.skyac.check.impl.BadPacketsCheck;
import com.skyac.check.impl.AutoClickerCheck;
import com.skyac.check.impl.NoSlowCheck;
import com.skyac.check.impl.FastBowCheck;
import com.skyac.check.impl.NoFallCheck;
import com.skyac.check.impl.JesusCheck;
import com.skyac.check.impl.PhaseCheck;
import com.skyac.check.impl.VelocityCheck;
import com.skyac.check.impl.FastBreakCheck;
import com.skyac.check.impl.InvMoveCheck;
import com.skyac.check.impl.ScaffoldCheck;
import com.skyac.check.impl.AirPlaceCheck;
import com.skyac.check.impl.StepCheck;
import com.skyac.check.impl.NoWebCheck;
import com.skyac.check.impl.FastPlaceCheck;
import com.skyac.check.impl.CriticalsCheck;
import com.skyac.check.impl.EntityFlyCheck;
import com.skyac.check.impl.FastEatCheck;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PlayerData {
    private final Player player;
    private final SkyAC plugin;
    private final List<Check> checks = new ArrayList<>();

    private double velocityX = 0;
    private double velocityY = 0;
    private double velocityZ = 0;
    private int velocityTicks = 0;

    private org.bukkit.Location lastValidLocation;
    private boolean needsSetback = false;
    private long lastUseItemTime = 0;

    public PlayerData(Player player, SkyAC plugin) {
        this.player = player;
        this.plugin = plugin;
        this.lastValidLocation = player.getLocation().clone();
        
        checks.add(new FlyCheck(this));
        checks.add(new SpeedCheck(this));
        checks.add(new TimerCheck(this));
        checks.add(new ReachCheck(this));
        checks.add(new KillauraCheck(this));
        checks.add(new BadPacketsCheck(this));
        checks.add(new AutoClickerCheck(this));
        checks.add(new NoSlowCheck(this));
        checks.add(new FastBowCheck(this));
        checks.add(new NoFallCheck(this));
        checks.add(new JesusCheck(this));
        checks.add(new PhaseCheck(this));
        checks.add(new VelocityCheck(this));
        checks.add(new FastBreakCheck(this));
        checks.add(new InvMoveCheck(this));
        checks.add(new ScaffoldCheck(this));
        checks.add(new AirPlaceCheck(this));
        checks.add(new StepCheck(this));
        checks.add(new NoWebCheck(this));
        checks.add(new FastPlaceCheck(this));
        checks.add(new CriticalsCheck(this));
        checks.add(new EntityFlyCheck(this));
        checks.add(new FastEatCheck(this));
    }

    public boolean handlePacket(Object packet, boolean isIncoming) {
        if (isIncoming) {
            String packetName = packet.getClass().getSimpleName();
            if (packetName.equals("PacketPlayInFlying") || packetName.equals("PacketPlayInPosition") || packetName.equals("PacketPlayInPositionLook")) {
                tickVelocity();
            } else if (packetName.equals("PacketPlayInBlockPlace")) {
                lastUseItemTime = System.currentTimeMillis();
            }
        } else {
            String name = packet.getClass().getSimpleName();
            if (name.equals("PacketPlayOutEntityVelocity")) {
                try {
                    Field idField = getField(packet.getClass(), "a");
                    if (idField != null && idField.getInt(packet) == player.getEntityId()) {
                        Field xField = getField(packet.getClass(), "b");
                        Field yField = getField(packet.getClass(), "c");
                        Field zField = getField(packet.getClass(), "d");
                        if (xField != null && yField != null && zField != null) {
                            this.velocityX = ((Number) xField.get(packet)).doubleValue() / 8000.0;
                            this.velocityY = ((Number) yField.get(packet)).doubleValue() / 8000.0;
                            this.velocityZ = ((Number) zField.get(packet)).doubleValue() / 8000.0;
                            this.velocityTicks = 20; 
                        }
                    }
                } catch (Exception e) {}
            }
        }

        for (Check check : checks) {
            check.handle(packet, isIncoming);
        }

        if (isIncoming) {
            String packetName = packet.getClass().getSimpleName();
            if (packetName.equals("PacketPlayInFlying") || packetName.equals("PacketPlayInPosition") || packetName.equals("PacketPlayInPositionLook")) {
                if (needsSetback) {
                    teleportBack();
                    needsSetback = false;
                } else if (player.isOnGround()) {
                    this.lastValidLocation = player.getLocation().clone();
                }
            }
        }

        return true; 
    }

    public void flagSetback() {
        this.needsSetback = true;
    }

    private void teleportBack() {
        if (lastValidLocation != null) {
            com.skyac.SchedulerUtils.runTask(plugin, player, () -> {
                player.teleport(lastValidLocation);
            });
        }
    }

    private void tickVelocity() {
        if (velocityTicks > 0) {
            velocityTicks--;
            if (velocityTicks == 0) {
                velocityX = 0;
                velocityY = 0;
                velocityZ = 0;
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

    public double getVelocityX() { return velocityX; }
    public double getVelocityY() { return velocityY; }
    public double getVelocityZ() { return velocityZ; }
    public int getVelocityTicks() { return velocityTicks; }

    public Player getPlayer() {
        return player;
    }

    public SkyAC getPlugin() {
        return plugin;
    }

    public long getLastUseItemTime() {
        return lastUseItemTime;
    }

    @SuppressWarnings("unchecked")
    public <T extends Check> T getCheck(Class<T> clazz) {
        for (Check check : checks) {
            if (clazz.isInstance(check)) {
                return (T) check;
            }
        }
        return null;
    }
}

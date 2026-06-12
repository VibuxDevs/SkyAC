package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;

import java.util.LinkedList;

public class TimerCheck extends Check {
    private final LinkedList<Long> packetTimes = new LinkedList<>();
    private static final int SAMPLE_SIZE = 20;

    public TimerCheck(PlayerData data) {
        super(data, "Timer");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
        if (packetName.equals("PacketPlayInFlying") || packetName.equals("PacketPlayInPosition") || packetName.equals("PacketPlayInPositionLook")) {
            long now = System.currentTimeMillis();
            packetTimes.add(now);

            if (packetTimes.size() > SAMPLE_SIZE) {
                packetTimes.poll();
                
                long totalTime = packetTimes.getLast() - packetTimes.getFirst();
                double averageTime = (double) totalTime / (SAMPLE_SIZE - 1);

                // Normal 20 tps should yield average 50ms per packet
                double minAverage = plugin.getConfig().getDouble("checks.timer.min_average", 46.0);

                if (averageTime < minAverage) {
                    flag(String.format("Timer active. Avg: %.2fms", averageTime));
                }
            }
        }
    }
}

package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;
import java.util.LinkedList;

public class AutoClickerCheck extends Check {
    private final LinkedList<Long> clickTimes = new LinkedList<>();
    private final LinkedList<Long> clickDelays = new LinkedList<>();
    private long lastClickTime = 0;

    public AutoClickerCheck(PlayerData data) {
        super(data, "AutoClicker");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        if (!isIncoming) return;

        String packetName = packet.getClass().getSimpleName();
        // Arm animation signifies a client-side click (left-click swing)
        if (packetName.equals("PacketPlayInArmAnimation")) {
            long now = System.currentTimeMillis();
            clickTimes.add(now);

            // Keep track of delays between consecutive clicks
            if (lastClickTime != 0) {
                long delay = now - lastClickTime;
                // Exclude delays that are too long (e.g. paused clicking)
                if (delay < 1000) {
                    clickDelays.add(delay);
                    if (clickDelays.size() > 20) {
                        clickDelays.poll();
                    }
                }
            }
            this.lastClickTime = now;

            // Remove clicks older than 1 second (1000ms)
            while (!clickTimes.isEmpty() && now - clickTimes.getFirst() > 1000) {
                clickTimes.poll();
            }

            int cps = clickTimes.size();
            int maxCps = plugin.getConfig().getInt("checks.autoclicker.max_cps", 18);

            if (cps > maxCps) {
                flag(String.format("Unnatural CPS: %d (Max: %d)", cps, maxCps));
                return;
            }

            // Check click delay consistency (Autoclickers with fixed delay intervals)
            if (clickDelays.size() >= 15 && cps > 6) {
                double mean = 0;
                for (long delay : clickDelays) {
                    mean += delay;
                }
                mean /= clickDelays.size();

                double variance = 0;
                for (long delay : clickDelays) {
                    variance += Math.pow(delay - mean, 2);
                }
                variance /= clickDelays.size();
                double stdDev = Math.sqrt(variance);

                double minStdDev = plugin.getConfig().getDouble("checks.autoclicker.min_deviation", 5.0);

                // Constant clicking with zero or very low variation is physically impossible for humans
                if (stdDev < minStdDev) {
                    flag(String.format("Extremely consistent click pattern. StdDev: %.2fms", stdDev));
                }
            }
        }
    }
}

package com.skyac.check.impl;

import com.skyac.check.Check;
import com.skyac.check.PlayerData;

public class FastEatCheck extends Check {

    public FastEatCheck(PlayerData data) {
        super(data, "FastEat");
    }

    @Override
    public void handle(Object packet, boolean isIncoming) {
        // Tracked via PlayerData Bukkit event bridge
    }

    public void verify(long duration) {
        if (duration < 1100) {
            flag(String.format("Consumed item too fast. Duration: %dms, Limit: 1100ms", duration));
        }
    }
}

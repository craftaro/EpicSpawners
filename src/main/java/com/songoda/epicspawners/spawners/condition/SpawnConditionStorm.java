package com.songoda.epicspawners.spawners.condition;

import com.songoda.epicspawners.spawners.spawner.PlacedSpawner;

public class SpawnConditionStorm implements SpawnCondition {

    private final boolean stormOnly;

    public SpawnConditionStorm(boolean stormOnly) {
        this.stormOnly = stormOnly;
    }

    @Override
    public String getName() {
        return "storm";
    }

    @Override
    public String getDescription() {
        return "There is no storm.";
    }

    @Override
    public boolean isMet(PlacedSpawner spawner) {
        return !stormOnly || spawner.getLocation().getWorld().hasStorm();
    }

    public boolean isStormOnly() {
        return stormOnly;
    }
}

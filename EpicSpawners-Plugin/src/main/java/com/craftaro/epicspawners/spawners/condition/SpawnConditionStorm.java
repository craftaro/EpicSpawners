package com.craftaro.epicspawners.spawners.condition;

import com.craftaro.epicspawners.api.spawners.condition.SpawnCondition;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;

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
        return !this.stormOnly || spawner.getLocation().getWorld().hasStorm();
    }

    public boolean isStormOnly() {
        return this.stormOnly;
    }
}

package com.songoda.epicspawners.spawners.condition;

import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.condition.SpawnCondition;

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
        return "Must be in a storm";
    }

    @Override
    public boolean isMet(Spawner spawner) {
        return (stormOnly && spawner.getLocation().getWorld().hasStorm()) || (!stormOnly && !spawner.getLocation().getWorld().hasStorm());
    }

    public boolean isStormOnly() {
        return stormOnly;
    }
}

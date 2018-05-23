package com.songoda.epicspawners.spawners.condition;

import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.condition.SpawnCondition;

public class SpawnerConditionStorm implements SpawnCondition {

    private final boolean inStorm;

    public SpawnerConditionStorm(boolean inStorm) {
        this.inStorm = inStorm;
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
        return inStorm && spawner.getLocation().getWorld().hasStorm();
    }
}

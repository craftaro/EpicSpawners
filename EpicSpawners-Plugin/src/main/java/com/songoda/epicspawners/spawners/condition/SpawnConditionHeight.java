package com.songoda.epicspawners.spawners.condition;

import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.condition.SpawnCondition;

public class SpawnConditionHeight implements SpawnCondition {

    private final int min, max;

    public SpawnConditionHeight(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public String getName() {
        return "height";
    }

    @Override
    public String getDescription() {
        return "Spawner y coordinate must be between " + min + " and " + max + ".";
    }

    @Override
    public boolean isMet(Spawner spawner) {
        double y = spawner.getLocation().getY();
        return y >= min && y <= max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
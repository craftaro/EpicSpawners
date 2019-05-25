package com.songoda.epicspawners.spawners.condition;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.Spawner;

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
        return EpicSpawners.getInstance().getLocale().getMessage("interface.spawner.conditionHeight",min, max);
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
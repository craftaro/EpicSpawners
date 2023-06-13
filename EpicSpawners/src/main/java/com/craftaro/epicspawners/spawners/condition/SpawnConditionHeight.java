package com.craftaro.epicspawners.spawners.condition;

import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.condition.SpawnCondition;
import com.craftaro.epicspawners.spawners.spawner.PlacedSpawnerImpl;

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
        return EpicSpawners.getInstance().getLocale().getMessage("interface.spawner.conditionHeight")
                .processPlaceholder("min", min)
                .processPlaceholder("max", max)
                .getMessage();
    }

    @Override
    public boolean isMet(PlacedSpawnerImpl spawner) {
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
package com.songoda.epicspawners.spawners.condition;

import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.condition.SpawnCondition;
import org.bukkit.World;

public class SpawnerConditionDayNight implements SpawnCondition {

    private final boolean day;

    SpawnerConditionDayNight(boolean day) {
        this.day = day;
    }

    @Override
    public String getName() {
        return "daynight";
    }

    @Override
    public String getDescription() {
        return day ? "Only spawns during the day." : "Only spawns during the night.";
    }

    @Override
    public boolean isMet(Spawner spawner) {
        World world = spawner.getLocation().getWorld();
        if (day && day(world.getTime())) {
            return true;
        } else if (!day && !day(world.getTime())) {
            return true;
        }
        return false;
    }

    public boolean day(long time) {
        return time < 12300 || time > 23850;
    }
}

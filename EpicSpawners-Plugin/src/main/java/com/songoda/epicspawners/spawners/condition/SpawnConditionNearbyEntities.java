package com.songoda.epicspawners.spawners.condition;

import java.util.Collection;

import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.condition.SpawnCondition;

import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class SpawnConditionNearbyEntities implements SpawnCondition {

    private final int max;

    public SpawnConditionNearbyEntities(int max) {
        this.max = max;
    }

    @Override
    public String getName() {
        return "nearby_entities";
    }

    @Override
    public String getDescription() {
        return "Must be less than " + max + " around this spawner.";
    }

    @Override
    public boolean isMet(Spawner spawner) {
        Location location = spawner.getLocation().add(0.5, 0.5, 0.5);

        int amt = Methods.countEntitiesAroundLoation(location);

        return amt < max;
    }

    public int getMax() {
        return max;
    }
}
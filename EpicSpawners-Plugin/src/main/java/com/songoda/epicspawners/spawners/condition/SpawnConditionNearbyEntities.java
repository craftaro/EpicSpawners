package com.songoda.epicspawners.spawners.condition;

import java.util.Collection;

import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.condition.SpawnCondition;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class SpawnConditionNearbyEntities implements SpawnCondition {

    private final int max;
    private final double radius;

    public SpawnConditionNearbyEntities(int max, double radius) {
        this.max = max;
        this.radius = radius;
    }

    @Override
    public String getName() {
        return "nearby_entities";
    }

    @Override
    public String getDescription() {
        return "Must be less than " + max + " entities within a " + radius + " block radius";
    }

    @Override
    public boolean isMet(Spawner spawner) {
        Location location = spawner.getLocation().add(0.5, 0.5, 0.5);

        Collection<Entity> entities = location.getWorld().getNearbyEntities(location, radius, radius, radius);
        entities.removeIf(e -> e.getType() == EntityType.PLAYER);

        return entities.size() < max;
    }

}
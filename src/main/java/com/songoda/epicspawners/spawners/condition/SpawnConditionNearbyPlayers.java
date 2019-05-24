package com.songoda.epicspawners.spawners.condition;

import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.condition.SpawnCondition;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Collection;

public class SpawnConditionNearbyPlayers implements SpawnCondition {

    private final int distance;
    private final int amount;

    public SpawnConditionNearbyPlayers(int distance, int amount) {
        this.amount = amount;
        this.distance = distance;
    }

    @Override
    public String getName() {
        return "nearby_player";
    }

    @Override
    public String getDescription() {
        return amount + " Players must be at least" + distance + " blocks away for this spawner to spawn.";
    }

    @Override
    public boolean isMet(Spawner spawner) {
        Location location = spawner.getLocation().add(0.5, 0.5, 0.5);

        Collection<Entity> players = location.getWorld().getNearbyEntities(location, distance, distance, distance);
        players.removeIf(e -> e.getType() != EntityType.PLAYER);

        return players.size() >= amount;
    }

    public int getDistance() {
        return distance;
    }

    public int getAmount() {
        return amount;
    }
}
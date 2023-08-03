package com.craftaro.epicspawners.api.events;

import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;

/**
 * Represents an abstract {@link Event} given a {@link Player} and {@link PlacedSpawner} instance
 */
public abstract class SpawnerEvent extends PlayerEvent {
    protected final PlacedSpawner spawner;

    public SpawnerEvent(Player who, PlacedSpawner spawner) {
        super(who);
        this.spawner = spawner;
    }

    /**
     * Get the {@link PlacedSpawner} involved in this event
     *
     * @return the broken spawner
     */
    public PlacedSpawner getSpawner() {
        return this.spawner;
    }
}

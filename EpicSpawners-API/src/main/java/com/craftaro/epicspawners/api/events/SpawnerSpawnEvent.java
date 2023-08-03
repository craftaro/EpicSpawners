package com.craftaro.epicspawners.api.events;

import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

/**
 * Called when a spawner spawns an entity.
 */
public class SpawnerSpawnEvent extends EntityEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private boolean canceled = false;
    private final PlacedSpawner spawner;

    public SpawnerSpawnEvent(Entity what, PlacedSpawner spawner) {
        super(what);
        this.spawner = spawner;
    }

    public PlacedSpawner getSpawner() {
        return this.spawner;
    }

    @Override
    public boolean isCancelled() {
        return this.canceled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.canceled = cancel;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

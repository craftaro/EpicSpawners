package com.craftaro.epicspawners.api.events;

import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a spawner has been placed in the world
 */
public class SpawnerPlaceEvent extends SpawnerEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private boolean canceled = false;

    public SpawnerPlaceEvent(Player who, PlacedSpawner spawner) {
        super(who, spawner);
    }

    @Override
    public boolean isCancelled() {
        return this.canceled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.canceled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

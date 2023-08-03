package com.craftaro.epicspawners.api.events;

import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when someone attempts to access a spawner.
 */
public class SpawnerAccessEvent extends SpawnerEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;

    public SpawnerAccessEvent(Player who, PlacedSpawner spawner) {
        super(who, spawner);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

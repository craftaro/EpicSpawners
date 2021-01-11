package com.songoda.epicspawners.api.events;

import com.songoda.epicspawners.spawners.spawner.PlacedSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when someone attempts to access a spawner. Can be cancelled.
 */
public class SpawnerAccessEvent extends SpawnerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;

    public SpawnerAccessEvent(Player player, PlacedSpawner spawner) {
        super(player, spawner);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean canceled) {
        this.cancelled = canceled;
    }
}

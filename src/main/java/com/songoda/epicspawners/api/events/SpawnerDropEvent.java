package com.songoda.epicspawners.api.events;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.PlacedSpawner;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a spawner has been dropped in the world after being broken
 */
public class SpawnerDropEvent extends SpawnerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean canceled = false;

    public SpawnerDropEvent(Player player, PlacedSpawner spawner) {
        super(player, spawner);
    }

    @Deprecated
    public SpawnerDropEvent(Location location, Player player) {
        this(player, EpicSpawners.getInstance().getSpawnerManager().getSpawnerFromWorld(location));
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
        return canceled;
    }

    @Override
    public void setCancelled(boolean canceled) {
        this.canceled = canceled;
    }

}
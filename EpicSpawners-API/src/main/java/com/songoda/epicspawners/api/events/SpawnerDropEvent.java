package com.songoda.epicspawners.api.events;

import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.api.spawner.Spawner;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a spawner has been dropped in the world after being broken
 */
public class SpawnerDropEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private Location location;
    private Player player;
    private int stackSize;
    private EntityType type;

    private boolean canceled = false;

    public SpawnerDropEvent(Location location, Player player) {
        Spawner spawner = EpicSpawnersAPI.getSpawnerManager().getSpawnerFromWorld(location);
        this.location = location;
        this.player = player;
        this.stackSize = spawner.getSpawnerDataCount();
        if (spawner.getCreatureSpawner() == null)
            this.type = null;
        else
            this.type = spawner.getCreatureSpawner().getSpawnedType();
    }

    /**
     * Get the location at which the spawner was broken
     * 
     * @return the spawner location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Get the player involved in this event
     * 
     * @return the involved player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the stack size of the broken spawner
     * 
     * @return the spawner stack size
     */
    public int getStackSize() {
        return stackSize;
    }

    /**
     * Get the type of entity that was spawned from the broken spawner
     * 
     * @return the spawner type
     */
    public EntityType getType() {
        return type;
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

    /**
     * Get the multiplier of the broken spawner
     * 
     * @return the stack size
     * 
     * @deprecated see {@link #getStackSize()}
     */
    @Deprecated
    public int getMultiSize() {
        return stackSize;
    }
}
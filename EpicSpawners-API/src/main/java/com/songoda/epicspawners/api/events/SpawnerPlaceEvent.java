package com.songoda.epicspawners.api.events;

import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.api.spawner.Spawner;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a spawner has been placed in the world
 */
public class SpawnerPlaceEvent extends SpawnerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean canceled = false;

    @Deprecated private final Location location;
    @Deprecated private final int multi;
    @Deprecated private String type;

    /**
     * Fires when a spawner is placed into the game world.
     *
     * @param location location of spawner that was placed
     * @param player player who placed this spawner
     * @param type type of this spawner
     * 
     * @deprecated Legacy code. See {@link #SpawnerPlaceEvent(Player, Spawner)}
     */
    @Deprecated
    public SpawnerPlaceEvent(Location location, Player player, String type) {
        super(player, EpicSpawnersAPI.getSpawnerManager().getSpawnerFromWorld(location));

        // Legacy
        this.location = location;
        this.multi = spawner.getSpawnerDataCount();
        this.type = type;
    }

    public SpawnerPlaceEvent(Player player, Spawner spawner) {
        super(player, spawner);

        // Legacy
        this.location = spawner.getLocation();
        this.multi = spawner.getSpawnerDataCount();
        this.type = spawner.getDisplayName();
    }

    /**
     * Get the location at which the spawner was placed
     * 
     * @return the placement location
     * 
     * @deprecated Legacy API. See {@link Spawner#getLocation()}
     */
    @Deprecated
    public Location getLocation() {
        return location;
    }

    /**
     * Get the multiplier for this spawner.
     *
     * @return the multiplier for this spawner
     * @see #getSpawner()
     * 
     * @deprecated Legacy API. See {@link Spawner#getSpawnerDataCount()}
     */
    @Deprecated
    public int getMulti() {
        return multi;
    }

    /**
     * Get the type of this spawner.
     *
     * @return the type of this spawner
     * @see #getSpawner()
     * 
     * @deprecated Legacy API. See {@link Spawner#getDisplayName()}
     */
    public String getType() {
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
}

package com.songoda.epicspawners.api.events;

import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.api.spawner.Spawner;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by songoda on 4/22/2017.
 */
public class SpawnerPlaceEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private Location location;
    private Player player;
    private int multi;
    private String type;

    private Spawner spawner;

    private boolean canceled = false;

    /**
     * Fires when a spawner is placed into the game world.
     *
     * @param location location of spawner that was placed
     * @param player   player who placed this spawner
     * @param type     type of this spawner
     * @deprecated Legacy code. See {@link SpawnerPlaceEvent(Player, Spawner , Location)}
     */
    @Deprecated
    public SpawnerPlaceEvent(Location location, Player player, String type) {
        Spawner spawner = EpicSpawnersAPI.getImplementation().getSpawnerManager().getSpawnerFromWorld(location);
        this.location = location;
        this.player = player;
        this.multi = spawner.getSpawnerDataCount();
        this.type = type;
    }

    public SpawnerPlaceEvent(Player player, Spawner spawner, Location location) {
        this.player = player;
        this.spawner = spawner;
        this.location = location;

        // LEGACY
        this.multi = spawner.getSpawnerDataCount(); //ToDo: Might not be correct
        this.type = spawner.getDisplayName();
    }

    public Location getLocation() {
        return location;
    }

    public Player getPlayer() {
        return player;
    }

    public Spawner getSpawner() {
        return spawner;
    }

    /**
     * Get the multiplier for this spawner.
     *
     * @return the multiplier for this spawner
     * @see #getSpawner()
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

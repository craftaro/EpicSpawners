package com.songoda.epicspawners.api.events;

import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.api.spawner.Spawner;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a spawner has been broken in the world
 */
public class SpawnerBreakEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private Location location;
    private Player player;
    private int multi;
    private String type;

    private boolean canceled = false;

    public SpawnerBreakEvent(Location location, Player player) {
        Spawner spawner = EpicSpawnersAPI.getImplementation().getSpawnerManager().getSpawnerFromWorld(location);
        this.location = location;
        this.player = player;
        this.multi = spawner.getSpawnerDataCount();
        if (spawner.getIdentifyingName() == null)
            this.type = null;
        else
            this.type = spawner.getIdentifyingName();
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
     * Get the amount of SpawnerData objects held by the broken spawner
     * 
     * @return the spawner multiplier
     */
    public int getMulti() {
        return multi;
    }

    /**
     * Get the type of spawner data
     * 
     * @return the spawner data
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

package com.songoda.epicspawners.api.events;

import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a spawner has been changed. This includes changes such as stack size as
 * well as a change in {@link SpawnerData}
 */
public class SpawnerChangeEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private Location location;

    private Player player;

    private int stackSize;
    private int oldStackSize;

    private SpawnerData spawnerData, oldSpawnerData;

    private boolean canceled = false;

    public SpawnerChangeEvent(Location location, Player player, int stackSize, int oldStackSize) {
        this.location = location;
        this.player = player;
        this.stackSize = stackSize;
        this.oldStackSize = oldStackSize;
    }

    public SpawnerChangeEvent(Location location, Player player, SpawnerData data, SpawnerData oldSpawnerData) {
        this.location = location;
        this.player = player;
        this.spawnerData = data;
        this.oldSpawnerData = oldSpawnerData;
    }

    @Deprecated
    public SpawnerChangeEvent(Location location, Player player, String type, String oldType) {
        this.location = location;
        this.player = player;
        SpawnerManager spawnerManager = EpicSpawnersAPI.getSpawnerManager();
        this.spawnerData = spawnerManager.getSpawnerData(type);
        this.oldSpawnerData = spawnerManager.getSpawnerData(oldType);
    }

    /**
     * Get the spawner block involved with this event
     * 
     * @return the spawner block
     */
    public Block getSpawner() {
        return location.getBlock();
    }

    /**
     * Get the new stack size of the spawner after this event completes. If this
     * event is not to do with stack size changing, this method simply returns 0
     * 
     * @return the new stack size
     */
    public int getStackSize() {
        return stackSize;
    }

    /**
     * Get the old stack size of the spawner from before this event was called. If
     * this event is not to do with stack size changing, this method simply returns 0
     * 
     * @return the old stack size
     */
    public int getOldStackSize() {
        return oldStackSize;
    }

    /**
     * Get the player involved in this event
     * 
     * @return the involved player
     */
    public Player getPlayer() {
        return player;
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
     * Get the new spawner data after this event completes. If this event is not
     * to do with the spawner data changing, this method simply returns null
     * 
     * @return the new spawner data
     */
    public SpawnerData getSpawnerData() {
        return spawnerData;
    }

    /**
     * Get the old spawner data from before this event was called. If this event
     * is not to do with the spawner data changing, this method simply returns null
     * 
     * @return the old spawner data
     */
    public SpawnerData getOldSpawnerData() {
        return oldSpawnerData;
    }

    /**
     * The spawner's multiplier (stack size)
     * 
     * @return the stack size
     * 
     * @deprecated see {@link #getStackSize()}
     */
    @Deprecated
    public int getCurrentMulti() {
        return stackSize;
    }

    /**
     * Get the old stack size
     * 
     * @return the old stack size
     * 
     * @deprecated see {@link #getOldStackSize()}
     */
    @Deprecated
    public int getOldMulti() {
        return oldStackSize;
    }

    /**
     * Get the new spawner data type
     * 
     * @return the spawner type
     * 
     * @deprecated see {@link #getSpawnerData()}
     */
    @Deprecated
    public String getType() {
        return spawnerData.getIdentifyingName();
    }

    /**
     * Get the old spawner data type
     * 
     * @return the spawner type
     * 
     * @deprecated see {@link #getOldSpawnerData()}
     */
    @Deprecated
    public String getOldType() {
        return oldSpawnerData.getIdentifyingName();
    }
}

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
 * Created by songoda on 4/22/2017.
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

    public Block getSpawner() {
        return location.getBlock();
    }

    public int getStackSize() {
        return stackSize;
    }

    public int getOldStackSize() {
        return oldStackSize;
    }

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

    public SpawnerData getSpawnerData() {
        return spawnerData;
    }

    public SpawnerData getOldSpawnerData() {
        return oldSpawnerData;
    }

    @Deprecated
    public int getCurrentMulti() {
        return stackSize;
    }

    @Deprecated
    public int getOldMulti() {
        return oldStackSize;
    }

    @Deprecated
    public String getType() {
        return spawnerData.getIdentifyingName();
    }

    @Deprecated
    public String getOldType() {
        return oldSpawnerData.getIdentifyingName();
    }
}

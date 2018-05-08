package com.songoda.epicspawners.Spawners;

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

    private int multi;
    private int oldMulti;

    private String type;
    private String oldType;

    private boolean canceled = false;

    public SpawnerChangeEvent(Location location, Player player, int multi, int oldMulti) {
        this.location = location;
        this.player = player;
        this.multi = multi;
        this.oldMulti = oldMulti;
    }

    public SpawnerChangeEvent(Location location, Player player, String type, String oldType) {
        this.location = location;
        this.player = player;
        this.type = type;
        this.oldType = oldType;
    }

    public Block getSpawner() {
        return location.getBlock();
    }

    public int getCurrentMulti() {
        return multi;
    }

    public int getOldMulti() {
        return oldMulti;
    }

    public String getType() {
        return type;
    }

    public String getOldType() {
        return oldType;
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
}

package com.songoda.epicspawners.spawners.events;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.object.Spawner;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by songoda on 4/22/2017.
 */
public class SpawnerBreakEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private Location location;
    private Player player;
    private int multi;
    private String type;

    private boolean canceled = false;

    public SpawnerBreakEvent(Location location, Player player) {
        Spawner spawner = EpicSpawners.getInstance().getSpawnerManager().getSpawnerFromWorld(location);
        this.location = location;
        this.player = player;
        this.multi = spawner.getSpawnerMultiplier();
        if (spawner.getName() == null)
            this.type = null;
        else
            this.type = spawner.getName();
    }

    public Location getLocation() {
        return location;
    }

    public Player getPlayer() {
        return player;
    }

    public int getMulti() {
        return multi;
    }

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

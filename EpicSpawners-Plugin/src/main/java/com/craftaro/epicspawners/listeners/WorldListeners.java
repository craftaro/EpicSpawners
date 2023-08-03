package com.craftaro.epicspawners.listeners;

import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldListeners implements Listener {
    private final EpicSpawners plugin;

    public WorldListeners(EpicSpawners plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        // Unload previous spawners belonging to this world.
        for (PlacedSpawner ps : this.plugin.getSpawnerManager().getSpawners()) {
            if (e.getWorld().getName().equals(ps.getWorld().getName())) {
                this.plugin.getSpawnerManager().removeSpawnerFromWorld(ps);
            }
        }
    }
}

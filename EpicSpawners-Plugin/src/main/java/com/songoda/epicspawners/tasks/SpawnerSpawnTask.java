package com.songoda.epicspawners.tasks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerManager;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnerSpawnTask extends BukkitRunnable {

    private static SpawnerSpawnTask instance;

    private final SpawnerManager manager;
    private final Map<Location, Integer> timer = new HashMap<>();

    private SpawnerSpawnTask(EpicSpawnersPlugin plugin) {
        this.manager = plugin.getSpawnerManager();
    }

    @Override
    public void run() {
        for (Spawner spawner : manager.getSpawners()) {
            if (spawner == null || spawner.getLocation() == null
                || spawner.getSpawnerDataCount() == 0 || spawner.getFirstStack().getSpawnerData() == null) continue;

            Location location = spawner.getLocation();

            int x = location.getBlockX() >> 4;
            int z = location.getBlockZ() >> 4;

            try {
                if (!location.getWorld().isChunkLoaded(x, z)) {
                    continue;
                }
            } catch (Exception e) {
                continue;
            }

            if (spawner.checkConditions() == false)
                continue;

            // If not present in map, init to 0 and put in map. Otherwise, add 30 to existing value
            int amount = timer.merge(location, 30, (oldValue, value) -> (oldValue == null) ? 0 : oldValue + value);
            int delay = spawner.updateDelay();
            if (amount < delay) continue;

            this.timer.remove(location);
            spawner.spawn();
        }
    }

    public static SpawnerSpawnTask startTask(EpicSpawnersPlugin plugin) {
        if (instance == null) {
            instance = new SpawnerSpawnTask(plugin);
            instance.runTaskTimer(plugin, 0, plugin.getConfig().getInt("Main.Custom Spawner Tick Rate"));
        }

        return instance;
    }

}
package com.songoda.epicspawners.tasks;

import java.util.HashMap;
import java.util.Map;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerManager;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnerCustomSpawnTask extends BukkitRunnable {

    private static SpawnerCustomSpawnTask instance;

    private final SpawnerManager manager;
    private final Map<Location, Integer> timer = new HashMap<>();

    private SpawnerCustomSpawnTask(EpicSpawnersPlugin plugin) {
        this.manager = plugin.getSpawnerManager();
    }

    @Override
    public void run() {
        for (Spawner spawner : manager.getSpawners()) {
            if (spawner == null || spawner.getLocation() == null
                || spawner.getSpawnerDataCount() == 0 || spawner.getFirstStack().getSpawnerData() == null
                || spawner.getCreatureSpawner().getSpawnedType() != EntityType.DROPPED_ITEM) continue;

            Location location = spawner.getLocation();

            // If not present in map, init to 0 and put in map. Otherwise, add 30 to existing value
            int amount = timer.merge(location, 30, (oldValue, value) -> (oldValue == null) ? 0 : oldValue + value);
            int delay = spawner.updateDelay();
            if (amount < delay) continue;
            
            this.timer.remove(location);
            spawner.spawn();
        }
    }

    public static SpawnerCustomSpawnTask startTask(EpicSpawnersPlugin plugin) {
        if (instance == null) {
            instance = new SpawnerCustomSpawnTask(plugin);
            instance.runTaskTimer(plugin, 0, plugin.getConfig().getInt("Main.Custom Spawner Tick Rate"));
        }

        return instance;
    }

}
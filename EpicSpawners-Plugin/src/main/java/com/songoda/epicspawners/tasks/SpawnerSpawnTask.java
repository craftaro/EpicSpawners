package com.songoda.epicspawners.tasks;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerManager;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class SpawnerSpawnTask extends BukkitRunnable {

    private static SpawnerSpawnTask instance;

    private final SpawnerManager manager;
    private final Map<Spawner, Integer> timer = new HashMap<>();

    private SpawnerSpawnTask(EpicSpawnersPlugin plugin) {
        this.manager = plugin.getSpawnerManager();
    }

    public static SpawnerSpawnTask startTask(EpicSpawnersPlugin plugin) {
        if (instance == null) {
            instance = new SpawnerSpawnTask(plugin);
            instance.runTaskTimer(plugin, 0, plugin.getConfig().getInt("Main.Custom Spawner Tick Rate"));
        }

        return instance;
    }

    @Override
    public void run() {
        for (Spawner spawner : manager.getSpawners()) {
            try {
                if (spawner == null || spawner.getSpawnerDataCount() == 0)
                    continue;

                int x = spawner.getX() >> 4;
                int z = spawner.getZ() >> 4;

                if (!spawner.getWorld().isChunkLoaded(x, z) || !spawner.checkConditions())
                    continue;

                // If not present in map, init to 0 and put in map. Otherwise, add 30 to existing value
                int amount = timer.merge(spawner, 30, (oldValue, value) -> (oldValue == null) ? 0 : oldValue + value);
                int delay = spawner.updateDelay();
                if (amount < delay) continue;

                this.timer.remove(spawner);
                spawner.spawn();
            } catch (Exception e) {
                continue;
            }
        }
    }

}
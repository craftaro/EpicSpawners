package com.songoda.epicspawners.tasks;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerManager;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnerSpawnTask extends BukkitRunnable {

    private static SpawnerSpawnTask instance;

    private final SpawnerManager manager;

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
            if (spawner == null || spawner.getSpawnerDataCount() == 0)
                continue;

            int x = spawner.getX() >> 4;
            int z = spawner.getZ() >> 4;

            int delay = spawner.getCreatureSpawner().getDelay();
            delay = delay - 30;
            spawner.getCreatureSpawner().setDelay(delay);
            if (delay >= 0) continue;

            if (!spawner.getWorld().isChunkLoaded(x, z) || !spawner.checkConditions()) {
                spawner.getCreatureSpawner().setDelay(300);
                continue;
            }
            spawner.spawn();
        }
    }

}
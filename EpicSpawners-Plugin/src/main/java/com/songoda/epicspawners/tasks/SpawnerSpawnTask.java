package com.songoda.epicspawners.tasks;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnerSpawnTask extends BukkitRunnable {

    private static SpawnerSpawnTask instance;
    private static EpicSpawnersPlugin plugin;

    private final SpawnerManager manager;

    private SpawnerSpawnTask(EpicSpawnersPlugin plug) {
        plugin = plug;
        this.manager = plugin.getSpawnerManager();
    }

    public static SpawnerSpawnTask startTask(EpicSpawnersPlugin plug) {
        plugin = plug;
        if (instance == null) {
            instance = new SpawnerSpawnTask(plugin);
            instance.runTaskTimer(plugin, 0, plugin.getConfig().getInt("Main.Custom Spawner Tick Rate"));
        }


        return instance;
    }

    @Override
    public void run() {
        for (Spawner spawner : manager.getSpawners()) {
            if (spawner == null || spawner.getSpawnerDataCount() == 0) continue;

            int delay = spawner.getCreatureSpawner().getDelay();
            delay = delay - 30;
            spawner.getCreatureSpawner().setDelay(delay);
            if (delay >= 0) continue;

            if (!spawner.isRedstonePowered()) continue;

            if (!spawner.getWorld().isChunkLoaded(spawner.getX() >> 4, spawner.getZ() >> 4) || !spawner.checkConditions()) {
                spawner.getCreatureSpawner().setDelay(300);
                continue;
            }

            if (spawner.getLocation().getBlock().getType() != Material.SPAWNER) {
                Location location = spawner.getLocation();
                plugin.getAppearanceHandler().removeDisplayItem(spawner);
                plugin.getSpawnerManager().removeSpawnerFromWorld(location);
                plugin.getHologramHandler().despawn(location.getBlock());
                return;
            }

            spawner.spawn();
        }
    }

}
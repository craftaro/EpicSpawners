package com.songoda.epicspawners.tasks;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.spawner.SpawnerManager;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class SpawnerSpawnTask extends BukkitRunnable {

    private static SpawnerSpawnTask instance;
    private static EpicSpawners plugin;

    private final SpawnerManager manager;

    private SpawnerSpawnTask(EpicSpawners plug) {
        plugin = plug;
        this.manager = plugin.getSpawnerManager();
    }

    public static SpawnerSpawnTask startTask(EpicSpawners plug) {
        plugin = plug;
        if (instance == null) {
            instance = new SpawnerSpawnTask(plugin);
            instance.runTaskTimer(plugin, 50L, Settings.CUSTOM_SPAWNER_TICK_RATE.getInt());
        }

        return instance;
    }

    @Override
    public void run() {
        new ArrayList<>(manager.getSpawners()).forEach(spawner -> {
            try {
                if (spawner.getWorld() == null
                        || !spawner.getWorld().isChunkLoaded(spawner.getX() >> 4, spawner.getZ() >> 4)) return;

                if (spawner.getLocation().getBlock().getType() != CompatibleMaterial.SPAWNER.getMaterial()
                        || spawner.getFirstStack() == null || spawner.getFirstStack().getSpawnerData() == null) {
                    spawner.destroy(plugin);
                    return;
                }

                if (spawner.getSpawnerDataCount() == 0
                        || !spawner.checkConditions()
                        || (spawner.getPlacedBy() == null && Settings.DISABLE_NATURAL_SPAWNERS.getBoolean())) return;
                        
                CreatureSpawner cSpawner = spawner.getCreatureSpawner();
                if (cSpawner == null) return;
                int delay = spawner.getCreatureSpawner().getDelay();
                delay = delay - Settings.CUSTOM_SPAWNER_TICK_RATE.getInt();
                spawner.getCreatureSpawner().setDelay(delay);
                if (delay >= 0) return;

                if (!spawner.spawn())
                    spawner.updateDelay();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
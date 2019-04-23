package com.songoda.epicspawners.tasks;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerManager;
import com.songoda.epicspawners.utils.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.stream.Collectors;

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
            instance.runTaskTimer(plugin, 0, SettingsManager.Setting.CUSTOM_SPAWNER_TICK_RATE.getInt());
        }


        return instance;
    }

    @Override
    public void run() {
        new ArrayList<>(manager.getSpawners()).forEach(spawner -> {
            if (spawner == null || spawner.getSpawnerDataCount() == 0) return;

            CreatureSpawner cSpawner = spawner.getCreatureSpawner();
            if (cSpawner == null) return;
            int delay = spawner.getCreatureSpawner().getDelay();
            delay = delay - 30;
            spawner.getCreatureSpawner().setDelay(delay);
            if (delay >= 0) return;

            if (!spawner.getWorld().isChunkLoaded(spawner.getX() >> 4, spawner.getZ() >> 4) || !spawner.checkConditions()) {
                Bukkit.broadcastMessage("Conditons failed");
                spawner.getCreatureSpawner().setDelay(300);
                return;
            }

            if (spawner.getLocation().getBlock().getType() != Material.SPAWNER) {
                Location location = spawner.getLocation();
                plugin.getAppearanceHandler().removeDisplayItem(spawner);
                plugin.getSpawnerManager().removeSpawnerFromWorld(location);
                plugin.getHologram().remove(spawner);
                return;
            }


            if (!spawner.spawn()) {
                spawner.getCreatureSpawner().setDelay(300);
            }
        });
    }

}
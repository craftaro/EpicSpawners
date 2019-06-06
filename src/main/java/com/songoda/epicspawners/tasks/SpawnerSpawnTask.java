package com.songoda.epicspawners.tasks;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.SpawnerManager;
import com.songoda.epicspawners.utils.ServerVersion;
import com.songoda.epicspawners.utils.settings.Setting;
import org.bukkit.Location;
import org.bukkit.Material;
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
            instance.runTaskTimer(plugin, 0, Setting.CUSTOM_SPAWNER_TICK_RATE.getInt());
        }

        return instance;
    }

    @Override
    public void run() {
        new ArrayList<>(manager.getSpawners()).forEach(spawner -> {
            if (spawner == null
                    || spawner.getSpawnerDataCount() == 0
                    || spawner.getWorld() == null
                    || !spawner.getWorld().isChunkLoaded(spawner.getX() >> 4, spawner.getZ() >> 4)
                    || !spawner.checkConditions()
                    || (spawner.getPlacedBy() == null && Setting.DISABLE_NATURAL_SPAWNERS.getBoolean())) return;

            CreatureSpawner cSpawner = spawner.getCreatureSpawner();
            if (cSpawner == null) return;
            int delay = spawner.getCreatureSpawner().getDelay();
            delay = delay - Setting.CUSTOM_SPAWNER_TICK_RATE.getInt();
            spawner.getCreatureSpawner().setDelay(delay);
            if (delay >= 0) return;

            if (spawner.getLocation().getBlock().getType() != (plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))) {
                Location location = spawner.getLocation();
                plugin.getAppearanceTask().removeDisplayItem(spawner);
                plugin.getSpawnerManager().removeSpawnerFromWorld(location);
                plugin.getHologram().remove(spawner);
                return;
            }

            if (!spawner.spawn())
                spawner.updateDelay();
        });
    }

}
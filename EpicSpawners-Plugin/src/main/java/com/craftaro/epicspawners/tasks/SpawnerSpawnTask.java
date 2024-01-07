package com.craftaro.epicspawners.tasks;

import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerManager;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.scheduler.BukkitRunnable;

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
        for (PlacedSpawner spawner : this.manager.getSpawners().toArray(new PlacedSpawner[0])) {
            try {
                if (spawner.getWorld() == null
                        || plugin.getBlacklistHandler().isBlacklisted(spawner.getWorld())
                        || !spawner.getWorld().isChunkLoaded(spawner.getX() >> 4, spawner.getZ() >> 4)) {
                    continue;
                }

                if (spawner.getLocation().getBlock().getType() != XMaterial.SPAWNER.parseMaterial()
                        || !spawner.isValid()) {
                    spawner.destroy();
                    continue;
                }

                if (spawner.getStackSize() == 0
                        || (spawner.getPlacedBy() == null && Settings.DISABLE_NATURAL_SPAWNERS.getBoolean())
                        || !spawner.checkConditions()) {
                    continue;
                }

                CreatureSpawner cSpawner = spawner.getCreatureSpawner();
                if (cSpawner == null) {
                    continue;
                }
                int delay = spawner.getCreatureSpawner().getDelay();
                delay = delay - Settings.CUSTOM_SPAWNER_TICK_RATE.getInt();
                spawner.getCreatureSpawner().setDelay(delay);
                if (delay >= 0) {
                    continue;
                }

                if (!spawner.spawn()) {
                    spawner.updateDelay();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

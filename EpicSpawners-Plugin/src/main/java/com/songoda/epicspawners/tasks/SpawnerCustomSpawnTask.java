package com.songoda.epicspawners.tasks;

import com.songoda.arconix.api.packets.Particle;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.particles.ParticleDensity;
import com.songoda.epicspawners.api.particles.ParticleEffect;
import com.songoda.epicspawners.api.particles.ParticleType;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class SpawnerCustomSpawnTask extends BukkitRunnable {

    private static SpawnerCustomSpawnTask instance;

    private final SpawnerManager manager;

    private Map<Location, Integer> timer = new HashMap<>();

    private SpawnerCustomSpawnTask(EpicSpawnersPlugin plugin) {
        this.manager = plugin.getSpawnerManager();
    }

    @Override
    public void run() {
        for (Spawner spawner : manager.getSpawnersInWorld().values()) {
            if (spawner == null || spawner.getLocation() == null || spawner.getSpawnerDataCount() == 0 || spawner.getFirstStack().getSpawnerData() == null)
                continue;

            if (spawner.getCreatureSpawner().getSpawnedType() != EntityType.DROPPED_ITEM) continue;

            Location location = spawner.getLocation();

                int amt = 0;
                if (!timer.containsKey(location)) {
                    timer.put(location, amt);
                } else {
                    amt = timer.get(location);
                    amt = amt + 30;
                    timer.put(location, amt);
                }
                int delay = spawner.updateDelay();
                if (amt < delay) {
                    continue;
                }
                timer.remove(location);

                spawner.spawn();
            }


    }

    public static SpawnerCustomSpawnTask startTask(EpicSpawnersPlugin plugin) {
        if (instance == null) {
            instance = new SpawnerCustomSpawnTask(plugin);
            instance.runTaskTimer(plugin, 0, 20);
        }

        return instance;
    }

}
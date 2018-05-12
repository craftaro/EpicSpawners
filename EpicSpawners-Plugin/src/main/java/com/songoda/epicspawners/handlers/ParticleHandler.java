package com.songoda.epicspawners.handlers;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.particles.ParticleEffect;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class ParticleHandler {

    private EpicSpawnersPlugin instance;

    public ParticleHandler(EpicSpawnersPlugin instance) {
        this.instance = instance;
        try {
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(EpicSpawnersPlugin.getInstance(), this::animate, 0L, 2 * 18);
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(EpicSpawnersPlugin.getInstance(), this::animate2, 0L, 3);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void animate() {
        for (Spawner spawner : instance.getSpawnerManager().getSpawnersInWorld().values()) { //ToDo: Doesnt work properly for omni spawners.
            SpawnerData spawnerData = spawner.getFirstStack().getSpawnerData(); //ToDo: You can change this by making a method in Spawner that will get the "Overriding" particle effects.
            if (spawnerData == null || spawnerData.getParticleEffect() == ParticleEffect.NONE) continue;

            if (spawnerData.isParticleEffectBoostedOnly() && spawner.getBoost() == 0) continue;

            if (spawnerData.getParticleEffect() == ParticleEffect.HALO) {
                double size = 1;
                Location locationCenter = spawner.getLocation();
                locationCenter.add(.5, 0, .5);
                int num = 1;
                for (int i = 0; i < 360; i++) {
                    if (i % 10 != 0) continue;
                    double angle = (i * Math.PI / 180);
                    double x = size * Math.cos(angle);
                    double z = size * Math.sin(angle);
                    Location loc = locationCenter.clone().add(x, .8, z);
                    num = num + 1;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> Arconix.pl().getApi().packetLibrary.getParticleManager().broadcastParticle(loc, 0, 0, 0, 0, spawnerData.getSpawnEffectParticle().getEffect(), spawnerData.getParticleDensity().getEffect()), num);
                }
            }
        }
    }

    public void animate2() {
        for (Spawner spawner : instance.getSpawnerManager().getSpawnersInWorld().values()) { //ToDo: Doesnt work properly for omni spawners.
            SpawnerData spawnerData = spawner.getFirstStack().getSpawnerData(); //ToDo: You can change this by making a method in Spawner that will get the "Overriding" particle effects.
            if (spawnerData == null || spawnerData.getParticleEffect() == ParticleEffect.NONE) continue;

            if (spawnerData.isParticleEffectBoostedOnly() && spawner.getBoost() == 0) continue;

            if (spawnerData.getParticleEffect() == ParticleEffect.TARGET) {
                Location locationCenter = spawner.getLocation();
                locationCenter.add(.5, 0, .5);
                for (int i = 0; i < 360; i++) {
                    if (i % 10 != 0) continue;
                    double angle = (i * Math.PI / 180);
                    double x = 1.2 * Math.cos(angle);
                    double z = 1.2 * Math.sin(angle);
                    Location loc = locationCenter.clone().add(x, .2, z);
                    Arconix.pl().getApi().packetLibrary.getParticleManager().broadcastParticle(loc, 0, 0, 0, 0, spawnerData.getSpawnEffectParticle().getEffect(), spawnerData.getParticleDensity().getEffect());
                }
                for (int i = 0; i < 360; i++) {
                    if (i % 10 != 0) continue;
                    double angle = (i * Math.PI / 180);
                    double x = .8 * Math.cos(angle);
                    double z = .8 * Math.sin(angle);
                    Location loc = locationCenter.clone().add(x, .4, z);
                    Arconix.pl().getApi().packetLibrary.getParticleManager().broadcastParticle(loc, 0, 0, 0, 0, spawnerData.getSpawnEffectParticle().getEffect(), spawnerData.getParticleDensity().getEffect());
                }
            }
        }
    }
}

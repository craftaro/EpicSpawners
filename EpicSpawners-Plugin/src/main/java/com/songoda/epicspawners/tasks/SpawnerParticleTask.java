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

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnerParticleTask extends BukkitRunnable {

    private static final double THETA_INCREMENT = Math.PI / 18.0; // 10 degrees
    private static final int HALO_RADIUS = 1;

    private static SpawnerParticleTask instance;

    private double theta = 0;

    private final SpawnerManager manager;
    private final Particle particleManager;

    private SpawnerParticleTask(EpicSpawnersPlugin plugin) {
        this.manager = plugin.getSpawnerManager();
        this.particleManager = Arconix.pl().getApi().packetLibrary.getParticleManager();
    }

    @Override
    public void run() {
        for (Spawner spawner : manager.getSpawners()) {
            if (spawner == null || spawner.getLocation() == null || spawner.getSpawnerDataCount() == 0 || spawner.getFirstStack().getSpawnerData() == null) continue;

            SpawnerData data = spawner.getFirstStack().getSpawnerData();
            if (data == null) return;

            ParticleEffect effect = data.getParticleEffect();
            if (effect == null || effect == ParticleEffect.NONE || (data.isParticleEffectBoostedOnly() && spawner.getBoost() == 0)) continue;

            Location centre = spawner.getLocation().add(0.5, 0.5, 0.5);

            ParticleType particle = data.getSpawnEffectParticle();
            if (particle == null || particle.getEffect() == null) return;
            ParticleDensity density = data.getParticleDensity();
            if (density == null) return;

            // Particle effects
            if (effect == ParticleEffect.HALO) {
                double x = HALO_RADIUS * Math.cos(theta);
                double z = HALO_RADIUS * Math.sin(theta);

                centre.add(x, 0.2, z);
                this.particleManager.broadcastParticle(centre, 0, 0, 0, 0, particle.getEffect(), density.getEffect());
            }
            else if (effect == ParticleEffect.TARGET) {
                for (int i = 0; i < 360; i += 10) {
                    double angle = Math.toRadians(i);
                    double cosAngle = Math.cos(angle), sinAngle = Math.sin(angle);

                    // Outer circle
                    double x = 1.2 * cosAngle, z = 1.2 * sinAngle;
                    centre.add(x, -0.2, z);
                    this.particleManager.broadcastParticle(centre, 0, 0, 0, 0, particle.getEffect(), density.getEffect() - 2);
                    centre.subtract(x, -0.2, z);

                    // Inner circle
                    x = 0.8 * cosAngle;
                    z = 0.8 * sinAngle;
                    centre.add(x, 0, z);
                    this.particleManager.broadcastParticle(centre, 0, 0, 0, 0, particle.getEffect(), density.getEffect() - 2);
                    centre.subtract(x, 0, z);
                }
            }
        }

        if ((theta += THETA_INCREMENT) > 360) {
            this.theta = 0;
        }
    }

    public static SpawnerParticleTask startTask(EpicSpawnersPlugin plugin) {
        if (instance == null) {
            instance = new SpawnerParticleTask(plugin);
            instance.runTaskTimerAsynchronously(plugin, 0, 1);
        }

        return instance;
    }

}
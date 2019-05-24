package com.songoda.epicspawners.tasks;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.particles.ParticleDensity;
import com.songoda.epicspawners.particles.ParticleEffect;
import com.songoda.epicspawners.particles.ParticleType;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.utils.ServerVersion;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnerParticleTask extends BukkitRunnable {

    private static final double THETA_INCREMENT = Math.PI / 18.0; // 10 degrees
    private static final int HALO_RADIUS = 1;

    private static SpawnerParticleTask instance;
    private final EpicSpawners plugin;
    private double theta = 0;

    private SpawnerParticleTask(EpicSpawners plugin) {
        this.plugin = plugin;
    }

    public static SpawnerParticleTask startTask(EpicSpawners plugin) {
        if (instance == null) {
            instance = new SpawnerParticleTask(plugin);
            instance.runTaskTimerAsynchronously(plugin, 0, 1);
        }

        return instance;
    }

    @Override
    public void run() {
        for (Spawner spawner : plugin.getSpawnerManager().getSpawners()) {
            if (spawner == null || spawner.getLocation() == null || spawner.getSpawnerDataCount() == 0 || spawner.getFirstStack().getSpawnerData() == null)
                continue;

            SpawnerData data = spawner.getFirstStack().getSpawnerData();
            if (data == null) return;

            ParticleEffect effect = data.getParticleEffect();
            if (effect == null || effect == ParticleEffect.NONE || (data.isParticleEffectBoostedOnly() && spawner.getBoost() == 0))
                continue;

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
                if (plugin.isServerVersionAtLeast(ServerVersion.V1_13))
                    centre.getWorld().spawnParticle(Particle.valueOf(particle.getEffect()), centre, density.getEffect(), 0, 0, 0, 0, Particle.valueOf(particle.getEffect()) == org.bukkit.Particle.REDSTONE ? new org.bukkit.Particle.DustOptions(Color.RED, 1) : null);
                else
                    centre.getWorld().spawnParticle(Particle.valueOf(particle.getEffect()), centre, density.getEffect(), 0, 0, 0, 0);
            } else if (effect == ParticleEffect.TARGET) {
                for (int i = 0; i < 360; i += 10) {
                    double angle = Math.toRadians(i);
                    double cosAngle = Math.cos(angle), sinAngle = Math.sin(angle);

                    // Outer circle
                    double x = 1.2 * cosAngle, z = 1.2 * sinAngle;
                    centre.add(x, -0.2, z);

                    if (plugin.isServerVersionAtLeast(ServerVersion.V1_13))
                        centre.getWorld().spawnParticle(Particle.valueOf(particle.getEffect()), centre, density.getEffect(), 0, 0, 0, 0, Particle.valueOf(particle.getEffect()) == org.bukkit.Particle.REDSTONE ? new org.bukkit.Particle.DustOptions(Color.RED, 1) : null);
                    else
                        centre.getWorld().spawnParticle(Particle.valueOf(particle.getEffect()), centre, density.getEffect(), 0, 0, 0, 0);
                    centre.subtract(x, -0.2, z);

                    // Inner circle
                    x = 0.8 * cosAngle;
                    z = 0.8 * sinAngle;
                    centre.add(x, 0, z);
                    if (plugin.isServerVersionAtLeast(ServerVersion.V1_13))
                        centre.getWorld().spawnParticle(Particle.valueOf(particle.getEffect()), centre, density.getEffect(), 0, 0, 0, 0, Particle.valueOf(particle.getEffect()) == org.bukkit.Particle.REDSTONE ? new org.bukkit.Particle.DustOptions(Color.RED, 1) : null);
                    else
                        centre.getWorld().spawnParticle(Particle.valueOf(particle.getEffect()), centre, density.getEffect(), 0, 0, 0, 0);
                    centre.subtract(x, 0, z);
                }
            }
        }

        if ((theta += THETA_INCREMENT) > 360) {
            this.theta = 0;
        }
    }
}
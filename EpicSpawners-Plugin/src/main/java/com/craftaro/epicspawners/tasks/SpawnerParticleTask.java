package com.craftaro.epicspawners.tasks;

import com.craftaro.core.compatibility.CompatibleParticleHandler;
import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.particles.ParticleDensity;
import com.craftaro.epicspawners.api.particles.ParticleEffect;
import com.craftaro.epicspawners.api.particles.ParticleType;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.settings.Settings;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class SpawnerParticleTask extends BukkitRunnable {
    private static final double THETA_INCREMENT = Math.PI / 18.0; // 10 degrees
    private static final int HALO_RADIUS = 1;

    private static SpawnerParticleTask instance;
    private final EpicSpawners plugin;
    private double theta = 0;

    private boolean isRunning = false;

    private SpawnerParticleTask(EpicSpawners plugin) {
        this.plugin = plugin;
    }

    public static SpawnerParticleTask startTask(EpicSpawners plugin) {
        if (instance == null) {
            instance = new SpawnerParticleTask(plugin);
        }

        boolean shouldShowParticles = Settings.SHOW_PARTICLES.getBoolean();
        if (!instance.isRunning && shouldShowParticles) {
            instance.runTaskTimerAsynchronously(plugin, 50L, 1);
            instance.isRunning = true;
        }

        if (instance.isRunning && !shouldShowParticles) {
            instance.cancel();
            instance = new SpawnerParticleTask(plugin);
        }

        return instance;
    }

    @Override
    public void run() {
        for (PlacedSpawner spawner : new ArrayList<>(this.plugin.getSpawnerManager().getSpawners())) {
            if (spawner == null || spawner.getLocation() == null ||
                    spawner.getStackSize() == 0 || spawner.getFirstStack().getSpawnerData() == null) {
                continue;
            }

            SpawnerTier data = spawner.getFirstStack().getCurrentTier();
            if (data == null) {
                continue;
            }

            ParticleEffect effect = data.getParticleEffect();
            if (effect == null || effect == ParticleEffect.NONE || (data.isParticleEffectBoostedOnly() && spawner.getBoosts().isEmpty())) {
                continue;
            }

            Location centre = spawner.getLocation().add(0.5, 0.5, 0.5);

            ParticleType particle = data.getSpawnEffectParticle();
            if (particle == null || particle.getEffect() == null) {
                continue;
            }
            ParticleDensity density = data.getParticleDensity();
            if (density == null) {
                continue;
            }

            // Particle effects
            if (effect == ParticleEffect.HALO) {
                double x = HALO_RADIUS * Math.cos(this.theta);
                double z = HALO_RADIUS * Math.sin(this.theta);

                centre.add(x, 0.2, z);
                spawnParticles(centre, particle, density);
            } else if (effect == ParticleEffect.TARGET) {
                for (int i = 0; i < 360; i += 10) {
                    double angle = Math.toRadians(i);
                    double cosAngle = Math.cos(angle),
                            sinAngle = Math.sin(angle);

                    // Outer circle
                    double x = 1.2 * cosAngle,
                            z = 1.2 * sinAngle;
                    centre.add(x, -0.2, z);

                    spawnParticles(centre, particle, density);
                    centre.subtract(x, -0.2, z);

                    // Inner circle
                    x = 0.8 * cosAngle;
                    z = 0.8 * sinAngle;
                    centre.add(x, 0, z);
                    spawnParticles(centre, particle, density);
                    centre.subtract(x, 0, z);
                }
            }
        }

        if ((this.theta += THETA_INCREMENT) > 360) {
            this.theta = 0;
        }
    }

    private void spawnParticles(Location location, ParticleType type, ParticleDensity density) {
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
            location.getWorld().spawnParticle(Particle.valueOf(type.getEffect()), location,
                    density.getEffect(), 0, 0, 0, 0,
                    Particle.valueOf(type.getEffect()) == Particle.REDSTONE ? new Particle.DustOptions(Color.RED, 1) : null);
        } else {
            CompatibleParticleHandler.spawnParticles(CompatibleParticleHandler.ParticleType.getParticle(type.getEffect()),
                    location, density.getEffect(), 0, 0, 0, 0);
        }
    }
}

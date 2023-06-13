package com.craftaro.epicspawners.api.utils;

import com.craftaro.core.compatibility.CompatibleMaterial;
import com.craftaro.epicspawners.api.particles.ParticleDensity;
import com.craftaro.epicspawners.api.particles.ParticleEffect;
import com.craftaro.epicspawners.api.particles.ParticleType;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface SpawnerTierBuilder {

    /**
     * Creates a new SpawnerDataBuilder
     * @return a new SpawnerDataBuilder
     */
    SpawnerTierBuilder newBuilder(String identifier);

    SpawnerTierBuilder setDisplayName(String name);
    SpawnerTierBuilder displayItem(CompatibleMaterial material);

    SpawnerTierBuilder setEntities(List<EntityType> entities);

    SpawnerTierBuilder setBlocks(List<CompatibleMaterial> blocks);

    SpawnerTierBuilder setItems(List<ItemStack> items);

    SpawnerTierBuilder setCommands(List<String> commands);

    SpawnerTierBuilder setSpawnBlocks(List<CompatibleMaterial> spawnBlocks);

    SpawnerTierBuilder setSpawnOnFire(boolean spawnOnFire);

    SpawnerTierBuilder setPickupCost(double pickupCost);

    SpawnerTierBuilder setPickDamage(short pickDamage);
    SpawnerTierBuilder setCostEconomy(double costEconomy);

    SpawnerTierBuilder setCostLevels(int levels);

    SpawnerTierBuilder setTickRate(String tickRate);

    SpawnerTierBuilder setParticleEffect(ParticleEffect particle);

    SpawnerTierBuilder setSpawnEffectParticle(ParticleType particle);

    SpawnerTierBuilder setEntitySpawnParticle(ParticleType particle);

    SpawnerTierBuilder setSpawnerSpawnParticle(ParticleType particle);

    SpawnerTierBuilder setParticleDensity(ParticleDensity density);

    SpawnerTierBuilder setParticleEffectBoostedOnly(boolean boostedOnly);

    SpawnerTierBuilder setSpawnLimit(int spawnLimit);

    SpawnerTier build();
}
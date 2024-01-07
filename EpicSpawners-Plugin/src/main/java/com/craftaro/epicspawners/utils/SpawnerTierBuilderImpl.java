package com.craftaro.epicspawners.utils;

import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicspawners.api.particles.ParticleDensity;
import com.craftaro.epicspawners.api.particles.ParticleEffect;
import com.craftaro.epicspawners.api.particles.ParticleType;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.api.utils.SpawnerTierBuilder;
import com.craftaro.epicspawners.spawners.spawner.SpawnerDataImpl;
import com.craftaro.epicspawners.spawners.spawner.SpawnerTierImpl;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class SpawnerTierBuilderImpl implements SpawnerTierBuilder {
    private final SpawnerTier spawnerTier;

    public SpawnerTierBuilderImpl() {
        this.spawnerTier = null;
    }

    public SpawnerTierBuilderImpl(SpawnerData data) {
        this.spawnerTier = new SpawnerTierImpl(data);
        this.spawnerTier.reloadSpawnMethods();
    }

    @Override
    public SpawnerTierBuilder newBuilder(String identifier) {
        return new SpawnerTierBuilderImpl(new SpawnerDataImpl(identifier));
    }

    @Override
    public SpawnerTierBuilder setDisplayName(String name) {
        this.spawnerTier.setDisplayName(name);
        return this;
    }

    @Override
    public SpawnerTierBuilder displayItem(XMaterial material) {
        this.spawnerTier.setDisplayItem(material);
        return this;
    }

    @Override
    public SpawnerTierBuilder setEntities(List<EntityType> entities) {
        this.spawnerTier.setEntities(entities);
        return this;
    }

    @Override
    public SpawnerTierBuilder setBlocks(List<XMaterial> blocks) {
        this.spawnerTier.setBlocks(blocks);
        return this;
    }

    @Override
    public SpawnerTierBuilder setItems(List<ItemStack> items) {
        this.spawnerTier.setItems(items);
        return this;
    }

    @Override
    public SpawnerTierBuilder setCommands(List<String> commands) {
        this.spawnerTier.setCommands(commands);
        return this;
    }

    @Override
    public SpawnerTierBuilder setSpawnBlocks(List<XMaterial> spawnBlocks) {
        this.spawnerTier.setSpawnBlocks(spawnBlocks);
        return this;
    }

    @Override
    public SpawnerTierBuilder setSpawnOnFire(boolean spawnOnFire) {
        this.spawnerTier.setSpawnOnFire(spawnOnFire);
        return this;
    }

    @Override
    public SpawnerTierBuilder setPickupCost(double pickupCost) {
        this.spawnerTier.setPickupCost(pickupCost);
        return this;
    }

    @Override
    public SpawnerTierBuilder setPickDamage(short pickDamage) {
        this.spawnerTier.setPickDamage(pickDamage);
        return this;
    }

    @Override
    public SpawnerTierBuilder setCostEconomy(double costEconomy) {
        this.spawnerTier.setCostEconomy(costEconomy);
        return this;
    }

    @Override
    public SpawnerTierBuilder setCostLevels(int levels) {
        this.spawnerTier.setCostLevels(levels);
        return this;
    }

    @Override
    public SpawnerTierBuilder setTickRate(String tickRate) {
        this.spawnerTier.setTickRate(tickRate);
        return this;
    }

    @Override
    public SpawnerTierBuilder setParticleEffect(ParticleEffect particle) {
        this.spawnerTier.setParticleEffect(particle);
        return this;
    }

    @Override
    public SpawnerTierBuilder setSpawnEffectParticle(ParticleType particle) {
        this.spawnerTier.setSpawnEffectParticle(particle);
        return this;
    }

    @Override
    public SpawnerTierBuilder setEntitySpawnParticle(ParticleType particle) {
        this.spawnerTier.setEntitySpawnParticle(particle);
        return this;
    }

    @Override
    public SpawnerTierBuilder setSpawnerSpawnParticle(ParticleType particle) {
        this.spawnerTier.setSpawnerSpawnParticle(particle);
        return this;
    }

    @Override
    public SpawnerTierBuilder setParticleDensity(ParticleDensity density) {
        this.spawnerTier.setParticleDensity(density);
        return this;
    }

    @Override
    public SpawnerTierBuilder setParticleEffectBoostedOnly(boolean boostedOnly) {
        this.spawnerTier.setParticleEffectBoostedOnly(boostedOnly);
        return this;
    }

    @Override
    public SpawnerTierBuilder setSpawnLimit(int spawnLimit) {
        this.spawnerTier.setSpawnLimit(spawnLimit);
        return this;
    }

    @Override
    public SpawnerTier build() {
        return this.spawnerTier;
    }
}

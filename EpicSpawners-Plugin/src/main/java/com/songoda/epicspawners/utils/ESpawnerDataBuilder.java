package com.songoda.epicspawners.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.songoda.epicspawners.api.particles.ParticleDensity;
import com.songoda.epicspawners.api.particles.ParticleEffect;
import com.songoda.epicspawners.api.particles.ParticleType;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.utils.SpawnerDataBuilder;
import com.songoda.epicspawners.spawners.object.ESpawnerData;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public final class ESpawnerDataBuilder implements SpawnerDataBuilder {

    private final ESpawnerData spawnerData;

    public ESpawnerDataBuilder(String identifier) {
        this.spawnerData = new ESpawnerData(identifier);
    }

    @Override
    public SpawnerDataBuilder displayName(String name) {
        this.spawnerData.setDisplayName(name);
        return this;
    }

    @Override
    public SpawnerDataBuilder pickupCost(double cost) {
        this.spawnerData.setPickupCost(cost);
        return this;
    }

    @Override
    public SpawnerDataBuilder spawnBlocks(Collection<Material> spawnBlocks) {
        this.spawnerData.setSpawnBlocks(new ArrayList<>(spawnBlocks));
        return this;
    }

    @Override
    public SpawnerDataBuilder spawnBlocks(Material... spawnBlocks) {
        this.spawnerData.setSpawnBlocks(Arrays.asList(spawnBlocks));
        return this;
    }

    @Override
    public SpawnerDataBuilder active() {
        return active(true);
    }

    @Override
    public SpawnerDataBuilder active(boolean active) {
        this.spawnerData.setActive(active);
        return this;
    }

    @Override
    public SpawnerDataBuilder inShop() {
        return inShop(true);
    }

    @Override
    public SpawnerDataBuilder inShop(boolean inShop) {
        this.spawnerData.setInShop(inShop);
        return this;
    }

    @Override
    public SpawnerDataBuilder spawnOnFire() {
        return spawnOnFire(true);
    }

    @Override
    public SpawnerDataBuilder spawnOnFire(boolean onFire) {
        this.spawnerData.setSpawnOnFire(onFire);
        return this;
    }

    @Override
    public SpawnerDataBuilder upgradeable() {
        return upgradeable(true);
    }

    @Override
    public SpawnerDataBuilder upgradeable(boolean upgradeable) {
        this.spawnerData.setUpgradeable(upgradeable);
        return this;
    }

    @Override
    public SpawnerDataBuilder convertible() {
        return convertible(true);
    }

    @Override
    public SpawnerDataBuilder convertible(boolean convertible) {
        this.spawnerData.setConvertible(convertible);
        return this;
    }

    @Override
    public SpawnerDataBuilder shopPrice(double price) {
        this.spawnerData.setShopPrice(price);
        return this;
    }

    @Override
    public SpawnerDataBuilder convertRatio(String ratio) {
        this.spawnerData.setConvertRatio(ratio);
        return this;
    }

    @Override
    public SpawnerDataBuilder upgradeCostEconomy(double cost) {
        this.spawnerData.setUpgradeCostEconomy(cost);
        return this;
    }

    @Override
    public SpawnerDataBuilder upgradeCostExperience(int cost) {
        this.spawnerData.setUpgradeCostExperience(cost);
        return this;
    }

    @Override
    public SpawnerDataBuilder killGoal(int goal) {
        this.spawnerData.setKillGoal(goal);
        return this;
    }

    @Override
    public SpawnerDataBuilder displayItem(Material material) {
        this.spawnerData.setDisplayItem(material);
        return this;
    }

    @Override
    public SpawnerDataBuilder entities(Collection<EntityType> entities) {
        this.spawnerData.setEntities(new ArrayList<>(entities));
        return this;
    }

    @Override
    public SpawnerDataBuilder entities(EntityType... entities) {
        this.spawnerData.setEntities(Arrays.asList(entities));
        return this;
    }

    @Override
    public SpawnerDataBuilder blocks(Collection<Material> blocks) {
        this.spawnerData.setBlocks(new ArrayList<>(blocks));
        return this;
    }

    @Override
    public SpawnerDataBuilder blocks(Material... blocks) {
        this.spawnerData.setBlocks(Arrays.asList(blocks));
        return this;
    }

    @Override
    public SpawnerDataBuilder items(Collection<ItemStack> items) {
        this.spawnerData.setItems(new ArrayList<>(items));
        return this;
    }

    @Override
    public SpawnerDataBuilder items(ItemStack... items) {
        this.spawnerData.setItems(Arrays.asList(items));
        return this;
    }

    @Override
    public SpawnerDataBuilder commands(Collection<String> commands) {
        this.spawnerData.setCommands(new ArrayList<>(commands));
        return this;
    }

    @Override
    public SpawnerDataBuilder commands(String... commands) {
        this.spawnerData.setCommands(Arrays.asList(commands));
        return this;
    }

    @Override
    public SpawnerDataBuilder entityDroppedItems(Collection<ItemStack> items) {
        this.spawnerData.setEntityDroppedItems(new ArrayList<>(items));
        return this;
    }

    @Override
    public SpawnerDataBuilder entityDroppedItems(ItemStack... items) {
        this.spawnerData.setEntityDroppedItems(Arrays.asList(items));
        return this;
    }

    @Override
    public SpawnerDataBuilder tickRate(String tickRate) {
        this.spawnerData.setTickRate(tickRate);
        return this;
    }

    @Override
    public SpawnerDataBuilder particleEffect(ParticleEffect particle) {
        this.spawnerData.setParticleEffect(particle);
        return this;
    }

    @Override
    public SpawnerDataBuilder spawnEffectParticle(ParticleType particle) {
        this.spawnerData.setSpawnEffectParticle(particle);
        return this;
    }

    @Override
    public SpawnerDataBuilder entitySpawnParticle(ParticleType particle) {
        this.spawnerData.setEntitySpawnParticle(particle);
        return this;
    }

    @Override
    public SpawnerDataBuilder spawnerSpawnParticle(ParticleType particle) {
        this.spawnerData.setSpawnerSpawnParticle(particle);
        return this;
    }

    @Override
    public SpawnerDataBuilder particleDensity(ParticleDensity density) {
        this.spawnerData.setParticleDensity(density);
        return this;
    }

    @Override
    public SpawnerDataBuilder particleEffectBoostedOnly() {
        return particleEffectBoostedOnly(true);
    }

    @Override
    public SpawnerDataBuilder particleEffectBoostedOnly(boolean boostedOnly) {
        this.spawnerData.setParticleEffectBoostedOnly(boostedOnly);
        return this;
    }

    @Override
    public SpawnerData build() {
        return spawnerData;
    }

}
package com.songoda.epicspawners.utils;

import com.songoda.epicspawners.particles.ParticleDensity;
import com.songoda.epicspawners.particles.ParticleEffect;
import com.songoda.epicspawners.particles.ParticleType;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class SpawnerDataBuilder {

    private final SpawnerData spawnerData;

    public SpawnerDataBuilder(String identifier) {
        this.spawnerData = new SpawnerData(identifier);
        spawnerData.reloadSpawnMethods();
    }


    public SpawnerDataBuilder displayName(String name) {
        this.spawnerData.setDisplayName(name);
        return this;
    }


    public SpawnerDataBuilder craftable(boolean craftable) {
        this.spawnerData.setCraftable(craftable);
        return this;
    }


    public SpawnerDataBuilder recipe(String recipe) {
        this.spawnerData.setRecipe(recipe);
        return this;
    }


    public SpawnerDataBuilder recipeIngredients(List<String> recipeIngredients) {
        this.spawnerData.setRecipeIngredients(recipeIngredients);
        return this;
    }


    public SpawnerDataBuilder uuid(int uuid) {
        this.spawnerData.setUUID(uuid);
        return this;
    }


    public SpawnerDataBuilder pickupCost(double cost) {
        this.spawnerData.setPickupCost(cost);
        return this;
    }


    public SpawnerDataBuilder spawnBlocks(Collection<Material> spawnBlocks) {
        this.spawnerData.setSpawnBlocks(new ArrayList<>(spawnBlocks));
        return this;
    }


    public SpawnerDataBuilder spawnBlocks(Material... spawnBlocks) {
        this.spawnerData.setSpawnBlocks(Arrays.asList(spawnBlocks));
        return this;
    }


    public SpawnerDataBuilder active() {
        return active(true);
    }


    public SpawnerDataBuilder active(boolean active) {
        this.spawnerData.setActive(active);
        return this;
    }

    public SpawnerDataBuilder spawnLimit(int spawnLimit) {
        this.spawnerData.setSpawnLimit(spawnLimit);
        return this;
    }


    public SpawnerDataBuilder inShop() {
        return inShop(true);
    }


    public SpawnerDataBuilder inShop(boolean inShop) {
        this.spawnerData.setInShop(inShop);
        return this;
    }


    public SpawnerDataBuilder spawnOnFire() {
        return spawnOnFire(true);
    }


    public SpawnerDataBuilder spawnOnFire(boolean onFire) {
        this.spawnerData.setSpawnOnFire(onFire);
        return this;
    }


    public SpawnerDataBuilder upgradeable() {
        return upgradeable(true);
    }


    public SpawnerDataBuilder upgradeable(boolean upgradeable) {
        this.spawnerData.setUpgradeable(upgradeable);
        return this;
    }


    public SpawnerDataBuilder convertible() {
        return convertible(true);
    }


    public SpawnerDataBuilder convertible(boolean convertible) {
        this.spawnerData.setConvertible(convertible);
        return this;
    }


    public SpawnerDataBuilder shopPrice(double price) {
        this.spawnerData.setShopPrice(price);
        return this;
    }


    public SpawnerDataBuilder shopOrder(int order) {
        this.spawnerData.setShopOrder(order);
        return this;
    }


    public SpawnerDataBuilder convertRatio(String ratio) {
        this.spawnerData.setConvertRatio(ratio);
        return this;
    }


    public SpawnerDataBuilder upgradeCostEconomy(double cost) {
        this.spawnerData.setUpgradeCostEconomy(cost);
        return this;
    }


    public SpawnerDataBuilder upgradeCostExperience(int cost) {
        this.spawnerData.setUpgradeCostExperience(cost);
        return this;
    }


    public SpawnerDataBuilder killGoal(int goal) {
        this.spawnerData.setKillGoal(goal);
        return this;
    }


    public SpawnerDataBuilder displayItem(Material material) {
        this.spawnerData.setDisplayItem(material);
        return this;
    }


    public SpawnerDataBuilder entities(Collection<EntityType> entities) {
        this.spawnerData.setEntities(new ArrayList<>(entities));
        return this;
    }


    public SpawnerDataBuilder entities(EntityType... entities) {
        this.spawnerData.setEntities(Arrays.asList(entities));
        return this;
    }


    public SpawnerDataBuilder blocks(Collection<Material> blocks) {
        this.spawnerData.setBlocks(new ArrayList<>(blocks));
        return this;
    }


    public SpawnerDataBuilder blocks(Material... blocks) {
        this.spawnerData.setBlocks(Arrays.asList(blocks));
        return this;
    }


    public SpawnerDataBuilder items(Collection<ItemStack> items) {
        this.spawnerData.setItems(new ArrayList<>(items));
        return this;
    }


    public SpawnerDataBuilder items(ItemStack... items) {
        this.spawnerData.setItems(Arrays.asList(items));
        return this;
    }


    public SpawnerDataBuilder commands(Collection<String> commands) {
        this.spawnerData.setCommands(new ArrayList<>(commands));
        return this;
    }


    public SpawnerDataBuilder commands(String... commands) {
        this.spawnerData.setCommands(Arrays.asList(commands));
        return this;
    }


    public SpawnerDataBuilder tickRate(String tickRate) {
        this.spawnerData.setTickRate(tickRate);
        return this;
    }


    public SpawnerDataBuilder particleEffect(ParticleEffect particle) {
        this.spawnerData.setParticleEffect(particle);
        return this;
    }


    public SpawnerDataBuilder spawnEffectParticle(ParticleType particle) {
        this.spawnerData.setSpawnEffectParticle(particle);
        return this;
    }


    public SpawnerDataBuilder isCustom(boolean custom) {
        this.spawnerData.setCustom(custom);
        return this;
    }


    public SpawnerDataBuilder entitySpawnParticle(ParticleType particle) {
        this.spawnerData.setEntitySpawnParticle(particle);
        return this;
    }


    public SpawnerDataBuilder spawnerSpawnParticle(ParticleType particle) {
        this.spawnerData.setSpawnerSpawnParticle(particle);
        return this;
    }


    public SpawnerDataBuilder particleDensity(ParticleDensity density) {
        this.spawnerData.setParticleDensity(density);
        return this;
    }


    public SpawnerDataBuilder particleEffectBoostedOnly() {
        return particleEffectBoostedOnly(true);
    }


    public SpawnerDataBuilder particleEffectBoostedOnly(boolean boostedOnly) {
        this.spawnerData.setParticleEffectBoostedOnly(boostedOnly);
        return this;
    }


    public SpawnerData build() {
        return spawnerData;
    }

}
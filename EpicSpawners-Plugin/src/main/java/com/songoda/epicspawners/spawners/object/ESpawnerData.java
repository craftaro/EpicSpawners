package com.songoda.epicspawners.spawners.object;

import com.google.common.base.Preconditions;
import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.api.particles.ParticleDensity;
import com.songoda.epicspawners.api.particles.ParticleEffect;
import com.songoda.epicspawners.api.particles.ParticleType;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.spawners.object.option.*;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/*
 * This class is used to store actual spawner types.
 * For an example this will contain all spawners
 * in the spawners yaml.
 *
 * This object will be held inside the SpawnerStack
 * class when used in the game world.
 */
public class ESpawnerData implements SpawnerData {

    private final String name;
    private double pickupCost = 0.0;
    private List<Material> spawnBlocks = Collections.singletonList(Material.DIRT);
    private boolean active = true, inShop = true;
    private boolean spawnOnFire = false, upgradeable = true, convertible = true;
    private double shopPrice = 1000.0;
    private String convertRatio = "45%";
    private double upgradeCostEconomy = 0.0;
    private int upgradeCostExperience = 0;
    private int killGoal = 0;
    private String displayName;
    private Material displayItem = null;

    private String tickRate = "800:200";

    private ParticleEffect particleEffect = ParticleEffect.HALO;
    private ParticleType spawnEffectParticle = ParticleType.REDSTONE;
    private ParticleType entitySpawnParticle = ParticleType.SMOKE;
    private ParticleType spawnerSpawnParticle = ParticleType.FIRE;

    private ParticleDensity particleDensity = ParticleDensity.NORMAL;

    private boolean particleEffectBoostedOnly = true;

    private List<ItemStack> itemDrops;
    private List<EntityType> entities;
    private List<Material> blocks;
    private List<ItemStack> items;
    private List<String> commands;

    private Set<SpawnOption> spawnOptions = new HashSet<>();

    public ESpawnerData(String name, List<EntityType> entities, List<Material> blocks, List<ItemStack> items, List<ItemStack> itemDrops, List<String> commands) {
        Preconditions.checkNotNull(name, "Name cannot be null");

        this.name = name;
        this.displayName = name;

        this.entities = entities;
        this.blocks = blocks;
        this.items = items;
        this.itemDrops = itemDrops;
        this.commands = commands;
        reloadSpawnMethods();
    }

    public void reloadSpawnMethods() {
        spawnOptions.clear();
        if (!entities.isEmpty())
            spawnOptions.add(new SpawnOptionEntity(entities));
        if (!blocks.isEmpty())
            spawnOptions.add(new SpawnOptionBlock(blocks));
        if (!items.isEmpty())
            spawnOptions.add(new SpawnOptionItem(items));
        if (!commands.isEmpty())
            spawnOptions.add(new SpawnOptionCommand(commands));
    }

    /**
     * This method will trigger the spawn method(s)
     * for this spawner type.
     */
    public void spawn(Spawner spawner, SpawnerStack stack) {
        for (SpawnOption spawnOption : spawnOptions) {
            spawnOption.spawn(this, stack, spawner);
        }
    }

    @Override
    public ItemStack toItemStack() {
        return toItemStack(1);
    }

    @Override
    public ItemStack toItemStack(int amount) {
        return toItemStack(amount, 1);
    }

    @Override
    public ItemStack toItemStack(int amount, int stackSize) {
        return EpicSpawnersAPI.newSpawnerItem(this, amount, stackSize);
    }

    @Override
    public String getIdentifyingName() {
        return name;
    }

    @Override
    public double getPickupCost() {
        return pickupCost;
    }

    @Override
    public void setPickupCost(double pickupCost) {
        this.pickupCost = pickupCost;
    }

    @Override
    public Material[] getSpawnBlocks() { //ToDO: Why isnt this used?
        return (Material[]) spawnBlocks.toArray();
    }

    public void setSpawnBlocks(String[] spawnBlock) {
        this.spawnBlocks = new ArrayList<>();
        for (String block : spawnBlock) {
            if (block.toUpperCase().trim().equals("")) continue;
            this.spawnBlocks.add(Material.valueOf(block.toUpperCase().trim()));
        }
    }

    @Override
    public List<Material> getSpawnBlocksList() {
        return spawnBlocks;
    }

    @Override
    public void setSpawnBlocks(List<Material> spawnBlock) {
        this.spawnBlocks = spawnBlock;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isInShop() {
        return inShop;
    }

    @Override
    public void setInShop(boolean inShop) {
        this.inShop = inShop;
    }

    @Override
    public boolean isSpawnOnFire() {
        return spawnOnFire;
    }

    @Override
    public void setSpawnOnFire(boolean spawnOnFire) {
        this.spawnOnFire = spawnOnFire;
    }

    @Override
    public boolean isUpgradeable() {
        return upgradeable;
    }

    @Override
    public void setUpgradeable(boolean upgradeable) {
        this.upgradeable = upgradeable;
    }

    @Override
    public boolean isConvertible() {
        return convertible;
    }

    @Override
    public void setConvertible(boolean convertible) {
        this.convertible = convertible;
    }

    @Override
    public double getShopPrice() {
        return shopPrice;
    }

    @Override
    public void setShopPrice(double shopPrice) {
        this.shopPrice = shopPrice;
    }

    @Override
    public String getConvertRatio() {
        return convertRatio;
    }

    @Override
    public void setConvertRatio(String convertRatio) {
        this.convertRatio = convertRatio;
    }

    @Override
    public double getUpgradeCostEconomy() {
        return upgradeCostEconomy;
    }

    @Override
    public void setUpgradeCostEconomy(double upgradeCostEconomy) {
        this.upgradeCostEconomy = upgradeCostEconomy;
    }

    @Override
    public int getUpgradeCostExperience() {
        return upgradeCostExperience;
    }

    @Override
    public void setUpgradeCostExperience(int upgradeCostExperience) {
        this.upgradeCostExperience = upgradeCostExperience;
    }

    @Override
    public int getKillGoal() {
        return killGoal;
    }

    @Override
    public void setKillGoal(int killGoal) {
        this.killGoal = killGoal;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public Material getDisplayItem() {
        if (displayItem == Material.AIR)
            return Material.DIRT;
        else
            return displayItem;
    }

    @Override
    public void setDisplayItem(Material displayItem) {
        this.displayItem = displayItem;
    }

    @Override
    public List<EntityType> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    @Override
    public void setEntities(List<EntityType> entities) {
        this.entities = entities;
    }

    @Override
    public double getConvertPrice() {
        return (int) (shopPrice * (Double.valueOf(convertRatio.substring(0, convertRatio.length() - 1)) / 100.0f));
    }

    @Override
    public List<Material> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    @Override
    public void setBlocks(List<Material> blocks) {
        this.blocks = blocks;
    }

    @Override
    public List<ItemStack> getEntityDroppedItems() {
        return itemDrops;
    }

    @Override
    public void setEntityDroppedItems(List<ItemStack> itemDrops) {
        this.itemDrops = itemDrops;
    }

    @Override
    public List<ItemStack> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public void setItems(List<ItemStack> items) {
        this.items = items;
    }

    @Override
    public List<String> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    @Override
    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    @Override
    public String getTickRate() {
        return tickRate;
    }

    @Override
    public void setTickRate(String tickRate) {
        this.tickRate = tickRate;
    }

    @Override
    public ParticleEffect getParticleEffect() {
        return particleEffect;
    }

    @Override
    public void setParticleEffect(ParticleEffect particleEffect) {
        this.particleEffect = particleEffect;
    }

    @Override
    public ParticleType getSpawnEffectParticle() {
        return spawnEffectParticle;
    }

    @Override
    public void setSpawnEffectParticle(ParticleType spawnEffectParticle) {
        this.spawnEffectParticle = spawnEffectParticle;
    }

    @Override
    public ParticleType getEntitySpawnParticle() {
        return entitySpawnParticle;
    }

    @Override
    public void setEntitySpawnParticle(ParticleType entitySpawnParticle) {
        this.entitySpawnParticle = entitySpawnParticle;
    }

    @Override
    public ParticleType getSpawnerSpawnParticle() {
        return spawnerSpawnParticle;
    }

    @Override
    public void setSpawnerSpawnParticle(ParticleType spawnerSpawnParticle) {
        this.spawnerSpawnParticle = spawnerSpawnParticle;
    }

    @Override
    public ParticleDensity getParticleDensity() {
        return particleDensity;
    }

    @Override
    public void setParticleDensity(ParticleDensity particleDensity) {
        this.particleDensity = particleDensity;
    }

    @Override
    public boolean isParticleEffectBoostedOnly() {
        return particleEffectBoostedOnly;
    }

    @Override
    public void setParticleEffectBoostedOnly(boolean particleEffectBoostedOnly) {
        this.particleEffectBoostedOnly = particleEffectBoostedOnly;
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof ESpawnerData)) return false;

        ESpawnerData other = (ESpawnerData) object;
        return Objects.equals(name, other.name);
    }
}

package com.songoda.epicspawners.spawners.object;

import com.google.common.base.Preconditions;
import com.songoda.epicspawners.particles.ParticleAmount;
import com.songoda.epicspawners.particles.ParticleEffect;
import com.songoda.epicspawners.particles.ParticleType;
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
 * class.
 */
public class SpawnerData {

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

    private final String name;
    private Material displayItem = null;

    private String tickRate = "800:200";

    private ParticleEffect particleEffect = ParticleEffect.HALO;
    private ParticleType spawnEffectParticle = ParticleType.REDSTONE;
    private ParticleType entitySpawnParticle = ParticleType.SMOKE;
    private ParticleType spawnerSpawnParticle = ParticleType.FIRE;

    private ParticleAmount particleAmount = ParticleAmount.NORMAL;

    private boolean particleEffectBoostedOnly = true;

    private List<ItemStack> itemDrops;
    private List<EntityType> entities;
    private List<Material> blocks;
    private List<ItemStack> items;
    private List<String> commands;

    private Set<SpawnOption> spawnOptions = new HashSet<>();

    public SpawnerData(String name, List<EntityType> entities, List<Material> blocks, List<ItemStack> items, List<ItemStack> itemDrops, List<String> commands) {
        Preconditions.checkNotNull(name, "Name cannot be null");

        this.name = name;
        this.displayName = name;

        this.entities = entities;
        this.blocks = blocks;
        this.items = items;
        this.itemDrops = itemDrops;
        this.commands = commands;
        reload();
    }

    public void reload() {
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

    public String getName() {
        return name;
    }

    public double getPickupCost() {
        return pickupCost;
    }

    public void setPickupCost(double pickupCost) {
        this.pickupCost = pickupCost;
    }

    public Material[] getSpawnBlocks() { //ToDO: Why isnt this used?
        return (Material[]) spawnBlocks.toArray();
    }

    public List<Material> getSpawnBlocksList() {
        return spawnBlocks;
    }

    public void setSpawnBlocks(List<Material> spawnBlock) {
        this.spawnBlocks = spawnBlock;
    }

    public void setSpawnBlocks(String[] spawnBlock) {
        this.spawnBlocks = new ArrayList<>();
        for (String block : spawnBlock) {
            if (block.toUpperCase().trim().equals("")) continue;
            this.spawnBlocks.add(Material.valueOf(block.toUpperCase().trim()));
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isInShop() {
        return inShop;
    }

    public void setInShop(boolean inShop) {
        this.inShop = inShop;
    }

    public boolean isSpawnOnFire() {
        return spawnOnFire;
    }

    public void setSpawnOnFire(boolean spawnOnFire) {
        this.spawnOnFire = spawnOnFire;
    }

    public boolean isUpgradeable() {
        return upgradeable;
    }

    public void setUpgradeable(boolean upgradeable) {
        this.upgradeable = upgradeable;
    }

    public boolean isConvertible() {
        return convertible;
    }

    public void setConvertible(boolean convertible) {
        this.convertible = convertible;
    }

    public double getShopPrice() {
        return shopPrice;
    }

    public void setShopPrice(double shopPrice) {
        this.shopPrice = shopPrice;
    }

    public String getConvertRatio() {
        return convertRatio;
    }

    public void setConvertRatio(String convertRatio) {
        this.convertRatio = convertRatio;
    }

    public double getUpgradeCostEconomy() {
        return upgradeCostEconomy;
    }

    public void setUpgradeCostEconomy(double upgradeCostEconomy) {
        this.upgradeCostEconomy = upgradeCostEconomy;
    }

    public int getUpgradeCostExperience() {
        return upgradeCostExperience;
    }

    public void setUpgradeCostExperience(int upgradeCostExperience) {
        this.upgradeCostExperience = upgradeCostExperience;
    }

    public int getKillGoal() {
        return killGoal;
    }

    public void setKillGoal(int killGoal) {
        this.killGoal = killGoal;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Material getDisplayItem() {
        return displayItem;
    }

    public void setDisplayItem(Material displayItem) {
        this.displayItem = displayItem;
    }

    public List<EntityType> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    public double getConvertPrice() {
        return (int) (shopPrice * (Double.valueOf(convertRatio.substring(0, convertRatio.length() - 1)) / 100.0f));
    }

    public void setEntities(List<EntityType> entities) {
        this.entities = entities;
    }

    public List<Material> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    public void setBlocks(List<Material> blocks) {
        this.blocks = blocks;
    }

    public List<ItemStack> getItemDrops() {
        return itemDrops;
    }

    public void setItemDrops(List<ItemStack> itemDrops) {
        this.itemDrops = itemDrops;
    }

    public List<ItemStack> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
    }

    public List<String> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public String getTickRate() {
        return tickRate;
    }

    public void setTickRate(String tickRate) {
        this.tickRate = tickRate;
    }

    public ParticleEffect getParticleEffect() {
        return particleEffect;
    }

    public void setParticleEffect(ParticleEffect particleEffect) {
        this.particleEffect = particleEffect;
    }

    public ParticleType getSpawnEffectParticle() {
        return spawnEffectParticle;
    }

    public void setSpawnEffectParticle(ParticleType spawnEffectParticle) {
        this.spawnEffectParticle = spawnEffectParticle;
    }

    public ParticleType getEntitySpawnParticle() {
        return entitySpawnParticle;
    }

    public void setEntitySpawnParticle(ParticleType entitySpawnParticle) {
        this.entitySpawnParticle = entitySpawnParticle;
    }

    public ParticleType getSpawnerSpawnParticle() {
        return spawnerSpawnParticle;
    }

    public void setSpawnerSpawnParticle(ParticleType spawnerSpawnParticle) {
        this.spawnerSpawnParticle = spawnerSpawnParticle;
    }

    public ParticleAmount getParticleAmount() {
        return particleAmount;
    }

    public void setParticleAmount(ParticleAmount particleAmount) {
        this.particleAmount = particleAmount;
    }

    public boolean isParticleEffectBoostedOnly() {
        return particleEffectBoostedOnly;
    }

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
        if (!(object instanceof SpawnerData)) return false;

        SpawnerData other = (SpawnerData) object;
        return Objects.equals(name, other.name);
    }
}

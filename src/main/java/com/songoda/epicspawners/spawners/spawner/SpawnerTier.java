package com.songoda.epicspawners.spawners.spawner;

import com.google.common.base.Preconditions;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.third_party.de.tr7zw.nbtapi.NBTItem;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.particles.ParticleDensity;
import com.songoda.epicspawners.particles.ParticleEffect;
import com.songoda.epicspawners.particles.ParticleType;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.condition.SpawnCondition;
import com.songoda.epicspawners.spawners.spawner.option.SpawnOption;
import com.songoda.epicspawners.spawners.spawner.option.SpawnOptionBlock;
import com.songoda.epicspawners.spawners.spawner.option.SpawnOptionCommand;
import com.songoda.epicspawners.spawners.spawner.option.SpawnOptionEntity;
import com.songoda.epicspawners.spawners.spawner.option.SpawnOptionItem;
import com.songoda.epicspawners.utils.CostType;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SpawnerTier {

    private final SpawnerData spawnerData;
    private String identifyingName;
    private String displayName;

    private double pickupCost = 0.0;
    private double costEconomy = 1000.0;
    private int CostLevels = 2;

    private List<CompatibleMaterial> spawnBlocks = Collections.singletonList(CompatibleMaterial.AIR);
    private boolean spawnOnFire = false;
    private int spawnLimit = -1;
    private short pickDamage = 1;
    private CompatibleMaterial displayItem = null;

    private String tickRate = "800:200";

    private ParticleEffect particleEffect = ParticleEffect.HALO;
    private ParticleType spawnEffectParticle = ParticleType.REDSTONE;
    private ParticleType entitySpawnParticle = ParticleType.SMOKE;
    private ParticleType spawnerSpawnParticle = ParticleType.FIRE;

    private ParticleDensity particleDensity = ParticleDensity.NORMAL;

    private boolean particleEffectBoostedOnly = true;

    private List<EntityType> entities = new ArrayList<>();
    private List<CompatibleMaterial> blocks = new ArrayList<>();
    private List<ItemStack> items = new ArrayList<>();
    private List<String> commands = new ArrayList<>();

    private final Set<SpawnOption> spawnOptions = new HashSet<>();

    private final List<SpawnCondition> spawnConditions = new ArrayList<>();

    public SpawnerTier(SpawnerData spawnerData) {

        List<SpawnerTier> tiers = spawnerData.getTiers();

        this.identifyingName = "Tier_" + (tiers.isEmpty() ? "1" : Integer.parseInt(tiers.get(tiers.size() - 1).getIdentifyingName().split("_")[1]) + 1);
        this.spawnerData = spawnerData;

        this.displayName = identifyingName;
        reloadSpawnMethods();
    }

    public void reloadSpawnMethods() {
        spawnOptions.clear();
        if (!entities.isEmpty()) spawnOptions.add(new SpawnOptionEntity(entities));
        if (!blocks.isEmpty()) spawnOptions.add(new SpawnOptionBlock(blocks));
        if (!items.isEmpty()) spawnOptions.add(new SpawnOptionItem(items));
        if (!commands.isEmpty()) spawnOptions.add(new SpawnOptionCommand(commands));
    }

    public void spawn(PlacedSpawner spawner, SpawnerStack stack) {
        for (SpawnOption spawnOption : spawnOptions) {
            spawnOption.spawn(this, stack, spawner);
        }
    }

    public ItemStack toItemStack() {
        return toItemStack(1);
    }

    public ItemStack toItemStack(int amount) {
        return toItemStack(amount, 1);
    }

    public ItemStack toItemStack(int amount, int stackSize) {
        Preconditions.checkArgument(stackSize >= 0, "Stack size must be greater than or equal to 0");

        ItemStack item = GuiUtils.createButtonItem(CompatibleMaterial.SPAWNER, getCompiledDisplayName(false, stackSize));
        item.setAmount(amount);

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("data", spawnerData.getIdentifyingName());
        nbtItem.setString("tier", identifyingName);
        nbtItem.setInteger("size", stackSize);

        return nbtItem.getItem();
    }

    public String getIdentifyingName() {
        return identifyingName;
    }

    public String getFullyIdentifyingName() {
        return spawnerData.getIdentifyingName() + identifyingName;
    }

    public void setIdentifyingName(String identifyingName) {
        this.identifyingName = identifyingName;
    }

    public double getPickupCost() {
        return pickupCost;
    }

    public void setPickupCost(double pickupCost) {
        this.pickupCost = pickupCost;
    }

    public CompatibleMaterial[] getSpawnBlocks() {
        return spawnBlocks.toArray(new CompatibleMaterial[spawnBlocks.size()]);
    }

    public void setSpawnBlocks(List<CompatibleMaterial> spawnBlock) {
        this.spawnBlocks = spawnBlock;
    }

    public List<CompatibleMaterial> getSpawnBlocksList() {
        return Collections.unmodifiableList(spawnBlocks);
    }

    public boolean isSpawnOnFire() {
        return spawnOnFire;
    }

    public void setSpawnOnFire(boolean spawnOnFire) {
        this.spawnOnFire = spawnOnFire;
    }

    public double getCostEconomy() {
        return costEconomy;
    }

    public void setCostEconomy(double costEconomy) {
        this.costEconomy = costEconomy;
    }

    public int getCostLevels() {
        return CostLevels;
    }

    public void setCostLevels(int costLevels) {
        this.CostLevels = costLevels;
    }

    public double getUpgradeCost(CostType type) {
        double cost = 0;
        if (type == CostType.ECONOMY)
            cost = getCostEconomy();
        else if (type == CostType.LEVELS)
            cost = getCostLevels();
        return cost;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCompiledDisplayName() {
        return getCompiledDisplayName(false, 1);
    }

    public String getCompiledDisplayName(boolean omni) {
        return getCompiledDisplayName(omni, 1);
    }

    public String getCompiledDisplayName(boolean omni, int stackSize) {
        String nameFormat = Settings.NAME_FORMAT.getString();
        String displayName = getDisplayName();

        nameFormat = nameFormat.replace("{TYPE}", omni ?
                EpicSpawners.getInstance().getLocale().getMessage("general.nametag.omni").getMessage() : displayName);

        if (stackSize > 1 || Settings.DISPLAY_TIER_ONE.getBoolean() && stackSize >= 0) {
            nameFormat = nameFormat.replace("{AMT}", Integer.toString(stackSize))
                    .replace("[", "").replace("]", "");
        } else {
            nameFormat = nameFormat.replaceAll("\\[.*?]", "");
        }

        return TextUtils.formatText(nameFormat).trim();
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public CompatibleMaterial getDisplayItem() {

        return displayItem == null ? CompatibleMaterial.AIR : displayItem;
    }

    public void setDisplayItem(CompatibleMaterial displayItem) {
        this.displayItem = displayItem;
    }

    public List<EntityType> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    public void setEntities(List<EntityType> entities) {
        this.entities = entities;
    }

    public List<CompatibleMaterial> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    public void setBlocks(List<CompatibleMaterial> blocks) {
        this.blocks = blocks;
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

    public short getPickDamage() {
        return pickDamage;
    }

    public void setPickDamage(short pickDamage) {
        this.pickDamage = pickDamage;
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

    public void setSpawnLimit(int spawnLimit) {
        this.spawnLimit = spawnLimit;
    }

    public int getSpawnLimit() {
        return spawnLimit;
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

    public ParticleDensity getParticleDensity() {
        return particleDensity;
    }

    public void setParticleDensity(ParticleDensity particleDensity) {
        this.particleDensity = particleDensity;
    }

    public boolean isParticleEffectBoostedOnly() {
        return particleEffectBoostedOnly;
    }

    public void setParticleEffectBoostedOnly(boolean particleEffectBoostedOnly) {
        this.particleEffectBoostedOnly = particleEffectBoostedOnly;
    }

    public void addCondition(SpawnCondition spawnCondition) {
        spawnConditions.add(spawnCondition);
    }

    public void removeCondition(SpawnCondition spawnCondition) {
        spawnConditions.remove(spawnCondition);
    }

    public List<SpawnCondition> getConditions() {
        return Collections.unmodifiableList(spawnConditions);
    }

    public String toString() {
        return "SpawnerData:{Name:\"" + identifyingName + "\"}";
    }

    public int getStackSize(ItemStack item) {
        Preconditions.checkNotNull(item, "Cannot get stack size of null item");
        NBTItem nbtItem = new NBTItem(item);

        if (nbtItem.hasKey("size"))
            return nbtItem.getInteger("size");

        // Legacy
        if (!item.hasItemMeta() && !item.getItemMeta().hasDisplayName()) return 1;

        String name = item.getItemMeta().getDisplayName();
        if (!name.contains(":")) return 1;

        String amount = name.replace(String.valueOf(ChatColor.COLOR_CHAR), "").replace(";", "").split(":")[1];
        if (amount == null) {
            return 1;
        }

        return Integer.parseInt(amount);
    }

    public String getGuiTitle() {
        if (spawnerData.getTiers().size() == 1)
            return TextUtils.formatText("&e" + displayName);
        else
            return TextUtils.formatText("&e" + spawnerData.getIdentifyingName() + " &8(" + displayName + ")");
    }

    public SpawnerData getSpawnerData() {
        return spawnerData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpawnerTier tier = (SpawnerTier) o;
        return Objects.equals(spawnerData, tier.spawnerData) &&
                Objects.equals(identifyingName, tier.identifyingName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spawnerData, identifyingName);
    }
}

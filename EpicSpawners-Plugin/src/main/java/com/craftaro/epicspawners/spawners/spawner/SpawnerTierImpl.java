package com.craftaro.epicspawners.spawners.spawner;

import com.craftaro.core.gui.GuiUtils;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.third_party.de.tr7zw.nbtapi.NBTItem;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.particles.ParticleDensity;
import com.craftaro.epicspawners.api.particles.ParticleEffect;
import com.craftaro.epicspawners.api.particles.ParticleType;
import com.craftaro.epicspawners.api.spawners.condition.SpawnCondition;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerStack;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.api.spawners.spawner.option.SpawnOption;
import com.craftaro.epicspawners.api.utils.CostType;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.epicspawners.spawners.spawner.option.SpawnOptionBlock;
import com.craftaro.epicspawners.spawners.spawner.option.SpawnOptionCommand;
import com.craftaro.epicspawners.spawners.spawner.option.SpawnOptionEntity;
import com.craftaro.epicspawners.spawners.spawner.option.SpawnOptionItem;
import com.google.common.base.Preconditions;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SpawnerTierImpl implements SpawnerTier {
    private final SpawnerData spawnerData;
    private String identifyingName;
    private String displayName;

    private double pickupCost = 0.0;
    private double costEconomy = 1000.0;
    private int CostLevels = 2;

    private List<XMaterial> spawnBlocks = Collections.singletonList(XMaterial.AIR);
    private boolean spawnOnFire = false;
    private int spawnLimit = -1;
    private short pickDamage = 1;
    private XMaterial displayItem = null;

    private String tickRate = "800:200";

    private ParticleEffect particleEffect = ParticleEffect.HALO;
    private ParticleType spawnEffectParticle = ParticleType.REDSTONE;
    private ParticleType entitySpawnParticle = ParticleType.SMOKE;
    private ParticleType spawnerSpawnParticle = ParticleType.FIRE;

    private ParticleDensity particleDensity = ParticleDensity.NORMAL;

    private boolean particleEffectBoostedOnly = true;

    private List<EntityType> entities = new ArrayList<>();
    private List<XMaterial> blocks = new ArrayList<>();
    private List<ItemStack> items = new ArrayList<>();
    private List<String> commands = new ArrayList<>();

    private final Set<SpawnOption> spawnOptions = new HashSet<>();

    private final List<SpawnCondition> spawnConditions = new ArrayList<>();

    public SpawnerTierImpl(SpawnerData spawnerData) {

        List<SpawnerTier> tiers = spawnerData.getTiers();

        this.identifyingName = "Tier_" + (tiers.isEmpty() ? "1" : Integer.parseInt(tiers.get(tiers.size() - 1).getIdentifyingName().split("_")[1]) + 1);
        this.spawnerData = spawnerData;

        this.displayName = this.identifyingName;
        reloadSpawnMethods();
    }

    @Override
    public void reloadSpawnMethods() {
        this.spawnOptions.clear();
        if (!this.entities.isEmpty()) {
            this.spawnOptions.add(new SpawnOptionEntity(this.entities));
        }
        if (!this.blocks.isEmpty()) {
            this.spawnOptions.add(new SpawnOptionBlock(this.blocks));
        }
        if (!this.items.isEmpty()) {
            this.spawnOptions.add(new SpawnOptionItem(this.items));
        }
        if (!this.commands.isEmpty()) {
            this.spawnOptions.add(new SpawnOptionCommand(this.commands));
        }
    }

    @Override
    public void spawn(PlacedSpawner spawner, SpawnerStack stack) {
        for (SpawnOption spawnOption : this.spawnOptions) {
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
        Preconditions.checkArgument(stackSize >= 0, "Stack size must be greater than or equal to 0");

        ItemStack item = GuiUtils.createButtonItem(XMaterial.SPAWNER, getCompiledDisplayName(false, stackSize));
        item.setAmount(amount);

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("data", this.spawnerData.getIdentifyingName());
        nbtItem.setString("tier", this.identifyingName);
        nbtItem.setInteger("size", stackSize);

        return nbtItem.getItem();
    }

    @Override
    public String getIdentifyingName() {
        return this.identifyingName;
    }

    @Override
    public String getFullyIdentifyingName() {
        return this.spawnerData.getIdentifyingName() + this.identifyingName;
    }

    @Override
    public void setIdentifyingName(String identifyingName) {
        this.identifyingName = identifyingName;
    }

    @Override
    public double getPickupCost() {
        return this.pickupCost;
    }

    @Override
    public void setPickupCost(double pickupCost) {
        this.pickupCost = pickupCost;
    }

    @Override
    public XMaterial[] getSpawnBlocks() {
        return this.spawnBlocks.toArray(new XMaterial[0]);
    }

    @Override
    public void setSpawnBlocks(List<XMaterial> spawnBlock) {
        this.spawnBlocks = spawnBlock;
    }

    @Override
    public List<XMaterial> getSpawnBlocksList() {
        return Collections.unmodifiableList(this.spawnBlocks);
    }

    @Override
    public boolean isSpawnOnFire() {
        return this.spawnOnFire;
    }

    @Override
    public void setSpawnOnFire(boolean spawnOnFire) {
        this.spawnOnFire = spawnOnFire;
    }

    @Override
    public double getCostEconomy() {
        return this.costEconomy;
    }

    @Override
    public void setCostEconomy(double costEconomy) {
        this.costEconomy = costEconomy;
    }

    @Override
    public int getCostLevels() {
        return this.CostLevels;
    }

    @Override
    public void setCostLevels(int costLevels) {
        this.CostLevels = costLevels;
    }

    @Override
    public double getUpgradeCost(CostType type) {
        double cost = 0;
        if (type == CostType.ECONOMY) {
            cost = getCostEconomy();
        } else if (type == CostType.LEVELS) {
            cost = getCostLevels();
        }
        return cost;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public String getCompiledDisplayName() {
        return getCompiledDisplayName(false, 1);
    }

    @Override
    public String getCompiledDisplayName(boolean omni) {
        return getCompiledDisplayName(omni, 1);
    }

    @Override
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

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public XMaterial getDisplayItem() {
        return this.displayItem == null ? XMaterial.AIR : this.displayItem;
    }

    @Override
    public void setDisplayItem(XMaterial displayItem) {
        this.displayItem = displayItem;
    }

    @Override
    public List<EntityType> getEntities() {
        return Collections.unmodifiableList(this.entities);
    }

    @Override
    public void setEntities(List<EntityType> entities) {
        this.entities = entities;
    }

    @Override
    public List<XMaterial> getBlocks() {
        return Collections.unmodifiableList(this.blocks);
    }

    @Override
    public void setBlocks(List<XMaterial> blocks) {
        this.blocks = blocks;
    }

    @Override
    public List<ItemStack> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    @Override
    public void setItems(List<ItemStack> items) {
        this.items = items;
    }

    @Override
    public List<String> getCommands() {
        return Collections.unmodifiableList(this.commands);
    }

    @Override
    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    @Override
    public short getPickDamage() {
        return this.pickDamage;
    }

    @Override
    public void setPickDamage(short pickDamage) {
        this.pickDamage = pickDamage;
    }

    @Override
    public String getTickRate() {
        return this.tickRate;
    }

    @Override
    public void setTickRate(String tickRate) {
        this.tickRate = tickRate;
    }

    @Override
    public ParticleEffect getParticleEffect() {
        return this.particleEffect;
    }

    @Override
    public void setParticleEffect(ParticleEffect particleEffect) {
        this.particleEffect = particleEffect;
    }

    @Override
    public ParticleType getSpawnEffectParticle() {
        return this.spawnEffectParticle;
    }

    @Override
    public void setSpawnEffectParticle(ParticleType spawnEffectParticle) {
        this.spawnEffectParticle = spawnEffectParticle;
    }

    @Override
    public void setSpawnLimit(int spawnLimit) {
        this.spawnLimit = spawnLimit;
    }

    @Override
    public int getSpawnLimit() {
        return this.spawnLimit;
    }

    @Override
    public ParticleType getEntitySpawnParticle() {
        return this.entitySpawnParticle;
    }

    @Override
    public void setEntitySpawnParticle(ParticleType entitySpawnParticle) {
        this.entitySpawnParticle = entitySpawnParticle;
    }

    @Override
    public ParticleType getSpawnerSpawnParticle() {
        return this.spawnerSpawnParticle;
    }

    @Override
    public void setSpawnerSpawnParticle(ParticleType spawnerSpawnParticle) {
        this.spawnerSpawnParticle = spawnerSpawnParticle;
    }

    @Override
    public ParticleDensity getParticleDensity() {
        return this.particleDensity;
    }

    @Override
    public void setParticleDensity(ParticleDensity particleDensity) {
        this.particleDensity = particleDensity;
    }

    @Override
    public boolean isParticleEffectBoostedOnly() {
        return this.particleEffectBoostedOnly;
    }

    @Override
    public void setParticleEffectBoostedOnly(boolean particleEffectBoostedOnly) {
        this.particleEffectBoostedOnly = particleEffectBoostedOnly;
    }

    @Override
    public void addCondition(SpawnCondition spawnCondition) {
        this.spawnConditions.add(spawnCondition);
    }

    @Override
    public void removeCondition(SpawnCondition spawnCondition) {
        this.spawnConditions.remove(spawnCondition);
    }

    @Override
    public List<SpawnCondition> getConditions() {
        return Collections.unmodifiableList(this.spawnConditions);
    }

    @Override
    public String toString() {
        return "SpawnerData:{Name:\"" + this.identifyingName + "\"}";
    }

    @Override
    public int getStackSize(ItemStack item) {
        Preconditions.checkNotNull(item, "Cannot get stack size of null item");
        NBTItem nbtItem = new NBTItem(item);

        if (nbtItem.hasTag("size")) {
            return nbtItem.getInteger("size");
        }

        // Legacy
        if (!item.hasItemMeta() && !item.getItemMeta().hasDisplayName()) {
            return 1;
        }

        String name = item.getItemMeta().getDisplayName();
        if (!name.contains(":")) {
            return 1;
        }

        String amount = name.replace(String.valueOf(ChatColor.COLOR_CHAR), "").replace(";", "").split(":")[1];
        if (amount == null) {
            return 1;
        }

        return Integer.parseInt(amount);
    }

    @Override
    public String getGuiTitle() {
        if (this.spawnerData.getTiers().size() == 1) {
            return TextUtils.formatText("&e" + this.displayName);
        } else {
            return TextUtils.formatText("&e" + this.spawnerData.getIdentifyingName() + " &8(" + this.displayName + ")");
        }
    }

    @Override
    public SpawnerData getSpawnerData() {
        return this.spawnerData;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SpawnerTierImpl tier = (SpawnerTierImpl) obj;
        return Objects.equals(this.spawnerData, tier.spawnerData) &&
                Objects.equals(this.identifyingName, tier.identifyingName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.spawnerData, this.identifyingName);
    }
}

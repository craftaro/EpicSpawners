package com.craftaro.epicspawners.api.spawners.spawner;

import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicspawners.api.particles.ParticleDensity;
import com.craftaro.epicspawners.api.particles.ParticleEffect;
import com.craftaro.epicspawners.api.particles.ParticleType;
import com.craftaro.epicspawners.api.spawners.condition.SpawnCondition;
import com.craftaro.epicspawners.api.utils.CostType;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface SpawnerTier {
    void reloadSpawnMethods();

    void spawn(PlacedSpawner spawner, SpawnerStack stack);

    ItemStack toItemStack();

    ItemStack toItemStack(int amount);

    ItemStack toItemStack(int amount, int stackSize);

    String getIdentifyingName();

    String getFullyIdentifyingName();

    void setIdentifyingName(String identifyingName);

    double getPickupCost();

    void setPickupCost(double pickupCost);

    XMaterial[] getSpawnBlocks();

    void setSpawnBlocks(List<XMaterial> spawnBlock);

    List<XMaterial> getSpawnBlocksList();

    boolean isSpawnOnFire();

    void setSpawnOnFire(boolean spawnOnFire);

    double getCostEconomy();

    void setCostEconomy(double costEconomy);

    int getCostLevels();

    void setCostLevels(int costLevels);

    double getUpgradeCost(CostType type);

    String getDisplayName();

    String getCompiledDisplayName();

    String getCompiledDisplayName(boolean omni);

    String getCompiledDisplayName(boolean omni, int stackSize);

    void setDisplayName(String displayName);

    XMaterial getDisplayItem();

    void setDisplayItem(XMaterial displayItem);

    List<EntityType> getEntities();

    void setEntities(List<EntityType> entities);

    List<XMaterial> getBlocks();

    void setBlocks(List<XMaterial> blocks);

    List<ItemStack> getItems();

    void setItems(List<ItemStack> items);

    List<String> getCommands();

    void setCommands(List<String> commands);

    short getPickDamage();

    void setPickDamage(short pickDamage);

    String getTickRate();

    void setTickRate(String tickRate);

    ParticleEffect getParticleEffect();

    void setParticleEffect(ParticleEffect particleEffect);

    ParticleType getSpawnEffectParticle();

    void setSpawnEffectParticle(ParticleType spawnEffectParticle);

    void setSpawnLimit(int spawnLimit);

    int getSpawnLimit();

    ParticleType getEntitySpawnParticle();

    void setEntitySpawnParticle(ParticleType entitySpawnParticle);

    ParticleType getSpawnerSpawnParticle();

    void setSpawnerSpawnParticle(ParticleType spawnerSpawnParticle);

    ParticleDensity getParticleDensity();

    void setParticleDensity(ParticleDensity particleDensity);

    boolean isParticleEffectBoostedOnly();

    void setParticleEffectBoostedOnly(boolean particleEffectBoostedOnly);

    void addCondition(SpawnCondition spawnCondition);

    void removeCondition(SpawnCondition spawnCondition);

    List<SpawnCondition> getConditions();

    int getStackSize(ItemStack item);

    String getGuiTitle();

    SpawnerData getSpawnerData();
}

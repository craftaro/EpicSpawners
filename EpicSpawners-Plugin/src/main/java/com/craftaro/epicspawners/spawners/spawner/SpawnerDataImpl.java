package com.craftaro.epicspawners.spawners.spawner;

import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.spawners.condition.SpawnConditionBiome;
import com.craftaro.epicspawners.spawners.condition.SpawnConditionHeight;
import com.craftaro.epicspawners.spawners.condition.SpawnConditionLightDark;
import com.craftaro.epicspawners.spawners.condition.SpawnConditionNearbyEntities;
import com.craftaro.epicspawners.spawners.condition.SpawnConditionNearbyPlayers;
import com.craftaro.epicspawners.spawners.condition.SpawnConditionStorm;
import org.bukkit.block.Biome;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SpawnerDataImpl implements SpawnerData {
    private final String identifyingName;

    private boolean custom = false;
    private boolean active = true;
    private boolean inShop = true;
    private boolean convertible = true;

    private String convertRatio = "45%";
    private int killDropGoal = 0;
    private int shopOrder = 0;

    private double shopPrice = 1000, killDropChance = 0;

    private final List<SpawnerTier> spawnerTiers = new LinkedList<>();

    private boolean craftable = false;
    private String recipe = "AAAABAAAA";
    private List<String> recipeIngredients = Arrays.asList("A, IRON_BARS", "B, SPAWN_EGG");

    public SpawnerDataImpl(String identifyingName) {
        this.identifyingName = identifyingName;
    }

    @Override
    public String getIdentifyingName() {
        return this.identifyingName;
    }

    @Override
    public boolean isCraftable() {
        return this.craftable;
    }

    @Override
    public void setCraftable(boolean craftable) {
        this.craftable = craftable;
    }

    @Override
    public String getRecipe() {
        return this.recipe;
    }

    @Override
    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    @Override
    public List<String> getRecipeIngredients() {
        return this.recipeIngredients;
    }

    @Override
    public void setRecipeIngredients(List<String> recipeIngredients) {
        this.recipeIngredients = recipeIngredients;
    }

    @Override
    public boolean isCustom() {
        return this.custom;
    }

    @Override
    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isInShop() {
        return this.inShop;
    }

    @Override
    public void setInShop(boolean inShop) {
        this.inShop = inShop;
    }

    @Override
    public double getShopPrice() {
        return this.shopPrice;
    }

    @Override
    public void setShopPrice(double shopPrice) {
        this.shopPrice = shopPrice;
    }

    @Override
    public boolean isConvertible() {
        return this.convertible;
    }

    @Override
    public void setConvertible(boolean convertible) {
        this.convertible = convertible;
    }

    @Override
    public String getConvertRatio() {
        return this.convertRatio;
    }

    @Override
    public void setConvertRatio(String convertRatio) {
        this.convertRatio = convertRatio;
    }

    @Override
    public double getConvertPrice() {
        return getFirstTier().getCostEconomy() * (Double.parseDouble(this.convertRatio.substring(0, this.convertRatio.length() - 1)) / 100.0f);
    }

    @Override
    public int getKillDropGoal() {
        return this.killDropGoal;
    }

    @Override
    public void setKillDropGoal(int killDropGoal) {
        this.killDropGoal = killDropGoal;
    }

    @Override
    public double getKillDropChance() {
        return this.killDropChance;
    }

    @Override
    public void setKillDropChance(double killDropChance) {
        this.killDropChance = killDropChance;
    }

    @Override
    public int getShopOrder() {
        return this.shopOrder;
    }

    @Override
    public void setShopOrder(int slot) {
        this.shopOrder = slot;
    }

    @Override
    public List<SpawnerTier> getTiers() {
        return Collections.unmodifiableList(this.spawnerTiers);
    }

    @Override
    public SpawnerTier getTier(String tierName) {
        tierName = tierName.replace(" ", "_");
        final String finalTierName = tierName;
        return this.spawnerTiers.stream().filter(tier -> tier.getIdentifyingName().replace(" ", "_")
                .equalsIgnoreCase(finalTierName)).findFirst().orElse(null);
    }

    @Override
    public SpawnerTier getTierOrFirst(String tierName) {
        SpawnerTier tier = getTier(tierName);
        return tier == null ? getFirstTier() : tier;
    }

    @Override
    public SpawnerTier getFirstTier() {
        return this.spawnerTiers.get(0);
    }

    @Override
    public SpawnerTier getNextTier(SpawnerTier tier) {
        int idx = this.spawnerTiers.indexOf(tier);

        if (idx < 0 || idx + 1 == this.spawnerTiers.size()) {
            return null;
        }
        return this.spawnerTiers.get(idx + 1);
    }

    @Override
    public void addTier(SpawnerTier tier) {
        this.spawnerTiers.add(tier);
    }

    @Override
    public void removeTier(SpawnerTier tier) {
        this.spawnerTiers.remove(tier);
    }

    @Override
    public void replaceTiers(Collection<SpawnerTier> newTiers) {
        this.spawnerTiers.clear();
        this.spawnerTiers.addAll(newTiers);
    }

    @Override
    public XMaterial getDisplayItem() {
        return getFirstTier().getDisplayItem();
    }

    @Override
    public void reloadSpawnMethods() {
        for (SpawnerTier spawnerTier : this.spawnerTiers) {
            spawnerTier.reloadSpawnMethods();
        }
    }

    @Override
    public void addDefaultTier() {
        SpawnerTier tier = new SpawnerTierImpl(this);
        tier.addCondition(new SpawnConditionNearbyPlayers(16, 1));
        tier.addCondition(new SpawnConditionHeight(0, 265));
        tier.addCondition(new SpawnConditionBiome(Biome.values()));
        tier.addCondition(new SpawnConditionLightDark(SpawnConditionLightDark.Type.BOTH));
        tier.addCondition(new SpawnConditionStorm(false));
        tier.addCondition(new SpawnConditionNearbyEntities(6));
        this.spawnerTiers.add(tier);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return this.identifyingName.equals(((SpawnerDataImpl) obj).identifyingName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.identifyingName);
    }

}

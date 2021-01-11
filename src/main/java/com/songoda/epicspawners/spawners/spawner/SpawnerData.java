package com.songoda.epicspawners.spawners.spawner;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.epicspawners.spawners.condition.SpawnConditionBiome;
import com.songoda.epicspawners.spawners.condition.SpawnConditionHeight;
import com.songoda.epicspawners.spawners.condition.SpawnConditionLightDark;
import com.songoda.epicspawners.spawners.condition.SpawnConditionNearbyEntities;
import com.songoda.epicspawners.spawners.condition.SpawnConditionNearbyPlayers;
import com.songoda.epicspawners.spawners.condition.SpawnConditionStorm;
import org.bukkit.block.Biome;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SpawnerData {

    private final String identifyingName;

    private boolean custom = false, active = true,
            inShop = true, convertible = true;

    private String convertRatio = "45%";
    private int killGoal = 0,
            shopOrder = 0;

    private double shopPrice = 1000;

    private final List<SpawnerTier> spawnerTiers = new LinkedList<>();

    private boolean craftable = false;
    private String recipe = "AAAABAAAA";
    private List<String> recipeIngredients = Arrays.asList("A, IRON_BARS", "B, SPAWN_EGG");

    public SpawnerData(String identifyingName) {
        this.identifyingName = identifyingName;
    }

    public String getIdentifyingName() {
        return identifyingName;
    }

    public boolean isCraftable() {
        return craftable;
    }

    public void setCraftable(boolean craftable) {
        this.craftable = craftable;
    }

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public List<String> getRecipeIngredients() {
        return recipeIngredients;
    }

    public void setRecipeIngredients(List<String> recipeIngredients) {
        this.recipeIngredients = recipeIngredients;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
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

    public double getShopPrice() {
        return shopPrice;
    }

    public void setShopPrice(double shopPrice) {
        this.shopPrice = shopPrice;
    }

    public boolean isConvertible() {
        return convertible;
    }

    public void setConvertible(boolean convertible) {
        this.convertible = convertible;
    }

    public String getConvertRatio() {
        return convertRatio;
    }

    public void setConvertRatio(String convertRatio) {
        this.convertRatio = convertRatio;
    }

    public double getConvertPrice() {
        return (int) (getFirstTier().getCostEconomy() * (Double.parseDouble(convertRatio.substring(0, convertRatio.length() - 1)) / 100.0f));
    }

    public int getKillGoal() {
        return killGoal;
    }

    public void setKillGoal(int killGoal) {
        this.killGoal = killGoal;
    }

    public int getShopOrder() {
        return shopOrder;
    }

    public void setShopOrder(int slot) {
        this.shopOrder = slot;
    }

    public List<SpawnerTier> getTiers() {
        return Collections.unmodifiableList(spawnerTiers);
    }

    public SpawnerTier getTier(String tierName) {
        tierName = tierName.replace(" ", "_");
        final String finalTierName = tierName;
        return spawnerTiers.stream().filter(tier -> tier.getIdentifyingName().replace(" ", "_")
                .equalsIgnoreCase(finalTierName)).findFirst().orElse(null);
    }

    public SpawnerTier getTierOrFirst(String tierName) {
        SpawnerTier tier = getTier(tierName);
        return tier == null ? getFirstTier() : tier;
    }

    public SpawnerTier getFirstTier() {
        return spawnerTiers.get(0);
    }

    public SpawnerTier getNextTier(SpawnerTier tier) {
        int idx = spawnerTiers.indexOf(tier);
        if (idx < 0 || idx + 1 == spawnerTiers.size()) return null;
        return spawnerTiers.get(idx + 1);
    }

    public void addTier(SpawnerTier tier) {
        spawnerTiers.add(tier);
    }

    public void removeTier(SpawnerTier tier) {
        spawnerTiers.remove(tier);
    }

    public void replaceTiers(Collection<SpawnerTier> newTiers) {
        spawnerTiers.clear();
        spawnerTiers.addAll(newTiers);
    }

    public CompatibleMaterial getDisplayItem() {
        return getFirstTier().getDisplayItem();
    }

    public void reloadSpawnMethods() {
        for (SpawnerTier spawnerTier : spawnerTiers)
            spawnerTier.reloadSpawnMethods();
    }

    public void addDefaultTier() {
        SpawnerTier tier = new SpawnerTier(this);
        tier.addCondition(new SpawnConditionNearbyPlayers(16, 1));
        tier.addCondition(new SpawnConditionHeight(0, 265));
        tier.addCondition(new SpawnConditionBiome(Biome.values()));
        tier.addCondition(new SpawnConditionLightDark(SpawnConditionLightDark.Type.BOTH));
        tier.addCondition(new SpawnConditionStorm(false));
        tier.addCondition(new SpawnConditionNearbyEntities(6));
        spawnerTiers.add(tier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpawnerData that = (SpawnerData) o;
        return identifyingName.equals(that.identifyingName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifyingName);
    }

}

package com.craftaro.epicspawners.api.spawners.spawner;

import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;

import java.util.Collection;
import java.util.List;

public interface SpawnerData {
    String getIdentifyingName();

    boolean isCraftable();

    void setCraftable(boolean craftable);

    String getRecipe();

    void setRecipe(String recipe);

    List<String> getRecipeIngredients();

    void setRecipeIngredients(List<String> recipeIngredients);

    boolean isCustom();

    void setCustom(boolean custom);

    boolean isActive();

    void setActive(boolean active);

    boolean isInShop();

    void setInShop(boolean inShop);

    double getShopPrice();

    void setShopPrice(double shopPrice);

    boolean isConvertible();

    void setConvertible(boolean convertible);

    String getConvertRatio();

    void setConvertRatio(String convertRatio);

    double getConvertPrice();

    int getKillDropGoal();

    void setKillDropGoal(int killDropGoal);

    double getKillDropChance();

    void setKillDropChance(double killDropChance);

    int getShopOrder();

    void setShopOrder(int slot);

    List<SpawnerTier> getTiers();

    SpawnerTier getTier(String tierName);

    SpawnerTier getTierOrFirst(String tierName);

    SpawnerTier getFirstTier();

    SpawnerTier getNextTier(SpawnerTier tier);

    void addTier(SpawnerTier tier);

    void removeTier(SpawnerTier tier);

    void replaceTiers(Collection<SpawnerTier> newTiers);

    XMaterial getDisplayItem();

    void reloadSpawnMethods();

    void addDefaultTier();
}

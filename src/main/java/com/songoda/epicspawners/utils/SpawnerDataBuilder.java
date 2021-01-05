package com.songoda.epicspawners.utils;

import com.songoda.epicspawners.spawners.spawner.SpawnerData;

import java.util.List;

public final class SpawnerDataBuilder {

    private final SpawnerData spawnerData;

    public SpawnerDataBuilder(String identifier) {
        this.spawnerData = new SpawnerData(identifier);
        spawnerData.reloadSpawnMethods();
    }

    public SpawnerDataBuilder setCustom(boolean custom) {
        this.spawnerData.setCustom(custom);
        return this;
    }

    public SpawnerDataBuilder setActive(boolean active) {
        this.spawnerData.setActive(active);
        return this;
    }

    public SpawnerDataBuilder setUpgradable(boolean upgradable) {
        this.spawnerData.setUpgradeable(upgradable);
        return this;
    }

    public SpawnerDataBuilder setKillGoal(int killGoal) {
        this.spawnerData.setKillGoal(killGoal);
        return this;
    }

    public SpawnerDataBuilder setConvertable(boolean convertible) {
        this.spawnerData.setConvertible(convertible);
        return this;
    }

    public SpawnerDataBuilder convertRatio(String convertRatio) {
        this.spawnerData.setConvertRatio(convertRatio);
        return this;
    }

    public SpawnerDataBuilder setInShop(boolean inShop) {
        this.spawnerData.setInShop(inShop);
        return this;
    }

    public SpawnerDataBuilder setCraftable(boolean craftable) {
        this.spawnerData.setCraftable(craftable);
        return this;
    }

    public SpawnerDataBuilder setRecipe(String recipe) {
        this.spawnerData.setRecipe(recipe);
        return this;
    }

    public SpawnerDataBuilder setRecipeIngredients(List<String> ingredients) {
        this.spawnerData.setRecipeIngredients(ingredients);
        return this;
    }

    public SpawnerDataBuilder shopOrder(int shopOrder) {
        this.spawnerData.setShopOrder(shopOrder);
        return this;
    }

    public SpawnerDataBuilder shopPrice(double shopPrice) {
        this.spawnerData.setShopPrice(shopPrice);
        return this;
    }

    public SpawnerData build() {
        return spawnerData;
    }

}
package com.craftaro.epicspawners.utils;

import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.api.utils.SpawnerDataBuilder;
import com.craftaro.epicspawners.spawners.spawner.SpawnerDataImpl;

import java.util.List;

public final class SpawnerDataBuilderImpl implements SpawnerDataBuilder {
    private final SpawnerData spawnerData;

    public SpawnerDataBuilderImpl(String identifier) {
        this.spawnerData = new SpawnerDataImpl(identifier);
        this.spawnerData.reloadSpawnMethods();
    }

    @Override
    public SpawnerDataBuilder newBuilder(String identifier) {
        return new SpawnerDataBuilderImpl(identifier);
    }

    public SpawnerDataBuilder setCustom(boolean custom) {
        this.spawnerData.setCustom(custom);
        return this;
    }

    public SpawnerDataBuilder setActive(boolean active) {
        this.spawnerData.setActive(active);
        return this;
    }

    public SpawnerDataBuilder setKillDropGoal(int goal) {
        this.spawnerData.setKillDropGoal(goal);
        return this;
    }

    public SpawnerDataBuilder setKillDropChance(double chance) {
        this.spawnerData.setKillDropChance(chance);
        return this;
    }

    public SpawnerDataBuilder setConvertible(boolean convertible) {
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
        return this.spawnerData;
    }
}

package com.craftaro.epicspawners.api.utils;

import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;

import java.util.List;

public interface SpawnerDataBuilder {
    SpawnerDataBuilder newBuilder(String identifier);

    SpawnerDataBuilder setCustom(boolean custom);

    SpawnerDataBuilder setActive(boolean active);

    SpawnerDataBuilder setKillDropGoal(int goal);

    SpawnerDataBuilder setKillDropChance(double chance);

    SpawnerDataBuilder setConvertible(boolean convertible);

    SpawnerDataBuilder convertRatio(String convertRatio);

    SpawnerDataBuilder setInShop(boolean inShop);

    SpawnerDataBuilder setCraftable(boolean craftable);

    SpawnerDataBuilder setRecipe(String recipe);

    SpawnerDataBuilder setRecipeIngredients(List<String> ingredients);

    SpawnerDataBuilder shopOrder(int shopOrder);

    SpawnerDataBuilder shopPrice(double shopPrice);

    SpawnerData build();
}

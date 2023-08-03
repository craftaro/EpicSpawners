package com.craftaro.epicspawners.api.spawners.spawner.option;

import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerStack;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;

public interface SpawnOption {
    void spawn(SpawnerTier data, SpawnerStack stack, PlacedSpawner spawner);

    SpawnOptionType getType();
}

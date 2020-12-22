package com.songoda.epicspawners.spawners.spawner.option;

import com.songoda.epicspawners.spawners.spawner.PlacedSpawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.spawners.spawner.SpawnerTier;

public interface SpawnOption {

    void spawn(SpawnerTier data, SpawnerStack stack, PlacedSpawner spawner);

    SpawnOptionType getType();

}

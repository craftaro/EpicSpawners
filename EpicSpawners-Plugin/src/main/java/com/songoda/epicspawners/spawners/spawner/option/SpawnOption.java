package com.songoda.epicspawners.spawners.spawner.option;

import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.spawners.spawner.SpawnOptionType;

public interface SpawnOption {

    void spawn(SpawnerData data, SpawnerStack stack, Spawner spawner);

    SpawnOptionType getType();

}

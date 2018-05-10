package com.songoda.epicspawners.spawners.object.option;

import com.songoda.epicspawners.spawners.object.SpawnOptionType;
import com.songoda.epicspawners.spawners.object.Spawner;
import com.songoda.epicspawners.spawners.object.SpawnerData;
import com.songoda.epicspawners.spawners.object.SpawnerStack;

public interface SpawnOption {

    void spawn(SpawnerData data, SpawnerStack stack, Spawner spawner);

    SpawnOptionType getType();

}

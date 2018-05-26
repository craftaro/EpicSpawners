package com.songoda.epicspawners.api.spawner.condition;

import com.songoda.epicspawners.api.spawner.Spawner;

public interface SpawnCondition {

    String getName();

    String getDescription();

    boolean isMet(Spawner spawner);

}
package com.songoda.epicspawners.api;

import com.songoda.epicspawners.api.spawner.SpawnerManager;

public class EpicSpawnersAPI {

    private static EpicSpawners implementation;

    public static void setImplementation(EpicSpawners implementation) {
        if (EpicSpawnersAPI.implementation != null) {
            throw new IllegalArgumentException("Cannot set API implementation twice");
        }

        EpicSpawnersAPI.implementation = implementation;
    }

    public static EpicSpawners getImplementation() {
        return implementation;
    }

    public static SpawnerManager getSpawnerManager() {
        return implementation.getSpawnerManager();
    }

}

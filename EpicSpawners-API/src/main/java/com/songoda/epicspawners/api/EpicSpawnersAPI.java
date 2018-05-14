package com.songoda.epicspawners.api;

import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerManager;
import org.bukkit.inventory.ItemStack;

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

    public static ItemStack newSpawnerItem(SpawnerData type, int amount) {
        return implementation.newSpawnerItem(type, amount);
    }

    public static ItemStack newSpawnerItem(SpawnerData type, int amount, int stackSize) {
        return implementation.newSpawnerItem(type, amount, stackSize);
    }

    public static SpawnerData getSpawnerDataFromItem(ItemStack item) {
        return implementation.getSpawnerDataFromItem(item);
    }

    public static int getStackSizeFromItem(ItemStack item) {
        return implementation.getStackSizeFromItem(item);
    }

}

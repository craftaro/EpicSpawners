package com.craftaro.epicspawners.api;

import com.craftaro.epicspawners.api.utils.SpawnerDataBuilder;
import com.craftaro.epicspawners.api.utils.SpawnerTierBuilder;
import org.bukkit.plugin.Plugin;

public class EpicSpawnersAPI {

    private static Plugin plugin;
    private static SpawnerDataBuilder spawnerDataBuilder;
    private static SpawnerTierBuilder spawnerTierBuilder;

    public EpicSpawnersAPI(Plugin plugin, SpawnerDataBuilder spawnerDataBuilder, SpawnerTierBuilder spawnerTierBuilder) {
        if (EpicSpawnersAPI.plugin != null) {
            throw new IllegalStateException("EpicSpawnersAPI has already been initialized!");
        }
        EpicSpawnersAPI.plugin = plugin;
        EpicSpawnersAPI.spawnerDataBuilder = spawnerDataBuilder;
        EpicSpawnersAPI.spawnerTierBuilder = spawnerTierBuilder;
    }

    /**
     * @return The plugin instance of EpicSpawners.
     */
    public static Plugin getPlugin() {
        return plugin;
    }

    /**
     * @return A new instance of {@link SpawnerDataBuilder}.
     */
    public static SpawnerDataBuilder getSpawnerDataBuilder(String identifier) {
        return spawnerDataBuilder.newBuilder(identifier);
    }

    /**
     * @return A new instance of {@link SpawnerTierBuilder}.
     */
    public static SpawnerTierBuilder getSpawnerTierBuilder(String identifier) {
        return spawnerTierBuilder.newBuilder(identifier);
    }

}
package com.craftaro.epicspawners.api;

import com.craftaro.epicspawners.api.spawners.spawner.SpawnerManager;
import com.craftaro.epicspawners.api.utils.SpawnerDataBuilder;
import com.craftaro.epicspawners.api.utils.SpawnerTierBuilder;
import org.bukkit.plugin.Plugin;

/**
 * The main class of the API
 * <p>
 * <b>!! {@link EpicSpawnersApi#getVersion()} value is automatically replaced by maven don't change it !!</b>
 */
public class EpicSpawnersApi {
    private static Plugin plugin;
    private static SpawnerManager spawnerManager;
    private static SpawnerDataBuilder spawnerDataBuilder;
    private static SpawnerTierBuilder spawnerTierBuilder;

    public EpicSpawnersApi(Plugin plugin, SpawnerManager spawnerManager, SpawnerDataBuilder spawnerDataBuilder, SpawnerTierBuilder spawnerTierBuilder) {
        if (EpicSpawnersApi.plugin != null) {
            throw new IllegalStateException("EpicSpawnersAPI has already been initialized!");
        }
        EpicSpawnersApi.plugin = plugin;
        EpicSpawnersApi.spawnerManager = spawnerManager;
        EpicSpawnersApi.spawnerDataBuilder = spawnerDataBuilder;
        EpicSpawnersApi.spawnerTierBuilder = spawnerTierBuilder;
    }

    /**
     * @return The plugin instance of EpicSpawners.
     */
    public static Plugin getPlugin() {
        return plugin;
    }

    public static SpawnerManager getSpawnerManager() {
        return spawnerManager;
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

    /**
     * Used to get the version of the plugin
     *
     * @return The version of the plugin
     */
    public static String getVersion() {
        return "UNKNOWN_VERSION";
    }
}

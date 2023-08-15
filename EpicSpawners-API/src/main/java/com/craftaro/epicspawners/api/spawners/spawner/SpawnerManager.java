package com.craftaro.epicspawners.api.spawners.spawner;

import com.craftaro.core.configuration.Config;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SpawnerManager {
    SpawnerData getSpawnerData(String name);

    SpawnerData getSpawnerData(EntityType type);

    SpawnerTier getSpawnerTier(ItemStack item);

    SpawnerData addSpawnerData(String name, SpawnerData spawnerData);

    void addSpawnerData(SpawnerData spawnerData);

    void removeSpawnerData(String name);

    Collection<SpawnerData> getAllSpawnerData();

    Collection<SpawnerData> getAllEnabledSpawnerData();

    boolean isSpawner(Location location);

    boolean isSpawnerData(String type);

    PlacedSpawner getSpawnerFromWorld(Location location);

    void addSpawnerToWorld(Location location, PlacedSpawner spawner);

    PlacedSpawner removeSpawnerFromWorld(Location location);

    PlacedSpawner removeSpawnerFromWorld(PlacedSpawner spawner);

    Collection<PlacedSpawner> getSpawners();

    void addSpawners(Map<Location, PlacedSpawner> spawners);

    void addSpawners(List<PlacedSpawner> spawners);

    void addCooldown(PlacedSpawner spawner);

    void removeCooldown(PlacedSpawner spawner);

    boolean hasCooldown(PlacedSpawner spawner);

    int getAmountPlaced(Player player);

    void loadSpawnerDataFromFile();

    void reloadSpawnerData();

    void saveSpawnerDataToFile();

    boolean wasConfigModified();

    Config getSpawnerConfig();

    void reloadFromFile();

    PlacedSpawner getSpawner(int id);

    PlacedSpawner getSpawner(Location location);
}

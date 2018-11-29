package com.songoda.epicspawners.spawners.spawner;

import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerManager;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.*;

public class ESpawnerManager implements SpawnerManager {

    // These are the spawner types loaded into memory.
    private static final Map<String, SpawnerData> spawners = new LinkedHashMap<>();

    // These are spawners that exist in the game world.
    private static final Map<Location, Spawner> spawnersInWorld = new HashMap<>();

    // This is the map that holds the cooldowns for picking up stuffs
    private static final List<Spawner> pickingUp = new ArrayList<>();

    @Override
    public SpawnerData getSpawnerData(String name) {
        return spawners.get(name.toLowerCase());
    }

    @Override
    public SpawnerData getSpawnerData(EntityType type) {
        return getSpawnerData(type.name().replaceAll("_", " "));
    }

    @Override
    public void addSpawnerData(String name, SpawnerData spawnerData) {
        spawners.put(name.toLowerCase(), spawnerData);
        spawnerData.reloadSpawnMethods();
    }

    public void addSpawnerData(SpawnerData spawnerData) {
        spawners.put(spawnerData.getIdentifyingName().toLowerCase(), spawnerData);
    }

    @Override
    public void removeSpawnerData(String name) {
        spawners.remove(name.toLowerCase());
    }

    @Override
    @Deprecated
    public Map<String, SpawnerData> getRegisteredSpawnerData() {
        return Collections.unmodifiableMap(spawners);
    }

    @Override
    public Collection<SpawnerData> getAllSpawnerData() {
        return Collections.unmodifiableCollection(spawners.values());
    }

    @Override
    public boolean isSpawner(Location location) {
        return spawnersInWorld.containsKey(roundLocation(location));
    }

    @Override
    public boolean isSpawnerData(String type) {
        return spawners.containsKey(type);
    }

    @Override
    public Spawner getSpawnerFromWorld(Location location) {
        return spawnersInWorld.get(roundLocation(location));
    }

    @Override
    public void addSpawnerToWorld(Location location, Spawner spawner) {
        spawnersInWorld.put(roundLocation(location), spawner);
    }

    @Override
    public Spawner removeSpawnerFromWorld(Location location) {
        return spawnersInWorld.remove(roundLocation(location));
    }

    @Override
    @Deprecated
    public Map<Location, Spawner> getSpawnersInWorld() {
        return Collections.unmodifiableMap(spawnersInWorld);
    }

    @Override
    public Collection<Spawner> getSpawners() {
        return Collections.unmodifiableCollection(spawnersInWorld.values());
    }

    public void addCooldown(Spawner spawner) {
        pickingUp.add(spawner);
    }

    public void removeCooldown(Spawner spawner) {
        pickingUp.remove(spawner);
    }

    public boolean hasCooldown(Spawner spawner) {
        return pickingUp.contains(spawner);
    }

    private Location roundLocation(Location location) {
        location = location.clone();
        location.setX(location.getBlockX());
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ());
        return location;
    }
}

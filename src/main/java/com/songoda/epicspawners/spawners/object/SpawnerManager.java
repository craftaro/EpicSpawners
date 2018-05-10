package com.songoda.epicspawners.spawners.object;

import org.bukkit.Location;

import java.util.*;

public class SpawnerManager {

    // These are the spawner types loaded into memory.
    private final Map<String, SpawnerData> spawners = new HashMap<>();

    // These are spawners that exist in the game world.
    private final Map<Location, Spawner> spawnersInWorld = new HashMap<>();

    // This is the map that holds the cooldowns for picking up stuffs
    private final List<Spawner> pickingUp = new ArrayList<>();

    public SpawnerData getSpawnerType(String name) {
        return spawners.get(name.toLowerCase());
    }

    public void addSpawnerType(String name, SpawnerData spawnerData) {
        spawners.put(name.toLowerCase(), spawnerData);
    }

    public void removeSpawnerType(String name) {
        spawners.remove(name.toLowerCase());
    }

    public Map<String, SpawnerData> getSpawnerTypes() {
        return Collections.unmodifiableMap(spawners);
    }

    public boolean isSpawner(Location location) {
        return spawnersInWorld.containsKey(location);
    }

    public boolean isSpawnerType(String type) {
        return spawners.containsKey(type);
    }

    public Spawner getSpawnerFromWorld(Location location) {
        return spawnersInWorld.get(roundLocation(location));
    }

    public void addSpawnerToWorld(Location location, Spawner spawner) {
        spawnersInWorld.put(roundLocation(location), spawner);
    }

    public Spawner removeSpawnerFromWorld(Location location) {
        return spawnersInWorld.remove(roundLocation(location));
    }

    public Map<Location, Spawner> getSpawnersInWorld() {
        return Collections.unmodifiableMap(spawnersInWorld);
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

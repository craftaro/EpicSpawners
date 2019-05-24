package com.songoda.epicspawners.spawners.spawner;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

public class SpawnerManager {

    // These are the spawner types loaded into memory.
    private static final Map<String, SpawnerData> spawners = new LinkedHashMap<>();

    // These are spawners that exist in the game world.
    private static final Map<Location, Spawner> spawnersInWorld = new HashMap<>();

    // This is the map that holds the cooldowns for picking up stuffs
    private static final List<Spawner> pickingUp = new ArrayList<>();


    public SpawnerData getSpawnerData(String name) {
        return spawners.get(name.toLowerCase());
    }


    public SpawnerData getSpawnerData(EntityType type) {
        return getSpawnerData(type.name().replaceAll("_", " "));
    }


    public void addSpawnerData(String name, SpawnerData spawnerData) {
        spawners.put(name.toLowerCase(), spawnerData);
        spawnerData.reloadSpawnMethods();
    }

    public void addSpawnerData(SpawnerData spawnerData) {
        spawners.put(spawnerData.getIdentifyingName().toLowerCase(), spawnerData);
    }


    public void removeSpawnerData(String name) {
        spawners.remove(name.toLowerCase());
    }


    @Deprecated
    public Map<String, SpawnerData> getRegisteredSpawnerData() {
        return Collections.unmodifiableMap(spawners);
    }


    public Collection<SpawnerData> getAllSpawnerData() {
        return Collections.unmodifiableCollection(spawners.values());
    }


    public Collection<SpawnerData> getAllEnabledSpawnerData() {
        Collection<SpawnerData> spawners = new ArrayList<>(getAllSpawnerData());
        spawners.removeIf(spawnerData -> !spawnerData.isActive() || spawnerData.getIdentifyingName().equals("Omni"));
        return Collections.unmodifiableCollection(spawners);
    }


    public boolean isSpawner(Location location) {
        return spawnersInWorld.containsKey(roundLocation(location));
    }


    public boolean isSpawnerData(String type) {
        return spawners.containsKey(type.toLowerCase());
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


    @Deprecated
    public Map<Location, Spawner> getSpawnersInWorld() {
        return Collections.unmodifiableMap(spawnersInWorld);
    }


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


    public int getAmountPlaced(Player player) {
        int amount = 0;
        for (Spawner spawner : spawnersInWorld.values()) {
            if (spawner.getPlacedBy() == null || player.getUniqueId() != spawner.getPlacedBy().getUniqueId()) continue;
            amount++;
        }
        return amount;
    }

    private Location roundLocation(Location location) {
        location = location.clone();
        location.setX(location.getBlockX());
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ());
        return location;
    }
}

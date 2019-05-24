package com.songoda.epicspawners.spawners.spawner;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.utils.ConfigWrapper;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.ServerVersion;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.util.*;

public class SpawnerManager {

    // These are the spawner types loaded into memory.
    private static final Map<String, SpawnerData> spawners = new LinkedHashMap<>();

    // These are spawners that exist in the game world.
    private static final Map<Location, Spawner> spawnersInWorld = new HashMap<>();

    // This is the map that holds the cooldowns for picking up stuffs
    private static final List<Spawner> pickingUp = new ArrayList<>();

    private ConfigWrapper spawnerFile = new ConfigWrapper(EpicSpawners.getInstance(), "", "spawners.yml");

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


    private void setupSpawners() {
        for (EntityType value : EntityType.values()) {
            if (value.isSpawnable() && value.isAlive() && !value.toString().toLowerCase().contains("armor")) {
                processDefault(value.name());
            }
        }

        this.processDefault("Omni");
        this.spawnerFile.getConfig().options().copyDefaults(true);
        this.spawnerFile.saveConfig();
    }

    private void processDefault(String value) {
        FileConfiguration spawnerConfig = spawnerFile.getConfig();

        String type = Methods.getTypeFromString(value);
        Random rn = new Random();
        int uuid = rn.nextInt(9999);
        spawnerConfig.addDefault("Entities." + type + ".uuid", uuid);

        if (!spawnerConfig.contains("Entities." + type + ".Display-Name")) {
            spawnerConfig.set("Entities." + type + ".Display-Name", type);
        }
        if (!spawnerConfig.contains("Entities." + type + ".Pickup-cost")) {
            spawnerConfig.addDefault("Entities." + type + ".Pickup-cost", 0);
        }

        String spawnBlock = "AIR";
        if (value.equalsIgnoreCase("pig") || value.equalsIgnoreCase("sheep") || value.equalsIgnoreCase("chicken") ||
                value.equalsIgnoreCase("cow") || value.equalsIgnoreCase("rabbit") || value.equalsIgnoreCase("llamma") ||
                value.equalsIgnoreCase("horse") || value.equalsIgnoreCase("OCELOT")) {
            spawnBlock = "GRASS_BLOCK";
        }

        if (value.equalsIgnoreCase("MUSHROOM_COW")) {
            spawnBlock = "MYCELIUM";
        }

        if (value.equalsIgnoreCase("SQUID") || value.equalsIgnoreCase("ELDER_GUARDIAN") || value.equalsIgnoreCase("COD") ||
                value.equalsIgnoreCase("SALMON") || value.toUpperCase().contains("FISH")) {
            spawnBlock = "WATER";
        }

        if (value.equalsIgnoreCase("OCELOT")) {
            spawnBlock += ", LEAVES";
        }

        EntityType entityType = null;

        for (EntityType val : EntityType.values()) {
            if (val.isSpawnable() && val.isAlive()) {
                if (val.name().equals(value)) {
                    entityType = val;
                    List<String> list = new ArrayList<>();
                    list.add(value);
                    if (!spawnerConfig.contains("Entities." + type + ".entities"))
                        spawnerConfig.addDefault("Entities." + type + ".entities", list);
                }
            }
        }

        spawnerConfig.addDefault("Entities." + type + ".custom", false);
        spawnerConfig.addDefault("Entities." + type + ".Spawn-Block", spawnBlock);
        spawnerConfig.addDefault("Entities." + type + ".Allowed", true);
        spawnerConfig.addDefault("Entities." + type + ".Spawn-On-Fire", false);
        spawnerConfig.addDefault("Entities." + type + ".Upgradable", true);
        spawnerConfig.addDefault("Entities." + type + ".Convertible", true);
        spawnerConfig.addDefault("Entities." + type + ".Convert-Ratio", "45%");
        spawnerConfig.addDefault("Entities." + type + ".In-Shop", true);
        spawnerConfig.addDefault("Entities." + type + ".Shop-Price", 1000.00);
        spawnerConfig.addDefault("Entities." + type + ".CustomGoal", 0);
        spawnerConfig.addDefault("Entities." + type + ".Custom-ECO-Cost", 0);
        spawnerConfig.addDefault("Entities." + type + ".Custom-XP-Cost", 0);
        spawnerConfig.addDefault("Entities." + type + ".Tick-Rate", "800:200");
        spawnerConfig.addDefault("Entities." + type + ".Spawn-Effect", "NONE");
        spawnerConfig.addDefault("Entities." + type + ".Spawn-Effect-Particle", "REDSTONE");
        spawnerConfig.addDefault("Entities." + type + ".Entity-Spawn-Particle", "SMOKE");
        spawnerConfig.addDefault("Entities." + type + ".Spawner-Spawn-Particle", "FIRE");
        spawnerConfig.addDefault("Entities." + type + ".Particle-Amount", "NORMAL");
        spawnerConfig.addDefault("Entities." + type + ".Particle-Effect-Boosted-Only", true);
        spawnerConfig.addDefault("Entities." + type + ".Craftable", false);
        spawnerConfig.addDefault("Entities." + type + ".Recipe-Layout", "AAAABAAAA");
        spawnerConfig.addDefault("Entities." + type + ".Recipe-Ingredients", Arrays.asList("A, IRON_BARS", "B, SPAWN_EGG"));

        if (entityType == EntityType.SLIME) {
            spawnerConfig.addDefault("Entities." + type + ".Conditions.Biomes",
                    EpicSpawners.getInstance().isServerVersionAtLeast(ServerVersion.V1_13) ? Biome.SWAMP.name() : Biome.valueOf("SWAMPLAND").name());
            spawnerConfig.addDefault("Entities." + type + ".Conditions.Height", "50:70");
        } else {
            spawnerConfig.addDefault("Entities." + type + ".Conditions.Biomes", "ALL");
            spawnerConfig.addDefault("Entities." + type + ".Conditions.Height", "0:256");
        }
        if (entityType != null && Monster.class.isAssignableFrom(entityType.getEntityClass())) {
            spawnerConfig.addDefault("Entities." + type + ".Conditions.Light", "DARK");
        } else {
            spawnerConfig.addDefault("Entities." + type + ".Conditions.Light", "BOTH");
        }
        spawnerConfig.addDefault("Entities." + type + ".Conditions.Storm Only", false);
        spawnerConfig.addDefault("Entities." + type + ".Conditions.Max Entities Around Spawner", 6);
        spawnerConfig.addDefault("Entities." + type + ".Conditions.Required Player Distance And Amount", 16 + ":" + 1);
    }

    public ConfigWrapper getSpawnerFile() {
        return spawnerFile;
    }

    private Location roundLocation(Location location) {
        location = location.clone();
        location.setX(location.getBlockX());
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ());
        return location;
    }
}

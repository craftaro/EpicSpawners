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

    public SpawnerManager() {
        for (EntityType value : EntityType.values()) {
            if (value.isSpawnable() && value.isAlive() && !value.toString().toLowerCase().contains("armor")) {
                processDefaultSpawner(value.name());
            }
        }

        this.processDefaultSpawner("Omni");
        this.spawnerFile.getConfig().options().copyDefaults(true);
        this.spawnerFile.saveConfig();
    }

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

    private void processDefaultSpawner(String value) {
        FileConfiguration spawnerConfig = spawnerFile.getConfig();

        String type = Methods.getTypeFromString(value);
        Random rn = new Random();
        int uuid = rn.nextInt(9999);
        
        String section = "Entities." + type;
        
        spawnerConfig.addDefault(section + ".uuid", uuid);

        if (!spawnerConfig.contains(section + ".Display-Name")) {
            spawnerConfig.set(section + ".Display-Name", type);
        }
        if (!spawnerConfig.contains(section + ".Pickup-cost")) {
            spawnerConfig.addDefault(section + ".Pickup-cost", 0);
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
                    if (!spawnerConfig.contains(section + ".entities"))
                        spawnerConfig.addDefault(section + ".entities", list);
                }
            }
        }

        spawnerConfig.addDefault(section + ".custom", false);
        spawnerConfig.addDefault(section + ".Spawn-Block", spawnBlock);
        spawnerConfig.addDefault(section + ".Allowed", true);
        spawnerConfig.addDefault(section + ".Spawn-On-Fire", false);
        spawnerConfig.addDefault(section + ".Upgradable", true);
        spawnerConfig.addDefault(section + ".Convertible", true);
        spawnerConfig.addDefault(section + ".Convert-Ratio", "45%");
        spawnerConfig.addDefault(section + ".In-Shop", true);
        spawnerConfig.addDefault(section + ".Shop-Price", 1000.00);
        spawnerConfig.addDefault(section + ".CustomGoal", 0);
        spawnerConfig.addDefault(section + ".Custom-ECO-Cost", 0);
        spawnerConfig.addDefault(section + ".Custom-XP-Cost", 0);
        spawnerConfig.addDefault(section + ".Tick-Rate", "800:200");
        spawnerConfig.addDefault(section + ".Spawn-Effect", "NONE");
        spawnerConfig.addDefault(section + ".Spawn-Effect-Particle", "REDSTONE");
        spawnerConfig.addDefault(section + ".Entity-Spawn-Particle", "SMOKE");
        spawnerConfig.addDefault(section + ".Spawner-Spawn-Particle", "FIRE");
        spawnerConfig.addDefault(section + ".Particle-Amount", "NORMAL");
        spawnerConfig.addDefault(section + ".Particle-Effect-Boosted-Only", true);
        spawnerConfig.addDefault(section + ".Craftable", false);
        spawnerConfig.addDefault(section + ".Recipe-Layout", "AAAABAAAA");
        spawnerConfig.addDefault(section + ".Recipe-Ingredients", Arrays.asList("A, IRON_BARS", "B, SPAWN_EGG"));

        if (entityType == EntityType.SLIME) {
            spawnerConfig.addDefault(section + ".Conditions.Biomes",
                    EpicSpawners.getInstance().isServerVersionAtLeast(ServerVersion.V1_13) ? Biome.SWAMP.name() : Biome.valueOf("SWAMPLAND").name());
            spawnerConfig.addDefault(section + ".Conditions.Height", "50:70");
        } else {
            spawnerConfig.addDefault(section + ".Conditions.Biomes", "ALL");
            spawnerConfig.addDefault(section + ".Conditions.Height", "0:256");
        }
        if (entityType != null && Monster.class.isAssignableFrom(entityType.getEntityClass())) {
            spawnerConfig.addDefault(section + ".Conditions.Light", "DARK");
        } else {
            spawnerConfig.addDefault(section + ".Conditions.Light", "BOTH");
        }
        spawnerConfig.addDefault(section + ".Conditions.Storm Only", false);
        spawnerConfig.addDefault(section + ".Conditions.Max Entities Around Spawner", 6);
        spawnerConfig.addDefault(section + ".Conditions.Required Player Distance And Amount", 16 + ":" + 1);
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

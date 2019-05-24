package com.songoda.epicspawners.spawners.spawner;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.particles.ParticleDensity;
import com.songoda.epicspawners.particles.ParticleEffect;
import com.songoda.epicspawners.particles.ParticleType;
import com.songoda.epicspawners.spawners.condition.*;
import com.songoda.epicspawners.utils.ConfigWrapper;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.ServerVersion;
import com.songoda.epicspawners.utils.SpawnerDataBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class SpawnerManager {

    private final EpicSpawners plugin;

    // These are the spawner types loaded into memory.
    private static final Map<String, SpawnerData> spawners = new LinkedHashMap<>();

    // These are spawners that exist in the game world.
    private static final Map<Location, Spawner> spawnersInWorld = new HashMap<>();

    // This is the map that holds the cooldowns for picking up stuffs
    private static final List<Spawner> pickingUp = new ArrayList<>();

    private ConfigWrapper spawnerFile = new ConfigWrapper(EpicSpawners.getInstance(), "", "spawners.yml");

    public SpawnerManager(EpicSpawners plugin) {
        this.plugin = plugin;
        Arrays.stream(EntityType.values()).filter(entityType -> entityType.isSpawnable()
                && entityType.isAlive()
                && entityType != EntityType.ARMOR_STAND).forEach(entityType ->
                processDefaultSpawner(entityType.name()));

        this.processDefaultSpawner("Omni");
        this.spawnerFile.getConfig().options().copyDefaults(true);
        this.spawnerFile.saveConfig();

        this.loadSpawnersFromFile();
    }

    public SpawnerData getSpawnerData(String name) {
        return spawners.values().stream().filter(spawnerData -> spawnerData.getIdentifyingName()
                .equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public SpawnerData getSpawnerData(EntityType type) {
        return getSpawnerData(type.name().replaceAll("_", " "));
    }


    public void addSpawnerData(String name, SpawnerData spawnerData) {
        spawners.put(name.toLowerCase(), spawnerData);
        spawnerData.reloadSpawnMethods();
        spawnerData.reloadSpawnMethods();
    }

    public void addSpawnerData(SpawnerData spawnerData) {
        spawners.put(spawnerData.getIdentifyingName().toLowerCase(), spawnerData);
    }

    public void removeSpawnerData(String name) {
        spawners.remove(name.toLowerCase());
    }

    public Collection<SpawnerData> getAllSpawnerData() {
        return Collections.unmodifiableCollection(spawners.values());
    }

    public Collection<SpawnerData> getAllEnabledSpawnerData() {
        return spawners.values().stream().filter(spawnerData -> spawnerData.isActive()
                && !spawnerData.getIdentifyingName().equals("Omni")).collect(Collectors.toList());
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
        return Math.toIntExact(spawnersInWorld.values().stream().filter(spawner -> spawner.getPlacedBy() != null
                && player.getUniqueId().equals(spawner.getPlacedBy().getUniqueId())).count());
    }

    private void processDefaultSpawner(String value) {
        EpicSpawners plugin = EpicSpawners.getInstance();
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

        switch (value.toUpperCase()) {
            case "PIG":
            case "SHEEP":
            case "CHICKEN":
            case "COW":
            case "RABBIT":
            case "LLAMA":
            case "HORSE":
            case "CAT":
                spawnBlock = plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? "GRASS_BLOCK" : "GRASS";
                break;
            case "MUSHROOM_COW":
                spawnBlock = plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? "MYCELIUM" : "MYCEL";
                break;
            case "SQUID":
            case "ELDER_GUARDIAN":
            case "COD":
            case "SALMON":
            case "PUFFERFISH":
            case "TROPICAL_FISH":
                spawnBlock = plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? "WATER" : "STATIONARY_WATER, WATER";
                break;
            case "OCELOT":
                spawnBlock = plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? "GRASS_BLOCK, JUNGLE_LEAVES, " +
                        "ACACIA_LEAVES, BIRCH_LEAVES, DARK_OAK_LEAVES, OAK_LEAVES, SPRUCE_LEAVES" : "GRASS, LEAVES";
                break;
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
                    plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Biome.SWAMP.name() : Biome.valueOf("SWAMPLAND").name());
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

    @SuppressWarnings("unchecked")
    private void loadSpawnersFromFile() {
        // Register spawner data into SpawnerRegistry from configuration.
        FileConfiguration spawnerConfig = spawnerFile.getConfig();
        if (!spawnerConfig.contains("Entities")) return;
        for (String key : spawnerConfig.getConfigurationSection("Entities").getKeys(false)) {
            ConfigurationSection currentSection = spawnerConfig.getConfigurationSection("Entities." + key);

            List<EntityType> entities = new ArrayList<>();
            List<Material> blocks = new ArrayList<>();
            List<Material> spawnBlocks = new ArrayList<>();
            List<ItemStack> itemDrops = (List<ItemStack>) currentSection.getList("itemDrops", new ArrayList<>());
            List<ItemStack> items = (List<ItemStack>) currentSection.getList("items", new ArrayList<>());
            List<String> commands = currentSection.getStringList("command");

            for (String block : currentSection.getStringList("blocks")) {
                blocks.add(Material.matchMaterial(block.toUpperCase()));
            }
            for (String block : currentSection.getString("Spawn-Block").split(",")) {
                spawnBlocks.add(Material.matchMaterial(block.toUpperCase().trim()));
            }
            for (String entity : currentSection.getStringList("entities")) {
                entities.add(EntityType.valueOf(entity));
            }

            SpawnerDataBuilder dataBuilder = new SpawnerDataBuilder(key).uuid(currentSection.getInt("uuid"))
                    .entities(entities).blocks(blocks).items(items).entityDroppedItems(itemDrops).commands(commands)
                    .spawnBlocks(spawnBlocks)
                    .active(currentSection.getBoolean("Allowed"))
                    .spawnOnFire(currentSection.getBoolean("Spawn-On-Fire"))
                    .upgradeable(currentSection.getBoolean("Upgradable"))
                    .convertible(currentSection.getBoolean("Convertible"))
                    .convertRatio(currentSection.getString("Convert-Ratio"))
                    .inShop(currentSection.getBoolean("In-Shop"))
                    .pickupCost(currentSection.getDouble("Pickup-Cost"))
                    .craftable(currentSection.getBoolean("Craftable"))
                    .recipe(currentSection.getString("Recipe-Layout"))
                    .recipeIngredients(currentSection.getStringList("Recipe-Ingredients"))
                    .shopPrice(currentSection.getDouble("Shop-Price"))
                    .killGoal(currentSection.getInt("CustomGoal"))
                    .upgradeCostEconomy(currentSection.getInt("Custom-ECO-Cost"))
                    .upgradeCostExperience(currentSection.getInt("Custom-XP-Cost"))
                    .tickRate(currentSection.getString("Tick-Rate"))
                    .particleEffect(ParticleEffect.valueOf(currentSection.getString("Spawn-Effect", "HALO")))
                    .spawnEffectParticle(ParticleType.valueOf(currentSection.getString("Spawn-Effect-Particle", "REDSTONE")))
                    .entitySpawnParticle(ParticleType.valueOf(currentSection.getString("Entity-Spawn-Particle", "SMOKE")))
                    .spawnerSpawnParticle(ParticleType.valueOf(currentSection.getString("Spawner-Spawn-Particle", "FIRE")))
                    .particleDensity(ParticleDensity.valueOf(currentSection.getString("Particle-Amount", "NORMAL")))
                    .particleEffectBoostedOnly(currentSection.getBoolean("Particle-Effect-Boosted-Only"));

            if (currentSection.contains("custom")) {
                dataBuilder.isCustom(currentSection.getBoolean("custom"));
            } else {
                dataBuilder.isCustom(key.toLowerCase().contains("custom"));
            }

            if (currentSection.contains("Display-Name")) {
                dataBuilder.displayName(currentSection.getString("Display-Name"));
            }
            if (currentSection.contains("Display-Item")) {
                dataBuilder.displayItem(Material.valueOf(currentSection.getString("Display-Item")));
            }

            SpawnerData data = dataBuilder.build();

            if (currentSection.contains("Conditions")) {
                String biomeString = currentSection.getString("Conditions.Biomes");
                Set<Biome> biomes;
                if (biomeString.toLowerCase().equals("all"))
                    biomes = EnumSet.allOf(Biome.class);
                else {
                    biomes = new HashSet<>();
                    for (String string : biomeString.split(", ")) {
                        biomes.add(Biome.valueOf(string));
                    }
                }

                String[] heightString = currentSection.getString("Conditions.Height").split(":");
                String[] playerString = currentSection.getString("Conditions.Required Player Distance And Amount").split(":");

                data.addCondition(new SpawnConditionNearbyPlayers(Integer.parseInt(playerString[0]), Integer.parseInt(playerString[1])));
                data.addCondition(new SpawnConditionNearbyEntities(currentSection.getInt("Conditions.Max Entities Around Spawner")));
                data.addCondition(new SpawnConditionBiome(biomes));
                data.addCondition(new SpawnConditionHeight(Integer.parseInt(heightString[0]), Integer.parseInt(heightString[1])));
                data.addCondition(new SpawnConditionLightDark(SpawnConditionLightDark.Type.valueOf(currentSection.getString("Conditions.Light"))));
                data.addCondition(new SpawnConditionStorm(currentSection.getBoolean("Conditions.Storm Only")));
            }

            addSpawnerData(key, data);
        }
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

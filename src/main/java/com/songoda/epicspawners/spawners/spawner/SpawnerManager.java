package com.songoda.epicspawners.spawners.spawner;

import com.songoda.core.compatibility.CompatibleBiome;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.configuration.Config;
import com.songoda.core.nms.NmsManager;
import com.songoda.core.nms.nbt.NBTItem;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.particles.ParticleDensity;
import com.songoda.epicspawners.particles.ParticleEffect;
import com.songoda.epicspawners.particles.ParticleType;
import com.songoda.epicspawners.spawners.condition.*;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.SpawnerDataBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.*;
import java.util.stream.Collectors;

public class SpawnerManager {

    private final EpicSpawners plugin;

    // These are the spawner types loaded into memory.
    private final Map<String, SpawnerData> registeredSpawnerData = new LinkedHashMap<>();

    // These are spawners that exist in the game world.
    private final Map<Location, Spawner> spawnersInWorld = new HashMap<>();

    // This is the map that holds the cooldowns for picking up stuffs
    private final List<Spawner> pickingUp = new ArrayList<>();

    private Config spawnerConfig = new Config(EpicSpawners.getInstance(), "spawners.yml");

    public SpawnerManager(EpicSpawners plugin) {
        this.plugin = plugin;
        this.spawnerConfig.load();
        Arrays.stream(EntityType.values()).filter(entityType -> entityType.isSpawnable()
                && entityType.isAlive()
                && entityType != EntityType.ARMOR_STAND).forEach(entityType ->
                processDefaultSpawner(entityType.name()));

        this.processDefaultSpawner("Omni");
        this.spawnerConfig.options().copyDefaults(true);
        this.spawnerConfig.save();

        this.loadSpawnerDataFromFile();
    }

    public SpawnerData getSpawnerData(String name) {
        return registeredSpawnerData.values().stream().filter(spawnerData -> spawnerData.getIdentifyingName()
                .equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public SpawnerData getSpawnerData(EntityType type) {
        return getSpawnerData(type.name().replaceAll("_", " "));
    }

    public SpawnerData getSpawnerData(int id) {
        return registeredSpawnerData.values().stream().filter(spawnerData -> spawnerData.getUUID() == id)
                .findFirst().orElse(null);
    }

    public SpawnerData getSpawnerData(ItemStack item) {
        if (item == null) return null;

        String name = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : null;

        NBTItem nbtItem = NmsManager.getNbt().of(item);
        if (nbtItem.has("type")) {
            String type = nbtItem.getNBTObject("type").asString();
            return getSpawnerData(type);
        } else if (name != null && name.contains(":")) {
            String[] raw = name.replace(";", "").split(":");
            String value = raw[0].replace(String.valueOf(ChatColor.COLOR_CHAR), "");
            if (Methods.isInt(value)) {
                SpawnerData spawnerData = getSpawnerData(Integer.parseInt(value));
                if (Methods.isInt(value) && spawnerData != null) {
                    return spawnerData;
                }
            }

            SpawnerData spawnerData = EpicSpawners.getInstance().getSpawnerManager().getSpawnerData(ChatColor.stripColor(raw[raw.length - 1]).split(" ")[0]);
            if (spawnerData != null)
                return spawnerData;
        }

        BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
        CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();
        return EpicSpawners.getInstance().getSpawnerManager().getSpawnerData(cs.getSpawnedType());
    }

    public SpawnerData addSpawnerData(String name, SpawnerData spawnerData) {
        this.registeredSpawnerData.put(name.toLowerCase(), spawnerData);
        spawnerData.reloadSpawnMethods();
        return spawnerData;
    }

    public void addSpawnerData(SpawnerData spawnerData) {
        this.registeredSpawnerData.put(spawnerData.getIdentifyingName().toLowerCase(), spawnerData);
    }

    public void removeSpawnerData(String name) {
        registeredSpawnerData.remove(name.toLowerCase());
    }

    public Collection<SpawnerData> getAllSpawnerData() {
        return Collections.unmodifiableCollection(registeredSpawnerData.values());
    }

    public Collection<SpawnerData> getAllEnabledSpawnerData() {
        return registeredSpawnerData.values().stream().filter(spawnerData -> spawnerData.isActive()
                && !spawnerData.getIdentifyingName().equals("Omni")).collect(Collectors.toList());
    }

    public boolean isSpawner(Location location) {
        return spawnersInWorld.containsKey(location);
    }

    public boolean isSpawnerData(String type) {
        return registeredSpawnerData.containsKey(type.toLowerCase());
    }

    public Spawner getSpawnerFromWorld(Location location) {
        return spawnersInWorld.get(location);
    }

    public void addSpawnerToWorld(Location location, Spawner spawner) {
        spawnersInWorld.put(location, spawner);
    }

    public Spawner removeSpawnerFromWorld(Location location) {
        return spawnersInWorld.remove(location);
    }

    public Spawner removeSpawnerFromWorld(Spawner spawner) {
        return spawnersInWorld.remove(spawner.getLocation());
    }

    public Collection<Spawner> getSpawners() {
        return Collections.unmodifiableCollection(spawnersInWorld.values());
    }

    public void addSpawners(Map<Location, Spawner> spawners) {
        spawnersInWorld.putAll(spawners);
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
                && player.getUniqueId().equals(spawner.getPlacedBy().getUniqueId())).count()) + 1;
    }

    private void processDefaultSpawner(String value) {
        FileConfiguration spawnerConfig = this.spawnerConfig.getFileConfig();

        String type = Methods.getTypeFromString(value);
        Random rn = new Random();
        int uuid = rn.nextInt(9999);

        String section = "Entities." + type;

        spawnerConfig.addDefault(section + ".uuid", uuid);

        if (!spawnerConfig.contains(section + ".Display-Name")) {
            spawnerConfig.set(section + ".Display-Name", type);
        }

        for (EntityType val : EntityType.values()) {
            if (!val.isSpawnable()
                    || !val.isAlive()
                    || !val.name().equals(value)) continue;
                spawnerConfig.addDefault(section
                        + ".entities", Collections.singletonList(value));
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
                spawnBlock = CompatibleMaterial.GRASS_BLOCK.getMaterial().name();
                break;
            case "MUSHROOM_COW":
                spawnBlock = CompatibleMaterial.MYCELIUM.getMaterial().name();
                break;
            case "SQUID":
            case "ELDER_GUARDIAN":
            case "COD":
            case "SALMON":
            case "PUFFERFISH":
            case "TROPICAL_FISH":
                spawnBlock = ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? "WATER" : "STATIONARY_WATER, WATER";
                break;
            case "OCELOT":
                spawnBlock = ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? "GRASS_BLOCK, JUNGLE_LEAVES, " +
                        "ACACIA_LEAVES, BIRCH_LEAVES, DARK_OAK_LEAVES, OAK_LEAVES, SPRUCE_LEAVES" : "GRASS, LEAVES";
                break;
        }

        EntityType entityType = null;

        spawnerConfig.addDefault(section + ".Pickup-Cost", 0);
        spawnerConfig.addDefault(section + ".custom", false);
        spawnerConfig.addDefault(section + ".Spawn-Block", spawnBlock);
        spawnerConfig.addDefault(section + ".Allowed", true);
        spawnerConfig.addDefault(section + ".Spawn-On-Fire", false);
        spawnerConfig.addDefault(section + ".Upgradable", true);
        spawnerConfig.addDefault(section + ".Convertible", true);
        spawnerConfig.addDefault(section + ".Convert-Ratio", "45%");
        spawnerConfig.addDefault(section + ".In-Shop", true);
        spawnerConfig.addDefault(section + ".Shop-Order", 0);
        spawnerConfig.addDefault(section + ".Shop-Price", 1000.00);
        spawnerConfig.addDefault(section + ".CustomGoal", 0);
        spawnerConfig.addDefault(section + ".Custom-ECO-Cost", 0);
        spawnerConfig.addDefault(section + ".Custom-XP-Cost", 0);
        spawnerConfig.addDefault(section + ".Spawn-Limit", -1);
        spawnerConfig.addDefault(section + ".Pick-Damage", 1);
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
                    CompatibleBiome.SWAMP.getBiome().toString());
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
    public void loadSpawnerDataFromFile() {
        registeredSpawnerData.clear();
        // Register spawner data into SpawnerRegistry from configuration.
        FileConfiguration spawnerConfig = this.spawnerConfig.getFileConfig();
        if (!spawnerConfig.contains("Entities")) return;
        for (String key : spawnerConfig.getConfigurationSection("Entities").getKeys(false)) {
            ConfigurationSection currentSection = spawnerConfig.getConfigurationSection("Entities." + key);

            List<EntityType> entities = new ArrayList<>();
            List<Material> blocks = new ArrayList<>();
            List<Material> spawnBlocks = new ArrayList<>();
            List<ItemStack> items = (List<ItemStack>) currentSection.getList("items", new ArrayList<>());
            List<String> commands = currentSection.getStringList("command");

            for (String block : currentSection.getStringList("blocks")) {
                Material material = Material.matchMaterial(block.toUpperCase());
                blocks.add(material == null ? Material.AIR : material);
            }
            for (String block : currentSection.getString("Spawn-Block").split(",")) {
                spawnBlocks.add(Material.matchMaterial(block.toUpperCase().trim()));
            }
            for (String entity : currentSection.getStringList("entities")) {
                try {
                    entities.add(EntityType.valueOf(entity));
                } catch (IllegalArgumentException ignored) {
                }
            }

            SpawnerDataBuilder dataBuilder = new SpawnerDataBuilder(key).uuid(currentSection.getInt("uuid"))
                    .entities(entities).blocks(blocks).items(items).commands(commands)
                    .spawnBlocks(spawnBlocks)
                    .active(currentSection.getBoolean("Allowed"))
                    .spawnOnFire(currentSection.getBoolean("Spawn-On-Fire"))
                    .upgradeable(currentSection.getBoolean("Upgradable"))
                    .convertible(currentSection.getBoolean("Convertible"))
                    .convertRatio(currentSection.getString("Convert-Ratio"))
                    .inShop(currentSection.getBoolean("In-Shop"))
                    .pickupCost(currentSection.getDouble("Pickup-Cost"))
                    .pickDamage(currentSection.getInt("Pick-Damage"))
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

            if (currentSection.contains("Spawn-Limit")) {
                dataBuilder.spawnLimit(currentSection.getInt("Spawn-Limit"));
            }

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

            if (currentSection.contains("Shop-Order")) {
                dataBuilder.shopOrder(currentSection.getInt("Shop-Order"));
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

        reloadSpawnerData();

    }

    public void reloadSpawnerData() {
        for (Spawner spawner : spawnersInWorld.values()) {
            for (SpawnerStack stack : spawner.getSpawnerStacks()) {
                stack.setSpawnerData(registeredSpawnerData.get(stack.getSpawnerData().getIdentifyingName().toLowerCase()));
                plugin.getDataManager().updateSpawnerStack(stack);
            }
        }
    }

    public void saveSpawnerDataToFile() {
        //ToDO: If the defaults are set correctly this could do the initial config save.

        // Save spawner settings
        FileConfiguration spawnerConfig = this.spawnerConfig.getFileConfig();
        spawnerConfig.set("Entities", null);

        ConfigurationSection entitiesSection = spawnerConfig.createSection("Entities");
        for (SpawnerData spawnerData : getAllSpawnerData()) {
            ConfigurationSection currentSection = entitiesSection.createSection(spawnerData.getIdentifyingName());

            currentSection.set("uuid", spawnerData.getUUID());
            currentSection.set("Display-Name", spawnerData.getDisplayName());

            currentSection.set("blocks", Methods.getStrings(spawnerData.getBlocks()));
            currentSection.set("entities", Methods.getStrings(spawnerData.getEntities()));
            currentSection.set("items", spawnerData.getItems());
            currentSection.set("command", spawnerData.getCommands());

            currentSection.set("custom", spawnerData.isCustom());
            currentSection.set("Spawn-Block", String.join(", ", Methods.getStrings(spawnerData.getSpawnBlocksList())));
            currentSection.set("Allowed", spawnerData.isActive());
            currentSection.set("Spawn-On-Fire", spawnerData.isSpawnOnFire());
            currentSection.set("Upgradable", spawnerData.isUpgradeable());
            currentSection.set("Convertible", spawnerData.isConvertible());
            currentSection.set("Convert-Ratio", spawnerData.getConvertRatio());
            currentSection.set("In-Shop", spawnerData.isInShop());
            currentSection.set("Shop-Order", spawnerData.getShopOrder());
            currentSection.set("Shop-Price", spawnerData.getShopPrice());
            currentSection.set("CustomGoal", spawnerData.getKillGoal());
            currentSection.set("Custom-ECO-Cost", spawnerData.getUpgradeCostEconomy());
            currentSection.set("Custom-XP-Cost", spawnerData.getUpgradeCostExperience());
            currentSection.set("Tick-Rate", spawnerData.getTickRate());
            currentSection.set("Pickup-Cost", spawnerData.getPickupCost());
            currentSection.set("Craftable", spawnerData.isCraftable());
            currentSection.set("Recipe-Layout", spawnerData.getRecipe());
            currentSection.set("Recipe-Ingredients", spawnerData.getRecipeIngredients());
            currentSection.set("Pick-Damage", spawnerData.getPickDamage());

            currentSection.set("Spawn-Effect", spawnerData.getParticleEffect().name());
            currentSection.set("Spawn-Effect-Particle", spawnerData.getSpawnEffectParticle().name());
            currentSection.set("Entity-Spawn-Particle", spawnerData.getEntitySpawnParticle().name());
            currentSection.set("Spawner-Spawn-Particle", spawnerData.getSpawnerSpawnParticle().name());
            currentSection.set("Particle-Amount", spawnerData.getParticleDensity().name());
            currentSection.set("Particle-Effect-Boosted-Only", spawnerData.isParticleEffectBoostedOnly());


            for (SpawnCondition spawnCondition : spawnerData.getConditions()) {
                if (spawnCondition instanceof SpawnConditionBiome) {
                    if (EnumSet.allOf(Biome.class).equals(((SpawnConditionBiome) spawnCondition).getBiomes())) {
                        currentSection.set("Conditions.Biomes", "ALL");
                    } else {
                        currentSection.set("Conditions.Biomes", String.join(", ", Methods.getStrings(((SpawnConditionBiome) spawnCondition).getBiomes())));
                    }
                }
                if (spawnCondition instanceof SpawnConditionHeight)
                    currentSection.set("Conditions.Height", ((SpawnConditionHeight) spawnCondition).getMin() + ":" + ((SpawnConditionHeight) spawnCondition).getMax());
                if (spawnCondition instanceof SpawnConditionLightDark)
                    currentSection.set("Conditions.Light", ((SpawnConditionLightDark) spawnCondition).getType().name());
                if (spawnCondition instanceof SpawnConditionStorm)
                    currentSection.set("Conditions.Storm Only", ((SpawnConditionStorm) spawnCondition).isStormOnly());
                if (spawnCondition instanceof SpawnConditionNearbyEntities)
                    currentSection.set("Conditions.Max Entities Around Spawner", ((SpawnConditionNearbyEntities) spawnCondition).getMax());
                if (spawnCondition instanceof SpawnConditionNearbyPlayers)
                    currentSection.set("Conditions.Required Player Distance And Amount", ((SpawnConditionNearbyPlayers) spawnCondition).getDistance() + ":" + ((SpawnConditionNearbyPlayers) spawnCondition).getAmount());
            }

            if (spawnerData.getDisplayItem() != null) {
                currentSection.set("Display-Item", spawnerData.getDisplayItem().name());
            }
        }
        this.spawnerConfig.save();
    }

    public Config getSpawnerConfig() {
        return spawnerConfig;
    }

    public void reloadFromFile() {
        getSpawnerConfig().load();
        loadSpawnerDataFromFile();
    }
}

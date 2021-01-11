package com.songoda.epicspawners.spawners.spawner;

import com.songoda.core.compatibility.CompatibleBiome;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.configuration.Config;
import com.songoda.core.nms.NmsManager;
import com.songoda.core.nms.nbt.NBTItem;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.particles.ParticleDensity;
import com.songoda.epicspawners.particles.ParticleEffect;
import com.songoda.epicspawners.particles.ParticleType;
import com.songoda.epicspawners.spawners.condition.SpawnCondition;
import com.songoda.epicspawners.spawners.condition.SpawnConditionBiome;
import com.songoda.epicspawners.spawners.condition.SpawnConditionHeight;
import com.songoda.epicspawners.spawners.condition.SpawnConditionLightDark;
import com.songoda.epicspawners.spawners.condition.SpawnConditionNearbyEntities;
import com.songoda.epicspawners.spawners.condition.SpawnConditionNearbyPlayers;
import com.songoda.epicspawners.spawners.condition.SpawnConditionStorm;
import com.songoda.epicspawners.utils.SpawnerDataBuilder;
import com.songoda.epicspawners.utils.SpawnerTierBuilder;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Location;
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
    private final Map<Location, PlacedSpawner> spawnersInWorld = new HashMap<>();

    // This is the map that holds the cooldowns for picking up stuffs
    private final List<PlacedSpawner> pickingUp = new ArrayList<>();

    private final Config spawnerConfig = new Config(EpicSpawners.getInstance(), "spawners.yml");

    public SpawnerManager(EpicSpawners plugin) {
        this.plugin = plugin;
        spawnerConfig.load();
        Arrays.stream(EntityType.values()).filter(entityType -> entityType.isSpawnable()
                && entityType.isAlive()
                && entityType != EntityType.ARMOR_STAND).forEach(this::processDefaultSpawner);

        saveSpawnerDataToFile();

        loadSpawnerDataFromFile();
    }

    public SpawnerData getSpawnerData(String name) {
        return registeredSpawnerData.values().stream().filter(spawnerData -> spawnerData.getIdentifyingName()
                .equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public SpawnerData getSpawnerData(EntityType type) {
        return getSpawnerData(type.name());
    }

    public SpawnerTier getSpawnerTier(ItemStack item) {
        if (item == null) return null;

        NBTItem nbtItem = NmsManager.getNbt().of(item);
        if (nbtItem.has("data")) {
            String type = nbtItem.getString("data");
            SpawnerData data = getSpawnerData(type);
            if (data != null && nbtItem.has("tier"))
                return data.getTierOrFirst(nbtItem.getString("tier"));
        }

        BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
        CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();
        return getSpawnerData(cs.getSpawnedType()).getFirstTier();
    }

    public SpawnerData addSpawnerData(String name, SpawnerData SpawnerData) {
        this.registeredSpawnerData.put(name.toLowerCase(), SpawnerData);
        SpawnerData.reloadSpawnMethods();
        return SpawnerData;
    }

    public void addSpawnerData(SpawnerData SpawnerData) {
        this.registeredSpawnerData.put(SpawnerData.getIdentifyingName().toLowerCase(), SpawnerData);
    }

    public void removeSpawnerData(String name) {
        registeredSpawnerData.remove(name.toLowerCase());
    }

    public Collection<SpawnerData> getAllSpawnerData() {
        return Collections.unmodifiableCollection(registeredSpawnerData.values());
    }

    public Collection<SpawnerData> getAllEnabledSpawnerData() {
        return registeredSpawnerData.values().stream().filter(SpawnerData::isActive).collect(Collectors.toList());
    }

    public boolean isSpawner(Location location) {
        return spawnersInWorld.containsKey(location);
    }

    public boolean isSpawnerData(String type) {
        return registeredSpawnerData.containsKey(type.toLowerCase());
    }

    public PlacedSpawner getSpawnerFromWorld(Location location) {
        return spawnersInWorld.get(location);
    }

    public void addSpawnerToWorld(Location location, PlacedSpawner spawner) {
        spawnersInWorld.put(location, spawner);
    }

    public PlacedSpawner removeSpawnerFromWorld(Location location) {
        return spawnersInWorld.remove(location);
    }

    public PlacedSpawner removeSpawnerFromWorld(PlacedSpawner spawner) {
        return spawnersInWorld.remove(spawner.getLocation());
    }

    public Collection<PlacedSpawner> getSpawners() {
        return Collections.unmodifiableCollection(spawnersInWorld.values());
    }

    public void addSpawners(Map<Location, PlacedSpawner> spawners) {
        spawnersInWorld.putAll(spawners);
    }

    public void addCooldown(PlacedSpawner spawner) {
        pickingUp.add(spawner);
    }

    public void removeCooldown(PlacedSpawner spawner) {
        pickingUp.remove(spawner);
    }

    public boolean hasCooldown(PlacedSpawner spawner) {
        return pickingUp.contains(spawner);
    }

    public int getAmountPlaced(Player player) {
        return Math.toIntExact(spawnersInWorld.values().stream().filter(spawner -> spawner.getPlacedBy() != null
                && player.getUniqueId().equals(spawner.getPlacedBy().getUniqueId())).count()) + 1;
    }

    private void processDefaultSpawner(EntityType type) {
        String typeString = type.name();
        FileConfiguration spawnerConfig = this.spawnerConfig.getFileConfig();

        if (spawnerConfig.isConfigurationSection("Spawners." + typeString))
            return;

        SpawnerData spawnerData = new SpawnerDataBuilder(typeString).setCustom(false)
                .setActive(true)
                .setKillGoal(0)
                .setConvertible(true)
                .convertRatio("45%")
                .setInShop(true)
                .shopOrder(0)
                .setCraftable(false)
                .setRecipe("AAAABAAAA")
                .setRecipeIngredients(Arrays.asList("A, IRON_BARS", "B, SPAWN_EGG"))
                .build();

        List<EntityType> entities = new ArrayList<>(Collections.singletonList(type));
        List<CompatibleMaterial> spawnBlocks = new ArrayList<>(Collections.singletonList(CompatibleMaterial.AIR));

        switch (typeString.toUpperCase()) {
            case "PIG":
            case "SHEEP":
            case "CHICKEN":
            case "COW":
            case "RABBIT":
            case "LLAMA":
            case "HORSE":
            case "CAT":
                spawnBlocks = new ArrayList<>(Collections.singletonList(CompatibleMaterial.GRASS_BLOCK));
                break;
            case "MUSHROOM_COW":
                spawnBlocks = new ArrayList<>(Collections.singletonList(CompatibleMaterial.MYCELIUM));
                break;
            case "SQUID":
            case "ELDER_GUARDIAN":
            case "COD":
            case "SALMON":
            case "PUFFERFISH":
            case "TROPICAL_FISH":
                spawnBlocks = new ArrayList<>(Collections.singletonList(CompatibleMaterial.WATER));
                break;
            case "OCELOT":
                spawnBlocks = new ArrayList<>(Arrays.asList(CompatibleMaterial.GRASS_BLOCK,
                        CompatibleMaterial.JUNGLE_LEAVES, CompatibleMaterial.ACACIA_LEAVES,
                        CompatibleMaterial.BIRCH_LEAVES, CompatibleMaterial.DARK_OAK_LEAVES,
                        CompatibleMaterial.OAK_LEAVES, CompatibleMaterial.SPRUCE_LEAVES));
                break;
            default:
                break;
        }

        SpawnerTierBuilder tierBuilder = new SpawnerTierBuilder(spawnerData)
                .setEntities(entities)
                .setSpawnBlocks(spawnBlocks)
                .setSpawnOnFire(false)
                .setPickupCost(0)
                .setPickDamage((short) 1)
                .setCostEconomy(10)
                .setCostLevels(2)
                .setTickRate("800:200")
                .setParticleEffect(ParticleEffect.valueOf("HALO"))
                .setSpawnEffectParticle(ParticleType.valueOf("REDSTONE"))
                .setEntitySpawnParticle(ParticleType.valueOf("SMOKE"))
                .setSpawnerSpawnParticle(ParticleType.valueOf("FIRE"))
                .setParticleDensity(ParticleDensity.valueOf("NORMAL"))
                .setParticleEffectBoostedOnly(true)
                .setSpawnLimit(-1)
                .setDisplayName(WordUtils.capitalizeFully(typeString.replace("_", " ")))
                .displayItem(CompatibleMaterial.AIR);

        SpawnerTier tier = tierBuilder.build();

        if (type == EntityType.SLIME) {
            tier.addCondition(new SpawnConditionBiome(CompatibleBiome.SWAMP.getBiome()));
            tier.addCondition(new SpawnConditionHeight(50, 70));
        } else {
            tier.addCondition(new SpawnConditionBiome(Biome.values()));
            tier.addCondition(new SpawnConditionHeight(0, 256));
        }
        if (Monster.class.isAssignableFrom(type.getEntityClass())) {
            tier.addCondition(new SpawnConditionLightDark(SpawnConditionLightDark.Type.DARK));
        } else {
            tier.addCondition(new SpawnConditionLightDark(SpawnConditionLightDark.Type.BOTH));
        }
        tier.addCondition(new SpawnConditionStorm(false));
        tier.addCondition(new SpawnConditionNearbyEntities(6));
        tier.addCondition(new SpawnConditionNearbyPlayers(16, 1));

        spawnerData.addTier(tier);

        registeredSpawnerData.put(typeString, spawnerData);
    }

    @SuppressWarnings("unchecked")
    public void loadSpawnerDataFromFile() {
        registeredSpawnerData.clear();
        // Register spawner data into SpawnerRegistry from configuration.
        FileConfiguration spawnerConfig = this.spawnerConfig.getFileConfig();

        if (!spawnerConfig.contains("Spawners")) return;
        for (String key : spawnerConfig.getConfigurationSection("Spawners").getKeys(false)) {
            ConfigurationSection currentSection = spawnerConfig.getConfigurationSection("Spawners." + key);

            SpawnerData spawnerData = new SpawnerDataBuilder(key).setCustom(currentSection.getBoolean("Custom", false))
                    .setActive(currentSection.getBoolean("Active", true))
                    .setKillGoal(currentSection.getInt("Custom-Goal", 0))
                    .setConvertible(currentSection.getBoolean("Convertible"))
                    .convertRatio(currentSection.getString("Convert-Ratio"))
                    .setInShop(currentSection.getBoolean("In-Shop", true))
                    .shopOrder(currentSection.getInt("Shop-Order", 0))
                    .shopPrice(currentSection.getDouble("Shop-Price", 1000))

                    .setCraftable(currentSection.getBoolean("Craftable", false))
                    .setRecipe(currentSection.getString("Recipe-Layout", "AAAABAAAA"))
                    .setRecipeIngredients(currentSection.getStringList("Recipe-Ingredients"))
                    .build();

            for (String tierKey : currentSection.getConfigurationSection("Tiers").getKeys(false)) {
                ConfigurationSection currentSection2 = currentSection.getConfigurationSection("Tiers." + tierKey);

                List<EntityType> entities = new ArrayList<>();
                List<CompatibleMaterial> blocks = new ArrayList<>();
                List<CompatibleMaterial> spawnBlocks = new ArrayList<>();
                List<ItemStack> items = (List<ItemStack>) currentSection2.getList("Items", new ArrayList<>());
                List<String> commands = currentSection2.getStringList("Command");

                for (String block : currentSection2.getStringList("Blocks")) {
                    CompatibleMaterial material = CompatibleMaterial.getMaterial(block.toUpperCase());
                    blocks.add(material == null ? CompatibleMaterial.AIR : material);
                }
                for (String block : currentSection2.getStringList("Spawn-Blocks")) {
                    spawnBlocks.add(CompatibleMaterial.getMaterial(block.toUpperCase().trim()));
                }
                for (String entity : currentSection2.getStringList("Entities")) {
                    try {
                        entities.add(EntityType.valueOf(entity));
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                SpawnerTierBuilder tierBuilder = new SpawnerTierBuilder(spawnerData)
                        .setEntities(entities).setBlocks(blocks).setItems(items).setCommands(commands)
                        .setSpawnBlocks(spawnBlocks)
                        .setSpawnOnFire(currentSection2.getBoolean("Spawn-On-Fire", false))
                        .setPickupCost(currentSection2.getDouble("Pickup-Cost", 0))
                        .setPickDamage((short) currentSection2.getInt("Pick-Damage", 1))
                        .setCostEconomy(currentSection2.getDouble("Cost-Economy", 10))
                        .setCostLevels(currentSection2.getInt("Cost-Levels", 2))
                        .setTickRate(currentSection2.getString("Tick-Rate", "800:200"))
                        .setParticleEffect(ParticleEffect.valueOf(currentSection2.getString("Spawn-Effect", "HALO")))
                        .setSpawnEffectParticle(ParticleType.valueOf(currentSection2.getString("Spawn-Effect-Particle", "REDSTONE")))
                        .setEntitySpawnParticle(ParticleType.valueOf(currentSection2.getString("Entity-Spawn-Particle", "SMOKE")))
                        .setSpawnerSpawnParticle(ParticleType.valueOf(currentSection2.getString("Spawner-Spawn-Particle", "FIRE")))
                        .setParticleDensity(ParticleDensity.valueOf(currentSection2.getString("Particle-Amount", "NORMAL")))
                        .setParticleEffectBoostedOnly(currentSection2.getBoolean("Particle-Effect-Boosted-Only", true))
                        .setSpawnLimit(currentSection2.getInt("Spawn-Limit", -1))
                        .setDisplayName(currentSection2.getString("Display-Name", key))
                        .displayItem(CompatibleMaterial.valueOf(currentSection2.getString(currentSection2.contains("Display-Item") ? "Display-Item" : "AIR")));

                SpawnerTier tier = tierBuilder.build();

                if (currentSection2.contains("Conditions")) {
                    String biomeString = currentSection2.getString("Conditions.Biomes");
                    Set<Biome> biomes;
                    if (biomeString.toLowerCase().equals("all"))
                        biomes = EnumSet.allOf(Biome.class);
                    else {
                        biomes = new HashSet<>();
                        for (String string : biomeString.split(", ")) {
                            if (!string.trim().equals(""))
                                biomes.add(CompatibleBiome.getBiome(string).getBiome());
                        }
                    }

                    String[] heightString = currentSection2.getString("Conditions.Height").split(":");
                    String[] playerString = currentSection2.getString("Conditions.Required Player Distance And Amount").split(":");

                    tier.addCondition(new SpawnConditionNearbyPlayers(Integer.parseInt(playerString[0]), Integer.parseInt(playerString[1])));
                    tier.addCondition(new SpawnConditionNearbyEntities(currentSection2.getInt("Conditions.Max Entities Around Spawner")));
                    tier.addCondition(new SpawnConditionBiome(biomes));
                    tier.addCondition(new SpawnConditionHeight(Integer.parseInt(heightString[0]), Integer.parseInt(heightString[1])));
                    tier.addCondition(new SpawnConditionLightDark(SpawnConditionLightDark.Type.valueOf(currentSection2.getString("Conditions.Light"))));
                    tier.addCondition(new SpawnConditionStorm(currentSection2.getBoolean("Conditions.Storm Only")));
                }
                spawnerData.addTier(tier);
            }
            addSpawnerData(key, spawnerData);
        }

        reloadSpawnerData();
    }

    public void reloadSpawnerData() {
        for (PlacedSpawner spawner : spawnersInWorld.values()) {
            for (SpawnerStack stack : spawner.getSpawnerStacks()) {
                stack.setTier(registeredSpawnerData.get(stack.getSpawnerData().getIdentifyingName().toLowerCase())
                        .getTierOrFirst(stack.getCurrentTier().getIdentifyingName()));
                plugin.getDataManager().updateSpawnerStack(stack);
            }
        }
    }

    public void saveSpawnerDataToFile() {
        // Save spawner settings
        FileConfiguration spawnerConfig = this.spawnerConfig.getFileConfig();

        ConfigurationSection spawnersSection = spawnerConfig.createSection("Spawners");

        if (spawnerConfig.contains("Spawners"))
            for (String spawnerName : spawnersSection.getKeys(false)) {
                if (registeredSpawnerData.containsKey(spawnerName))
                    spawnersSection.set(spawnerName, null);
            }


        for (SpawnerData spawnerData : getAllSpawnerData()) {
            ConfigurationSection currentSection = spawnersSection.createSection(spawnerData.getIdentifyingName());

            currentSection.set("Allowed", spawnerData.isActive());
            currentSection.set("Custom", spawnerData.isCustom());
            currentSection.set("Custom-Goal", spawnerData.getKillGoal());
            currentSection.set("Convertible", spawnerData.isConvertible());
            currentSection.set("Convert-Ratio", spawnerData.getConvertRatio());
            currentSection.set("In-Shop", spawnerData.isInShop());
            currentSection.set("Shop-Order", spawnerData.getShopOrder());
            currentSection.set("Shop-Price", spawnerData.getShopPrice());
            currentSection.set("Craftable", spawnerData.isCraftable());
            currentSection.set("Recipe-Layout", spawnerData.getRecipe());
            currentSection.set("Recipe-Ingredients", spawnerData.getRecipeIngredients());

            currentSection.set("Tiers", null);

            for (SpawnerTier spawnerTier : spawnerData.getTiers()) {
                ConfigurationSection currentSection2 = currentSection.createSection("Tiers." + spawnerTier.getIdentifyingName());

                currentSection2.set("Entities", spawnerTier.getEntities().stream().map(Enum::name).collect(Collectors.toList()));
                currentSection2.set("Blocks", spawnerTier.getBlocks().stream().map(Enum::name).collect(Collectors.toList()));
                currentSection2.set("Items", spawnerTier.getItems());
                currentSection2.set("Command", spawnerTier.getCommands());

                currentSection2.set("Display-Name", spawnerTier.getDisplayName());

                currentSection2.set("Spawn-Blocks", spawnerTier.getSpawnBlocksList().stream().map(Enum::name).collect(Collectors.toList()));
                currentSection2.set("Spawn-On-Fire", spawnerTier.isSpawnOnFire());
                currentSection2.set("Cost-Economy", spawnerTier.getCostEconomy());
                currentSection2.set("Cost-Levels", spawnerTier.getCostLevels());
                currentSection2.set("Tick-Rate", spawnerTier.getTickRate());
                currentSection2.set("Pickup-Cost", spawnerTier.getPickupCost());
                currentSection2.set("Pick-Damage", spawnerTier.getPickDamage());

                currentSection2.set("Spawn-Effect", spawnerTier.getParticleEffect().name());
                currentSection2.set("Spawn-Effect-Particle", spawnerTier.getSpawnEffectParticle().name());
                currentSection2.set("Entity-Spawn-Particle", spawnerTier.getEntitySpawnParticle().name());
                currentSection2.set("Spawner-Spawn-Particle", spawnerTier.getSpawnerSpawnParticle().name());
                currentSection2.set("Particle-Amount", spawnerTier.getParticleDensity().name());
                currentSection2.set("Particle-Effect-Boosted-Only", spawnerTier.isParticleEffectBoostedOnly());

                for (SpawnCondition spawnCondition : spawnerTier.getConditions()) {
                    if (spawnCondition instanceof SpawnConditionBiome) {
                        if (EnumSet.allOf(Biome.class).equals(((SpawnConditionBiome) spawnCondition).getBiomes())) {
                            currentSection2.set("Conditions.Biomes", "ALL");
                        } else {
                            currentSection2.set("Conditions.Biomes", String.join(", ", ((SpawnConditionBiome) spawnCondition).getBiomes().stream().map(Enum::name).collect(Collectors.toSet())));
                        }
                    }
                    if (spawnCondition instanceof SpawnConditionHeight)
                        currentSection2.set("Conditions.Height", ((SpawnConditionHeight) spawnCondition).getMin() + ":" + ((SpawnConditionHeight) spawnCondition).getMax());
                    if (spawnCondition instanceof SpawnConditionLightDark)
                        currentSection2.set("Conditions.Light", ((SpawnConditionLightDark) spawnCondition).getType().name());
                    if (spawnCondition instanceof SpawnConditionStorm)
                        currentSection2.set("Conditions.Storm Only", ((SpawnConditionStorm) spawnCondition).isStormOnly());
                    if (spawnCondition instanceof SpawnConditionNearbyEntities)
                        currentSection2.set("Conditions.Max Entities Around Spawner", ((SpawnConditionNearbyEntities) spawnCondition).getMax());
                    if (spawnCondition instanceof SpawnConditionNearbyPlayers)
                        currentSection2.set("Conditions.Required Player Distance And Amount", ((SpawnConditionNearbyPlayers) spawnCondition).getDistance() + ":" + ((SpawnConditionNearbyPlayers) spawnCondition).getAmount());
                }

                if (spawnerTier.getDisplayItem() != null) {
                    currentSection2.set("Display-Item", spawnerTier.getDisplayItem().name());
                }
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

package com.craftaro.epicspawners.spawners.spawner;

import com.craftaro.core.compatibility.CompatibleBiome;
import com.craftaro.core.configuration.Config;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.third_party.de.tr7zw.nbtapi.NBTItem;
import com.craftaro.third_party.org.apache.commons.text.WordUtils;
import com.craftaro.core.utils.EntityUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.particles.ParticleDensity;
import com.craftaro.epicspawners.api.particles.ParticleEffect;
import com.craftaro.epicspawners.api.particles.ParticleType;
import com.craftaro.epicspawners.api.spawners.condition.SpawnCondition;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerManager;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerStack;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.api.utils.SpawnerTierBuilder;
import com.craftaro.epicspawners.spawners.condition.SpawnConditionBiome;
import com.craftaro.epicspawners.spawners.condition.SpawnConditionHeight;
import com.craftaro.epicspawners.spawners.condition.SpawnConditionLightDark;
import com.craftaro.epicspawners.spawners.condition.SpawnConditionNearbyEntities;
import com.craftaro.epicspawners.spawners.condition.SpawnConditionNearbyPlayers;
import com.craftaro.epicspawners.spawners.condition.SpawnConditionStorm;
import com.craftaro.epicspawners.utils.SpawnerDataBuilderImpl;
import com.craftaro.epicspawners.utils.SpawnerTierBuilderImpl;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SpawnerManagerImpl implements SpawnerManager {
    private final EpicSpawners plugin;

    // These are the spawner types loaded into memory.
    private final Map<String, SpawnerData> registeredSpawnerData = new LinkedHashMap<>();

    // These are spawners that exist in the game world.
    private final Map<Location, PlacedSpawner> spawnersInWorld = new HashMap<>();

    // This is the map that holds the cooldowns for picking up stuffs
    private final List<PlacedSpawner> pickingUp = new ArrayList<>();

    private final Config spawnerConfig = new Config(EpicSpawners.getInstance(), "spawners.yml");

    private String lastLoad = null;

    public SpawnerManagerImpl(EpicSpawners plugin) {
        this.plugin = plugin;
        this.spawnerConfig.load();
        Arrays.stream(EntityType.values()).filter(entityType -> entityType.isSpawnable()
                && entityType.isAlive()
                && entityType != EntityType.ARMOR_STAND).forEach(this::processDefaultSpawner);

        saveSpawnerDataToFile();

        loadSpawnerDataFromFile();
    }

    @Override
    public SpawnerData getSpawnerData(String name) {
        return this.registeredSpawnerData.values()
                .stream()
                .filter(spawnerData -> spawnerData.getIdentifyingName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public SpawnerData getSpawnerData(EntityType type) {
        if (type == null) return null; //TODO fix this. Not sure why this is happening.
        return getSpawnerData(type.name());
    }

    @Override
    public SpawnerTier getSpawnerTier(ItemStack item) {
        if (item == null) {
            return null;
        }

        NBTItem nbtItem = new NBTItem(item);
        if (nbtItem.hasTag("data")) {
            String type = nbtItem.getString("data");
            SpawnerData data = getSpawnerData(type);
            if (data != null && nbtItem.hasTag("tier")) {
                return data.getTierOrFirst(nbtItem.getString("tier"));
            }
        }

        BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
        if (bsm == null) {
            return null;
        }
        CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();
        return getSpawnerData(cs.getSpawnedType()).getFirstTier();
    }

    @Override
    public SpawnerData addSpawnerData(String name, SpawnerData spawnerData) {
        this.registeredSpawnerData.put(name.toLowerCase(), spawnerData);
        spawnerData.reloadSpawnMethods();
        return spawnerData;
    }

    @Override
    public void addSpawnerData(SpawnerData spawnerData) {
        this.registeredSpawnerData.put(spawnerData.getIdentifyingName().toLowerCase(), spawnerData);
    }

    @Override
    public void removeSpawnerData(String name) {
        this.registeredSpawnerData.remove(name.toLowerCase());
    }

    @Override
    public Collection<SpawnerData> getAllSpawnerData() {
        return Collections.unmodifiableCollection(this.registeredSpawnerData.values());
    }

    @Override
    public Collection<SpawnerData> getAllEnabledSpawnerData() {
        return this.registeredSpawnerData.values().stream().filter(SpawnerData::isActive).collect(Collectors.toList());
    }

    @Override
    public boolean isSpawner(Location location) {
        return this.spawnersInWorld.containsKey(location);
    }

    @Override
    public boolean isSpawnerData(String type) {
        return this.registeredSpawnerData.containsKey(type.toLowerCase());
    }

    @Override
    public PlacedSpawner getSpawnerFromWorld(Location location) {
        return this.spawnersInWorld.get(location);
    }

    @Override
    public void addSpawnerToWorld(Location location, PlacedSpawner spawner) {
        this.spawnersInWorld.put(location, spawner);
    }

    @Override
    public PlacedSpawner removeSpawnerFromWorld(Location location) {
        return this.spawnersInWorld.remove(location);
    }

    @Override
    public PlacedSpawner removeSpawnerFromWorld(PlacedSpawner spawner) {
        return this.spawnersInWorld.remove(spawner.getLocation());
    }

    @Override
    public Collection<PlacedSpawner> getSpawners() {
        return Collections.unmodifiableCollection(this.spawnersInWorld.values());
    }

    @Override
    public void addSpawners(Map<Location, PlacedSpawner> spawners) {
        this.spawnersInWorld.putAll(spawners);
    }

    @Override
    public void addSpawners(List<PlacedSpawner> spawners) {
        spawners.forEach(spawner -> this.spawnersInWorld.put(spawner.getLocation(), (PlacedSpawnerImpl) spawner));
    }

    @Override
    public void addCooldown(PlacedSpawner spawner) {
        this.pickingUp.add(spawner);
    }

    @Override
    public void removeCooldown(PlacedSpawner spawner) {
        this.pickingUp.remove(spawner);
    }

    @Override
    public boolean hasCooldown(PlacedSpawner spawner) {
        return this.pickingUp.contains(spawner);
    }

    @Override
    public int getAmountPlaced(Player player) {
        return Math.toIntExact(this.spawnersInWorld.values().stream().filter(spawner -> spawner.getPlacedBy() != null
                && player.getUniqueId().equals(spawner.getPlacedBy().getUniqueId())).count()) + 1;
    }

    private void processDefaultSpawner(EntityType type) {
        String typeString = type.name();
        FileConfiguration spawnerConfig = this.spawnerConfig.getFileConfig();

        if (spawnerConfig.isConfigurationSection("Spawners." + typeString)) {
            return;
        }

        SpawnerData spawnerData = new SpawnerDataBuilderImpl(typeString).setCustom(false)
                .setActive(true)
                .setKillDropGoal(0)
                .setKillDropChance(0)
                .setConvertible(true)
                .convertRatio("45%")
                .setInShop(true)
                .shopOrder(0)
                .setCraftable(false)
                .setRecipe("AAAABAAAA")
                .setRecipeIngredients(Arrays.asList("A, IRON_BARS", "B, SPAWN_EGG"))
                .build();

        List<EntityType> entities = new ArrayList<>(Collections.singletonList(type));
        List<XMaterial> spawnBlocks;

        spawnBlocks = EntityUtils.getSpawnBlocks(type);

        SpawnerTierBuilder tierBuilder = new SpawnerTierBuilderImpl(spawnerData)
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
                .displayItem(XMaterial.AIR);

        SpawnerTier tier = tierBuilder.build();

        if (type == EntityType.SLIME) {
            tier.addCondition(new SpawnConditionBiome(CompatibleBiome.SWAMP.getBiome()));
            tier.addCondition(new SpawnConditionHeight(50, 70));
        } else {
            tier.addCondition(new SpawnConditionBiome(Biome.values()));
            // TODO: These values should probably be *world* dependent as even in older versions, the max build height could be higher (vanilla spawners probably work up there too?)
            tier.addCondition(new SpawnConditionHeight(-64, 320));
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

        this.registeredSpawnerData.put(typeString, spawnerData);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadSpawnerDataFromFile() {
        this.registeredSpawnerData.clear();
        // Register spawner data into SpawnerRegistry from configuration.
        FileConfiguration spawnerConfig = this.spawnerConfig.getFileConfig();

        this.lastLoad = spawnerConfig.saveToString();

        if (!spawnerConfig.contains("Spawners")) {
            return;
        }
        for (String key : spawnerConfig.getConfigurationSection("Spawners").getKeys(false)) {
            ConfigurationSection currentSection = spawnerConfig.getConfigurationSection("Spawners." + key);

            SpawnerData spawnerData = new SpawnerDataBuilderImpl(key).setCustom(currentSection.getBoolean("Custom", false))
                    .setActive(currentSection.getBoolean("Active", true))
                    .setKillDropGoal(currentSection.getInt("Kill-Drop-Goal", 0))
                    .setKillDropChance(currentSection.getDouble("Kill-Drop-Chance", 0))
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
                List<XMaterial> blocks = new ArrayList<>();
                List<XMaterial> spawnBlocks = new ArrayList<>();
                List<ItemStack> items = (List<ItemStack>) currentSection2.getList("Items", new ArrayList<>());
                List<String> commands = currentSection2.getStringList("Command");

                for (String block : currentSection2.getStringList("Blocks")) {
                    Optional<XMaterial> material = XMaterial.matchXMaterial(block.toUpperCase());
                    blocks.add(material.orElse(XMaterial.AIR));
                }
                for (String block : currentSection2.getStringList("Spawn-Blocks")) {
                    XMaterial.matchXMaterial(block.toUpperCase().trim()).ifPresent(spawnBlocks::add);
                }
                for (String entity : currentSection2.getStringList("Entities")) {
                    try {
                        entities.add(EntityType.valueOf(entity));
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                SpawnerTierBuilder tierBuilder = new SpawnerTierBuilderImpl(spawnerData)
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
                        .displayItem(XMaterial.valueOf(currentSection2.getString(currentSection2.contains("Display-Item") ? "Display-Item" : "AIR")));

                SpawnerTier tier = tierBuilder.build();

                if (currentSection2.contains("Conditions")) {
                    String biomeString = currentSection2.getString("Conditions.Biomes");
                    Set<Biome> biomes;
                    if ("ALL".equalsIgnoreCase(biomeString)) {
                        biomes = EnumSet.allOf(Biome.class);
                    } else {
                        biomes = new HashSet<>();
                        for (String string : biomeString.split(", ")) {
                            if (!string.trim().isEmpty()) {
                                biomes.add(CompatibleBiome.getBiome(string).getBiome());
                            }
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

    @Override
    public void reloadSpawnerData() {
        for (PlacedSpawner spawner : this.spawnersInWorld.values()) {
            for (SpawnerStack stack : spawner.getSpawnerStacks()) {
                stack.setTier(this.registeredSpawnerData.get(stack.getSpawnerData().getIdentifyingName().toLowerCase())
                        .getTierOrFirst(stack.getCurrentTier().getIdentifyingName()));
                this.plugin.getDataManager().save(stack, "spawner_id", spawner.getId());
            }
        }
    }

    @Override
    public void saveSpawnerDataToFile() {
        // Save spawner settings
        FileConfiguration spawnerConfig = this.spawnerConfig.getFileConfig();

        ConfigurationSection spawnersSection = spawnerConfig.createSection("Spawners");

        if (spawnerConfig.contains("Spawners")) {
            for (String spawnerName : spawnersSection.getKeys(false)) {
                if (this.registeredSpawnerData.containsKey(spawnerName)) {
                    spawnersSection.set(spawnerName, null);
                }
            }
        }


        for (SpawnerData spawnerData : getAllSpawnerData()) {
            ConfigurationSection currentSection = spawnersSection.createSection(spawnerData.getIdentifyingName());

            currentSection.set("Active", spawnerData.isActive());
            currentSection.set("Custom", spawnerData.isCustom());
            currentSection.set("Kill-Drop-Goal", spawnerData.getKillDropGoal());
            currentSection.set("Kill-Drop-Chance", spawnerData.getKillDropChance());
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
                    if (spawnCondition instanceof SpawnConditionHeight) {
                        currentSection2.set("Conditions.Height", ((SpawnConditionHeight) spawnCondition).getMin() + ":" + ((SpawnConditionHeight) spawnCondition).getMax());
                    }
                    if (spawnCondition instanceof SpawnConditionLightDark) {
                        currentSection2.set("Conditions.Light", ((SpawnConditionLightDark) spawnCondition).getType().name());
                    }
                    if (spawnCondition instanceof SpawnConditionStorm) {
                        currentSection2.set("Conditions.Storm Only", ((SpawnConditionStorm) spawnCondition).isStormOnly());
                    }
                    if (spawnCondition instanceof SpawnConditionNearbyEntities) {
                        currentSection2.set("Conditions.Max Entities Around Spawner", ((SpawnConditionNearbyEntities) spawnCondition).getMax());
                    }
                    if (spawnCondition instanceof SpawnConditionNearbyPlayers) {
                        currentSection2.set("Conditions.Required Player Distance And Amount", ((SpawnConditionNearbyPlayers) spawnCondition).getDistance() + ":" + ((SpawnConditionNearbyPlayers) spawnCondition).getAmount());
                    }
                }

                if (spawnerTier.getDisplayItem() != null) {
                    currentSection2.set("Display-Item", spawnerTier.getDisplayItem().name());
                }
            }
        }
        this.spawnerConfig.save();
    }

    @Override
    public boolean wasConfigModified() {
        getSpawnerConfig().load();
        return !this.spawnerConfig.getFileConfig().saveToString().equals(this.lastLoad);
    }

    @Override
    public Config getSpawnerConfig() {
        return this.spawnerConfig;
    }

    @Override
    public void reloadFromFile() {
        getSpawnerConfig().load();
        loadSpawnerDataFromFile();
    }

    @Override
    public PlacedSpawner getSpawner(int id) {
        return this.spawnersInWorld.values().stream().filter(spawner -> spawner.getId() == id).findFirst().orElse(null);
    }

    @Override
    public PlacedSpawner getSpawner(Location location) {
        return this.spawnersInWorld.get(location);
    }
}

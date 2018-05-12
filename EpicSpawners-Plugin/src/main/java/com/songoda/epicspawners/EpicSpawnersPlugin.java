package com.songoda.epicspawners;

import com.songoda.arconix.api.mcupdate.MCUpdate;
import com.songoda.arconix.api.utils.ConfigWrapper;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.api.*;
import com.songoda.epicspawners.api.particles.ParticleDensity;
import com.songoda.epicspawners.api.particles.ParticleEffect;
import com.songoda.epicspawners.api.particles.ParticleType;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerManager;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.boost.BoostData;
import com.songoda.epicspawners.boost.BoostManager;
import com.songoda.epicspawners.boost.BoostType;
import com.songoda.epicspawners.handlers.*;
import com.songoda.epicspawners.listeners.*;
import com.songoda.epicspawners.player.PlayerActionManager;
import com.songoda.epicspawners.player.PlayerData;
import com.songoda.epicspawners.spawners.Shop;
import com.songoda.epicspawners.spawners.SpawnManager;
import com.songoda.epicspawners.spawners.editor.SpawnerEditor;
import com.songoda.epicspawners.spawners.object.ESpawner;
import com.songoda.epicspawners.spawners.object.ESpawnerData;
import com.songoda.epicspawners.spawners.object.ESpawnerManager;
import com.songoda.epicspawners.spawners.object.ESpawnerStack;
import com.songoda.epicspawners.utils.Heads;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Created by songoda on 2/25/2017.
 */
public class EpicSpawnersPlugin extends JavaPlugin implements EpicSpawners {
    public static CommandSender console = Bukkit.getConsoleSender();

    private static EpicSpawnersPlugin INSTANCE;

    public Map<String, Integer> cache = new HashMap<>();

    public List<Player> change = new ArrayList<>();

    public Map<Player, Integer> boostAmt = new HashMap<>();

    public boolean v1_7 = Bukkit.getServer().getClass().getPackage().getName().contains("1_7");
    public boolean v1_8 = Bukkit.getServer().getClass().getPackage().getName().contains("1_8");
    public boolean v1_9 = Bukkit.getServer().getClass().getPackage().getName().contains("1_9");
    public boolean v1_10 = Bukkit.getServer().getClass().getPackage().getName().contains("1_10");
    public boolean v1_11 = Bukkit.getServer().getClass().getPackage().getName().contains("1_12");
    public boolean v1_12 = Bukkit.getServer().getClass().getPackage().getName().contains("1_11");

    public String newSpawnerName = "";

    public Map<Player, String> chatEditing = new HashMap<>();

    public Map<Player, SpawnerData> inShow = new HashMap<>();
    public Map<Player, Integer> page = new HashMap<>();

    private ConfigWrapper langFile = new ConfigWrapper(this, "", "lang.yml");
    public ConfigWrapper dataFile = new ConfigWrapper(this, "", "data.yml");
    public ConfigWrapper spawnerFile = new ConfigWrapper(this, "", "spawners.yml");

    public References references = null;


    private BlacklistHandler blacklistHandler;
    private HookHandler hookHandler;
    private SpawnerEditor spawnerEditor;
    private Heads heads;
    private SettingsManager settingsManager;
    private Shop shop;

    private Locale locale;

    private SpawnManager spawnManager;
    private PlayerActionManager playerActionManager;
    private SpawnerManager spawnerManager;
    private HologramHandler hologramHandler;
    private ParticleHandler particleHandler;
    private BoostManager boostManager;
    private AppearanceHandler appearanceHandler;

    public void onDisable() {
        saveToFile();
        // TODO: Save SpawnerRegistryData contents to file
        //this.spawnerRegistry.clearRegistry();
        console.sendMessage(Arconix.pl().getApi().format().formatText("&a============================="));
        console.sendMessage(Arconix.pl().getApi().format().formatText("&7EpicSpawners " + this.getDescription().getVersion() + " by &5Songoda <3!"));
        console.sendMessage(Arconix.pl().getApi().format().formatText("&7Action: &cDisabling&7..."));
        console.sendMessage(Arconix.pl().getApi().format().formatText("&a============================="));
    }

    @SuppressWarnings("unchecked")
    public void onEnable() {
        EpicSpawnersAPI.setImplementation(this);
        INSTANCE = this;

        Arconix.pl().hook(this);

        console.sendMessage(Arconix.pl().getApi().format().formatText("&a============================="));
        console.sendMessage(Arconix.pl().getApi().format().formatText("&7EpicSpawners " + this.getDescription().getVersion() + " by &5Brianna <3&7!"));
        console.sendMessage(Arconix.pl().getApi().format().formatText("&7Action: &aEnabling&7..."));

        hookHandler = new HookHandler();
        hookHandler.hook();

        heads = new Heads(this);

        settingsManager = new SettingsManager();

        setupConfig();
        setupSpawners();

        // Locales
        Locale.init(this);
        Locale.saveDefaultLocale("en_US");
        this.locale = Locale.getLocale(this.getConfig().getString("Locale", "en_US"));

        langFile.createNewFile("Loading Language File", "EpicSpawners Language File");
        dataFile.createNewFile("Loading Data File", "EpicSpawners Data File");
        loadDataFile();

        blacklistHandler = new BlacklistHandler();
        references = new References();

        this.spawnManager = new SpawnManager();
        this.playerActionManager = new PlayerActionManager();
        this.boostManager = new BoostManager();
        this.spawnerManager = new ESpawnerManager();
        this.particleHandler = new ParticleHandler(this);

        hologramHandler = new HologramHandler(this);


        /*
         * Register spawner data into SpawnerRegistry from configuration.
         */
        if (spawnerFile.getConfig().contains("Entities")) {
            for (String key : spawnerFile.getConfig().getConfigurationSection("Entities").getKeys(false)) {

                List<EntityType> entities = new ArrayList<>();
                List<Material> blocks = new ArrayList<>();
                List<ItemStack> itemDrops = (List<ItemStack>) spawnerFile.getConfig().getList("Entities." + key + ".itemDrops", new ArrayList<>());
                List<ItemStack> items = (List<ItemStack>) spawnerFile.getConfig().getList("Entities." + key + ".items", new ArrayList<>());
                List<String> commands = (List<String>) spawnerFile.getConfig().getList("Entities." + key + ".commands", new ArrayList<>());

                if (spawnerFile.getConfig().contains("Entities." + key + ".blocks")) {
                    for (String block : spawnerFile.getConfig().getStringList("Entities." + key + ".blocks")) {
                        blocks.add(Material.valueOf(block));
                    }
                }
                if (spawnerFile.getConfig().contains("Entities." + key + ".entities")) {
                    for (String entity : spawnerFile.getConfig().getStringList("Entities." + key + ".entities")) {
                        entities.add(EntityType.valueOf(entity));
                    }
                }

                ESpawnerData data = new ESpawnerData(key, entities, blocks, items, itemDrops, commands);

                data.setSpawnBlocks(spawnerFile.getConfig().getString("Entities." + key + ".Spawn-Block").split(","));
                data.setActive(spawnerFile.getConfig().getBoolean("Entities." + key + ".Allowed"));
                if (spawnerFile.getConfig().contains("Entities." + key + ".Display-Name"))
                    data.setDisplayName(spawnerFile.getConfig().getString("Entities." + key + ".Display-Name"));
                data.setSpawnOnFire(spawnerFile.getConfig().getBoolean("Entities." + key + ".Spawn-On-Fire"));
                data.setUpgradeable(spawnerFile.getConfig().getBoolean("Entities." + key + ".Upgradable"));
                data.setConvertible(spawnerFile.getConfig().getBoolean("Entities." + key + ".Convertible"));
                data.setConvertRatio(spawnerFile.getConfig().getString("Entities." + key + ".Convert-Ratio"));
                data.setInShop(spawnerFile.getConfig().getBoolean("Entities." + key + ".In-Shop"));
                data.setPickupCost(spawnerFile.getConfig().getDouble("Entities." + key + ".Pickup-cost"));
                data.setShopPrice(spawnerFile.getConfig().getDouble("Entities." + key + ".Shop-Price"));
                data.setKillGoal(spawnerFile.getConfig().getInt("Entities." + key + ".CustomGoal"));
                data.setUpgradeCostEconomy(spawnerFile.getConfig().getInt("Entities." + key + ".Custom-ECO-Cost"));
                data.setUpgradeCostExperience(spawnerFile.getConfig().getInt("Entities." + key + ".Custom-XP-Cost"));
                data.setTickRate(spawnerFile.getConfig().getString("Entities." + key + ".Tick-Rate"));

                data.setParticleEffect(ParticleEffect.valueOf(spawnerFile.getConfig().getString("Entities." + key + ".Spawn-Effect", "HALO")));
                data.setSpawnEffectParticle(ParticleType.valueOf(spawnerFile.getConfig().getString("Entities." + key + ".Spawn-Effect-Particle", "REDSTONE")));
                data.setEntitySpawnParticle(ParticleType.valueOf(spawnerFile.getConfig().getString("Entities." + key + ".Entity-Spawn-Particle", "SMOKE")));
                data.setSpawnerSpawnParticle(ParticleType.valueOf(spawnerFile.getConfig().getString("Entities." + key + ".Spawner-Spawn-Particle", "FIRE")));

                data.setParticleDensity(ParticleDensity.valueOf(spawnerFile.getConfig().getString("Entities." + key + ".Particle-Amount", ParticleDensity.NORMAL.toString())));

                data.setParticleEffectBoostedOnly(spawnerFile.getConfig().getBoolean("Entities." + key + ".Particle-Effect-Boosted-Only"));

                if (spawnerFile.getConfig().contains("Entities." + key + ".Display-Item"))
                    data.setDisplayItem(Material.valueOf(spawnerFile.getConfig().getString("Entities." + key + ".Display-Item")));

                spawnerManager.addSpawnerData(key, data);
            }
        }

        int amtConverted = 0;
        System.out.println("Checking for legacy spawners...");
        if (dataFile.getConfig().contains("data.spawnerstats")) {

            /*
             * Adding in Legacy Spawners.
             */
            for (String key : dataFile.getConfig().getConfigurationSection("data.spawnerstats").getKeys(false)) {
                Location location = Arconix.pl().getApi().serialize().unserializeLocation(key);
                location.setX(location.getBlockX());
                location.setY(location.getBlockY());
                location.setZ(location.getBlockZ());

                if (location.getBlock().getType() != Material.MOB_SPAWNER) continue;

                CreatureSpawner spawnerState = (CreatureSpawner) location.getBlock().getState();

                String type = Methods.getType(spawnerState.getSpawnedType()).toLowerCase();

                // Is custom spawner.
                if (dataFile.getConfig().contains("data.spawnerstats." + key + ".type"))
                    type = dataFile.getConfig().getString("data.spawnerstats." + key + ".type").toLowerCase().replace("_", " ");

                ESpawner spawner = new ESpawner(location);

                try {
                    spawner.getCreatureSpawner().setSpawnedType(EntityType.valueOf(type.toUpperCase().replace(" ", "_")));
                } catch (Exception ex) {
                    spawner.getCreatureSpawner().setSpawnedType(EntityType.valueOf("PIG"));
                }

                if (!spawnerManager.isSpawnerData(type)) continue;

                SpawnerData spawnerData = spawnerManager.getSpawnerData(type);

                int stackSize = 1;

                if (dataFile.getConfig().contains("data.spawner." + key))
                    stackSize = dataFile.getConfig().getInt("data.spawner." + key);

                ESpawnerStack spawnerStack = new ESpawnerStack(spawnerData, stackSize);
                amtConverted++;

                if (dataFile.getConfig().contains("data.spawnerstats." + key + ".spawns"))
                    spawner.setSpawnCount(dataFile.getConfig().getInt("data.spawnerstats." + key + ".spawns"));

                if (dataFile.getConfig().contains("data.spawnerstats." + key + ".player"))
                    spawner.setPlacedBy(UUID.fromString(dataFile.getConfig().getString("data.spawnerstats." + key + ".player")));

                spawner.addSpawnerStack(spawnerStack);
                getInstance().getSpawnerManager().addSpawnerToWorld(location, spawner);
            }
        }

        if (amtConverted != 0) {
            System.out.println("Converted " + amtConverted + " legacy spawners...");
            dataFile.getConfig().set("data", null);
        } else {
            System.out.println("No legacy spawners found.");
        }


        /*
         * Adding in spawners.
         */
        if (dataFile.getConfig().contains("data.spawners")) {

            for (String key : dataFile.getConfig().getConfigurationSection("data.spawners").getKeys(false)) {
                Location location = Arconix.pl().getApi().serialize().unserializeLocation(key);

                if (location.getWorld() == null || location.getBlock().getType() != Material.MOB_SPAWNER) {
                    if (location.getWorld() != null && location.getBlock().getType() != Material.MOB_SPAWNER)
                        hologramHandler.despawn(location.getBlock());
                    continue;
                }

                ESpawner spawner = new ESpawner(location);

                for (String stackKey : dataFile.getConfig().getConfigurationSection("data.spawners." + key + ".Stacks").getKeys(false)) {
                    if (!spawnerManager.isSpawnerData(stackKey.toLowerCase())) continue;
                    spawner.addSpawnerStack(new ESpawnerStack(spawnerManager.getSpawnerData(stackKey), dataFile.getConfig().getInt("data.spawners." + key + ".Stacks." + stackKey)));
                }

                if (dataFile.getConfig().contains("data.spawners." + key + ".PlacedBy"))
                    spawner.setPlacedBy(UUID.fromString(dataFile.getConfig().getString("data.spawners." + key + ".PlacedBy")));

                spawner.setSpawnCount(dataFile.getConfig().getInt("data.spawners." + key + ".Spawns"));
                spawnerManager.addSpawnerToWorld(location, spawner);
            }
        }

        /*
         * Adding in Boosts
         */
        if (dataFile.getConfig().contains("data.boosts")) {
            for (String key : dataFile.getConfig().getConfigurationSection("data.boosts").getKeys(false)) {

                if (!dataFile.getConfig().contains("data.boosts." + key + ".BoostType")) continue;

                BoostData boostData = new BoostData(
                        BoostType.valueOf(dataFile.getConfig().getString("data.boosts." + key + ".BoostType")),
                        dataFile.getConfig().getInt("data.boosts." + key + ".Amount"),
                        Long.parseLong(key),
                        dataFile.getConfig().get("data.boosts." + key + ".Data"));

                boostManager.addBoostToSpawner(boostData);
            }
        }

        /*
         * Adding in Player Data
         */
        if (dataFile.getConfig().contains("data.players")) {
            for (String key : dataFile.getConfig().getConfigurationSection("data.players").getKeys(false)) {

                PlayerData playerData = playerActionManager.getPlayerAction(UUID.fromString(key));

                Map<EntityType, Integer> entityKills = new HashMap<>();
                for (String key2 : dataFile.getConfig().getConfigurationSection("data.players." + key + ".EntityKills").getKeys(false)) {
                    EntityType entityType = EntityType.valueOf(key2);
                    int amt = dataFile.getConfig().getInt("data.players." + key + ".EntityKills." + key2);
                    entityKills.put(entityType, amt);
                }
                playerData.setEntityKills(entityKills);
            }
        }

        // Save data initially so that if the person
        // reloads again fast they don't lose all their data.
        saveToFile();

        shop = new Shop(this);
        spawnerEditor = new SpawnerEditor(this);
        appearanceHandler = new AppearanceHandler();

        new MCUpdate(this, true);

        this.getCommand("EpicSpawners").setExecutor(new CommandHandler(this));
        this.getCommand("SpawnerStats").setExecutor(new CommandHandler(this));
        this.getCommand("SpawnerShop").setExecutor(new CommandHandler(this));

        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new BlockListeners(this), this);
        manager.registerEvents(new ChatListeners(this), this);
        manager.registerEvents(new EntityListeners(this), this);
        manager.registerEvents(new InteractListeners(this), this);
        manager.registerEvents(new InventoryListeners(this), this);
        manager.registerEvents(new SpawnerListeners(this), this);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::saveToFile, 6000, 6000);

        if (!v1_7) {
            getServer().getPluginManager().registerEvents(new TestListeners(), this);
        }
        console.sendMessage(Arconix.pl().getApi().format().formatText("&a============================="));
    }

    private void saveToFile() {
        //ToDO: If the defaults are set correctly this could do the initial config save.

        spawnerFile.getConfig().set("Entities", null);

        for (SpawnerData spawnerData : spawnerManager.getRegisteredSpawnerData().values()) {
            String key = spawnerData.getIdentifyingName();

            spawnerFile.getConfig().set("Entities." + key + ".Display-Name", spawnerData.getDisplayName());

            spawnerFile.getConfig().set("Entities." + key + ".blocks", getStrings(spawnerData.getBlocks()));
            spawnerFile.getConfig().set("Entities." + key + ".entities", getStrings(spawnerData.getEntities()));
            spawnerFile.getConfig().set("Entities." + key + ".itemDrops", spawnerData.getEntityDroppedItems());
            spawnerFile.getConfig().set("Entities." + key + ".items", spawnerData.getItems());
            spawnerFile.getConfig().set("Entities." + key + ".commands", spawnerData.getCommands());

            spawnerFile.getConfig().set("Entities." + key + ".Spawn-Block", String.join(", ", getStrings(spawnerData.getSpawnBlocksList())));
            spawnerFile.getConfig().set("Entities." + key + ".Allowed", spawnerData.isActive());
            spawnerFile.getConfig().set("Entities." + key + ".Spawn-On-Fire", spawnerData.isSpawnOnFire());
            spawnerFile.getConfig().set("Entities." + key + ".Upgradable", spawnerData.isUpgradeable());
            spawnerFile.getConfig().set("Entities." + key + ".Convertible", spawnerData.isConvertible());
            spawnerFile.getConfig().set("Entities." + key + ".Convert-Price", spawnerData.getConvertRatio());
            spawnerFile.getConfig().set("Entities." + key + ".In-Shop", spawnerData.isInShop());
            spawnerFile.getConfig().set("Entities." + key + ".Shop-Price", spawnerData.getShopPrice());
            spawnerFile.getConfig().set("Entities." + key + ".CustomGoal", spawnerData.getKillGoal());
            spawnerFile.getConfig().set("Entities." + key + ".Custom-ECO-Cost", spawnerData.getUpgradeCostExperience());
            spawnerFile.getConfig().set("Entities." + key + ".Custom-XP-Cost", spawnerData.getUpgradeCostExperience());
            spawnerFile.getConfig().set("Entities." + key + ".Tick-Rate", spawnerData.getTickRate());
            spawnerFile.getConfig().set("Entities." + key + ".Pickup-cost", spawnerData.getPickupCost());

            spawnerFile.getConfig().set("Entities." + key + ".Spawn-Effect", spawnerData.getParticleEffect().name());
            spawnerFile.getConfig().set("Entities." + key + ".Spawn-Effect-Particle", spawnerData.getSpawnEffectParticle().name());
            spawnerFile.getConfig().set("Entities." + key + ".Entity-Spawn-Particle", spawnerData.getEntitySpawnParticle().name());
            spawnerFile.getConfig().set("Entities." + key + ".Spawner-Spawn-Particle", spawnerData.getSpawnerSpawnParticle().name());

            spawnerFile.getConfig().set("Entities." + key + ".Particle-Amount", spawnerData.getParticleDensity().name());

            spawnerFile.getConfig().set("Entities." + key + ".Particle-Effect-Boosted-Only", spawnerData.isParticleEffectBoostedOnly());

            if (spawnerData.getDisplayItem() != null)
                spawnerFile.getConfig().set("Entities." + key + ".Display-Item", spawnerData.getDisplayItem().name());
        }

        spawnerFile.saveConfig();

        dataFile.getConfig().set("data", null);

        for (Spawner spawner : spawnerManager.getSpawnersInWorld().values()) {
            if (spawner.getFirstStack() == null
                    || spawner.getFirstStack().getSpawnerData() == null
                    || spawner.getLocation() == null
                    || spawner.getLocation().getWorld() == null) continue;
            try {

                String key = Arconix.pl().getApi().serialize().serializeLocation(spawner.getLocation());

                for (SpawnerStack stack : spawner.getSpawnerStacks()) {
                    dataFile.getConfig().set("data.spawners." + key + ".Stacks." + stack.getSpawnerData().getIdentifyingName(), stack.getStackSize());
                }

                dataFile.getConfig().set("data.spawners." + key + ".Spawns", spawner.getSpawnCount());

                if (spawner.getPlacedBy() != null)
                    dataFile.getConfig().set("data.spawners." + key + ".PlacedBy", spawner.getPlacedBy().getUniqueId().toString());
            } catch (Exception e) {
            }
        }

        for (BoostData boostData : boostManager.getBoosts()) {

            String key = boostData.getEndTime().toString();

            dataFile.getConfig().set("data.boosts." + key + ".BoostType", boostData.getBoostType().name());

            dataFile.getConfig().set("data.boosts." + key + ".Data", boostData.getData());

            dataFile.getConfig().set("data.boosts." + key + ".Amount", boostData.getAmtBoosted());
        }

        for (PlayerData playerData : playerActionManager.getRegisteredPlayers()) {

            for (Map.Entry<EntityType, Integer> entry : playerData.getEntityKills().entrySet()) {
                dataFile.getConfig().set("data.players." + playerData.getPlayer().getUniqueId().toString() + ".EntityKills." + entry.getKey().name(), entry.getValue());
            }
        }

        //ToDo: Save for player data.

        dataFile.saveConfig();
    }

    private String[] getStrings(List mats) {
        List<String> strings = new ArrayList<>();
        for (Object object : mats) {
            if (object instanceof Material) {
                strings.add(((Material) object).name());
            } else if (object instanceof EntityType) {
                strings.add(((EntityType) object).name());
            }
        }
        String[] stockArr = new String[strings.size()];
        return strings.toArray(stockArr);
    }

    private void setupConfig() {
        settingsManager.updateSettings();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void setupSpawners() {
        for (final EntityType value : EntityType.values()) {
            if (value.isSpawnable() && value.isAlive() &&
                    !value.toString().toLowerCase().contains("armor") &&
                    !value.toString().toLowerCase().contains("giant"))
                processDefault(value.name());
        }
        processDefault("Omni");
        spawnerFile.getConfig().options().copyDefaults(true);
        spawnerFile.saveConfig();
    }

    public void processDefault(String value) {
        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Display-Name"))
            spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(value) + ".Display-Name", Methods.getTypeFromString(value));
        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Pickup-cost"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Pickup-cost", 0);

        String spawnBlock = "AIR";

        if (value.equalsIgnoreCase("pig") || value.equalsIgnoreCase("sheep") || value.equalsIgnoreCase("chicken") ||
                value.equalsIgnoreCase("cow") || value.equalsIgnoreCase("rabbit") || value.equalsIgnoreCase("llamma") ||
                value.equalsIgnoreCase("horse") || value.equalsIgnoreCase("OCELOT")) {
            spawnBlock = "GRASS";
        }

        if (value.equalsIgnoreCase("MUSHROOM_COW")) {
            spawnBlock = "MYCEL";
        }

        if (value.equalsIgnoreCase("SQUID") || value.equalsIgnoreCase("ELDER_GUARDIAN")) {
            spawnBlock = "WATER";
        }

        if (value.equalsIgnoreCase("OCELOT")) {
            spawnBlock = ", LEAVES";
        }

        for (final EntityType val : EntityType.values()) {
            if (val.isSpawnable() && val.isAlive()) {
                if (val.name().equals(value)) {
                    List<String> list = new ArrayList<>();
                    list.add(value);
                    if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".entities"))
                        spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".entities", list);
                }
            }
        }

        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Spawn-Block"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Spawn-Block", spawnBlock);

        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Allowed"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Allowed", true);

        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Spawn-On-Fire"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Spawn-On-Fire", false);

        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Upgradable"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Upgradable", true);
        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Convertible"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Convertible", true);
        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Convert-Ratio"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Convert-Ratio", "45%");
        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".In-Shop"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".In-Shop", true);
        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Shop-Price"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Shop-Price", 1000.00);
        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".CustomGoal"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".CustomGoal", 0);
        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Custom-ECO-Cost"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Custom-ECO-Cost", 0);
        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Custom-XP-Cost"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Custom-XP-Cost", 0);
        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Tick-Rate"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Tick-Rate", "800:200");

        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Spawn-Effect"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Spawn-Effect", "NONE");
        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Spawn-Effect-Particle"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Spawn-Effect-Particle", "REDSTONE");
        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Entity-Spawn-Particle"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Entity-Spawn-Particle", "SMOKE");
        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Spawner-Spawn-Particle"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Spawner-Spawn-Particle", "FIRE");

        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Particle-Amount"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Particle-Amount", "NORMAL");

        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Particle-Effect-Boosted-Only"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Particle-Effect-Boosted-Only", false);
    }

    private void loadDataFile() {
        dataFile.getConfig().options().copyDefaults(true);
        dataFile.saveConfig();
    }

    public void reload() {
        locale.reloadMessages();
        langFile.createNewFile("Loading language file", "EpicSpawners language file");
        spawnerFile.createNewFile("Loading Spawners File", "EpicSpawners Spawners File");
        hookHandler.hooksFile.createNewFile("Loading hookHandler File", "EpicSpawners Spawners File");
        hookHandler = new HookHandler();
        hookHandler.hook();
        references = new References();
        blacklistHandler.reload();
        reloadConfig();
        saveConfig();
    }

    public Locale getLocale() {
        return locale;
    }

    public static EpicSpawnersPlugin getInstance() {
        return INSTANCE;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    @Override
    public SpawnerManager getSpawnerManager() {
        return spawnerManager;
    }

    public BoostManager getBoostManager() {
        return boostManager;
    }

    public PlayerActionManager getPlayerActionManager() {
        return playerActionManager;
    }

    public HologramHandler getHologramHandler() {
        return hologramHandler;
    }

    public AppearanceHandler getAppearanceHandler() {
        return appearanceHandler;
    }

    public SpawnerEditor getSpawnerEditor() {
        return spawnerEditor;
    }

    public BlacklistHandler getBlacklistHandler() {
        return blacklistHandler;
    }

    public HookHandler getHookHandler() {
        return hookHandler;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public Shop getShop() {
        return shop;
    }

    public Heads getHeads() {
        return heads;
    }
}

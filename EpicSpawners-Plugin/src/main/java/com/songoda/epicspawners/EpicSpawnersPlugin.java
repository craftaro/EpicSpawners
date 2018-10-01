package com.songoda.epicspawners;

import com.google.common.base.Preconditions;
import com.songoda.arconix.api.methods.Maths;
import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.api.methods.serialize.Serialize;
import com.songoda.arconix.api.utils.ConfigWrapper;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.api.EpicSpawners;
import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.api.particles.ParticleDensity;
import com.songoda.epicspawners.api.particles.ParticleEffect;
import com.songoda.epicspawners.api.particles.ParticleType;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerManager;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.api.spawner.condition.SpawnCondition;
import com.songoda.epicspawners.api.utils.ClaimableProtectionPluginHook;
import com.songoda.epicspawners.api.utils.ProtectionPluginHook;
import com.songoda.epicspawners.api.utils.SpawnerDataBuilder;
import com.songoda.epicspawners.boost.BoostData;
import com.songoda.epicspawners.boost.BoostManager;
import com.songoda.epicspawners.boost.BoostType;
import com.songoda.epicspawners.command.CommandManager;
import com.songoda.epicspawners.handlers.AppearanceHandler;
import com.songoda.epicspawners.handlers.BlacklistHandler;
import com.songoda.epicspawners.handlers.HologramHandler;
import com.songoda.epicspawners.hooks.*;
import com.songoda.epicspawners.listeners.*;
import com.songoda.epicspawners.player.PlayerActionManager;
import com.songoda.epicspawners.player.PlayerData;
import com.songoda.epicspawners.spawners.SpawnManager;
import com.songoda.epicspawners.spawners.condition.*;
import com.songoda.epicspawners.spawners.editor.SpawnerEditor;
import com.songoda.epicspawners.spawners.shop.Shop;
import com.songoda.epicspawners.spawners.spawner.ESpawner;
import com.songoda.epicspawners.spawners.spawner.ESpawnerManager;
import com.songoda.epicspawners.spawners.spawner.ESpawnerStack;
import com.songoda.epicspawners.storage.Storage;
import com.songoda.epicspawners.storage.StorageItem;
import com.songoda.epicspawners.storage.StorageRow;
import com.songoda.epicspawners.storage.types.StorageMysql;
import com.songoda.epicspawners.storage.types.StorageYaml;
import com.songoda.epicspawners.tasks.SpawnerParticleTask;
import com.songoda.epicspawners.tasks.SpawnerSpawnTask;
import com.songoda.epicspawners.utils.*;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Supplier;

public class EpicSpawnersPlugin extends JavaPlugin implements EpicSpawners {

    private static final Set<Biome> BIOMES = EnumSet.allOf(Biome.class);
    private static EpicSpawnersPlugin INSTANCE;
    private References references;

    private ChatListeners chatListeners;

    private ConfigWrapper spawnerFile = new ConfigWrapper(this, "", "spawners.yml");
    private ConfigWrapper hooksFile = new ConfigWrapper(this, "", "hooks.yml");

    private SpawnManager spawnManager;
    private PlayerActionManager playerActionManager;
    private SpawnerManager spawnerManager;
    private BoostManager boostManager;
    private SettingsManager settingsManager;
    private CommandManager commandManager;

    private BlacklistHandler blacklistHandler;
    private HologramHandler hologramHandler;
    private AppearanceHandler appearanceHandler;

    private SpawnerParticleTask particleTask;
    private SpawnerSpawnTask spawnerCustomSpawnTask;

    private SpawnerEditor spawnerEditor;
    private Heads heads;
    private Shop shop;
    private Locale locale;

    private List<ProtectionPluginHook> protectionHooks = new ArrayList<>();
    private ClaimableProtectionPluginHook factionsHook, townyHook, aSkyblockHook, uSkyblockHook;

    private Storage storage;

    public static EpicSpawnersPlugin getInstance() {
        return INSTANCE;
    }

    private boolean checkVersion() {
        int workingVersion = 13;
        int currentVersion = Integer.parseInt(Bukkit.getServer().getClass()
                .getPackage().getName().split("\\.")[3].split("_")[1]);

        if (currentVersion < workingVersion) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                Bukkit.getConsoleSender().sendMessage("");
                Bukkit.getConsoleSender().sendMessage(String.format("%sYou installed the 1.%s only version of %s on a 1.%s server. Since you are on the wrong version we disabled the plugin for you. Please install correct version to continue using %s.", ChatColor.RED, workingVersion, this.getDescription().getName(), currentVersion, this.getDescription().getName()));
                Bukkit.getConsoleSender().sendMessage("");
            }, 20L);
            return false;
        }
        return true;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        // Check to make sure the Bukkit version is compatible.
        if (!checkVersion()) return;

        EpicSpawnersAPI.setImplementation(this);

        Arconix.pl().hook(this);

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(TextComponent.formatText("&a============================="));
        console.sendMessage(TextComponent.formatText(String.format("&7%s %s by &5Brianna <3&7!", this.getName(), this.getDescription().getVersion())));
        console.sendMessage(TextComponent.formatText("&7Action: &aEnabling&7..."));

        this.heads = new Heads(this);
        this.settingsManager = new SettingsManager(this);

        this.setupConfig();
        this.setupSpawners();

        String langMode = getConfig().getString("System.Language Mode");
        Locale.init(this);
        Locale.saveDefaultLocale("en_US");
        Locale.saveDefaultLocale("fr_FR");
        Locale.saveDefaultLocale("pt_BR");
        Locale.saveDefaultLocale("es_ES");
        Locale.saveDefaultLocale("nl_NL");
        Locale.saveDefaultLocale("pr_FA");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));

        this.hooksFile.createNewFile("Loading Hooks File", "EpicSpawners Hooks File");

        this.references = new References();
        this.boostManager = new BoostManager();
        this.spawnManager = new SpawnManager();
        this.spawnerManager = new ESpawnerManager();
        this.commandManager = new CommandManager(this);
        this.blacklistHandler = new BlacklistHandler();
        this.hologramHandler = new HologramHandler(this);
        this.playerActionManager = new PlayerActionManager();
        this.chatListeners = new ChatListeners(this);

        loadSpawnersFromFile();

        checkStorage();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            // Adding in spawners.
            if (storage.containsGroup("spawners")) {
                for (StorageRow row : storage.getRowsByGroup("spawners")) {
                    try {
                        Location location = Serialize.getInstance().unserializeLocation(row.getKey());

                        if (location.getWorld() == null || location.getBlock().getType() != Material.SPAWNER) {
                            if (location.getWorld() != null && location.getBlock().getType() != Material.SPAWNER) {
                                this.hologramHandler.despawn(location.getBlock());
                            }
                            continue;
                        }

                        ESpawner spawner = new ESpawner(location);

                        for (String stackKey : row.get("stacks").asString().split(";")) {
                            if (stackKey == null) continue;
                            String[] stack = stackKey.split(":");
                            if (!spawnerManager.isSpawnerData(stack[0].toLowerCase())) continue;
                            spawner.addSpawnerStack(new ESpawnerStack(spawnerManager.getSpawnerData(stack[0]), Integer.parseInt(stack[1])));
                        }

                        if (row.getItems().containsKey("placedby"))
                            spawner.setPlacedBy(UUID.fromString(row.get("placedby").asString()));

                        spawner.setSpawnCount(row.get("spawns").asInt());
                        this.spawnerManager.addSpawnerToWorld(location, spawner);
                    } catch (Exception e) {
                        console.sendMessage("Failed to load spawner.");
                    }
                }
            }

            // Adding in Boosts
            if (storage.containsGroup("boosts")) {
                for (StorageRow row : storage.getRowsByGroup("boosts")) {
                    if (!row.getItems().containsKey("boosttype") || row.get("boosttype").asString().equals(""))
                        continue;

                    BoostData boostData = new BoostData(
                            BoostType.valueOf(row.get("boosttype").asString()),
                            row.get("amount").asInt(),
                            Long.parseLong(row.getKey()),
                            row.get("data").asObject());

                    this.boostManager.addBoostToSpawner(boostData);
                }
            }

            // Adding in Player Data
            if (storage.containsGroup("players")) {
                for (StorageRow row : storage.getRowsByGroup("players")) {
                    PlayerData playerData = playerActionManager.getPlayerAction(UUID.fromString(row.getKey()));

                    Map<EntityType, Integer> entityKills = new HashMap<>();
                    if (row.get("entitykills") == null || row.get("entitykills").asString().equals("")) continue;
                    for (String entityKillsKey : row.get("entitykills").asString().split(";")) {
                        if (entityKillsKey == null) continue;
                        String[] entityKills2 = entityKillsKey.split(":");
                        if (entityKills2[0] == null || entityKills2[0].equals("")) continue;
                        EntityType entityType = EntityType.valueOf(entityKills2[0]);
                        int amt = Integer.parseInt(entityKills2[1]);
                        entityKills.put(entityType, amt);
                    }

                    playerData.setEntityKills(entityKills);
                }
            }
            // Save data initially so that if the person reloads again fast they don't lose all their data.
            this.saveToFile();
        }, 10);

        this.shop = new Shop(this);
        this.spawnerEditor = new SpawnerEditor(this);
        this.appearanceHandler = new AppearanceHandler();

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Event registration
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(chatListeners, this);
        pluginManager.registerEvents(new EntityListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);
        pluginManager.registerEvents(new InventoryListeners(this), this);
        pluginManager.registerEvents(new SpawnerListeners(this), this);
        pluginManager.registerEvents(new PlayerJoinListeners(this), this);

        AbstractGUI.initializeListeners(this);

        // Register default hooks
        if (pluginManager.isPluginEnabled("ASkyBlock")) this.register(HookASkyBlock::new);
        if (pluginManager.isPluginEnabled("FactionsFramework")) this.register(HookFactions::new);
        if (pluginManager.isPluginEnabled("GriefPrevention")) this.register(HookGriefPrevention::new);
        if (pluginManager.isPluginEnabled("Kingdoms")) this.register(HookKingdoms::new);
        if (pluginManager.isPluginEnabled("PlotSquared")) this.register(HookPlotSquared::new);
        if (pluginManager.isPluginEnabled("RedProtect")) this.register(HookRedProtect::new);
        if (pluginManager.isPluginEnabled("Towny")) this.register(HookTowny::new);
        if (pluginManager.isPluginEnabled("USkyBlock")) this.register(HookUSkyBlock::new);
        if (pluginManager.isPluginEnabled("WorldGuard")) this.register(HookWorldGuard::new);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveToFile, 6000, 6000);

        // Start tasks
        this.particleTask = SpawnerParticleTask.startTask(this);
        this.spawnerCustomSpawnTask = SpawnerSpawnTask.startTask(this);

        console.sendMessage(TextComponent.formatText("&a============================="));

    }

    @Override
    public void onDisable() {
        this.saveToFile();
        this.storage.closeConnection();
        this.particleTask.cancel();
        this.protectionHooks.clear();
        this.spawnerCustomSpawnTask.cancel();

        //this.spawnerRegistry.clearRegistry();
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(TextComponent.formatText("&a============================="));
        console.sendMessage(TextComponent.formatText("&7EpicSpawners " + this.getDescription().getVersion() + " by &5Brianna <3!"));
        console.sendMessage(TextComponent.formatText("&7Action: &cDisabling&7..."));
        console.sendMessage(TextComponent.formatText("&a============================="));
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
                spawnBlocks.add(Material.matchMaterial(block.toUpperCase()));
            }
            for (String entity : currentSection.getStringList("entities")) {
                entities.add(EntityType.valueOf(entity));
            }

            SpawnerDataBuilder dataBuilder = new ESpawnerDataBuilder(key).uuid(currentSection.getInt("uuid"))
                    .entities(entities).blocks(blocks).items(items).entityDroppedItems(itemDrops).commands(commands)
                    .spawnBlocks(spawnBlocks)
                    .active(currentSection.getBoolean("Allowed"))
                    .spawnOnFire(currentSection.getBoolean("Spawn-On-Fire"))
                    .upgradeable(currentSection.getBoolean("Upgradable"))
                    .convertible(currentSection.getBoolean("Convertible"))
                    .convertRatio(currentSection.getString("Convert-Ratio"))
                    .inShop(currentSection.getBoolean("In-Shop"))
                    .pickupCost(currentSection.getDouble("Pickup-Cost"))
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
                    biomes = EnumSet.copyOf(BIOMES);
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

            this.spawnerManager.addSpawnerData(key, data);
        }
    }

    private void checkStorage() {
        if (getConfig().getBoolean("Database.Activate Mysql Support")) {
            this.storage = new StorageMysql(this);
        } else {
            this.storage = new StorageYaml(this);
        }
    }

    private void saveToFile() {

        this.storage.closeConnection();
        checkStorage();

        //ToDO: If the defaults are set correctly this could do the initial config save.

        FileConfiguration spawnerConfig = spawnerFile.getConfig();
        spawnerConfig.set("Entities", null);

        ConfigurationSection entitiesSection = spawnerConfig.createSection("Entities");
        for (SpawnerData spawnerData : spawnerManager.getAllSpawnerData()) {
            ConfigurationSection currentSection = entitiesSection.createSection(spawnerData.getIdentifyingName());

            currentSection.set("uuid", spawnerData.getUUID());
            currentSection.set("Display-Name", spawnerData.getDisplayName());

            currentSection.set("blocks", getStrings(spawnerData.getBlocks()));
            currentSection.set("entities", getStrings(spawnerData.getEntities()));
            currentSection.set("itemDrops", spawnerData.getEntityDroppedItems());
            currentSection.set("items", spawnerData.getItems());
            currentSection.set("command", spawnerData.getCommands());

            currentSection.set("Spawn-Block", String.join(", ", getStrings(spawnerData.getSpawnBlocksList())));
            currentSection.set("Allowed", spawnerData.isActive());
            currentSection.set("Spawn-On-Fire", spawnerData.isSpawnOnFire());
            currentSection.set("Upgradable", spawnerData.isUpgradeable());
            currentSection.set("Convertible", spawnerData.isConvertible());
            currentSection.set("Convert-Price", spawnerData.getConvertRatio());
            currentSection.set("In-Shop", spawnerData.isInShop());
            currentSection.set("Shop-Price", spawnerData.getShopPrice());
            currentSection.set("CustomGoal", spawnerData.getKillGoal());
            currentSection.set("Custom-ECO-Cost", spawnerData.getUpgradeCostEconomy());
            currentSection.set("Custom-XP-Cost", spawnerData.getUpgradeCostExperience());
            currentSection.set("Tick-Rate", spawnerData.getTickRate());
            currentSection.set("Pickup-cost", spawnerData.getPickupCost());

            currentSection.set("Spawn-Effect", spawnerData.getParticleEffect().name());
            currentSection.set("Spawn-Effect-Particle", spawnerData.getSpawnEffectParticle().name());
            currentSection.set("Entity-Spawn-Particle", spawnerData.getEntitySpawnParticle().name());
            currentSection.set("Spawner-Spawn-Particle", spawnerData.getSpawnerSpawnParticle().name());
            currentSection.set("Particle-Amount", spawnerData.getParticleDensity().name());
            currentSection.set("Particle-Effect-Boosted-Only", spawnerData.isParticleEffectBoostedOnly());


            for (SpawnCondition spawnCondition : spawnerData.getConditions()) {
                if (spawnCondition instanceof SpawnConditionBiome) {
                    if (BIOMES.equals(((SpawnConditionBiome) spawnCondition).getBiomes())) {
                        currentSection.set("Conditions.Biomes", "ALL");
                    } else {
                        currentSection.set("Conditions.Biomes", String.join(", ", getStrings(((SpawnConditionBiome) spawnCondition).getBiomes())));
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

        this.spawnerFile.saveConfig();

        storage.clearFile();

        for (Spawner spawner : spawnerManager.getSpawners()) {
            if (spawner.getFirstStack() == null
                    || spawner.getFirstStack().getSpawnerData() == null
                    || spawner.getLocation() == null
                    || spawner.getLocation().getWorld() == null) continue;

            StorageItem location = new StorageItem("location", Serialize.getInstance().serializeLocation(spawner.getLocation()));

            String stacksStr = "";
            for (SpawnerStack stack : spawner.getSpawnerStacks()) {
                stacksStr += stack.getSpawnerData().getIdentifyingName() + ":" + stack.getStackSize() + ";";
            }
            StorageItem stacks = new StorageItem("stacks", stacksStr);

            StorageItem placedBy = spawner.getPlacedBy() != null ? new StorageItem("placedby", spawner.getPlacedBy().getUniqueId().toString()) : null;

            storage.saveItem("spawners", location, stacks, new StorageItem("spawns", spawner.getSpawnCount()), placedBy);
        }

        for (BoostData boostData : boostManager.getBoosts()) {
            storage.saveItem("boosts", new StorageItem("endtime", String.valueOf(boostData.getEndTime())),
                    new StorageItem("boosttype", boostData.getBoostType().name()),
                    new StorageItem("data", boostData.getData()),
                    new StorageItem("amount", boostData.getAmtBoosted()));
        }

        for (PlayerData playerData : playerActionManager.getRegisteredPlayers()) {

            storage.saveItem("players", new StorageItem("uuid", playerData.getPlayer().getUniqueId().toString()),
                    new StorageItem("entitykills", playerData.getEntityKills()));
        }

    }

    private <T extends Enum<T>> String[] getStrings(List<T> mats) {
        List<String> strings = new ArrayList<>();

        for (Object object : mats) {
            if (object instanceof Material) {
                strings.add(((Material) object).name());
            } else if (object instanceof EntityType) {
                strings.add(((EntityType) object).name());
            }
        }

        return strings.toArray(new String[strings.size()]);
    }

    private String[] getStrings(Set<Biome> biomes) {
        List<String> strings = new ArrayList<>();

        for (Biome biome : biomes) {
            strings.add(biome.name());
        }

        return strings.toArray(new String[strings.size()]);
    }

    private void setupConfig() {
        this.settingsManager.updateSettings();
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
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

    private void register(Supplier<ProtectionPluginHook> hookSupplier) {
        this.registerProtectionHook(hookSupplier.get());
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
            spawnBlock = "MYCEL";
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


        if (entityType == EntityType.SLIME) {
            spawnerConfig.addDefault("Entities." + type + ".Conditions.Biomes", Biome.SWAMP);
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

    public void reload() {
        String langMode = getConfig().getString("System.Language Mode");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));
        this.locale.reloadMessages();
        this.spawnerFile.createNewFile("Loading Spawners File", "EpicSpawners Spawners File");
        this.hooksFile.createNewFile("Loading hookHandler File", "EpicSpawners Spawners File");
        this.references = new References();
        this.blacklistHandler.reload();
        this.loadSpawnersFromFile();
        this.reloadConfig();
        //this.saveConfig();
    }

    public Locale getLocale() {
        return locale;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
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

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public Shop getShop() {
        return shop;
    }

    public Heads getHeads() {
        return heads;
    }

    public References getReferences() {
        return references;
    }

    public ChatListeners getChatListeners() {
        return chatListeners;
    }

    public boolean canBuild(Player player, Location location) {
        if (player.hasPermission(getDescription().getName() + ".bypass")) {
            return true;
        }

        for (ProtectionPluginHook hook : protectionHooks)
            if (!hook.canBuild(player, location)) return false;
        return true;
    }

    public boolean isInFaction(String name, Location l) {
        return factionsHook != null && factionsHook.isInClaim(l, name);
    }

    public String getFactionId(String name) {
        return (factionsHook != null) ? factionsHook.getClaimID(name) : null;
    }

    public boolean isInTown(String name, Location l) {
        return townyHook != null && townyHook.isInClaim(l, name);
    }

    public String getTownId(String name) {
        return (townyHook != null) ? townyHook.getClaimID(name) : null;
    }

    public boolean isInIsland(String name, Location l) {
        return (aSkyblockHook != null && aSkyblockHook.isInClaim(l, name)) || (uSkyblockHook != null && uSkyblockHook.isInClaim(l, name));
    }

    @SuppressWarnings("deprecation")
    public String getIslandId(String name) {
        return Bukkit.getOfflinePlayer(name).getUniqueId().toString();
    }

    @Override
    public SpawnerManager getSpawnerManager() {
        return spawnerManager;
    }

    @Override
    public ItemStack newSpawnerItem(SpawnerData data, int amount) {
        return newSpawnerItem(data, amount, 1);
    }

    @Override
    public ItemStack newSpawnerItem(SpawnerData data, int amount, int stackSize) {
        Preconditions.checkArgument(stackSize > 0, "Stack size must be greater than or equal to 0");

        ItemStack item = new ItemStack(Material.SPAWNER, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Methods.compileName(data, stackSize, true));
        item.setItemMeta(meta);

        return item;
    }

    @Override
    public SpawnerData identifySpawner(String sid) {
        int id = Integer.parseInt(sid.replace(";", ""));
        for (SpawnerData data : spawnerManager.getAllSpawnerData()) {
            if (data.getUUID() == id)
                return data;
        }
        return null;
    }

    @Override
    public SpawnerData getSpawnerDataFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;


        String name = item.getItemMeta().getDisplayName();
        if (name == null) return null;

        if (name.contains(":")) {
            String value = name.replace(String.valueOf(ChatColor.COLOR_CHAR), "").replace(";", "").split(":")[0];
            if (Maths.isInt(value)) {
                return identifySpawner(value);
            }
            return spawnerManager.getSpawnerData(value.toLowerCase().replace("_", " "));
        }

        String typeName = name.replace("_", " ");
        for (EntityType type : EntityType.values()) {
            if (!type.isSpawnable() || !type.isAlive()) continue;

            if (typeName.contains(type.name().toLowerCase())) {
                return spawnerManager.getSpawnerData(type.name().toLowerCase().replace("_", " "));
            }
        }
        return null;
    }

    @Override
    public SpawnerDataBuilder createSpawnerData(String identifier) {
        return new ESpawnerDataBuilder(identifier);
    }

    @Override
    public int getStackSizeFromItem(ItemStack item) {
        Preconditions.checkNotNull(item, "Cannot get stack size of null item");
        if (!item.hasItemMeta()) return 1;

        String name = item.getItemMeta().getDisplayName();
        if (name == null || !name.contains(":")) return 1;

        String amount = name.replace(String.valueOf(ChatColor.COLOR_CHAR), "").replace(";", "").split(":")[1];
        return NumberUtils.toInt(amount, 1);
    }

    @Override
    public void registerProtectionHook(ProtectionPluginHook hook) {
        Preconditions.checkNotNull(hook, "Cannot register null hook");
        Preconditions.checkNotNull(hook.getPlugin(), "Protection plugin hook returns null plugin instance (#getPlugin())");

        JavaPlugin hookPlugin = hook.getPlugin();
        for (ProtectionPluginHook existingHook : protectionHooks) {
            if (existingHook.getPlugin().equals(hookPlugin)) {
                throw new IllegalArgumentException("Hook already registered");
            }
        }

        this.hooksFile.getConfig().addDefault("hooks." + hookPlugin.getName(), true);
        if (!hooksFile.getConfig().getBoolean("hooks." + hookPlugin.getName(), true)) return;
        this.hooksFile.getConfig().options().copyDefaults(true);
        this.hooksFile.saveConfig();

        this.protectionHooks.add(hook);
        this.getLogger().info("Registered protection hook for plugin: " + hook.getPlugin().getName());
    }
}

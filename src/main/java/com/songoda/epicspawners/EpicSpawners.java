package com.songoda.epicspawners;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.commands.CommandManager;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.configuration.Config;
import com.songoda.core.database.DataManagerAbstract;
import com.songoda.core.database.DataMigrationManager;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.core.database.SQLiteConnector;
import com.songoda.core.gui.GuiManager;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.core.hooks.EntityStackerManager;
import com.songoda.core.hooks.HologramManager;
import com.songoda.epicspawners.blacklist.BlacklistHandler;
import com.songoda.epicspawners.boost.BoostManager;
import com.songoda.epicspawners.boost.types.Boosted;
import com.songoda.epicspawners.boost.types.BoostedPlayer;
import com.songoda.epicspawners.boost.types.BoostedSpawner;
import com.songoda.epicspawners.commands.*;
import com.songoda.epicspawners.database.DataManager;
import com.songoda.epicspawners.database.migrations._1_InitialMigration;
import com.songoda.epicspawners.listeners.*;
import com.songoda.epicspawners.player.PlayerData;
import com.songoda.epicspawners.player.PlayerDataManager;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.SpawnManager;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerManager;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.storage.Storage;
import com.songoda.epicspawners.storage.StorageRow;
import com.songoda.epicspawners.storage.types.StorageYaml;
import com.songoda.epicspawners.tasks.AppearanceTask;
import com.songoda.epicspawners.tasks.SpawnerParticleTask;
import com.songoda.epicspawners.tasks.SpawnerSpawnTask;
import com.songoda.epicspawners.utils.Heads;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.*;

public class EpicSpawners extends SongodaPlugin {

    private static EpicSpawners INSTANCE;

    private final GuiManager guiManager = new GuiManager(this);
    private SpawnManager spawnManager;
    private PlayerDataManager playerActionManager;
    private SpawnerManager spawnerManager;
    private BoostManager boostManager;
    private CommandManager commandManager;

    private BlacklistHandler blacklistHandler;

    private AppearanceTask appearanceTask;
    private SpawnerParticleTask particleTask;
    private SpawnerSpawnTask spawnerCustomSpawnTask;

    private Heads heads;

    private DatabaseConnector databaseConnector;
    private DataMigrationManager dataMigrationManager;
    private DataManager dataManager;

    public static EpicSpawners getInstance() {
        return INSTANCE;
    }

    @Override
    public void onPluginLoad() {
        INSTANCE = this;
    }

    @Override
    public void onPluginDisable() {
        this.saveToFile();
        this.particleTask.cancel();
        this.spawnerCustomSpawnTask.cancel();
        this.databaseConnector.closeConnection();
        HologramManager.removeAllHolograms();
    }

    @Override
    public void onPluginEnable() {
        // Run Songoda Updater
        SongodaCore.registerPlugin(this, 13, CompatibleMaterial.SPAWNER);

        // Load Economy & Hologram hooks
        EconomyManager.load();
        HologramManager.load(this);

        // Setup Config
        Settings.setupConfig();
        this.setLocale(Settings.LANGUGE_MODE.getString(), false);

        // Load the entity stacker manager.
        EntityStackerManager.load();

        // Set Economy & Hologram preference
        EconomyManager.getManager().setPreferredHook(Settings.ECONOMY_PLUGIN.getString());
        HologramManager.getManager().setPreferredHook(Settings.HOLOGRAM_PLUGIN.getString());

        // Register commands
        this.commandManager = new CommandManager(this);
        this.commandManager.addMainCommand("es")
                .addSubCommands(
                        new CommandGive(this),
                        new CommandBoost(this),
                        new CommandEditor(this),
                        new CommandSettings(this),
                        new CommandReload(this),
                        new CommandChange(this),
                        new CommandSpawn(this)
                );
        this.commandManager.addCommand(new CommandSpawnerStats(this));
        this.commandManager.addCommand(new CommandSpawnerShop(this));

        this.heads = new Heads();

        this.boostManager = new BoostManager();
        this.spawnManager = new SpawnManager();
        this.spawnerManager = new SpawnerManager(this);
        this.blacklistHandler = new BlacklistHandler();
        this.playerActionManager = new PlayerDataManager();

        this.checkStorage();

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Listeners
        guiManager.init();
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new EntityListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);
        pluginManager.registerEvents(new InventoryListeners(this), this);
        pluginManager.registerEvents(new SpawnerListeners(this), this);

        AbstractGUI.initializeListeners(this);

        int timeout = Settings.AUTOSAVE.getInt() * 60 * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveToFile, timeout, timeout);

        // Start tasks
        this.particleTask = SpawnerParticleTask.startTask(this);
        this.spawnerCustomSpawnTask = SpawnerSpawnTask.startTask(this);
        this.appearanceTask = AppearanceTask.startTask(this);


        // ShopGUI+ support
        if (Bukkit.getPluginManager().isPluginEnabled("ShopGUIPlus")) {
            try {
                // For some reason simply creating a new instance of the class without ShopGUIPlus being installed was giving a NoClassDefFoundError.
                // We're using reflection to get around this problem.
                Object provider = Class.forName("com.songoda.epicspawners.utils.EpicSpawnerProvider").newInstance();
                net.brcdev.shopgui.ShopGuiPlusApi.registerSpawnerProvider((net.brcdev.shopgui.spawner.external.provider.ExternalSpawnerProvider) provider);
            } catch (Exception ignored) {
            }
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            // Legacy Data
            File folder = getDataFolder();
            File dataFile = new File(folder, "data.yml");

            boolean converted = false;
            if (dataFile.exists()) {
                converted = true;
                Storage storage = new StorageYaml(this);

                // Adding in spawners
                if (storage.containsGroup("spawners")) {
                    console.sendMessage("[" + getDescription().getName() + "] " + ChatColor.RED + "Conversion process starting DO NOT turn off your server... " +
                            "EpicSpawners hasn't fully loaded yet so its best users don't interact with the plugin until conversion completes.");
                    List<Spawner> spawners = new ArrayList<>();
                    for (StorageRow row : storage.getRowsByGroup("spawners")) {
                        try {
                            if (row.get("location") == null) continue;
                            Location location = Methods.unserializeLocation(row.getKey());
                            if (location == null || location.getWorld() == null) continue;

                            Spawner spawner = new Spawner(location);

                            for (String stackKey : row.get("stacks").asString().split(";")) {
                                if (stackKey == null) continue;
                                String[] stack = stackKey.split(":");
                                if (!spawnerManager.isSpawnerData(stack[0].toLowerCase())) continue;
                                spawner.addSpawnerStack(new SpawnerStack(spawnerManager.getSpawnerData(stack[0]), Integer.parseInt(stack[1])));
                            }

                            if (row.getItems().containsKey("placedby"))
                                spawner.setPlacedBy(UUID.fromString(row.get("placedby").asString()));

                            spawner.setSpawnCount(row.get("spawns").asInt());
                            spawners.add(spawner);
                        } catch (Exception e) {
                            System.out.println("Failed to load spawner.");
                            e.printStackTrace();
                        }
                    }
                    dataManager.createSpawners(spawners);
                }

                // Adding in Boosts
                if (storage.containsGroup("boosts")) {
                    for (StorageRow row : storage.getRowsByGroup("boosts")) {
                        if (row.get("boosttype").asObject() == null)
                            continue;

                        int amount = row.get("amount").asInt();
                        long endTime = Long.parseLong(row.getKey());

                        Boosted boosted = null;

                        if (row.get("boosttype").asString().equalsIgnoreCase("PLAYER"))
                            boosted = new BoostedPlayer(UUID.fromString(row.get("data").asString()), amount, endTime);
                        else if (row.get("boosttype").asString().equalsIgnoreCase("LOCATION")) {

                            String locationStr = row.get("data").asString();

                            String[] locationArray = locationStr.replace("Location", "")
                                    .replace("{", "")
                                    .replace("}", "")
                                    .replace("world=CraftWorld", "").split(",");

                            World world = Bukkit.getWorld(locationArray[0].split("=")[1]);

                            if (world == null) continue;

                            Location location = new Location(world,
                                    Double.parseDouble(locationArray[1].split("=")[1]),
                                    Double.parseDouble(locationArray[2].split("=")[1]),
                                    Double.parseDouble(locationArray[3].split("=")[1]));

                            boosted = new BoostedSpawner(location, amount, endTime);
                        }

                        if (boosted == null)
                            continue;

                        getDataManager().createBoost(boosted);
                    }
                }

                // Adding in Players
                if (storage.containsGroup("players")) {
                    for (StorageRow row : storage.getRowsByGroup("players")) {
                        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(row.getKey()));
                        if (row.get("entitykills").asObject() == null) continue;
                        for (String entityKillsKey : row.get("entitykills").asString().split(";")) {
                            if (entityKillsKey == null) continue;
                            String[] entityKills2 = entityKillsKey.split(":");
                            if (entityKills2[0] == null || entityKills2[0].equals("")) continue;
                            EntityType entityType = EntityType.valueOf(entityKills2[0]);
                            int amt = Integer.parseInt(entityKills2[1]);
                            dataManager.createEntityKill(player, entityType, amt);
                        }
                    }
                }
                dataFile.delete();
            }

            final boolean convrted = converted;
            getDataManager().queueAsync(() -> {
                if (convrted)
                    console.sendMessage("[" + getDescription().getName() + "] " + ChatColor.GREEN + "Conversion complete :)");
                // Load data from DB
                this.dataManager.getSpawners((spawners) -> {
                    this.spawnerManager.addSpawners(spawners);
                    loadHolograms();
                    this.dataManager.getBoosts((boosts) -> this.boostManager.addBoosts(boosts));
                    this.dataManager.getEntityKills((kills) -> {
                        for (Map.Entry<UUID, Map<EntityType, Integer>> entry : kills.entrySet()) {
                            PlayerData playerData = this.playerActionManager.getPlayerData(entry.getKey());
                            for (Map.Entry<EntityType, Integer> entry2 : entry.getValue().entrySet())
                                playerData.addKilledEntity(entry2.getKey(), entry2.getValue());
                        }
                    });

                    System.out.println("[" + getDescription().getName() + "] Loading Crafting Recipes");
                    this.enabledRecipe();
                });
            }, "create");

        }, 20);

        // Database stuff, go!
        this.databaseConnector = new SQLiteConnector(this);
        this.getLogger().info("Data handler connected using SQLite.");

        this.dataManager = new DataManager(this.databaseConnector, this);
        this.dataMigrationManager = new DataMigrationManager(this.databaseConnector, this.dataManager,
                new _1_InitialMigration());
        this.dataMigrationManager.runMigrations();
    }


    @Override
    public void onConfigReload() {
        this.setLocale(Settings.LANGUGE_MODE.getString(), true);
        this.locale.reloadMessages();
        this.blacklistHandler.reload();
        this.spawnerManager.loadSpawnerDataFromFile();
    }

    @Override
    public List<Config> getExtraConfig() {
        return Arrays.asList(spawnerManager.getSpawnerConfig(), blacklistHandler.getBlackConfig());
    }

    void loadHolograms() {
        Collection<Spawner> spawners = getSpawnerManager().getSpawners();
        if (spawners.size() == 0) return;

        for (Spawner spawner : spawners) {
            if (spawner.getWorld() == null) continue;
            updateHologram(spawner);
        }
    }

    public void clearHologram(Spawner spawner) {
        HologramManager.removeHologram(spawner.getLocation());
    }

    public void updateHologram(Spawner spawner) {
        // are holograms enabled?
        if (!Settings.SPAWNER_HOLOGRAMS.getBoolean() || !HologramManager.getManager().isEnabled()) return;

        int multi = spawner.getSpawnerDataCount();
        if (spawner.getSpawnerStacks().size() == 0) return;
        String name = Methods.compileName(getSpawnerManager().getSpawnerData(spawner.getIdentifyingName()), multi, false).trim();

        // create the hologram
        HologramManager.updateHologram(spawner.getLocation(), name);
    }

    public void processChange(Block block) {
        if (block.getType() != CompatibleMaterial.SPAWNER.getMaterial())
            return;
        Spawner spawner = getSpawnerManager().getSpawnerFromWorld(block.getLocation());
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () ->
                updateHologram(spawner), 1L);
    }


    private void checkStorage() {
        Storage storage = new StorageYaml(this);
    }

    private void saveToFile() {
        this.spawnerManager.saveSpawnerDataToFile();
    }

    private void enabledRecipe() {
        top:
        for (SpawnerData spawnerData : spawnerManager.getAllSpawnerData()) {
            if (!spawnerData.isCraftable()) continue;

            String recipe = spawnerData.getRecipe();

            String type = spawnerData.getIdentifyingName().toUpperCase().replace(" ", "_").replace("MUSHROOM_COW", "MOOSHROOM");

            ShapedRecipe spawnerRecipe = new ShapedRecipe(new NamespacedKey(this, "SPAWNER_RECIPE_" + type), spawnerData.toItemStack());

            if (recipe.length() != 9) continue;

            String[] split = Methods.splitStringEvery(recipe, 3);
            spawnerRecipe.shape(split[0], split[1], split[2]);

            List<String> ingredients = spawnerData.getRecipeIngredients();

            if (ingredients.isEmpty()) continue;

            for (String ingredient : ingredients) {
                try {
                    if (!ingredient.contains(",")) continue top;
                    String[] s = ingredient.split(",");
                    char letter = s[0].trim().toCharArray()[0];
                    String materialStr = s[1].trim();

                    Material material;

                    if (materialStr.equals("SPAWN_EGG")) {
                        try {
                            material = Material.valueOf(type + "_SPAWN_EGG");
                        } catch (Exception ignored) {
                            continue top;
                        }
                    } else {
                        material = Material.valueOf(materialStr);
                    }

                    spawnerRecipe.setIngredient(letter, material);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            getServer().addRecipe(spawnerRecipe);
        }
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

    public PlayerDataManager getPlayerDataManager() {
        return playerActionManager;
    }

    public BlacklistHandler getBlacklistHandler() {
        return blacklistHandler;
    }

    public Heads getHeads() {
        return heads;
    }

    public SpawnerManager getSpawnerManager() {
        return spawnerManager;
    }

    public AppearanceTask getAppearanceTask() {
        return appearanceTask;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public DatabaseConnector getDatabaseConnector() {
        return databaseConnector;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}

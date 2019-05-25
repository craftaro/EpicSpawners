package com.songoda.epicspawners;

import com.songoda.epicspawners.boost.BoostData;
import com.songoda.epicspawners.boost.BoostManager;
import com.songoda.epicspawners.boost.BoostType;
import com.songoda.epicspawners.command.CommandManager;
import com.songoda.epicspawners.economy.Economy;
import com.songoda.epicspawners.economy.PlayerPointsEconomy;
import com.songoda.epicspawners.economy.VaultEconomy;
import com.songoda.epicspawners.blacklist.BlacklistHandler;
import com.songoda.epicspawners.hologram.Hologram;
import com.songoda.epicspawners.hologram.HologramHolographicDisplays;
import com.songoda.epicspawners.listeners.*;
import com.songoda.epicspawners.player.PlayerActionManager;
import com.songoda.epicspawners.player.PlayerData;
import com.songoda.epicspawners.spawners.SpawnManager;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerManager;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.storage.Storage;
import com.songoda.epicspawners.storage.StorageRow;
import com.songoda.epicspawners.storage.types.StorageMysql;
import com.songoda.epicspawners.storage.types.StorageYaml;
import com.songoda.epicspawners.tasks.AppearanceTask;
import com.songoda.epicspawners.tasks.SpawnerParticleTask;
import com.songoda.epicspawners.tasks.SpawnerSpawnTask;
import com.songoda.epicspawners.utils.Heads;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.Metrics;
import com.songoda.epicspawners.utils.ServerVersion;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import com.songoda.epicspawners.utils.settings.Setting;
import com.songoda.epicspawners.utils.settings.SettingsManager;
import com.songoda.epicspawners.utils.updateModules.LocaleModule;
import com.songoda.update.Plugin;
import com.songoda.update.SongodaUpdate;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EpicSpawners extends JavaPlugin {

    private static EpicSpawners INSTANCE;

    private ServerVersion serverVersion = ServerVersion.fromPackageName(Bukkit.getServer().getClass().getPackage().getName());

    private SpawnManager spawnManager;
    private PlayerActionManager playerActionManager;
    private SpawnerManager spawnerManager;
    private BoostManager boostManager;
    private SettingsManager settingsManager;
    private CommandManager commandManager;

    private Economy economy;

    private BlacklistHandler blacklistHandler;

    private Hologram hologram;

    private AppearanceTask appearanceTask;
    private SpawnerParticleTask particleTask;
    private SpawnerSpawnTask spawnerCustomSpawnTask;

    private Heads heads;
    private Locale locale;

    private Storage storage;

    public static EpicSpawners getInstance() {
        return INSTANCE;
    }

    public void onEnable() {
        INSTANCE = this;

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(Methods.formatText("&a============================="));
        console.sendMessage(Methods.formatText(String.format("&7%s %s by &5Songoda <3&7!", this.getName(), this.getDescription().getVersion())));
        console.sendMessage(Methods.formatText("&7Action: &aEnabling&7..."));

        this.heads = new Heads();

        this.settingsManager = new SettingsManager(this);
        this.settingsManager.setupConfig();

        this.setupLanguage();

        //Running Songoda Updater
        Plugin plugin = new Plugin(this, 13);
        plugin.addModule(new LocaleModule());
        SongodaUpdate.load(plugin);

        this.boostManager = new BoostManager();
        this.spawnManager = new SpawnManager();
        this.spawnerManager = new SpawnerManager(this);
        this.commandManager = new CommandManager(this);
        this.blacklistHandler = new BlacklistHandler();
        this.playerActionManager = new PlayerActionManager();

        this.checkStorage();

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Event registration
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new EntityListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);
        pluginManager.registerEvents(new InventoryListeners(this), this);
        pluginManager.registerEvents(new SpawnerListeners(this), this);
        pluginManager.registerEvents(new PlayerJoinListeners(this), this);

        AbstractGUI.initializeListeners(this);

        // Register Hologram Plugin
        if (Setting.SPAWNER_HOLOGRAMS.getBoolean()
                && pluginManager.isPluginEnabled("HolographicDisplays"))
            hologram = new HologramHolographicDisplays(this);

        int timeout = Setting.AUTOSAVE.getInt() * 60 * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveToFile, timeout, timeout);

        // Start tasks
        if (isServerVersionAtLeast(ServerVersion.V1_12))
            this.particleTask = SpawnerParticleTask.startTask(this);
        this.spawnerCustomSpawnTask = SpawnerSpawnTask.startTask(this);
        this.appearanceTask = AppearanceTask.startTask(this);

        // Setup Economy
        if (Setting.VAULT_ECONOMY.getBoolean()
                && getServer().getPluginManager().getPlugin("Vault") != null)
            this.economy = new VaultEconomy(this);
        else if (Setting.PLAYER_POINTS_ECONOMY.getBoolean()
                && getServer().getPluginManager().getPlugin("PlayerPoints") != null)
            this.economy = new PlayerPointsEconomy(this);

        // Load Spawners
        Bukkit.getScheduler().runTaskLater(this, this::loadData, 10);

        // Start Metrics
        new Metrics(this);

        console.sendMessage(Methods.formatText("&a============================="));
    }


    public void onDisable() {
        this.saveToFile();
        this.storage.closeConnection();
        if (isServerVersionAtLeast(ServerVersion.V1_12))
            this.particleTask.cancel();
        this.spawnerCustomSpawnTask.cancel();
        if (hologram != null)
            this.hologram.unloadHolograms();

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(Methods.formatText("&a============================="));
        console.sendMessage(Methods.formatText("&7EpicSpawners " + this.getDescription().getVersion() + " by &5Songoda <3!"));
        console.sendMessage(Methods.formatText("&7Action: &cDisabling&7..."));
        console.sendMessage(Methods.formatText("&a============================="));
    }

    private void loadData() {
        // Adding in spawners.
        if (storage.containsGroup("spawners")) {
            for (StorageRow row : storage.getRowsByGroup("spawners")) {
                try {
                    if (row.get("location") == null) continue;
                    Location location = Methods.unserializeLocation(row.getKey());

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
                    this.spawnerManager.addSpawnerToWorld(location, spawner);
                } catch (Exception e) {
                    System.out.println("Failed to load spawner.");
                    e.printStackTrace();
                }
            }
        }

        // Adding in Boosts
        if (storage.containsGroup("boosts")) {
            for (StorageRow row : storage.getRowsByGroup("boosts")) {
                if (row.get("boosttype").asObject() == null)
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
                if (row.get("entitykills").asObject() == null) continue;
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
        if (hologram != null)
            hologram.loadHolograms();

        //Register Crafting Recipe
        System.out.println("[" + getDescription().getName() + "] Loading Crafting Recipes");
        this.enabledRecipe();

        // Save data initially so that if the person reloads again fast they don't lose all their data.
        this.saveToFile();
    }

    private void checkStorage() {
        if (getConfig().getBoolean("Database.Activate Mysql Support")) {
            this.storage = new StorageMysql(this);
        } else {
            this.storage = new StorageYaml(this);
        }
    }

    private void saveToFile() {
        checkStorage();

        this.spawnerManager.saveSpawnersToFile();

        storage.doSave();
    }

    public ServerVersion getServerVersion() {
        return serverVersion;
    }

    public boolean isServerVersion(ServerVersion version) {
        return serverVersion == version;
    }

    public boolean isServerVersion(ServerVersion... versions) {
        return ArrayUtils.contains(versions, serverVersion);
    }

    public boolean isServerVersionAtLeast(ServerVersion version) {
        return serverVersion.ordinal() >= version.ordinal();
    }

    private void setupLanguage() {
        String langMode = getConfig().getString("System.Language Mode");
        Locale.init(this);
        Locale.saveDefaultLocale("en_US");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));
    }

    private void enabledRecipe() {
        top:
        for (SpawnerData spawnerData : spawnerManager.getAllSpawnerData()) {
            if (!spawnerData.isCraftable()) continue;

            String recipe = spawnerData.getRecipe();

            String type = spawnerData.getIdentifyingName().toUpperCase().replace(" ", "_").replace("MUSHROOM_COW", "MOOSHROOM");

            ShapedRecipe spawnerRecipe = new ShapedRecipe(new NamespacedKey(this, "SPAWNER_RECIPE_" + type), spawnerData.toItemStack());

            if (recipe.length() != 9) return;

            String[] split = Methods.splitStringEvery(recipe, 3);
            spawnerRecipe.shape(split[0], split[1], split[2]);

            List<String> ingredients = spawnerData.getRecipeIngredients();

            if (ingredients.isEmpty()) return;

            for (String ingredient : ingredients) {
                try {
                    if (!ingredient.contains(",")) return;
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

    public void reload() {
        String langMode = getConfig().getString("System.Language Mode");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));
        this.locale.reloadMessages();
        this.blacklistHandler.reload();
        this.settingsManager.reloadConfig();
        this.spawnerManager.getSpawnerFile().reloadConfig();
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

    public Hologram getHologram() {
        return hologram;
    }

    public BlacklistHandler getBlacklistHandler() {
        return blacklistHandler;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public Heads getHeads() {
        return heads;
    }

    public SpawnerManager getSpawnerManager() {
        return spawnerManager;
    }

    public Economy getEconomy() {
        return economy;
    }

    public AppearanceTask getAppearanceTask() {
        return appearanceTask;
    }
}

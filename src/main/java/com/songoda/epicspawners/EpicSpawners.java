package com.songoda.epicspawners;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.commands.CommandManager;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.configuration.Config;
import com.songoda.core.database.DataMigrationManager;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.core.database.SQLiteConnector;
import com.songoda.core.gui.GuiManager;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.core.hooks.EntityStackerManager;
import com.songoda.core.hooks.HologramManager;
import com.songoda.core.hooks.ProtectionManager;
import com.songoda.epicspawners.blacklist.BlacklistHandler;
import com.songoda.epicspawners.boost.BoostManager;
import com.songoda.epicspawners.commands.*;
import com.songoda.epicspawners.database.DataManager;
import com.songoda.epicspawners.database.migrations._1_InitialMigration;
import com.songoda.epicspawners.database.migrations._2_AddTiers;
import com.songoda.epicspawners.listeners.BlockListeners;
import com.songoda.epicspawners.listeners.EntityListeners;
import com.songoda.epicspawners.listeners.InteractListeners;
import com.songoda.epicspawners.listeners.InventoryListeners;
import com.songoda.epicspawners.listeners.SpawnerListeners;
import com.songoda.epicspawners.listeners.WorldListeners;
import com.songoda.epicspawners.lootables.LootablesManager;
import com.songoda.epicspawners.player.PlayerData;
import com.songoda.epicspawners.player.PlayerDataManager;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.SpawnManager;
import com.songoda.epicspawners.spawners.spawner.PlacedSpawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerManager;
import com.songoda.epicspawners.tasks.AppearanceTask;
import com.songoda.epicspawners.tasks.SpawnerParticleTask;
import com.songoda.epicspawners.tasks.SpawnerSpawnTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EpicSpawners extends SongodaPlugin {

    private static EpicSpawners INSTANCE;

    private final GuiManager guiManager = new GuiManager(this);
    private SpawnManager spawnManager;
    private PlayerDataManager playerActionManager;
    private SpawnerManager spawnerManager;
    private BoostManager boostManager;
    private CommandManager commandManager;
    private LootablesManager lootablesManager;

    private BlacklistHandler blacklistHandler;

    private AppearanceTask appearanceTask;
    private SpawnerParticleTask particleTask;
    private SpawnerSpawnTask spawnerCustomSpawnTask;

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
        if (!spawnerManager.wasConfigModified())
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

        // Load Economy, Hologram and Protection hooks
        EconomyManager.load();
        HologramManager.load(this);
        ProtectionManager.load(this);

        // Setup Config
        Settings.setupConfig();
        this.setLocale(Settings.LANGUGE_MODE.getString(), false);

        // Load the entity stacker manager.
        EntityStackerManager.load();

        // Set Economy & Hologram preference
        EconomyManager.getManager().setPreferredHook(Settings.ECONOMY_PLUGIN.getString());
        EconomyManager.setCurrencySymbol(getLocale().getMessage("general.nametag.currency").getMessage());
        HologramManager.getManager().setPreferredHook(Settings.HOLOGRAM_PLUGIN.getString());

        // Register commands
        this.commandManager = new CommandManager(this);
        this.commandManager.addMainCommand("es")
                .addSubCommands(
                        new CommandGive(this),
                        new CommandOpenShop(this),
                        new CommandBoost(this),
                        new CommandEditor(this),
                        new CommandSettings(this),
                        new CommandReload(this),
                        new CommandChange(this),
                        new CommandSpawn(this)
                );
        this.commandManager.addCommand(new CommandSpawnerStats(this));
        this.commandManager.addCommand(new CommandSpawnerShop(this));

        this.boostManager = new BoostManager();
        this.spawnManager = new SpawnManager();
        this.spawnerManager = new SpawnerManager(this);
        this.blacklistHandler = new BlacklistHandler();
        this.playerActionManager = new PlayerDataManager();

        this.lootablesManager = new LootablesManager();
        this.lootablesManager.getLootManager().loadLootables();

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Listeners
        guiManager.init();
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new EntityListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);
        pluginManager.registerEvents(new InventoryListeners(), this);
        pluginManager.registerEvents(new SpawnerListeners(this), this);
        pluginManager.registerEvents(new WorldListeners(this), this);

        int timeout = Settings.AUTOSAVE.getInt() * 60 * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveToFile, timeout, timeout);

        // Start tasks
        this.particleTask = SpawnerParticleTask.startTask(this);
        this.spawnerCustomSpawnTask = SpawnerSpawnTask.startTask(this);
        this.appearanceTask = AppearanceTask.startTask(this);

        // Database stuff, go!
        this.databaseConnector = new SQLiteConnector(this);
        this.getLogger().info("Data handler connected using SQLite.");

        this.dataManager = new DataManager(this.databaseConnector, this);
        this.dataMigrationManager = new DataMigrationManager(this.databaseConnector, this.dataManager,
                new _1_InitialMigration(), new _2_AddTiers());
        this.dataMigrationManager.runMigrations();
    }

    @Override
    public void onDataLoad() {
        // Adding in spawners
        getDataManager().queueAsync(() -> {
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

                getLogger().info("Loading Crafting Recipes");
                this.enabledRecipe();
            });
        }, "create");
    }

    @Override
    public void onConfigReload() {
        this.setLocale(Settings.LANGUGE_MODE.getString(), true);
        this.locale.reloadMessages();
        this.blacklistHandler.reload();
        if (spawnerManager.wasConfigModified())
            this.spawnerManager.reloadFromFile();
    }

    @Override
    public List<Config> getExtraConfig() {
        return Arrays.asList(spawnerManager.getSpawnerConfig(), blacklistHandler.getBlackConfig());
    }

    private void loadHolograms() {
        Collection<PlacedSpawner> spawners = getSpawnerManager().getSpawners();
        if (spawners.size() == 0) return;

        for (PlacedSpawner spawner : spawners) {
            if (spawner.getWorld() == null) continue;
            createHologram(spawner);
        }
    }

    public void clearHologram(PlacedSpawner spawner) {
        HologramManager.removeHologram(spawner.getHologramId());
    }

    public void createHologram(PlacedSpawner spawner) {
        // are holograms enabled?
        if (!Settings.SPAWNER_HOLOGRAMS.getBoolean() || !HologramManager.getManager().isEnabled()) return;

        // create the hologram
        HologramManager.createHologram(spawner.getHologramId(), spawner.getLocation(), getHologramName(spawner));
    }

    public void updateHologram(PlacedSpawner spawner) {
        // are holograms enabled?
        if (!Settings.SPAWNER_HOLOGRAMS.getBoolean() || !HologramManager.getManager().isEnabled()) return;

        if (spawner.getSpawnerStacks().isEmpty()) return;

        // check if it is created
        if (!HologramManager.isHologramLoaded(spawner.getHologramId())) return;

        // create the hologram
        HologramManager.updateHologram(spawner.getHologramId(), getHologramName(spawner));
    }

    public String getHologramName(PlacedSpawner spawner) {
        int stackSize = spawner.getStackSize();
        if (spawner.getSpawnerStacks().isEmpty()) return null;
        return spawner.getFirstTier().getCompiledDisplayName(spawner.getSpawnerStacks().size() > 1, stackSize).trim();
    }

    public void processChange(Block block) {
        if (block.getType() != CompatibleMaterial.SPAWNER.getMaterial())
            return;
        PlacedSpawner spawner = getSpawnerManager().getSpawnerFromWorld(block.getLocation());
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () ->
                updateHologram(spawner), 1L);
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

            ShapedRecipe spawnerRecipe = new ShapedRecipe(new NamespacedKey(this, "SPAWNER_RECIPE_" + type), spawnerData.getFirstTier().toItemStack());

            if (recipe.length() != 9) continue;

            int arrayLength = (int) Math.ceil(((recipe.length() / (double) 3)));
            String[] result = new String[arrayLength];

            int j = 0;
            int lastIndex = result.length - 1;
            for (int i = 0; i < lastIndex; i++) {
                result[i] = recipe.substring(j, j + 3);
                j += 3;
            } //Add the last bit
            result[lastIndex] = recipe.substring(j);

            spawnerRecipe.shape(result[0], result[1], result[2]);

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

    public LootablesManager getLootablesManager() {
        return lootablesManager;
    }
}

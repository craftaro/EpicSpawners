package com.craftaro.epicspawners;

import com.craftaro.core.SongodaCore;
import com.craftaro.core.SongodaPlugin;
import com.craftaro.core.commands.CommandManager;
import com.craftaro.core.configuration.Config;
import com.craftaro.core.database.DataManager;
import com.craftaro.core.gui.GuiManager;
import com.craftaro.core.hooks.EconomyManager;
import com.craftaro.core.hooks.EntityStackerManager;
import com.craftaro.core.hooks.HologramManager;
import com.craftaro.core.hooks.ProtectionManager;
import com.craftaro.epicspawners.api.EpicSpawnersApi;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerManager;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerStack;
import com.craftaro.epicspawners.blacklist.BlacklistHandler;
import com.craftaro.epicspawners.boost.BoostManagerImpl;
import com.craftaro.epicspawners.commands.CommandBoost;
import com.craftaro.epicspawners.commands.CommandChange;
import com.craftaro.epicspawners.commands.CommandEditor;
import com.craftaro.epicspawners.commands.CommandGive;
import com.craftaro.epicspawners.commands.CommandOpenShop;
import com.craftaro.epicspawners.commands.CommandReload;
import com.craftaro.epicspawners.commands.CommandSettings;
import com.craftaro.epicspawners.commands.CommandSpawn;
import com.craftaro.epicspawners.commands.CommandSpawnerShop;
import com.craftaro.epicspawners.commands.CommandSpawnerStats;
import com.craftaro.epicspawners.database.DataHelper;
import com.craftaro.epicspawners.database.migrations._1_InitialMigration;
import com.craftaro.epicspawners.database.migrations._2_AddTiers;
import com.craftaro.epicspawners.database.migrations._3_AddUniqueIndex;
import com.craftaro.epicspawners.listeners.BlockListeners;
import com.craftaro.epicspawners.listeners.EntityListeners;
import com.craftaro.epicspawners.listeners.InteractListeners;
import com.craftaro.epicspawners.listeners.InventoryListeners;
import com.craftaro.epicspawners.listeners.SpawnerListeners;
import com.craftaro.epicspawners.listeners.WorldListeners;
import com.craftaro.epicspawners.lootables.LootablesManager;
import com.craftaro.epicspawners.player.PlayerDataManagerImpl;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.epicspawners.spawners.SpawnManager;
import com.craftaro.epicspawners.spawners.spawner.PlacedSpawnerImpl;
import com.craftaro.epicspawners.spawners.spawner.SpawnerManagerImpl;
import com.craftaro.epicspawners.spawners.spawner.SpawnerStackImpl;
import com.craftaro.epicspawners.tasks.AppearanceTask;
import com.craftaro.epicspawners.tasks.SpawnerParticleTask;
import com.craftaro.epicspawners.tasks.SpawnerSpawnTask;
import com.craftaro.epicspawners.utils.SpawnerDataBuilderImpl;
import com.craftaro.epicspawners.utils.SpawnerTierBuilderImpl;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class EpicSpawners extends SongodaPlugin {
    private final GuiManager guiManager = new GuiManager(this);
    private SpawnManager spawnManager;
    private PlayerDataManagerImpl playerActionManager;
    private SpawnerManager spawnerManager;
    private BoostManagerImpl boostManager;
    private CommandManager commandManager;
    private LootablesManager lootablesManager;

    private BlacklistHandler blacklistHandler;

    private AppearanceTask appearanceTask;
    private SpawnerParticleTask particleTask;
    private SpawnerSpawnTask spawnerCustomSpawnTask;
    private CoreProtectAPI coreProtectAPI;

    /**
     * @deprecated Use {@link org.bukkit.plugin.java.JavaPlugin#getPlugin(Class)} instead
     */
    @Deprecated
    public static EpicSpawners getInstance() {
        return getPlugin(EpicSpawners.class);
    }

    @Override
    public void onPluginLoad() {
    }

    @Override
    public void onPluginDisable() {
        if (!this.spawnerManager.wasConfigModified()) {
            this.saveToFile();
        }

        if (Bukkit.getScheduler().isCurrentlyRunning(this.particleTask.getTaskId())) {
            this.particleTask.cancel();
        }

        if (Bukkit.getScheduler().isCurrentlyRunning(this.spawnerCustomSpawnTask.getTaskId())) {
            this.spawnerCustomSpawnTask.cancel();
        }

        HologramManager.removeAllHolograms();
    }

    @Override
    public void onPluginEnable() {
        // Run Songoda Updater
        SongodaCore.registerPlugin(this, 13, XMaterial.SPAWNER);

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

        this.boostManager = new BoostManagerImpl();
        this.spawnManager = new SpawnManager();
        this.spawnerManager = new SpawnerManagerImpl(this);
        this.blacklistHandler = new BlacklistHandler();
        this.playerActionManager = new PlayerDataManagerImpl();

        this.lootablesManager = new LootablesManager();
        this.lootablesManager.getLootManager().loadLootables();

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Listeners
        this.guiManager.init();
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new EntityListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);
        pluginManager.registerEvents(new InventoryListeners(), this);
        pluginManager.registerEvents(new SpawnerListeners(), this);
        pluginManager.registerEvents(new WorldListeners(this), this);

        int timeout = Settings.AUTOSAVE.getInt() * 60 * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveToFile, timeout, timeout);

        // Start tasks
        this.particleTask = SpawnerParticleTask.startTask(this);
        this.spawnerCustomSpawnTask = SpawnerSpawnTask.startTask(this);
        this.appearanceTask = AppearanceTask.startTask(this);

        //Note: Next migration revision should be 4. There was a deleted migration with revision 3.
        initDatabase(new _1_InitialMigration(), new _2_AddTiers(), new _3_AddUniqueIndex());
        new EpicSpawnersApi(this, this.spawnerManager, new SpawnerDataBuilderImpl(""), new SpawnerTierBuilderImpl());

        //Hook into CoreProtect
        if (Bukkit.getPluginManager().isPluginEnabled("CoreProtect")) {
            CoreProtect coreProtect = (CoreProtect) Bukkit.getPluginManager().getPlugin("CoreProtect");
            if (coreProtect.getAPI().APIVersion() < 9) {
                Bukkit.getLogger().warning("CoreProtect version is too old. Please update to the latest version.");
            } else {
                this.coreProtectAPI = coreProtect.getAPI();
            }
        }
    }

    @Override
    public void onDataLoad() {
        DataManager dataManager = getDataManager();
        this.spawnerManager.addSpawners(dataManager.loadBatch(PlacedSpawnerImpl.class, "placed_spawners"));
        //Need to load the SpawnerStacks to the loaded spawners now.
        List<SpawnerStack> stacks = dataManager.loadBatch(SpawnerStackImpl.class, "spawner_stacks");
        loadHolograms();

        DataHelper.loadData(dataManager, this.boostManager, this.playerActionManager);
    }

    @Override
    public void onConfigReload() {
        this.setLocale(Settings.LANGUGE_MODE.getString(), true);
        this.locale.reloadMessages();
        this.blacklistHandler.reload();
        if (this.spawnerManager.wasConfigModified()) {
            this.spawnerManager.reloadFromFile();
        }

        // Should start or stop particle task based on config setting
        this.particleTask = SpawnerParticleTask.startTask(this);
    }

    @Override
    public List<Config> getExtraConfig() {
        return Arrays.asList(this.spawnerManager.getSpawnerConfig(), this.blacklistHandler.getBlackConfig());
    }

    private void loadHolograms() {
        Collection<PlacedSpawner> spawners = getSpawnerManager().getSpawners();
        if (spawners.isEmpty()) {
            return;
        }

        for (PlacedSpawner spawner : spawners) {
            if (spawner.getWorld() == null) {
                continue;
            }
            createHologram(spawner);
        }
    }

    public void clearHologram(PlacedSpawner spawner) {
        HologramManager.removeHologram(spawner.getHologramId());
    }

    public void createHologram(PlacedSpawner spawner) {
        // are holograms enabled?
        if (!Settings.SPAWNER_HOLOGRAMS.getBoolean() || !HologramManager.getManager().isEnabled()) {
            return;
        }

        // create the hologram
        HologramManager.createHologram(spawner.getHologramId(), spawner.getLocation(), getHologramName(spawner));
    }

    public void updateHologram(PlacedSpawner spawner) {
        // are holograms enabled?
        if (!Settings.SPAWNER_HOLOGRAMS.getBoolean() || !HologramManager.getManager().isEnabled()) {
            return;
        }

        if (spawner.getSpawnerStacks().isEmpty()) {
            return;
        }

        // check if it is created
        if (!HologramManager.isHologramLoaded(spawner.getHologramId())) {
            return;
        }

        // create the hologram
        HologramManager.updateHologram(spawner.getHologramId(), getHologramName(spawner));
    }

    public String getHologramName(PlacedSpawner spawner) {
        int stackSize = spawner.getStackSize();
        if (spawner.getSpawnerStacks().isEmpty()) {
            return null;
        }
        return spawner.getFirstTier().getCompiledDisplayName(spawner.getSpawnerStacks().size() > 1, stackSize).trim();
    }

    public void processChange(Block block) {
        if (block.getType() != XMaterial.SPAWNER.parseMaterial()) {
            return;
        }
        PlacedSpawner spawner = getSpawnerManager().getSpawnerFromWorld(block.getLocation());
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () ->
                updateHologram(spawner), 1L);
    }

    private void saveToFile() {
        this.spawnerManager.saveSpawnerDataToFile();
    }

    private void enabledRecipe() {
        top:
        for (SpawnerData spawnerData : this.spawnerManager.getAllSpawnerData()) {
            if (!spawnerData.isCraftable()) {
                continue;
            }

            String recipe = spawnerData.getRecipe();

            String type = spawnerData.getIdentifyingName().toUpperCase().replace(" ", "_").replace("MUSHROOM_COW", "MOOSHROOM");

            ShapedRecipe spawnerRecipe = new ShapedRecipe(new NamespacedKey(this, "SPAWNER_RECIPE_" + type), spawnerData.getFirstTier().toItemStack());

            if (recipe.length() != 9) {
                continue;
            }

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

            if (ingredients.isEmpty()) {
                continue;
            }

            for (String ingredient : ingredients) {
                try {
                    if (!ingredient.contains(",")) {
                        continue top;
                    }
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
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            getServer().addRecipe(spawnerRecipe);
        }
    }


    public SpawnManager getSpawnManager() {
        return this.spawnManager;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public BoostManagerImpl getBoostManager() {
        return this.boostManager;
    }

    public PlayerDataManagerImpl getPlayerDataManager() {
        return this.playerActionManager;
    }

    public BlacklistHandler getBlacklistHandler() {
        return this.blacklistHandler;
    }

    public SpawnerManager getSpawnerManager() {
        return this.spawnerManager;
    }

    public AppearanceTask getAppearanceTask() {
        return this.appearanceTask;
    }

    public GuiManager getGuiManager() {
        return this.guiManager;
    }

    public LootablesManager getLootablesManager() {
        return this.lootablesManager;
    }
}

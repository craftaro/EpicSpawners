package com.craftaro.epicspawners;

import com.craftaro.core.SongodaCore;
import com.craftaro.core.SongodaPlugin;
import com.craftaro.core.commands.CommandManager;
import com.craftaro.core.compatibility.CompatibleMaterial;
import com.craftaro.core.configuration.Config;
import com.craftaro.core.database.DataManager;
import com.craftaro.core.gui.GuiManager;
import com.craftaro.core.hooks.EconomyManager;
import com.craftaro.core.hooks.EntityStackerManager;
import com.craftaro.core.hooks.HologramManager;
import com.craftaro.core.hooks.ProtectionManager;
import com.craftaro.core.third_party.org.jooq.Record;
import com.craftaro.core.third_party.org.jooq.Result;
import com.craftaro.epicspawners.api.EpicSpawnersAPI;
import com.craftaro.epicspawners.api.boosts.types.BoostedPlayer;
import com.craftaro.epicspawners.api.player.PlayerData;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
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
import com.craftaro.epicspawners.database.migrations._1_InitialMigration;
import com.craftaro.epicspawners.database.migrations._2_AddTiers;
import com.craftaro.epicspawners.listeners.BlockListeners;
import com.craftaro.epicspawners.listeners.EntityListeners;
import com.craftaro.epicspawners.listeners.InteractListeners;
import com.craftaro.epicspawners.listeners.InventoryListeners;
import com.craftaro.epicspawners.listeners.SpawnerListeners;
import com.craftaro.epicspawners.listeners.WorldListeners;
import com.craftaro.epicspawners.lootables.LootablesManager;
import com.craftaro.epicspawners.player.PlayerDataImpl;
import com.craftaro.epicspawners.player.PlayerDataManagerImpl;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.epicspawners.spawners.SpawnManager;
import com.craftaro.epicspawners.spawners.spawner.SpawnerManager;
import com.craftaro.epicspawners.tasks.AppearanceTask;
import com.craftaro.epicspawners.tasks.SpawnerParticleTask;
import com.craftaro.epicspawners.tasks.SpawnerSpawnTask;
import com.craftaro.epicspawners.utils.SpawnerDataBuilderImpl;
import com.craftaro.epicspawners.utils.SpawnerTierBuilderImpl;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class EpicSpawners extends SongodaPlugin {

    private static EpicSpawners INSTANCE;

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

        this.boostManager = new BoostManagerImpl();
        this.spawnManager = new SpawnManager();
        this.spawnerManager = new SpawnerManager(this);
        this.blacklistHandler = new BlacklistHandler();
        this.playerActionManager = new PlayerDataManagerImpl();

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

        initDatabase(Arrays.asList(new _1_InitialMigration(), new _2_AddTiers()));
        new EpicSpawnersAPI(this, new SpawnerDataBuilderImpl(""), new SpawnerTierBuilderImpl(null));
    }

    @Override
    public void onDataLoad() {
        DataManager dataManager = getDataManager();
        spawnerManager.addSpawners(dataManager.loadBatch(PlacedSpawner.class, "placed_spawners"));
        loadHolograms();
        boostManager.addBoosts(dataManager.loadBatch(BoostedPlayer.class, "boosted_players"));

        //Load entity kills
        dataManager.getDatabaseConnector().connectDSL(dslContext -> {
            @NotNull Result<Record> results = dslContext.select().from(dataManager.getTablePrefix() + "entity_kills").fetch();
            results.stream().iterator().forEachRemaining(record -> {
                UUID uuid = UUID.fromString(record.get("uuid").toString());
                EntityType entityType = EntityType.valueOf(record.get("entity_type").toString());
                int amount = Integer.parseInt(record.get("amount").toString());
                PlayerData playerData = playerActionManager.getPlayerData(uuid);
                playerData.addKilledEntity(entityType, amount);
            });
        });
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

    public BoostManagerImpl getBoostManager() {
        return boostManager;
    }

    public PlayerDataManagerImpl getPlayerDataManager() {
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

    public LootablesManager getLootablesManager() {
        return lootablesManager;
    }
}

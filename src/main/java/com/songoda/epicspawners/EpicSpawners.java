package com.songoda.epicspawners;

import com.songoda.arconix.api.utils.ConfigWrapper;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.handlers.*;
import com.songoda.epicspawners.listeners.*;
import com.songoda.epicspawners.spawners.Shop;
import com.songoda.epicspawners.spawners.SpawnerEditor;
import com.songoda.epicspawners.utils.Heads;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Created by songoda on 2/25/2017.
 */
public class EpicSpawners extends JavaPlugin implements Listener {
    public static CommandSender console = Bukkit.getConsoleSender();

    private static EpicSpawners INSTANCE;

    public Map<String, Integer> cache = new HashMap<>();

    public BlacklistHandler blacklist;

    public HookHandler hooks;

    public Shop shop;
    public SpawnerEditor editor;
    public HologramHandler holo;
    public Heads heads;
    public SettingsManager sm;

    public EpicSpawnersAPI api;

    public List<Player> change = new ArrayList<>();

    public List<Player> boosting = new ArrayList<>();
    public Map<Player, Integer> boostAmt = new HashMap<>();

    public Map<Player, Integer> infPage = new HashMap<>();

    public HashMap<String, Long> tickTracker = new HashMap<>();
    public HashMap<String, Long> tickTracker2 = new HashMap<>();

    public boolean v1_12 = Bukkit.getServer().getClass().getPackage().getName().contains("1_12");
    public boolean v1_11 = Bukkit.getServer().getClass().getPackage().getName().contains("1_11");
    public boolean v1_7 = Bukkit.getServer().getClass().getPackage().getName().contains("1_7");
    public boolean v1_8 = Bukkit.getServer().getClass().getPackage().getName().contains("1_8");
    public boolean v1_9 = Bukkit.getServer().getClass().getPackage().getName().contains("1_9");
    public boolean v1_10 = Bukkit.getServer().getClass().getPackage().getName().contains("1_10");
    public boolean v1_8_R1 = Bukkit.getServer().getClass().getPackage().getName().contains("1_8_R1");

    public List<ItemStack> itemEditorInstance = new ArrayList<>();
    public List<String> entityEditorInstance = new ArrayList<>();
    public List<String> commandEditorInstance = new ArrayList<>();
    public List<String> blockEditorInstance = new ArrayList<>();

    public String newSpawnerName = "";

    public boolean isItemInstanceSaved = false;
    public boolean isEntityInstanceSaved = false;
    public boolean isCommandInstanceSaved = false;
    public boolean isBlockInstanceSaved = false;

    public Map<Player, Integer> editing = new HashMap<>();
    public Map<Player, String> subediting = new HashMap<>();

    public Map<Player, String> chatEditing = new HashMap<>();

    public Map<Player, String> inShow = new HashMap<>();
    public Map<Player, Integer> page = new HashMap<>();

    public Map<Player, Boolean> pickup = new HashMap<>();
    public Map<Player, Location> freePickup = new HashMap<>();

    public Map<Location, Date> lastSpawn = new HashMap<>();

    private Locale locale;
    public ConfigWrapper dataFile = new ConfigWrapper(this, "", "data.yml");
    public ConfigWrapper spawnerFile = new ConfigWrapper(this, "", "spawners.yml");

    public References references = null;

    public Map<Player, Block> spawnerLoc = new HashMap<>();
    public Map<Player, Block> lastSpawner = new HashMap<>();

    public void onDisable() {
        thinData();
        dataFile.saveConfig();
        spawnerFile.saveConfig();
        console.sendMessage(Arconix.pl().getApi().format().formatText("&a============================="));
        console.sendMessage(Arconix.pl().getApi().format().formatText("&7EpicSpawners " + this.getDescription() + " by &5Brianna <3!"));
        console.sendMessage(Arconix.pl().getApi().format().formatText("&7Action: &cDisabling&7..."));
        console.sendMessage(Arconix.pl().getApi().format().formatText("&a============================="));
    }

    public void onEnable() {
        INSTANCE = this;

        Arconix.pl().hook(this);

        console.sendMessage(Arconix.pl().getApi().format().formatText("&a============================="));
        console.sendMessage(Arconix.pl().getApi().format().formatText("&7EpicSpawners " + this.getDescription().getVersion() + " by &5Brianna <3&7!"));
        console.sendMessage(Arconix.pl().getApi().format().formatText("&7Action: &aEnabling&7..."));
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        hooks = new HookHandler();
        hooks.hook();

        heads = new Heads();

        api = new EpicSpawnersAPI();

        sm = new SettingsManager();

        setupConfig();
        setupSpawners();

        dataFile.createNewFile("Loading Data File", "EpicSpawners Data File");
        loadDataFile();

        // Locales
        Locale.init(this);
        Locale.saveDefaultLocale("en_US");
        this.locale = Locale.getLocale(this.getConfig().getString("Locale", "en_US"));

        thinData();

        blacklist = new BlacklistHandler();
        references = new References();

        shop = new Shop(this);
        editor = new SpawnerEditor(this);
        holo = new HologramHandler();
        new OmniHandler();
        new BoostHandler();
        new ItemHandler();

        new com.massivestats.MassiveStats(this, 900);


        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            thinData();
            dataFile.saveConfig();
        }, 50000L, 50000L);

        this.getCommand("EpicSpawners").setExecutor(new CommandHandler(this));
        this.getCommand("SpawnerStats").setExecutor(new CommandHandler(this));
        this.getCommand("SpawnerShop").setExecutor(new CommandHandler(this));

        getServer().getPluginManager().registerEvents(new BlockListeners(this), this);
        getServer().getPluginManager().registerEvents(new ChatListeners(), this);
        getServer().getPluginManager().registerEvents(new EntityListeners(), this);
        getServer().getPluginManager().registerEvents(new InteractListeners(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListeners(this), this);
        getServer().getPluginManager().registerEvents(new SpawnerListeners(), this);

        if (!v1_7) {
            getServer().getPluginManager().registerEvents(new com.songoda.epicspawners.listeners.TestListeners(), this);
        }
        console.sendMessage(Arconix.pl().getApi().format().formatText("&a============================="));
    }

    private void setupConfig() {
        sm.updateSettings();
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

        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Has-AI") && !EpicSpawners.getInstance().v1_7)
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Has-AI", true);

        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Upgradable"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Upgradable", true);
        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Convertible"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Convertible", true);
        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Convert-Price"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Convert-Price", "45%");
        if (!spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(value) + ".Limited"))
            spawnerFile.getConfig().addDefault("Entities." + Methods.getTypeFromString(value) + ".Limited", 0);
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
    }

    private void loadDataFile() {
        dataFile.getConfig().options().copyDefaults(true);
        dataFile.saveConfig();
    }

    public void reload() {
        locale.reloadMessages();
        spawnerFile.createNewFile("Loading Spawners File", "EpicSpawners Spawners File");
        hooks.hooksFile.createNewFile("Loading Hooks File", "EpicSpawners Spawners File");
        hooks = new HookHandler();
        hooks.hook();
        references = new References();
        blacklist.reload();
        reloadConfig();
        saveConfig();
    }

    public void thinData() {
        if (dataFile.getConfig().contains("data.entityshop")) {
            ConfigurationSection cs = dataFile.getConfig().getConfigurationSection("data.entityshop");
            for (String key : cs.getKeys(true)) {
                if (isDead(UUID.fromString(key))) {
                    dataFile.getConfig().set("data.entityshop." + key, null);
                }
            }
        }

        if (getConfig().getBoolean("System.Remove Dead Entities from Data File")) {
            if (dataFile.getConfig().contains("data.Entities")) {
                ConfigurationSection cs = dataFile.getConfig().getConfigurationSection("data.Entities");
                for (String key : cs.getKeys(true)) {
                    if (isDead(UUID.fromString(key))) {
                        dataFile.getConfig().set("data.Entities." + key, null);
                    }
                }
            }
        } else {
            dataFile.getConfig().set("data.Entities", null);
        }

        if (!getConfig().getBoolean("Spawner Drops.Allow Killing Mobs To Drop Spawners")) {
            if (dataFile.getConfig().contains("data.kills")) {
                dataFile.getConfig().set("data.kills", null);
            }
        }
    }

    public boolean isDead(UUID uniqueId) {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(uniqueId)) {
                    return false;
                }
            }
        }

        return true;
    }

    public Locale getLocale() {
        return locale;
    }

    public EpicSpawnersAPI getApi() {
        return api;
    }

    public static EpicSpawners pl() {
        return INSTANCE;
    }

    public static EpicSpawners getInstance() {
        return INSTANCE;
    }
}

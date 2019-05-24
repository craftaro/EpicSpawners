package com.songoda.epicspawners.utils.settings;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.utils.ServerVersion;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Setting {

    SPAWNERS_MAX("Main.Spawner Max Upgrade", 5),
    NAME_FORMAT("Main.Spawner Name Format", "&e{TYPE} &fSpawner [&c{AMT}x]"),
    FORCE_COMBINE_RADIUS("Main.Force Combine Radius", 0),
    FORCE_COMBINE_DENY("Main.Deny Place On Force Combine", false),
    SEARCH_RADIUS("Main.Radius To Search Around Spawners", "8x4x8"),
    ALTER_DELAY("Main.Default Minecraft Spawner Cooldowns", true),
    ALERT_PLACE_BREAK("Main.Alerts On Place And Break", true),
    SNEAK_FOR_STACK("Main.Sneak To Receive A Stacked Spawner", true),
    SPAWNER_HOLOGRAMS("Main.Spawners Have Holograms", true),
    ONLY_DROP_PLACED("Main.Only Drop Placed Spawner", false),
    ONLY_CHARGE_NATURAL("Main.Only Charge Natural Spawners", false),
    CUSTOM_SPAWNER_TICK_RATE("Main.Custom Spawner Tick Rate", 10),
    RANDOM_LOW_HIGH("Main.Random Amount Added To Each Spawn", "1:4"),
    SOUNDS_ENABLED("Main.Sounds Enabled", true),
    DISPLAY_LEVEL_ONE("Main.Display Level In Spawner Title If Level 1", false),
    OMNI_SPAWNERS("Main.OmniSpawners Enabled", true),
    EGGS_CONVERT_SPAWNERS("Main.Convert Spawners With Eggs", true),
    HELPFUL_TIPS_ENABLED("Main.Display Helpful Tips For Operators", true),
    UPGRADE_WITH_ECO_ENABLED("Main.Upgrade With Economy", true),
    UPGRADE_WITH_XP_ENABLED("Main.Upgrade With XP", true),
    UPGRADE_COST_ECONOMY("Main.Cost To Upgrade With Economy", 10000),
    UPGRADE_COST_EXPERIANCE("Main.Cost To Upgrade With XP", 50),
    USE_CUSTOM_UPGRADE_EQUATION("Main.Use Custom Equations for Upgrade Costs", false),
    LIQUID_REPEL_RADIUS("Main.Spawner Repel Liquid Radius", 1),
    REDSTONE_ACTIVATE("Main.Redstone Power Deactivates Spawners", true),
    DISPLAY_HELP_BUTTON("Main.Display Help Button In Spawner Overview", true),
    SPAWNERS_DONT_EXPLODE("Main.Prevent Spawners From Exploding", false),
    SPAWNERS_TO_INVENTORY("Main.Add Spawners To Inventory On Drop", false),
    UPGRADE_PARTICLE_TYPE("Main.Upgrade Particle Type", "SPELL_WITCH"),
    EXTRA_SPAWN_TICKS("Main.Extra Ticks Added To Each Spawn", 0),
    MAX_SPAWNERS("Main.Max Spawners Per Player", -1),
    AUTOSAVE("Main.Auto Save Interval In Seconds", 15),
    NO_AI("Main.Nerf Spawner Mobs", false),

    COST_EQUATION_EXPERIANCE("Main.Equations.Calculate XP Upgrade Cost", "{XPCost} * {Level}"),
    COST_EQUATION_ECONOMY("Main.Equations.Calculate Economy Upgrade Cost", "{ECOCost} * {Level}"),
    SPAWNER_EQUATION_SPAWNS("Main.Equations.Mobs Spawned Per Spawn", "{MULTI} * {RAND}"),

    NAMED_SPAWNER_TIERS("Main.Named Spawners Tiers", false),
    TIER_NAMES("Main.Tier Names", Arrays.asList("&7Common", "&6Uncommon", "&4Rare", "&5Mythic")),

    BOOST_MULTIPLIER("Spawner Boosting.Boost Multiplier", "0.5"),
    MAX_PLAYER_BOOST("Spawner Boosting.Max Multiplier For A Spawner Boost", 5),
    BOOST_COST("Spawner Boosting.Item Charged For A Boost", "DIAMOND:2"),

    HOSTILE_MOBS_ATTACK_SECOND("entity.Hostile Mobs Attack Second", false),

    ONLY_DROP_STACKED("Spawner Drops.Only Drop Stacked Spawners", false),
    MOB_KILLING_COUNT("Spawner Drops.Allow Killing Mobs To Drop Spawners", true),
    COUNT_UNNATURAL_KILLS("Spawner Drops.Count Unnatural Kills Towards Spawner Drop", false),
    KILL_GOAL("Spawner Drops.Kills Needed for Drop", 100),
    ALERT_INTERVAL("Spawner Drops.Alert Every X Before Drop", 10),
    DROP_ON_CREEPER_EXPLOSION("Spawner Drops.Drop On Creeper Explosion", true),
    DROP_ON_TNT_EXPLOSION("Spawner Drops.Drop On TNT Explosion", true),
    EXPLOSION_DROP_CHANCE_TNT("Spawner Drops.Chance On TNT Explosion", "100%"),
    EXPLOSION_DROP_CHANCE_CREEPER("Spawner Drops.Chance On Creeper Explosion", "100%"),
    SILKTOUCH_SPAWNERS("Spawner Drops.Drop On SilkTouch", true),
    SILKTOUCH_MIN_LEVEL("Spawner Drops.Minimum Required Silktouch Level", 1),
    SILKTOUCH_NATURAL_SPAWNER_DROP_CHANCE("Spawner Drops.Chance On Natural Silktouch", "100%"),
    SILKTOUCH_PLACED_SPAWNER_DROP_CHANCE("Spawner Drops.Chance On Placed Silktouch", "100%"),

    EXIT_ICON("Interfaces.Exit Icon", EpicSpawners.getInstance().isServerVersionAtLeast(ServerVersion.V1_13) ? "OAK_DOOR" : "WOOD_DOOR"),
    BUY_ICON("Interfaces.Buy Icon", "EMERALD"),
    ECO_ICON("Interfaces.Economy Icon",  EpicSpawners.getInstance().isServerVersionAtLeast(ServerVersion.V1_13) ? "SUNFLOWER" : "DOUBLE_PLANT"),
    XP_ICON("Interfaces.XP Icon",  EpicSpawners.getInstance().isServerVersionAtLeast(ServerVersion.V1_13) ? "EXPERIENCE_BOTTLE" : "EXP_BOTTLE"),
    GLASS_TYPE_1("Interfaces.Glass Type 1", 7),
    GLASS_TYPE_2("Interfaces.Glass Type 2", 11),
    GLASS_TYPE_3("Interfaces.Glass Type 3", 3),
    RAINBOW_GLASS("Interfaces.Replace Glass Type 1 With Rainbow Glass", false),

    DATABASE_SUPPORT("Database.Activate Mysql Support", false,
            "Should MySQL be used for data storage?"),

    DATABASE_IP("Database.IP", "127.0.0.1",
            "MySQL IP"),

    DATABASE_PORT("Database.Port", 3306,
            "MySQL Port"),

    DATABASE_NAME("Database.Database Name", "EpicSpawners",
            "The database you are inserting data into."),

    DATABASE_PREFIX("Database.Prefix", "ES-",
            "The prefix for tables inserted into the database."),

    DATABASE_USERNAME("Database.Username", "PUT_USERNAME_HERE",
            "MySQL Username"),

    DATABASE_PASSWORD("Database.Password", "PUT_PASSWORD_HERE",
            "MySQL Password"),

    LANGUGE_MODE("System.Language Mode", "en_US",
            "The enabled language file.",
            "More language files (if available) can be found in the plugins data folder.");

    private String setting;
    private Object option;
    private String[] comments;

    Setting(String setting, Object option, String... comments) {
        this.setting = setting;
        this.option = option;
        this.comments = comments;
    }

    Setting(String setting, Object option) {
        this.setting = setting;
        this.option = option;
        this.comments = null;
    }

    public static Setting getSetting(String setting) {
        List<Setting> settings = Arrays.stream(values()).filter(setting1 -> setting1.setting.equals(setting)).collect(Collectors.toList());
        if (settings.isEmpty()) return null;
        return settings.get(0);
    }

    public String getSetting() {
        return setting;
    }

    public Object getOption() {
        return option;
    }

    public String[] getComments() {
        return comments;
    }

    public List<String> getStringList() {
        return EpicSpawners.getInstance().getConfig().getStringList(setting);
    }

    public boolean getBoolean() {
        return EpicSpawners.getInstance().getConfig().getBoolean(setting);
    }

    public int getInt() {
        return EpicSpawners.getInstance().getConfig().getInt(setting);
    }

    public long getLong() {
        return EpicSpawners.getInstance().getConfig().getLong(setting);
    }

    public String getString() {
        return EpicSpawners.getInstance().getConfig().getString(setting);
    }

    public char getChar() {
        return EpicSpawners.getInstance().getConfig().getString(setting).charAt(0);
    }

    public double getDouble() {
        return EpicSpawners.getInstance().getConfig().getDouble(setting);
    }
}
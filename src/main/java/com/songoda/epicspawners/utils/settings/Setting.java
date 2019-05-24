package com.songoda.epicspawners.utils.settings;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.utils.ServerVersion;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Setting {

    SPAWNERS_MAX("Main.Spawner Max Upgrade", 5,
            "The maximum level a spawner can be upgraded to."),

    NAME_FORMAT("Main.Spawner Name Format", "&e{TYPE} &fSpawner [&c{AMT}x]",
            "The text displayed in the hologram positioned above every spawner."),

    FORCE_COMBINE_RADIUS("Main.Force Combine Radius", 0,
            "Spawners placed next to each other within this radius will automatically",
            "combine with each other."),

    FORCE_COMBINE_DENY("Main.Deny Place On Force Combine", false,
            "Prevent spawners from being placed next to each other within the specified radius."),

    SEARCH_RADIUS("Main.Radius To Search Around Spawners", "8x4x8",
            "The radius checked around a spawner before spawning entities.",
            "By default this is used to make sure there are no more than 7 entities",
            "around any single spawner."),

    ALTER_DELAY("Main.Default Minecraft Spawner Cooldowns", true,
            "If enabled the cool down customized in EpicSpawners will take effect."),

    ALERT_PLACE_BREAK("Main.Alerts On Place And Break", true,
            "Toggle an alerting chat message after triggered by placing or breaking a spawner."),

    SNEAK_FOR_STACK("Main.Sneak To Receive A Stacked Spawner", true,
            "Toggle ability to receive a stacked spawner when breaking a spawner while sneaking."),

    SPAWNER_HOLOGRAMS("Main.Spawners Have Holograms", true,
            "Toggle holograms showing above spawners."),

    ONLY_DROP_PLACED("Main.Only Drop Placed Spawner", false,
            "Should natural mob spawners drop upon being broken?"),

    ONLY_CHARGE_NATURAL("Main.Only Charge Natural Spawners", false,
            "Should map generated spawners charge a price in order to be broken?",
            "You can configure the cost for each spawner type in the Spawners.yml."),

    CUSTOM_SPAWNER_TICK_RATE("Main.Custom Spawner Tick Rate", 10,
            "The tick rate in which spawners will attempt to spawn.",
            "Making this smaller or larger will not effect a spawners spawn rate as",
            "this value only effects the frequency in which a spawn attempt is triggered."),

    RANDOM_LOW_HIGH("Main.Random Amount Added To Each Spawn", "1:4",
            "Spawners will always spawn a single entity for every level it contains multiplied",
            "by a random number generated between (by default) 1 and 4.",
            "For example if the random number 3 is generated then by default",
            "a level 5 spawner will spawn (5 * 3) entities which would be 15."),

    SOUNDS_ENABLED("Main.Sounds Enabled", true,
            "Toggles various sound effects used throughout the plugin."),

    DISPLAY_LEVEL_ONE("Main.Display Level In Spawner Title If Level 1", false,
            "Should a spawners hologram display its level if it's level one?"),

    OMNI_SPAWNERS("Main.OmniSpawners Enabled", true,
            "Should spawners of different mob types be stackable into a single spawner?"),

    EGGS_CONVERT_SPAWNERS("Main.Convert Spawners With Eggs", true,
            "Ability to change mob spawner type with spawn eggs."),

    HELPFUL_TIPS_ENABLED("Main.Display Helpful Tips For Operators", true,
            "Show tips to server operators."),

    UPGRADE_WITH_ECO_ENABLED("Main.Upgrade With Economy", true,
            "Can spawners be upgraded with money?"),

    UPGRADE_WITH_XP_ENABLED("Main.Upgrade With XP", true,
            "Can spawners be upgraded with XP levels?"),

    UPGRADE_COST_ECONOMY("Main.Cost To Upgrade With Economy", 10000,
            "Cost to upgrade a spawners level."),

    UPGRADE_COST_EXPERIANCE("Main.Cost To Upgrade With XP", 50,
            "Experience cost to upgrade a spawners level."),

    USE_CUSTOM_UPGRADE_EQUATION("Main.Use Custom Equations for Upgrade Costs", false,
            "Should custom equations be used to generate upgrade costs?"),

    LIQUID_REPEL_RADIUS("Main.Spawner Repel Liquid Radius", 1,
            "Prevent water from flowing next to or on top of a spawner within the here declared radius.",
    "Set to 0 to disable."),

    REDSTONE_ACTIVATE("Main.Redstone Power Deactivates Spawners", true,
            "Does redstone power disable a spawner?"),

    DISPLAY_HELP_BUTTON("Main.Display Help Button In Spawner Overview", true,
            "should the button be visible in each spawners overview GUI."),

    SPAWNERS_DONT_EXPLODE("Main.Prevent Spawners From Exploding", false,
            "Should spawners break when blown up?"),

    SPAWNERS_TO_INVENTORY("Main.Add Spawners To Inventory On Drop", false,
            "Should broken spawners be added directly to the players inventory?",
            "Alternatively they will drop to the ground?"),

    UPGRADE_PARTICLE_TYPE("Main.Upgrade Particle Type", "SPELL_WITCH",
            "The name of the particle shown when upgrading a spawner."),

    EXTRA_SPAWN_TICKS("Main.Extra Ticks Added To Each Spawn", 0,
            "After every spawner successfully spawns, a new delay is added to it.",
            "That delay is different for every spawner type and can be configured in the Spawners.yml",
            "The number configured here is then added to that delay."),

    MAX_SPAWNERS("Main.Max Spawners Per Player", -1,
            "The maximum amount of spawners a player can place. Set to -1 to allow unlimited",
            "spawner placement."),

    AUTOSAVE("Main.Auto Save Interval In Seconds", 15,
            "The amount of time in between saving to file.",
            "This is purely a safety function to prevent against unplanned crashes or",
            "restarts. With that said it is advised to keep this enabled.",
            "If however you enjoy living on the edge, feel free to turn it off."),

    NO_AI("Main.Nerf Spawner Mobs", false,
            "If enabled mobs spawned by spawners will not move or attack."),

    COST_EQUATION_EXPERIANCE("Main.Equations.Calculate XP Upgrade Cost", "{XPCost} * {Level}",
            "The equation used to calculate the experience upgrade cost."),

    COST_EQUATION_ECONOMY("Main.Equations.Calculate Economy Upgrade Cost", "{ECOCost} * {Level}",
            "The equation used to calculate the economy upgrade cost."),

    SPAWNER_EQUATION_SPAWNS("Main.Equations.Mobs Spawned Per Spawn", "{MULTI} * {RAND}",
            "The equation that defines the amount of mobs a spawner will spawn each time it is triggered."),

    NAMED_SPAWNER_TIERS("Main.Named Spawners Tiers", false,
            "Whether or not spawners will have names rather than numbers."),

    TIER_NAMES("Main.Tier Names", Arrays.asList("&7Common", "&6Uncommon", "&4Rare", "&5Mythic"),
            "The names of each spawner tier.",
            "Where one spawner is common, two is uncommon, three is rate, and four and mythic."),

    MAX_PLAYER_BOOST("Spawner Boosting.Max Multiplier For A Spawner Boost", 5,
            "The highest multiplier a spawner can be boosted to."),

    BOOST_COST("Spawner Boosting.Item Charged For A Boost", "DIAMOND:2",
            "The cost required when a player boosts their own spawner.",
            "If you would rather charge experience or economy then enter respectively",
            "ECO or XP in place of the default DIAMOND."),

    HOSTILE_MOBS_ATTACK_SECOND("entity.Hostile Mobs Attack Second", false,
            "Should hostile mobs attack only if attacked first?"),

    ONLY_DROP_STACKED("Spawner Drops.Only Drop Stacked Spawners", false,
            "Should stacked spawners always drop their whole stack when broken?"),

    MOB_KILLING_COUNT("Spawner Drops.Allow Killing Mobs To Drop Spawners", true,
            "Should spawners drop when enough mobs of that spawners type are killed?"),

    COUNT_UNNATURAL_KILLS("Spawner Drops.Count Unnatural Kills Towards Spawner Drop", false,
            "Can mobs from spawners count towards the spawner drop count?"),

    KILL_GOAL("Spawner Drops.Kills Needed for Drop", 100,
            "Amount of mob kills required to drop a spawner."),

    ALERT_INTERVAL("Spawner Drops.Alert Every X Before Drop", 10,
            "Alert players every x amount of kills before dropping spawner."),

    DROP_ON_CREEPER_EXPLOSION("Spawner Drops.Drop On Creeper Explosion", true,
            "Should a spawner destroyed by a creeper drop?"),

    DROP_ON_TNT_EXPLOSION("Spawner Drops.Drop On TNT Explosion", true,
            "Should a spawner destroyed by TNT drop?"),

    EXPLOSION_DROP_CHANCE_TNT("Spawner Drops.Chance On TNT Explosion", "100%",
            "Chance of a TNT explosion dropping a spawner."),

    EXPLOSION_DROP_CHANCE_CREEPER("Spawner Drops.Chance On Creeper Explosion", "100%",
            "Chance of a creeper explosion dropping a spawner."),

    SILKTOUCH_SPAWNERS("Spawner Drops.Drop On SilkTouch", true,
            "Do spawners drop when broken with a pick enchanted with silk touch?"),

    SILKTOUCH_MIN_LEVEL("Spawner Drops.Minimum Required Silktouch Level", 1,
            "What level of silk touch is required to drop a spawner?"),

    SILKTOUCH_NATURAL_SPAWNER_DROP_CHANCE("Spawner Drops.Chance On Natural Silktouch", "100%",
            "Chance of a natural spawner dropping with silk touch."),

    SILKTOUCH_PLACED_SPAWNER_DROP_CHANCE("Spawner Drops.Chance On Placed Silktouch", "100%",
            "Chance of a placed spawner dropping with silk touch."),

    VAULT_ECONOMY("Economy.Use Vault Economy", true,
            "Should Vault be used?"),

    PLAYER_POINTS_ECONOMY("Economy.Use Player Points Economy", false,
            "Should PlayerPoints be used?"),

    EXIT_ICON("Interfaces.Exit Icon", EpicSpawners.getInstance().isServerVersionAtLeast(ServerVersion.V1_13) ? "OAK_DOOR" : "WOOD_DOOR",
            "Item to be displayed as the icon for exiting the interface."),

    BUY_ICON("Interfaces.Buy Icon", "EMERALD",
            "Item to be displayed as the icon for buying a spawner."),

    ECO_ICON("Interfaces.Economy Icon", EpicSpawners.getInstance().isServerVersionAtLeast(ServerVersion.V1_13) ? "SUNFLOWER" : "DOUBLE_PLANT",
            "Item to be displayed as the icon for economy upgrades."),

    XP_ICON("Interfaces.XP Icon", EpicSpawners.getInstance().isServerVersionAtLeast(ServerVersion.V1_13) ? "EXPERIENCE_BOTTLE" : "EXP_BOTTLE",
            "Item to be displayed as the icon for XP upgrades."),

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
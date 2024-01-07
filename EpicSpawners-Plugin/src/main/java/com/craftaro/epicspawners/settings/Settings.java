package com.craftaro.epicspawners.settings;

import com.craftaro.core.configuration.Config;
import com.craftaro.core.configuration.ConfigSetting;
import com.craftaro.core.hooks.EconomyManager;
import com.craftaro.core.hooks.HologramManager;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicspawners.EpicSpawners;

import java.util.stream.Collectors;

public class Settings {
    private static final Config config = EpicSpawners.getInstance().getCoreConfig();

    public static final ConfigSetting SPAWNERS_MAX = new ConfigSetting(config, "Main.Spawner Max Stack", 5,
            "The maximum stack size a spawner can be stacked to.");

    public static final ConfigSetting NAME_FORMAT = new ConfigSetting(config, "Main.Spawner Name Format", "&e{TYPE} &fSpawner [&c{AMT}x]",
            "The text displayed in the hologram positioned above every spawner.");

    public static final ConfigSetting FORCE_COMBINE_RADIUS = new ConfigSetting(config, "Main.Force Combine Radius", 0,
            "Spawners placed next to each other within this radius will automatically",
            "combine with each other.");

    public static final ConfigSetting MAX_SPAWNERS_PER_CHUNK = new ConfigSetting(config, "Main.Max Spawners Per Chunk", -1,
            "What should be the maximum amount of placed spawners in a chunk?",
            "Setting this to -1 will disable the function.");

    public static final ConfigSetting FORCE_COMBINE_DENY = new ConfigSetting(config, "Main.Deny Place On Force Combine", false,
            "Prevent spawners from being placed next to each other within the specified radius.");

    public static final ConfigSetting SEARCH_RADIUS = new ConfigSetting(config, "Main.Radius To Search Around Spawners", "8x4x8",
            "The radius checked around a spawner before spawning entities.",
            "By default this is used to make sure there are no more than 7 entities",
            "around any single spawner.");

    public static final ConfigSetting ALERT_PLACE_BREAK = new ConfigSetting(config, "Main.Alerts On Place And Break", true,
            "Toggle an alerting chat message after triggered by placing or breaking a spawner.");

    public static final ConfigSetting SNEAK_FOR_STACK = new ConfigSetting(config, "Main.Sneak To Receive A Stacked Spawner", true,
            "Toggle ability to receive a stacked spawner when breaking a spawner while sneaking.");

    public static final ConfigSetting SPAWNER_HOLOGRAMS = new ConfigSetting(config, "Main.Spawners Have Holograms", true,
            "Toggle holograms showing above spawners.");

    public static final ConfigSetting ONLY_DROP_PLACED = new ConfigSetting(config, "Main.Only Drop Placed Spawner", false,
            "Should natural mob spawners drop upon being broken?");

    public static final ConfigSetting ONLY_CHARGE_NATURAL = new ConfigSetting(config, "Main.Only Charge Natural Spawners", false,
            "Should map generated spawners charge a price in order to be broken?",
            "You can configure the cost for each spawner type in the Spawners.yml.");

    public static final ConfigSetting CUSTOM_SPAWNER_TICK_RATE = new ConfigSetting(config, "Main.Custom Spawner Tick Rate", 10,
            "The tick rate in which spawners will attempt to spawn.",
            "Making this smaller or larger will not effect a spawners spawn rate as",
            "this value only effects the frequency in which a spawn attempt is triggered.");

    public static final ConfigSetting SOUNDS_ENABLED = new ConfigSetting(config, "Main.Sounds Enabled", true,
            "Toggles various sound effects used throughout the plugin.");

    public static final ConfigSetting DISPLAY_TIER_ONE = new ConfigSetting(config, "Main.Display Tier In Spawner Title If Tier 1", false,
            "Should a spawners hologram display its tier if it's tier one?");

    public static final ConfigSetting OMNI_SPAWNERS = new ConfigSetting(config, "Main.OmniSpawners Enabled", true,
            "Should spawners of different mob types be stackable into a single spawner?");

    public static final ConfigSetting EGGS_CONVERT_SPAWNERS = new ConfigSetting(config, "Main.Convert Spawners With Eggs", true,
            "Ability to change mob spawner type with spawn eggs.");

    public static final ConfigSetting UPGRADE_WITH_ECONOMY_ENABLED = new ConfigSetting(config, "Main.Upgrade With Economy", true,
            "Can spawners be upgraded with money?");

    public static final ConfigSetting UPGRADE_WITH_LEVELS_ENABLED = new ConfigSetting(config, "Main.Upgrade With Levels", true,
            "Can spawners be upgraded with Levels?");

    public static final ConfigSetting LIQUID_REPEL_RADIUS = new ConfigSetting(config, "Main.Spawner Repel Liquid Radius", 1,
            "Prevent water from flowing next to or on top of a spawner within the here declared radius.",
            "Set to 0 to disable.");

    public static final ConfigSetting REDSTONE_ACTIVATE = new ConfigSetting(config, "Main.Redstone Power Deactivates Spawners", true,
            "Does redstone power disable a spawner?");

    public static final ConfigSetting DISPLAY_HELP_BUTTON = new ConfigSetting(config, "Main.Display Help Button In Spawner Overview", true,
            "should the button be visible in each spawners overview GUI.");

    public static final ConfigSetting SPAWNERS_DONT_EXPLODE = new ConfigSetting(config, "Main.Prevent Spawners From Exploding", false,
            "Should spawners not break when blown up?");

    public static final ConfigSetting SPAWNERS_TO_INVENTORY = new ConfigSetting(config, "Main.Add Spawners To Inventory On Drop", false,
            "Should broken spawners be added directly to the players inventory?",
            "Alternatively they will drop to the ground?");

    public static final ConfigSetting SHOW_PARTICLES = new ConfigSetting(config, "Main.Show Particles", true,
            "Should particles be shown around spawners?");

    public static final ConfigSetting UPGRADE_PARTICLE_TYPE = new ConfigSetting(config, "Main.Upgrade Particle Type", "SPELL_WITCH",
            "The name of the particle shown when upgrading a spawner.");

    public static final ConfigSetting EXTRA_SPAWN_TICKS = new ConfigSetting(config, "Main.Extra Ticks Added To Each Spawn", 0,
            "After every spawner successfully spawns, a new delay is added to it.",
            "That delay is different for every spawner type and can be configured in the Spawners.yml",
            "The number configured here is then added to that delay.");

    public static final ConfigSetting MAX_SPAWNERS = new ConfigSetting(config, "Main.Max Spawners Per Player", -1,
            "The maximum amount of spawners a player can place. Set to -1 to allow unlimited",
            "spawner placement.");

    public static final ConfigSetting GIVE_OLD_EGG = new ConfigSetting(config, "Main.Give Previous Mob as Egg when replacing mob", true,
            "Should the previous mob of the spawner be given as an egg when changing the spawner's mob?");

    public static final ConfigSetting AUTOSAVE = new ConfigSetting(config, "Main.Auto Save Interval In Seconds", 15,
            "The amount of time in between saving to file.",
            "This is purely a safety function to prevent against unplanned crashes or",
            "restarts. With that said it is advised to keep this enabled.",
            "If however you enjoy living on the edge, feel free to turn it off.");

    public static final ConfigSetting DISABLE_NATURAL_SPAWNERS = new ConfigSetting(config, "Main.Disable Natural Spawners", false,
            "Should natural spawners be disabled?");

    public static final ConfigSetting NO_AI = new ConfigSetting(config, "Main.Nerf Spawner Mobs", false,
            "If enabled mobs spawned by spawners will not move or attack.");

    public static final ConfigSetting SPAWNER_SPAWN_EQUATION = new ConfigSetting(config, "Main.Equations.Mobs Spawned Per Single Spawn", "{RAND}",
            "The equation that defines the amount of mobs a spawner will spawn each time it is triggered.",
            "This is ran once for each spawner in the stack then summed up after. You may use the variable {STACK_SIZE}",
            "or {RAND} If you like. For more information about how to make equations for this option look up the",
            "Java ScriptEngine.");

    public static final ConfigSetting RANDOM_LOW_HIGH = new ConfigSetting(config, "Main.Equations.Random Amount Variable", "1:4",
            "This value depicts the variable {RAND} in equations used by this plugin",
            "It generates a random number between (by default) 1 and 4.");

    public static final ConfigSetting IGNORE_MAX_ON_FIRST_SPAWN = new ConfigSetting(config, "Main.Ignore Max On First Spawn", false,
            "Should the max entity count around spawners be",
            "ignored on their first spawn?");

    public static final ConfigSetting REMOVE_CORRUPTED_SPAWNERS = new ConfigSetting(config, "Main.Remove Corrupted Spawners", true,
            "Should spawners without valid values be removed?",
            "This may need to be disabled for compatibility that use",
            "empty spawners for custom blocks.");

    public static final ConfigSetting ECONOMY_PLUGIN = new ConfigSetting(config, "Main.Economy", EconomyManager.getEconomy() == null ? "Vault" : EconomyManager.getEconomy().getName(),
            "Which economy plugin should be used?",
            "Supported plugins you have installed: \"" + EconomyManager.getManager().getRegisteredPlugins().stream().collect(Collectors.joining("\", \"")) + "\".");

    public static final ConfigSetting HOLOGRAM_PLUGIN = new ConfigSetting(config, "Main.Hologram",
            HologramManager.getHolograms() == null ? "HolographicDisplays" : HologramManager.getHolograms().getName(),
            "Which hologram plugin should be used?",
            "You can choose from \"" + HologramManager.getManager().getRegisteredPlugins().stream().collect(Collectors.joining(", ")) + "\".");

    public static final ConfigSetting EPIC_ANCHORS_PLAYER_WEIGHT = new ConfigSetting(config, "Main.EpicAnchorsPlayerWeight",
            1,
            "This setting affects how Anchors (Chunkloader) from EpicAnchors are affecting EpicSpawners.\n" +
                    "-1 = Skips NearbyPlayers condition if the chunk is loaded by an Anchor\n" +
                    "0 and greater = Counts as *n* players for the NearbyPlayers condition");

    public static final ConfigSetting USE_PROTECTION_PLUGINS = new ConfigSetting(config, "Main.Use Protection Plugins", true,
            "Should we use protection plugins?");

    public static final ConfigSetting CHARGE_FOR_CREATIVE = new ConfigSetting(config, "Main.Charge For Creative", false,
            "Should players in creative have to pay for perks like upgrades and boosting?");

    public static final ConfigSetting MAX_PLAYER_BOOST = new ConfigSetting(config, "Spawner Boosting.Max Multiplier For A Spawner Boost", 5,
            "The highest multiplier a spawner can be boosted to.");

    public static final ConfigSetting BOOST_COST = new ConfigSetting(config, "Spawner Boosting.Item Charged For A Boost", "DIAMOND:2",
            "The cost required when a player boosts their own spawner.",
            "If you would rather charge experience or economy then enter respectively",
            "ECO or XP in place of the default DIAMOND.");

    public static final ConfigSetting HOSTILE_MOBS_ATTACK_SECOND = new ConfigSetting(config, "entity.Hostile Mobs Attack Second", false,
            "Should hostile mobs attack only if attacked first?");

    public static final ConfigSetting ONLY_DROP_STACKED = new ConfigSetting(config, "Spawner Drops.Only Drop Stacked Spawners", false,
            "Should stacked spawners always drop their whole stack when broken?");

    public static final ConfigSetting MOB_KILLING_COUNT = new ConfigSetting(config, "Spawner Drops.Allow Killing Mobs To Drop Spawners", true,
            "Should spawners drop when enough mobs of that spawners type are killed?");

    public static final ConfigSetting COUNT_UNNATURAL_KILLS = new ConfigSetting(config, "Spawner Drops.Count Unnatural Kills Towards Spawner Drop", false,
            "Can mobs from spawners count towards the spawner drop count?");

    public static final ConfigSetting KILL_DROP_GOAL = new ConfigSetting(config, "Spawner Drops.Kills Needed for Drop", 100,
            "Amount of mob kills required to drop a spawner.");

    public static final ConfigSetting KILL_DROP_CHANCE = new ConfigSetting(config, "Spawner Drops.Chance of Drop", 2.5,
            "The chance a mob kill will drop a spawner.");

    public static final ConfigSetting ALERT_INTERVAL = new ConfigSetting(config, "Spawner Drops.Alert Every X Before Drop", 10,
            "Alert players every x amount of kills before dropping spawner.");

    public static final ConfigSetting EXPLOSION_DROP_CHANCE_TNT = new ConfigSetting(config, "Spawner Drops.Chance On TNT Explosion", "100%",
            "Chance of a TNT explosion dropping a spawner.");

    public static final ConfigSetting EXPLOSION_DROP_CHANCE_CREEPER = new ConfigSetting(config, "Spawner Drops.Chance On Creeper Explosion", "100%",
            "Chance of a creeper explosion dropping a spawner.");

    public static final ConfigSetting SILKTOUCH_SPAWNERS = new ConfigSetting(config, "Spawner Drops.Drop On SilkTouch", true,
            "Do spawners drop when broken with a pick enchanted with silk touch?");

    public static final ConfigSetting SILKTOUCH_MIN_LEVEL = new ConfigSetting(config, "Spawner Drops.Minimum Required Silktouch Level", 1,
            "What level of silk touch is required to drop a spawner?");

    public static final ConfigSetting SILKTOUCH_NATURAL_SPAWNER_DROP_CHANCE = new ConfigSetting(config, "Spawner Drops.Chance On Natural Silktouch", "100%",
            "Chance of a natural spawner dropping with silk touch.");

    public static final ConfigSetting SILKTOUCH_PLACED_SPAWNER_DROP_CHANCE = new ConfigSetting(config, "Spawner Drops.Chance On Placed Silktouch", "100%",
            "Chance of a placed spawner dropping with silk touch.");

    public static final ConfigSetting EXIT_ICON = new ConfigSetting(config, "Interfaces.Exit Icon", XMaterial.OAK_DOOR.parseMaterial().name(),
            "Item to be displayed as the icon for exiting the interface.");

    public static final ConfigSetting BUY_ICON = new ConfigSetting(config, "Interfaces.Buy Icon", "EMERALD",
            "Item to be displayed as the icon for buying a spawner.");

    public static final ConfigSetting ECO_ICON = new ConfigSetting(config, "Interfaces.Economy Icon", XMaterial.SUNFLOWER.parseMaterial().name(),
            "Item to be displayed as the icon for economy upgrades.");

    public static final ConfigSetting XP_ICON = new ConfigSetting(config, "Interfaces.XP Icon", XMaterial.EXPERIENCE_BOTTLE.parseMaterial().name(),
            "Item to be displayed as the icon for XP upgrades.");

    public static final ConfigSetting CONVERT_ICON = new ConfigSetting(config, "Interfaces.Convert Icon", "EGG");

    public static final ConfigSetting BOOST_ICON = new ConfigSetting(config, "Interfaces.Boost Icon", "BLAZE_POWDER");

    public static final ConfigSetting GLASS_TYPE_1 = new ConfigSetting(config, "Interfaces.Glass Type 1", "GRAY_STAINED_GLASS_PANE");
    public static final ConfigSetting GLASS_TYPE_2 = new ConfigSetting(config, "Interfaces.Glass Type 2", "BLUE_STAINED_GLASS_PANE");
    public static final ConfigSetting GLASS_TYPE_3 = new ConfigSetting(config, "Interfaces.Glass Type 3", "LIGHT_BLUE_STAINED_GLASS_PANE");

    public static final ConfigSetting LANGUGE_MODE = new ConfigSetting(config, "System.Language Mode", "en_US",
            "The enabled language file.",
            "More language files (if available) can be found in the plugins data folder.");

    /**
     * In order to set dynamic economy comment correctly, this needs to be
     * called after EconomyManager load
     */
    public static void setupConfig() {
        config.load();
        config.setAutoremove(true).setAutosave(true);

        // convert economy settings
        if (config.getBoolean("Economy.Use Vault Economy") && EconomyManager.getManager().isEnabled("Vault")) {
            config.set("Main.Economy", "Vault");
        } else if (config.getBoolean("Economy.Use Reserve Economy") && EconomyManager.getManager().isEnabled("Reserve")) {
            config.set("Main.Economy", "Reserve");
        } else if (config.getBoolean("Economy.Use Player Points Economy") && EconomyManager.getManager().isEnabled("PlayerPoints")) {
            config.set("Main.Economy", "PlayerPoints");
        }

        config.saveChanges();
    }
}

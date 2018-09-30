package com.songoda.epicspawners.utils;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.api.utils.ConfigWrapper;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by songo on 6/4/2017.
 */
public class SettingsManager implements Listener {

    private static final Pattern SETTINGS_PATTERN = Pattern.compile("(.{1,28}(?:\\s|$))|(.{0,28})", Pattern.DOTALL);

    private static ConfigWrapper defs;
    private final EpicSpawnersPlugin instance;
    private String pluginName = "EpicSpawners";
    private Map<Player, String> cat = new HashMap<>();
    private Map<Player, String> current = new HashMap<>();

    public SettingsManager(EpicSpawnersPlugin plugin) {
        this.instance = plugin;

        plugin.saveResource("SettingDefinitions.yml", true);
        defs = new ConfigWrapper(plugin, "", "SettingDefinitions.yml");
        defs.createNewFile("Loading data file", pluginName + " SettingDefinitions file");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();

        if (event.getInventory() != event.getWhoClicked().getOpenInventory().getTopInventory()
                || clickedItem == null || !clickedItem.hasItemMeta()
                || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        if (event.getInventory().getTitle().equals(pluginName + " Settings Manager")) {
            event.setCancelled(true);
            if (clickedItem.getType().name().contains("STAINED_GLASS")) return;

            String type = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            this.cat.put((Player) event.getWhoClicked(), type);
            this.openEditor((Player) event.getWhoClicked());
        } else if (event.getInventory().getTitle().equals(pluginName + " Settings Editor")) {
            event.setCancelled(true);
            if (clickedItem.getType().name().contains("STAINED_GLASS")) return;

            Player player = (Player) event.getWhoClicked();

            String key = cat.get(player) + "." + ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            if (instance.getConfig().get(key).getClass().getName().equals("java.lang.Boolean")) {
                this.instance.getConfig().set(key, !instance.getConfig().getBoolean(key));
                this.finishEditing(player);
            } else {
                this.editObject(player, key);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!current.containsKey(player)) return;

        String value = current.get(player);
        FileConfiguration config = instance.getConfig();
        if (config.isInt(value)) {
            config.set(value, Integer.parseInt(event.getMessage()));
        } else if (config.isDouble(value)) {
            config.set(value, Double.parseDouble(event.getMessage()));
        } else if (config.isString(value)) {
            config.set(value, event.getMessage());
        }

        this.finishEditing(player);
        event.setCancelled(true);
    }

    public void finishEditing(Player player) {
        this.current.remove(player);
        this.instance.saveConfig();
        this.openEditor(player);
    }


    public void editObject(Player player, String current) {
        this.current.put(player, ChatColor.stripColor(current));

        player.closeInventory();
        player.sendMessage("");
        player.sendMessage(TextComponent.formatText("&7Please enter a value for &6" + current + "&7."));
        if (instance.getConfig().isInt(current) || instance.getConfig().isDouble(current)) {
            player.sendMessage(TextComponent.formatText("&cUse only numbers."));
        }
        player.sendMessage("");
    }

    public void openSettingsManager(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, pluginName + " Settings Manager");
        ItemStack glass = Methods.getGlass();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glass);
        }

        int slot = 10;
        for (String key : instance.getConfig().getDefaultSection().getKeys(false)) {
            ItemStack item = new ItemStack(Material.WHITE_WOOL, 1, (byte) (slot - 9)); //ToDo: Make this function as it was meant to.
            ItemMeta meta = item.getItemMeta();
            meta.setLore(Collections.singletonList(TextComponent.formatText("&6Click To Edit This Category.")));
            meta.setDisplayName(TextComponent.formatText("&f&l" + key));
            item.setItemMeta(meta);
            inventory.setItem(slot, item);
            slot++;
        }

        player.openInventory(inventory);
    }

    public void openEditor(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, pluginName + " Settings Editor");
        FileConfiguration config = instance.getConfig();

        int slot = 0;
        for (String key : config.getConfigurationSection(cat.get(player)).getKeys(true)) {
            String fKey = cat.get(player) + "." + key;
            ItemStack item = new ItemStack(Material.DIAMOND_HELMET);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(TextComponent.formatText("&6" + key));

            List<String> lore = new ArrayList<>();
            if (config.isBoolean(fKey)) {
                item.setType(Material.LEVER);
                lore.add(TextComponent.formatText(config.getBoolean(fKey) ? "&atrue" : "&cfalse"));
            } else if (config.isString(fKey)) {
                item.setType(Material.PAPER);
                lore.add(TextComponent.formatText("&9" + config.getString(fKey)));
            } else if (config.isInt(fKey)) {
                item.setType(Material.CLOCK);
                lore.add(TextComponent.formatText("&5" + config.getInt(fKey)));
            }

            if (defs.getConfig().contains(fKey)) {
                String text = defs.getConfig().getString(key);

                Matcher m = SETTINGS_PATTERN.matcher(text);
                while (m.find()) {
                    if (m.end() != text.length() || m.group().length() != 0)
                        lore.add(TextComponent.formatText("&7" + m.group()));
                }
            }

            meta.setLore(lore);
            item.setItemMeta(meta);

            inventory.setItem(slot, item);
            slot++;
        }

        player.openInventory(inventory);
    }

    public void updateSettings() {
        FileConfiguration config = instance.getConfig();

        for (Setting setting : Setting.values()) {
            if (config.contains("settings." + setting.oldSetting)) {
                config.addDefault(setting.setting, instance.getConfig().get("settings." + setting.oldSetting));
                config.set("settings." + setting.oldSetting, null);
            } else {
                config.addDefault(setting.setting, setting.option);
            }
        }

        config.set("settings", null);
    }

    public enum Setting {

        SPAWNERS_MAX("spawners-max", "Main.Spawner Max Upgrade", 5),
        NAME_FORMAT("Name-format", "Main.Spawner Name Format", "&e{TYPE} &fSpawner [&c{AMT}x]"),
        FORCE_COMBINE_RADIUS("Force-Combine-Radius", "Main.Force Combine Radius", 0),
        FORCE_COMBINE_DENY("Force-Combine-Deny", "Main.Deny Place On Force Combine", false),
        SEARCH_RADIUS("Search-Radius", "Main.Radius To Search Around Spawners", "8x4x8"),
        ALTER_DELAY("Alter-Delay", "Main.Default Minecraft Spawner Cooldowns", true),
        ALERT_PLACE_BREAK("Alert-place-break", "Main.Alerts On Place And Break", true),
        SNEAK_FOR_STACK("Sneak-for-stack", "Main.Sneak To Receive A Stacked Spawner", true),
        SPAWNER_HOLOGRAMS("spawners-holograms", "Main.Spawners Have Holograms", true),
        ONLY_DROP_PLACED("Only-drop-placed", "Main.Only Drop Placed Spawner", false),
        ONLY_CHARGE_NATURAL("Only-charge-natural", "Main.Only Charge Natural Spawners", false),
        CUSTOM_SPAWNER_TICK_RATE("123", "Main.Custom Spawner Tick Rate", 10),
        RANDOM_LOW_HIGH("Random-Low & Random-High", "Main.Random Amount Added To Each Spawn", "1:3"),
        SOUNDS_ENABLED("Sounds", "Main.Sounds Enabled", true),
        DISPLAY_LEVEL_ONE("Display-Level-One", "Main.Display Level In Spawner Title If Level 1", false),
        OMNI_SPAWNERS("OmniSpawners", "Main.OmniSpawners Enabled", true),
        EGGS_CONVERT_SPAWNERS("Eggs-convert-spawners", "Main.Convert Spawners With Eggs", true),
        HELPFUL_TIPS_ENABLED("Helpful-Tips", "Main.Display Helpful Tips For Operators", true),
        UPGRADE_WITH_ECO_ENABLED("Upgrade-with-eco", "Main.Upgrade With Economy", true),
        UPGRADE_WITH_XP_ENABLED("Upgrade-with-xp", "Main.Upgrade With XP", true),
        UPGRADE_COST_ECO("Upgrade-eco-cost", "Main.Cost To Upgrade With Economy", 10000),
        UPGRADE_COST_XP("Upgrade-xp-cost", "Main.Cost To Upgrade With XP", 50),
        USE_CUSTOM_UPGRADE_EQUATION("Use-equations", "Main.Use Custom Equations for Upgrade Costs", false),
        LIQUID_REPEL_RADIUS("spawners-repel-radius", "Main.Spawner Repel Liquid Radius", 1),
        REDSTONE_ACTIVATE("redstone-activate", "Main.Redstone Power Deactivates Spawners", true),
        DISPLAY_HELP_BUTTON("How-to", "Main.Display Help Button In Spawner Overview", true),
        SPAWNERS_DONT_EXPLODE("spawners-dont-explode", "Main.Prevent Spawners From Exploding", false),
        SPAWNERS_TO_INVENTORY("Add-spawners-To-Inventory-On-Drop", "Main.Add Spawners To Inventory On Drop", false),
        UPGRADE_PARTICLE_TYPE("Upgrade-particle-Type", "Main.Upgrade Particle Type", "SPELL_WITCH"),
        EXTRA_SPAWN_TICKS("Upgrade-particle-Type", "Main.Extra Ticks Added To Each Spawn", 0),

        COST_EQUATION_XP("XP-cost-equation", "Main.Equations.Calculate XP Upgrade Cost", "{XPCost} * {Level}"),
        COST_EQUATION_ECO("ECO-cost-equation", "Main.Equations.Calculate Economy Upgrade Cost", "{ECOCost} * {Level}"),
        SPAWNER_EQUATION_SPAWNS("spawners-Spawn-Equation", "Main.Equations.Mobs Spawned Per Spawn", "{MULTI} + {RAND}"),

        NAMED_SPAWNER_TIERS("-", "Main.Named Spawners Tiers", false),
        TIER_NAMES("-", "Main.Tier Names", Arrays.asList("&7Common", "&6Uncommon", "&4Rare", "&5Mythic")),

        BOOST_MULTIPLIER("Boost-Multiplier", "Spawner Boosting.Boost Multiplier", "0.5"),
        MAX_PLAYER_BOOST("Max-Player-Boost", "Spawner Boosting.Max Multiplier For A Spawner Boost", 5),
        BOOST_COST("Boost-cost", "Spawner Boosting.Item Charged For A Boost", "DIAMOND:2"),

        HOSTILE_MOBS_ATTACK_SECOND("Hostile-mobs-attack-second", "entity.Hostile Mobs Attack Second", false),

        ONLY_DROP_STACKED("Only-drop-stacked", "Spawner Drops.Only Drop Stacked Spawners", false),
        MOB_KILLING_COUNT("Mob-kill-counting", "Spawner Drops.Allow Killing Mobs To Drop Spawners", true),
        COUNT_UNNATURAL_KILLS("Count-unnatural-kills", "Spawner Drops.Count Unnatural Kills Towards Spawner Drop", false),
        KILL_GOAL("Goal", "Spawner Drops.Kills Needed for Drop", 100),
        ALERT_INTERVAL("Alert-every", "Spawner Drops.Alert Every X Before Drop", 10),
        DROP_ON_CREEPER_EXPLOSION("Drop-on-creeper-explosion", "Spawner Drops.Drop On Creeper Explosion", true),
        DROP_ON_TNT_EXPLOSION("Drop-on-tnt-explosion", "Spawner Drops.Drop On TNT Explosion", true),
        EXPLOSION_DROP_CHANCE_TNT("Tnt-explosion-drop-chance", "Spawner Drops.Chance On TNT Explosion", "100%"),
        EXPLOSION_DROP_CHANCE_CREEPER("Creeper-explosion-drop-chance", "Spawner Drops.Chance On Creeper Explosion", "100%"),
        SILKTOUCH_SPAWNERS("Silktouch-spawners", "Spawner Drops.Drop On SilkTouch", true),
        SILKTOUCH_NATURAL_SPAWNER_DROP_CHANCE("Silktouch-natural-drop-chance", "Spawner Drops.Chance On Natural Silktouch", "100%"),
        SILKTOUCH_PLACED_SPAWNER_DROP_CHANCE("Silktouch-placed-drop-chance", "Spawner Drops.Chance On Placed Silktouch", "100%"),

        EXIT_ICON("Exit-Icon", "Interfaces.Exit Icon", "OAK_DOOR"),
        BUY_ICON("Buy-Icon", "Interfaces.Buy Icon", "EMERALD"),
        ECO_ICON("ECO-Icon", "Interfaces.Economy Icon", "SUNFLOWER"),
        XP_ICON("XP-Icon", "Interfaces.XP Icon", "EXPERIENCE_BOTTLE"),
        GLASS_TYPE_1("Glass-Type-1", "Interfaces.Glass Type 1", 7),
        GLASS_TYPE_2("Glass-Type-2", "Interfaces.Glass Type 2", 11),
        GLASS_TYPE_3("Glass-Type-3", "Interfaces.Glass Type 3", 3),
        RAINBOW_GLASS("Rainbow-Glass", "Interfaces.Replace Glass Type 1 With Rainbow Glass", false),

        DATABASE_SUPPORT("-", "Database.Activate Mysql Support", false),
        DATABASE_IP("-", "Database.IP", "127.0.0.1"),
        DATABASE_PORT("-", "Database.Port", 3306),
        DATABASE_NAME("-", "Database.Database Name", "EpicSpawners"),
        DATABASE_USERNAME("-", "Database.Username", "PUT_USERNAME_HERE"),
        DATABASE_PASSWORD("-", "Database.Password", "PUT_PASSWORD_HERE"),

        LANGUGE_MODE("-", "System.Language Mode", "en_US"),
        DEBUG_MODE("Debug-Mode", "System.Debugger Enabled", false);

        private final String setting, oldSetting;
        private final Object option;

        private Setting(String oldSetting, String setting, Object option) {
            this.oldSetting = oldSetting;
            this.setting = setting;
            this.option = option;
        }

    }

}

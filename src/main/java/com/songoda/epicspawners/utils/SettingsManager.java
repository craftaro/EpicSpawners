package com.songoda.epicspawners.utils;

import com.songoda.arconix.api.utils.ConfigWrapper;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

    private EpicSpawners instance = EpicSpawners.pl();

    private String pluginName = "EpicSpawners";

    private Map<Player, String> cat = new HashMap<>();

    private static ConfigWrapper defs;

    public SettingsManager() {
        instance.saveResource("SettingDefinitions.yml", true);
        defs = new ConfigWrapper(instance, "", "SettingDefinitions.yml");
        defs.createNewFile("Loading data file", pluginName + " SettingDefinitions file");
        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    public Map<Player, String> current = new HashMap<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory() == null
                || e.getCurrentItem() == null
                || !e.getCurrentItem().hasItemMeta()
                || !e.getCurrentItem().getItemMeta().hasDisplayName()
                || e.getWhoClicked().getOpenInventory().getTopInventory() != e.getInventory()) {
            return;
        }
        if (e.getInventory().getTitle().equals(pluginName + " Settings Manager")) {

            if (e.getCurrentItem().getType().equals(Material.STAINED_GLASS_PANE)) {
                e.setCancelled(true);
                return;
            }

            String type = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
            cat.put((Player) e.getWhoClicked(), type);
            openEditor((Player) e.getWhoClicked());
            e.setCancelled(true);
        } else if (e.getInventory().getTitle().equals(pluginName + " Settings Editor")) {

            if (e.getCurrentItem().getType().equals(Material.STAINED_GLASS_PANE)) {
                e.setCancelled(true);
                return;
            }

            Player p = (Player) e.getWhoClicked();
            e.setCancelled(true);

            String key = cat.get(p) + "." + ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());

            if (instance.getConfig().get(key).getClass().getName().equals("java.lang.Boolean")) {
                boolean bool = (Boolean) instance.getConfig().get(key);
                if (!bool)
                    instance.getConfig().set(key, true);
                else
                    instance.getConfig().set(key, false);
                finishEditing(p);
            } else {
                editObject(p, key);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        final Player p = e.getPlayer();
        if (!current.containsKey(p)) {
            return;
        }
        switch (instance.getConfig().get(current.get(p)).getClass().getName()) {
            case "java.lang.Integer":
                instance.getConfig().set(current.get(p), Integer.parseInt(e.getMessage()));
                break;
            case "java.lang.Double":
                instance.getConfig().set(current.get(p), Double.parseDouble(e.getMessage()));
                break;
            case "java.lang.String":
                instance.getConfig().set(current.get(p), e.getMessage());
                break;
        }
        finishEditing(p);
        e.setCancelled(true);

    }

    public void finishEditing(Player p) {
        current.remove(p);
        instance.saveConfig();
        openEditor(p);
    }


    public void editObject(Player p, String current) {
        this.current.put(p, ChatColor.stripColor(current));
        p.closeInventory();
        p.sendMessage("");
        p.sendMessage(Arconix.pl().getApi().format().formatText("&7Please enter a value for &6" + current + "&7."));
        if (instance.getConfig().get(current).getClass().getName().equals("java.lang.Integer")) {
            p.sendMessage(Arconix.pl().getApi().format().formatText("&cUse only numbers."));
        }
        p.sendMessage("");
    }

    public void openSettingsManager(Player p) {
        Inventory i = Bukkit.createInventory(null, 27, pluginName + " Settings Manager");
        int nu = 0;
        while (nu != 27) {
            i.setItem(nu, Methods.getGlass());
            nu++;
        }

        int spot = 10;
        for (String key : instance.getConfig().getConfigurationSection("").getKeys(false)) {
            ItemStack item = new ItemStack(Material.WOOL, 1, (byte) (spot - 9));
            ItemMeta meta = item.getItemMeta();
            meta.setLore(Collections.singletonList(Arconix.pl().getApi().format().formatText("&6Click To Edit This Category.")));
            meta.setDisplayName(Arconix.pl().getApi().format().formatText("&f&l" + key));
            item.setItemMeta(meta);
            i.setItem(spot, item);
            spot++;
        }
        p.openInventory(i);
    }

    public void openEditor(Player p) {
        Inventory i = Bukkit.createInventory(null, 54, pluginName + " Settings Editor");

        int num = 0;
        for (String key : instance.getConfig().getConfigurationSection(cat.get(p)).getKeys(true)) {
            String fKey = cat.get(p) + "." + key;
            ItemStack item = new ItemStack(Material.DIAMOND_HELMET);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Arconix.pl().getApi().format().formatText("&6" + key));
            ArrayList<String> lore = new ArrayList<>();
            switch (instance.getConfig().get(fKey).getClass().getName()) {
                case "java.lang.Boolean":

                    item.setType(Material.LEVER);
                    boolean bool = (Boolean) instance.getConfig().get(fKey);

                    if (!bool)
                        lore.add(Arconix.pl().getApi().format().formatText("&c" + false));
                    else
                        lore.add(Arconix.pl().getApi().format().formatText("&a" + true));

                    break;
                case "java.lang.String":
                    item.setType(Material.PAPER);
                    String str = (String) instance.getConfig().get(fKey);
                    lore.add(Arconix.pl().getApi().format().formatText("&9" + str));
                    break;
                case "java.lang.Integer":
                    item.setType(Material.WATCH);

                    int in = (Integer) instance.getConfig().get(fKey);
                    lore.add(Arconix.pl().getApi().format().formatText("&5" + in));
                    break;
                default:
                    continue;
            }
            if (defs.getConfig().contains(fKey)) {
                String text = defs.getConfig().getString(key);

                Pattern regex = Pattern.compile("(.{1,28}(?:\\s|$))|(.{0,28})", Pattern.DOTALL);
                Matcher m = regex.matcher(text);
                while (m.find()) {
                    if (m.end() != text.length() || m.group().length() != 0)
                        lore.add(Arconix.pl().getApi().format().formatText("&7" + m.group()));
                }
            }
            meta.setLore(lore);
            item.setItemMeta(meta);

            i.setItem(num, item);
            num++;
        }
        p.openInventory(i);
    }

    public void updateSettings() {
        for (settings s : settings.values()) {
            if (instance.getConfig().contains("settings." + s.oldSetting)) {
                instance.getConfig().addDefault(s.setting, instance.getConfig().get("settings." + s.oldSetting));
                instance.getConfig().set("settings." + s.oldSetting, null);
            } else if (s.setting.equals("Main.Upgrade Particle Type")) {
                if (instance.v1_7 || instance.v1_8)
                    instance.getConfig().addDefault(s.setting, "WITCH_MAGIC");
                else
                    instance.getConfig().addDefault(s.setting, s.option);
            } else
                instance.getConfig().addDefault(s.setting, s.option);
        }
        instance.getConfig().set("settings", null);
    }

    public enum settings {
        o1("Spawner-max", "Main.Spawner Max Upgrade", 5),
        o58("Name-format", "Main.Spawner Name Format", "&e{TYPE} &fSpawner [&c{AMT}x]"),
        o2("Force-Combine-Radius", "Main.Force Combine Radius", 0),
        o49("Force-Combine-Deny", "Main.Deny Place On Force Combine", false),
        o3("Search-Radius", "Main.Radius To Search Around Spawner", "8x4x8"),
        o10("Alter-Delay", "Main.Default Minecraft Spawner Cooldowns", true),
        o17("Alert-place-break", "Main.Alerts On Place And Break", true),
        o18("Sneak-for-stack", "Main.Sneak To Receive A Stacked Spawner", true),
        o32("Spawner-holograms", "Main.Spawners Have Holograms", true),
        o4("Only-drop-placed", "Main.Only Drop Placed Spawners", false),
        o5("Only-charge-natural", "Main.Only Charge Natural Spawners", false),
        o43("Random-Low & Random-High", "Main.Random Amount Added To Each Spawn", "1:3"),
        o63("Sounds", "Main.Sounds Enabled", true),
        o23("Display-Level-One", "Main.Display Level In Spawner Title If Level 1", false),
        o19("OmniSpawners", "Main.OmniSpawners Enabled", true),
        o21("Omni-Limit", "Main.Max Spawners Inside A OmniSpawner", 3),
        o215("Eggs-convert-spawners", "Main.Convert Spawners With Eggs", true),
        o42("Helpful-Tips", "Main.Display Helpful Tips For Operators", true),
        o33("Upgrade-with-eco", "Main.Upgrade With Economy", true),
        o34("Upgrade-with-xp", "Main.Upgrade With XP", true),
        o35("Upgrade-xp-cost", "Main.Cost To Upgrade With XP", 50),
        o36("Upgrade-eco-cost", "Main.Cost To Upgrade With Economy", 10000),
        o55("Use-equations", "Main.Use Custom Equations for Upgrade Costs", false),
        o62("spawners-repel-radius", "Main.Spawner Repel Liquid Radius", 1),
        o60("redstone-activate", "Main.Redstone Power Deactivates Spawners", true),
        o51("Max-Entities-Around-Single-Spawner", "Main.Max Entities Around Single Spawner", 6),
        o523("How-to", "Main.Display Help Button In Spawner Overview", true),
        o24("Inventory-Stacking", "Main.Allow Stacking Spawners In Survival Inventories", true),
        o27("Spawners-dont-explode", "Main.Prevent Spawners From Exploding", false),
        o53("Add-Spawner-To-Inventory-On-Drop", "Main.Add Spawner To Inventory On Drop", false),
        o54("Upgrade-particle-type", "Main.Upgrade Particle Type", "SPELL_WITCH"),
        o5344("ticks-until-disabled", "Main.Ticks Until AI Disabled", 20L),

        o99("-", "Main.Named Spawners Tiers", false),
        o98("-", "Main.Tier Names", Arrays.asList("&7Common", "&6Uncommon", "&4Rare", "&5Mythic")),

        o56("XP-cost-equation", "Main.Equations.Calculate XP Upgrade Cost", "{XPCost} * {Level}"),
        o57("ECO-cost-equation", "Main.Equations.Calculate Economy Upgrade Cost", "{ECOCost} * {Level}"),
        o554("Spawner-Spawn-Equation", "Main.Equations.Mobs Spawned Per Spawn", "{MULTI} + {RAND}"),
        o6("Spawner-Rate-Equation", "Main.Equations.Cooldown Between Spawns", "{DEFAULT} / {MULTI}"),


        o81("Boost-Multiplier", "Spawner Boosting.Boost Multiplier", "0.5"),
        o82("Max-Player-Boost", "Spawner Boosting.Max Multiplier For A Spawner Boost", 5),
        o83("Boost-cost", "Spawner Boosting.Item Charged For A Boost", "DIAMOND:2"),


        o30("Hostile-mobs-attack-second", "Entity.Hostile Mobs Attack Second", false),
        o50("SpawnEffect", "Entity.Spawn Particle Effect", "EXPLOSION_NORMAL"),
        o52("Large-Entity-Safe-Spawning", "Entity.Use Default Minecraft Spawn Method For Large Entities", true),


        o41("Only-drop-stacked", "Spawner Drops.Only Drop Stacked Spawners", false),
        o31("Mob-kill-counting", "Spawner Drops.Allow Killing Mobs To Drop Spawners", true),
        o40("Count-unnatural-kills", "Spawner Drops.Count Unnatural Kills Towards Spawner Drop", false),
        o623("Goal", "Spawner Drops.Kills Needed for Drop", 100),
        o7("Alert-every", "Spawner Drops.Alert Every X Before Drop", 10),
        o25("Drop-on-creeper-explosion", "Spawner Drops.Drop On Creeper Explosion", true),
        o26("Drop-on-tnt-explosion", "Spawner Drops.Drop On TNT Explosion", true),
        o28("Tnt-explosion-drop-chance", "Spawner Drops.Chance On TNT Explosion", "100%"),
        o29("Creeper-explosion-drop-chance", "Spawner Drops.Chance On Creeper Explosion", "100%"),
        o13("Silktouch-spawners", "Spawner Drops.Drop On SilkTouch", true),
        o14("Silktouch-natural-drop-chance", "Spawner Drops.Chance On Natural Silktouch", "100%"),
        o15("Silktouch-placed-drop-chance", "Spawner Drops.Chance On Placed Silktouch", "100%"),

        o8("Exit-Icon", "Interfaces.Exit Icon", "WOOD_DOOR"),
        o9("Buy-Icon", "Interfaces.Buy Icon", "EMERALD"),
        o37("ECO-Icon", "Interfaces.Economy Icon", "DOUBLE_PLANT"),
        o39("XP-Icon", "Interfaces.XP Icon", "EXP_BOTTLE"),
        o11("Glass-Type-1", "Interfaces.Glass Type 1", 7),
        o112("Glass-Type-2", "Interfaces.Glass Type 2", 11),
        o113("Glass-Type-3", "Interfaces.Glass Type 3", 3),
        o12("Rainbow-Glass", "Interfaces.Replace Glass Type 1 With Rainbow Glass", false),

        o45("Thin-Entity-data", "System.Remove Dead Entities from Data File", true),
        o48("Debug-Mode", "System.Debugger Enabled", false);

        private String setting;
        private String oldSetting;
        private Object option;

        settings(String oldSetting, String setting, Object option) {
            this.oldSetting = oldSetting;
            this.setting = setting;
            this.option = option;
        }

    }
}

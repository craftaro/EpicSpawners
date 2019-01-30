package com.songoda.epicspawners.gui;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.Locale;
import com.songoda.epicspawners.api.CostType;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.spawners.spawner.ESpawner;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.SettingsManager;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GUISpawnerOverview extends AbstractGUI {

    private static final Pattern REGEX = Pattern.compile("(.{1,28}(?:\\s|$))|(.{0,28})", Pattern.DOTALL);

    private static final ItemStack GLASS = Methods.getGlass();
    private static final ItemStack BACKGROUND_GLASS_TYPE_2 = Methods.getBackgroundGlass(true);
    private static final ItemStack BACKGROUND_GLASS_TYPE_3 = Methods.getBackgroundGlass(false);

    private final ESpawner spawner;
    private final Player player;
    private final EpicSpawnersPlugin plugin;

    private final FileConfiguration config;
    private final Locale locale;

    private int infoPage = 1;

    public GUISpawnerOverview(EpicSpawnersPlugin plugin, ESpawner spawner, Player player) {
        super(player);
        this.spawner = spawner;
        this.player = player;
        this.plugin = plugin;

        this.config = plugin.getConfig();
        this.locale = plugin.getLocale();
        init(Methods.compileName(spawner.getIdentifyingData(), spawner.getSpawnerDataCount(), false), 27);
    }

    @Override
    protected void constructGUI() {
        int showAmt = spawner.getSpawnerDataCount();
        if (showAmt > 64)
            showAmt = 1;
        else if (showAmt == 0)
            showAmt = 1;

        ItemStack item = new ItemStack(Material.PLAYER_HEAD, showAmt, (byte) 3);

        if (spawner.getSpawnerStacks().size() != 1) {
            item = plugin.getHeads().addTexture(item, plugin.getSpawnerManager().getSpawnerData("omni"));
        } else {
            Material displayItem = spawner.getFirstStack().getSpawnerData().getDisplayItem();
            if (displayItem != null && displayItem != Material.AIR) {
                item = new ItemStack(spawner.getFirstStack().getSpawnerData().getDisplayItem());
            } else {
                try {
                    item = plugin.getHeads().addTexture(item, spawner.getIdentifyingData());
                } catch (Exception e) {
                    item = new ItemStack(Material.SPAWNER, showAmt);
                }
            }
        }

        ItemMeta itemmeta = item.getItemMeta();
        itemmeta.setDisplayName(plugin.getLocale().getMessage("interface.spawner.statstitle"));
        ArrayList<String> lore = new ArrayList<>();

        if (spawner.getSpawnerStacks().size() != 1) {
            StringBuilder only = new StringBuilder("&6" + Methods.compileName(spawner.getFirstStack().getSpawnerData(), spawner.getFirstStack().getStackSize(), false));

            int num = 1;
            for (SpawnerStack stack : spawner.getSpawnerStacks()) {
                if (num != 1)
                    only.append("&8, &6").append(Methods.compileName(stack.getSpawnerData(), stack.getStackSize(), false));
                num++;
            }

            lore.add(Methods.formatText(only.toString()));
        }

        List<Material> blocks = spawner.getFirstStack().getSpawnerData().getSpawnBlocksList();

        StringBuilder only = new StringBuilder(blocks.get(0).name());

        int num = 1;
        for (Material block : blocks) {
            if (num != 1)
                only.append("&8, &6").append(Methods.getTypeFromString(block.name()));
            num++;
        }

        lore.add(plugin.getLocale().getMessage("interface.spawner.onlyspawnson", only.toString()));

        lore.add(plugin.getLocale().getMessage("interface.spawner.stats", spawner.getSpawnCount()));
        if (player.hasPermission("epicspawners.convert") && spawner.getSpawnerStacks().size() == 1) {
            lore.add("");
            lore.add(plugin.getLocale().getMessage("interface.spawner.convert"));
        }
        if (player.hasPermission("epicspawners.canboost")) {
            if (spawner.getBoost() == 0) {
                if (!player.hasPermission("epicspawners.convert") || spawner.getSpawnerStacks().size() != 1) {
                    lore.add("");
                }
                lore.add(plugin.getLocale().getMessage("interface.spawner.boost"));
            }
        }
        if (spawner.getBoost() != 0) {

            // ToDo: Make it display all boosts.
            String[] parts = plugin.getLocale().getMessage("interface.spawner.boostedstats", Integer.toString(spawner.getBoost()), spawner.getIdentifyingData().getIdentifyingName(), Methods.makeReadable(spawner.getBoostEnd().toEpochMilli() - System.currentTimeMillis())).split("\\|");
            lore.add("");
            for (String line : parts)
                lore.add(Methods.formatText(line));
        }
        itemmeta.setLore(lore);
        item.setItemMeta(itemmeta);

        int xpCost = spawner.getUpgradeCost(CostType.EXPERIENCE);

        int ecoCost = spawner.getUpgradeCost(CostType.ECONOMY);

        boolean maxed = false;
        if (spawner.getSpawnerDataCount() == plugin.getConfig().getInt("Main.Spawner Max Upgrade"))
            maxed = true;

        ItemStack itemXP = new ItemStack(Material.valueOf(plugin.getConfig().getString("Interfaces.XP Icon")), 1);
        ItemMeta itemmetaXP = itemXP.getItemMeta();
        itemmetaXP.setDisplayName(plugin.getLocale().getMessage("interface.spawner.upgradewithxp"));
        ArrayList<String> loreXP = new ArrayList<>();
        if (!maxed)
            loreXP.add(plugin.getLocale().getMessage("interface.spawner.upgradewithxplore", Integer.toString(xpCost)));
        else
            loreXP.add(plugin.getLocale().getMessage("event.upgrade.maxed"));
        itemmetaXP.setLore(loreXP);
        itemXP.setItemMeta(itemmetaXP);

        ItemStack itemECO = new ItemStack(Material.valueOf(plugin.getConfig().getString("Interfaces.Economy Icon")), 1);
        ItemMeta itemmetaECO = itemECO.getItemMeta();
        itemmetaECO.setDisplayName(plugin.getLocale().getMessage("interface.spawner.upgradewitheconomy"));
        ArrayList<String> loreECO = new ArrayList<>();
        if (!maxed)
            loreECO.add(plugin.getLocale().getMessage("interface.spawner.upgradewitheconomylore", Methods.formatEconomy(ecoCost)));
        else
            loreECO.add(plugin.getLocale().getMessage("event.upgrade.maxed"));
        itemmetaECO.setLore(loreECO);
        itemECO.setItemMeta(itemmetaECO);

        for (int i = 0; i < 27; inventory.setItem(i++, GLASS)) ;

        inventory.setItem(13, item);
        inventory.setItem(0, BACKGROUND_GLASS_TYPE_2);
        inventory.setItem(1, BACKGROUND_GLASS_TYPE_2);
        inventory.setItem(2, BACKGROUND_GLASS_TYPE_3);
        inventory.setItem(6, BACKGROUND_GLASS_TYPE_3);
        inventory.setItem(7, BACKGROUND_GLASS_TYPE_2);
        inventory.setItem(8, BACKGROUND_GLASS_TYPE_2);
        inventory.setItem(9, BACKGROUND_GLASS_TYPE_2);
        inventory.setItem(10, BACKGROUND_GLASS_TYPE_3);
        inventory.setItem(16, BACKGROUND_GLASS_TYPE_3);
        inventory.setItem(17, BACKGROUND_GLASS_TYPE_2);
        inventory.setItem(18, BACKGROUND_GLASS_TYPE_2);
        inventory.setItem(19, BACKGROUND_GLASS_TYPE_2);
        inventory.setItem(20, BACKGROUND_GLASS_TYPE_3);
        inventory.setItem(24, BACKGROUND_GLASS_TYPE_3);
        inventory.setItem(25, BACKGROUND_GLASS_TYPE_2);
        inventory.setItem(26, BACKGROUND_GLASS_TYPE_2);

        if (SettingsManager.Setting.DISPLAY_HELP_BUTTON.getBoolean()) {
            ItemStack itemO = new ItemStack(Material.PAPER, 1);
            ItemMeta itemmetaO = itemO.getItemMeta();
            itemmetaO.setDisplayName(plugin.getLocale().getMessage("interface.spawner.howtotitle"));
            ArrayList<String> loreO = new ArrayList<>();
            String text = plugin.getLocale().getMessage("interface.spawner.howtoinfo");

            int start = (14 * infoPage) - 14;
            int li = 1; // 12
            int added = 0;
            boolean max = false;

            String[] parts = text.split("\\|");
            for (String line : parts) {
                line = compileHow(player, line);
                if (line.equals(".") || line.isEmpty()) continue;

                Matcher m = REGEX.matcher(line);
                while (m.find()) {
                    if (li > start) {
                        if (li < start + 15) {
                            loreO.add(Methods.formatText("&7" + m.group()));
                            added++;
                        } else {
                            max = true;
                        }
                    }
                    li++;
                }
            }

            if (added == 0) {
                this.infoPage = 1;
                this.addInfo(inventory);
                return;
            }

            if (max) {
                loreO.add(locale.getMessage("interface.spawner.howtonext"));
            } else {
                loreO.add(locale.getMessage("interface.spawner.howtoback"));
            }

            itemmetaO.setLore(loreO);
            itemO.setItemMeta(itemmetaO);
            inventory.setItem(8, itemO);
        }
        if (spawner.getSpawnerStacks().size() == 1) {
            if (spawner.getFirstStack().getSpawnerData().isUpgradeable()) {
                if (plugin.getConfig().getBoolean("Main.Upgrade With XP"))
                    inventory.setItem(11, itemXP);
                if (plugin.getConfig().getBoolean("Main.Upgrade With Economy"))
                    inventory.setItem(15, itemECO);
            }
        }
    }

    @Override
    protected void registerClickables() {
        registerClickable(8, (player, inventory, cursor, slot, type) -> {
            this.infoPage++;
            addInfo(inventory);
        });

        registerClickable(13, (player, inventory, cursor, slot, type) -> {
            if (type.isRightClick() && spawner.getBoost() == 0 && player.hasPermission("epicspawners.canboost")) {
                new GUISpawnerBoost(plugin, spawner, player);
            } else if (type.isLeftClick() && spawner.getSpawnerStacks().size() == 1 && player.hasPermission("epicspawners.convert") ) {
                new GUISpawnerConvert(plugin, spawner, player);
            }
        });

        registerClickable(11, (player, inventory, cursor, slot, type) -> {
            if (config.getBoolean("Main.Upgrade With XP")
                    && !inventory.getItem(slot).getItemMeta().getDisplayName().equals(ChatColor.COLOR_CHAR + "l")) {
                this.spawner.upgrade(player, CostType.EXPERIENCE);
            }
            this.spawner.overview(player);
        });

        registerClickable(15, (player, inventory, cursor, slot, type) -> {
            if (config.getBoolean("Main.Upgrade With Economy")
                    && !inventory.getItem(slot).getItemMeta().getDisplayName().equals(ChatColor.COLOR_CHAR + "l")) {
                this.spawner.upgrade(player, CostType.ECONOMY);
            }
            this.spawner.overview(player);
        });
    }

    private void addInfo(Inventory inventory) {
        ItemStack itemO = new ItemStack(Material.PAPER, 1);
        ItemMeta itemmetaO = itemO.getItemMeta();
        itemmetaO.setDisplayName(locale.getMessage("interface.spawner.howtotitle"));
        List<String> loreO = new ArrayList<>();
        String text = locale.getMessage("interface.spawner.howtoinfo");

        int start = (14 * infoPage) - 14;
        int li = 1; // 12
        int added = 0;
        boolean max = false;

        String[] parts = text.split("\\|");
        for (String line : parts) {
            line = compileHow(player, line);
            if (line.equals(".") || line.isEmpty()) continue;

            Matcher m = REGEX.matcher(line);
            while (m.find()) {
                if (li > start) {
                    if (li < start + 15) {
                        loreO.add(Methods.formatText("&7" + m.group()));
                        added++;
                    } else {
                        max = true;
                    }
                }
                li++;
            }
        }

        if (added == 0) {
            this.infoPage = 1;
            this.addInfo(inventory);
            return;
        }

        if (max) {
            loreO.add(locale.getMessage("interface.spawner.howtonext"));
        } else {
            loreO.add(locale.getMessage("interface.spawner.howtoback"));
        }

        itemmetaO.setLore(loreO);
        itemO.setItemMeta(itemmetaO);
        inventory.setItem(8, itemO);
    }

    private String compileHow(Player p, String text) {
        try {
            Matcher m = Pattern.compile("\\{(.*?)}").matcher(text);
            while (m.find()) {
                Matcher mi = Pattern.compile("\\[(.*?)]").matcher(text);
                int nu = 0;
                int a = 0;
                String type = "";
                while (mi.find()) {
                    if (nu == 0) {
                        type = mi.group().replace("[", "").replace("]", "");
                        text = text.replace(mi.group(), "");
                    } else {
                        switch (type) {
                            case "LEVELUP":
                                if (nu == 1) {
                                    if (!p.hasPermission("epicspawners.combine." + spawner.getIdentifyingName()) && !p.hasPermission("epicspawners.combine." + spawner.getIdentifyingName())) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                        a++;
                                    }
                                } else if (nu == 2) {
                                    if (!config.getBoolean("Main.Upgrade With XP")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                        a++;
                                    }
                                } else if (nu == 3) {
                                    if (!config.getBoolean("Main.Upgrade With Economy")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                        a++;
                                    }
                                }
                                break;
                            case "WATER":
                                if (nu == 1) {
                                    if (!config.getBoolean("settings.Spawners-repel-liquid")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                            case "INVSTACK":
                                if (nu == 1) {
                                    if (!config.getBoolean("Main.Allow Stacking Spawners In Survival Inventories")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                            case "REDSTONE":
                                if (nu == 1) {
                                    if (!config.getBoolean("Main.Redstone Power Deactivates Spawners")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                            case "OMNI":
                                if (nu == 1) {
                                    if (!config.getBoolean("Main.OmniSpawners Enabled")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                            case "DROP":
                                if (!config.getBoolean("Spawner Drops.Allow Killing Mobs To Drop Spawners") || !p.hasPermission("epicspawners.Killcounter")) {
                                    text = "";
                                } else {
                                    text = text.replace("<TYPE>", spawner.getIdentifyingName().toLowerCase());
                                    spawner.getFirstStack().getSpawnerData().getKillGoal();
                                    if (spawner.getFirstStack().getSpawnerData().getKillGoal() != 0)
                                        text = text.replace("<AMT>", Integer.toString(spawner.getFirstStack().getSpawnerData().getKillGoal()));
                                    else
                                        text = text.replace("<AMT>", Integer.toString(config.getInt("Spawner Drops.Kills Needed for Drop")));
                                }
                                if (nu == 1) {
                                    if (config.getBoolean("Spawner Drops.Count Unnatural Kills Towards Spawner Drop")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                        }
                    }
                    nu++;
                }

            }
            text = text.replaceAll("[\\[\\]\\{\\}]", ""); // [, ], { or }
            return text;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    private String a(int a, String text) {
        try {
            if (a != 0) {
                text = ", " + text;
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return text;
    }


    @Override
    protected void registerOnCloses() {

    }
}
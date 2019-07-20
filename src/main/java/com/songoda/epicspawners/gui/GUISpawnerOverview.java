package com.songoda.epicspawners.gui;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.condition.SpawnCondition;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.utils.CostType;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.ServerVersion;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import com.songoda.epicspawners.utils.settings.Setting;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GUISpawnerOverview extends AbstractGUI {

    private static final Pattern REGEX = Pattern.compile("(.{1,28}(?:\\s|$))|(.{0,28})", Pattern.DOTALL);

    private static final ItemStack GLASS = Methods.getGlass();
    private static final ItemStack BACKGROUND_GLASS_TYPE_2 = Methods.getBackgroundGlass(true);
    private static final ItemStack BACKGROUND_GLASS_TYPE_3 = Methods.getBackgroundGlass(false);

    private final Spawner spawner;
    private final Player player;
    private final EpicSpawners plugin;

    private int infoPage = 1;

    private int task;

    public GUISpawnerOverview(EpicSpawners plugin, Spawner spawner, Player player) {
        super(player);
        this.spawner = spawner;
        this.player = player;
        this.plugin = plugin;

        init(Methods.compileName(spawner.getIdentifyingData(), spawner.getSpawnerDataCount(), false), 27);
        runTask();
    }

    @Override
    public void constructGUI() {
        resetClickables();
        registerClickables();
        int showAmt = spawner.getSpawnerDataCount();
        if (showAmt > 64)
            showAmt = 1;
        else if (showAmt == 0)
            showAmt = 1;

        ItemStack item = new ItemStack(plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), showAmt, (byte) 3);

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
                    item = new ItemStack(plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"), showAmt);
                }
            }
        }

        ItemMeta itemmeta = item.getItemMeta();
        itemmeta.setDisplayName(plugin.getLocale().getMessage("interface.spawner.statstitle").getMessage());
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

        if (blocks.isEmpty() || blocks.get(0) == null) blocks = Collections.singletonList(Material.AIR);

        StringBuilder only = new StringBuilder(blocks.get(0).name());

        int num = 1;
        for (Material block : blocks) {
            if (num != 1)
                only.append("&8, &a").append(block.name());
            num++;
        }

        String onlyStr = plugin.getLocale().getMessage("interface.spawner.onlyspawnson")
                .processPlaceholder("block", only.toString()).getMessage();

        lore.addAll(Methods.wrap("7", onlyStr));

        boolean met = true;
        for (SpawnCondition condition : spawner.getFirstStack().getSpawnerData().getConditions()) {
            if (!condition.isMet(spawner)) {
                if (met) {
                    met = false;
                    lore.add("");
                    lore.add(plugin.getLocale().getMessage("interface.spawner.paused").getMessage());
                }
                lore.addAll(Methods.wrap("7", " » " + condition.getDescription()));
            }
        }

        lore.add("");
        lore.add(plugin.getLocale().getMessage("interface.spawner.stats")
                .processPlaceholder("amount", spawner.getSpawnCount()).getMessage());
        itemmeta.setLore(lore);
        item.setItemMeta(itemmeta);

        int xpCost = spawner.getUpgradeCost(CostType.EXPERIENCE);

        int ecoCost = spawner.getUpgradeCost(CostType.ECONOMY);

        boolean maxed = false;
        if (spawner.getSpawnerDataCount() == plugin.getConfig().getInt("Main.Spawner Max Upgrade"))
            maxed = true;

        ItemStack itemXP = new ItemStack(Material.valueOf(plugin.getConfig().getString("Interfaces.XP Icon")), 1);
        ItemMeta itemmetaXP = itemXP.getItemMeta();
        itemmetaXP.setDisplayName(plugin.getLocale().getMessage("interface.spawner.upgradewithxp").getMessage());
        ArrayList<String> loreXP = new ArrayList<>();
        if (!maxed)
            loreXP.add(plugin.getLocale().getMessage("interface.spawner.upgradewithxplore")
                    .processPlaceholder("cost", Integer.toString(xpCost)).getMessage());
        else
            loreXP.add(plugin.getLocale().getMessage("event.upgrade.maxed").getMessage());
        itemmetaXP.setLore(loreXP);
        itemXP.setItemMeta(itemmetaXP);

        ItemStack itemECO = new ItemStack(Material.valueOf(plugin.getConfig().getString("Interfaces.Economy Icon")), 1);
        ItemMeta itemmetaECO = itemECO.getItemMeta();
        itemmetaECO.setDisplayName(plugin.getLocale().getMessage("interface.spawner.upgradewitheconomy").getMessage());
        ArrayList<String> loreECO = new ArrayList<>();
        if (!maxed)
            loreECO.add(plugin.getLocale().getMessage("interface.spawner.upgradewitheconomylore")
                    .processPlaceholder("cost", Methods.formatEconomy(ecoCost)).getMessage());
        else
            loreECO.add(plugin.getLocale().getMessage("event.upgrade.maxed").getMessage());
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

        if (player.hasPermission("epicspawners.convert") && spawner.getSpawnerStacks().size() == 1) {
            createButton(4, Setting.CONVERT_ICON.getMaterial(),
                    plugin.getLocale().getMessage("interface.spawner.convert").getMessage());
            registerClickable(4, (player, inventory, cursor, slot, type) ->
                    new GUISpawnerConvert(plugin, spawner, player));
        }

        if (player.hasPermission("epicspawners.canboost")) {
            lore.clear();

            if (spawner.getBoost() != 0) {

                // ToDo: Make it display all boosts.
                String[] parts = plugin.getLocale().getMessage("interface.spawner.boostedstats")
                        .processPlaceholder("amount", Integer.toString(spawner.getBoost()))
                        .processPlaceholder("type", spawner.getIdentifyingData().getIdentifyingName())
                        .processPlaceholder("time", spawner.getBoostEnd().toEpochMilli() == Long.MAX_VALUE
                                ? plugin.getLocale().getMessage("interface.spawner.boostednever")
                                : Methods.makeReadable(spawner.getBoostEnd().toEpochMilli() - System.currentTimeMillis()))
                        .getMessage().split("\\|");

                for (String line : parts)
                    lore.add(Methods.formatText(line));
            }

            createButton(22, Setting.BOOST_ICON.getMaterial(), spawner.getBoost() == 0 ? plugin.getLocale().getMessage("interface.spawner.boost").getMessage() : plugin.getLocale().getMessage("interface.spawner.cantboost").getMessage(), lore);


            if (spawner.getBoost() == 0)
                registerClickable(22, (player, inventory, cursor, slot, type) ->
                        new GUISpawnerBoost(plugin, spawner, player));
        }

        if (Setting.DISPLAY_HELP_BUTTON.getBoolean()) {
            ItemStack itemO = new ItemStack(Material.PAPER, 1);
            ItemMeta itemmetaO = itemO.getItemMeta();
            itemmetaO.setDisplayName(plugin.getLocale().getMessage("interface.spawner.howtotitle").getMessage());
            ArrayList<String> loreO = new ArrayList<>();
            String text = plugin.getLocale().getMessage("interface.spawner.howtoinfo").getMessage();

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
                loreO.add(plugin.getLocale().getMessage("interface.spawner.howtonext").getMessage());
            } else {
                loreO.add(plugin.getLocale().getMessage("interface.spawner.howtoback").getMessage());
            }

            itemmetaO.setLore(loreO);
            itemO.setItemMeta(itemmetaO);
            inventory.setItem(8, itemO);
        }
        if (spawner.getSpawnerStacks().size() == 1) {
            if (spawner.getFirstStack().getSpawnerData().isUpgradeable()) {
                if (Setting.UPGRADE_WITH_XP_ENABLED.getBoolean())
                    inventory.setItem(11, itemXP);
                if (Setting.UPGRADE_WITH_ECO_ENABLED.getBoolean())
                    inventory.setItem(15, itemECO);
            }
        }
    }

    private void runTask() {
        task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::constructGUI, 5L, 5L);
    }

    @Override
    protected void registerClickables() {
        registerClickable(8, (player, inventory, cursor, slot, type) -> {
            this.infoPage++;
            addInfo(inventory);
        });

        registerClickable(11, (player, inventory, cursor, slot, type) -> {
            if (Setting.UPGRADE_WITH_XP_ENABLED.getBoolean()
                    && !inventory.getItem(slot).getItemMeta().getDisplayName().equals(ChatColor.COLOR_CHAR + "l")) {
                this.spawner.upgrade(player, CostType.EXPERIENCE);
            }
            this.spawner.overview(player);
        });

        registerClickable(15, (player, inventory, cursor, slot, type) -> {
            if (Setting.UPGRADE_WITH_ECO_ENABLED.getBoolean()
                    && !inventory.getItem(slot).getItemMeta().getDisplayName().equals(ChatColor.COLOR_CHAR + "l")) {
                this.spawner.upgrade(player, CostType.ECONOMY);
            }
            this.spawner.overview(player);
        });
    }

    private void addInfo(Inventory inventory) {
        ItemStack itemO = new ItemStack(Material.PAPER, 1);
        ItemMeta itemmetaO = itemO.getItemMeta();
        itemmetaO.setDisplayName(plugin.getLocale().getMessage("interface.spawner.howtotitle").getMessage());
        List<String> loreO = new ArrayList<>();
        String text = plugin.getLocale().getMessage("interface.spawner.howtoinfo").getMessage();

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
            loreO.add(plugin.getLocale().getMessage("interface.spawner.howtonext").getMessage());
        } else {
            loreO.add(plugin.getLocale().getMessage("interface.spawner.howtoback").getMessage());
        }

        itemmetaO.setLore(loreO);
        itemO.setItemMeta(itemmetaO);
        inventory.setItem(8, itemO);
    }

    private String compileHow(Player p, String text) {
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
                                if (!Setting.UPGRADE_WITH_XP_ENABLED.getBoolean()) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                    a++;
                                }
                            } else if (nu == 3) {
                                if (!Setting.UPGRADE_WITH_ECO_ENABLED.getBoolean()) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                    a++;
                                }
                            }
                            break;
                        case "WATER":
                            if (nu == 1) {
                                if (!Setting.LIQUID_REPEL_RADIUS.getBoolean()) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                }
                            }
                            break;
                        case "REDSTONE":
                            if (nu == 1) {
                                if (!Setting.REDSTONE_ACTIVATE.getBoolean()) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                }
                            }
                            break;
                        case "OMNI":
                            if (nu == 1) {
                                if (!Setting.OMNI_SPAWNERS.getBoolean()) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                }
                            }
                            break;
                        case "DROP":
                            if (!Setting.MOB_KILLING_COUNT.getBoolean() || !p.hasPermission("epicspawners.Killcounter")) {
                                text = "";
                            } else {
                                text = text.replace("<TYPE>", spawner.getIdentifyingName().toLowerCase());
                                spawner.getFirstStack().getSpawnerData().getKillGoal();
                                if (spawner.getFirstStack().getSpawnerData().getKillGoal() != 0)
                                    text = text.replace("<AMT>", Integer.toString(spawner.getFirstStack().getSpawnerData().getKillGoal()));
                                else
                                    text = text.replace("<AMT>", Integer.toString(Setting.KILL_GOAL.getInt()));
                            }
                            if (nu == 1) {
                                if (Setting.COUNT_UNNATURAL_KILLS.getBoolean()) {
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
    }

    private String a(int a, String text) {
        if (a != 0) {
            text = ", " + text;
        }
        return text;
    }

    @Override
    protected void registerOnCloses() {
        registerOnClose(((player1, inventory1) -> Bukkit.getScheduler().cancelTask(task)));
    }
}
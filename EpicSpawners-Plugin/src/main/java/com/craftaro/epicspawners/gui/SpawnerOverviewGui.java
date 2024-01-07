package com.craftaro.epicspawners.gui;

import com.craftaro.core.gui.CustomizableGui;
import com.craftaro.core.hooks.EconomyManager;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.utils.NumberUtils;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.condition.SpawnCondition;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerStack;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.api.utils.CostType;
import com.craftaro.epicspawners.api.utils.HeadUtils;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.epicspawners.utils.GuiUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpawnerOverviewGui extends CustomizableGui {
    private static final Pattern REGEX = Pattern.compile("(.{1,28}(?:\\s|$))|(.{0,28})", Pattern.DOTALL);

    private final PlacedSpawner spawner;
    private final SpawnerStack stack;
    private final SpawnerData data;
    private final SpawnerTier tier;
    private final SpawnerTier nextTier;
    private final boolean onlyOneTier;
    private final Player player;
    private final EpicSpawners plugin;

    private int infoPage = 1;

    private int task;

    public SpawnerOverviewGui(EpicSpawners plugin, SpawnerStack stack, Player player) {
        super(plugin, "overview");
        setRows(3);
        this.stack = stack;
        this.spawner = stack.getSpawner();
        this.tier = stack.getCurrentTier();
        this.data = this.tier.getSpawnerData();
        this.nextTier = this.data.getNextTier(this.tier);
        this.onlyOneTier = this.tier.getSpawnerData().getTiers().size() == 1;
        this.player = player;
        this.plugin = plugin;

        setTitle(this.tier.getCompiledDisplayName(false, stack.getStackSize()));
        runTask();

        setOnClose(event -> Bukkit.getScheduler().cancelTask(this.task));

        paint();
    }

    public void paint() {
        reset();

        ItemStack glass1 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_1.getMaterial());
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial());
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial());

        setDefaultItem(glass1);

        mirrorFill("mirrorfill_1", 0, 0, true, true, glass2);
        mirrorFill("mirrorfill_2", 0, 1, true, true, glass2);
        mirrorFill("mirrorfill_3", 0, 2, true, true, glass3);
        mirrorFill("mirrorfill_4", 1, 0, false, true, glass2);
        mirrorFill("mirrorfill_5", 1, 1, false, true, glass3);

        if (this.spawner.getSpawnerStacks().size() != 1) {
            setButton("back", 0, com.craftaro.core.gui.GuiUtils.createButtonItem(Settings.EXIT_ICON.getMaterial(),
                            this.plugin.getLocale().getMessage("general.nametag.back").getMessage()),
                    (event) -> this.guiManager.showGUI(event.player, new SpawnerTiersGui(this.plugin, this.player, this.spawner)));
        }

        int showAmt = this.stack.getStackSize();
        if (showAmt > 64) {
            showAmt = 1;
        } else if (showAmt == 0) {
            showAmt = 1;
        }

        ItemStack spawnerItem;

        XMaterial displayItem = this.tier.getDisplayItem();
        if (displayItem != null && !displayItem.equals(XMaterial.AIR)) {
            spawnerItem = displayItem.parseItem();
        } else {
            try {
                spawnerItem = HeadUtils.getTexturedSkull(this.data);
            } catch (Exception e) {
                spawnerItem = XMaterial.SPAWNER.parseItem();
                spawnerItem.setAmount(showAmt);
            }
        }

        ItemMeta itemmeta = spawnerItem.getItemMeta();
        itemmeta.setDisplayName(this.plugin.getLocale().getMessage("interface.spawner.statstitle").getMessage());
        ArrayList<String> lore = new ArrayList<>();

        List<XMaterial> blocks = this.tier.getSpawnBlocksList();

        if (blocks.isEmpty() || blocks.get(0) == null) {
            blocks = Collections.singletonList(XMaterial.AIR);
        }

        StringBuilder only = new StringBuilder(blocks.get(0).name());

        int num = 1;
        for (XMaterial block : blocks) {
            if (num != 1) {
                only.append("&8, &a").append(block.name());
            }
            num++;
        }

        String onlyStr = this.plugin.getLocale().getMessage("interface.spawner.onlyspawnson")
                .processPlaceholder("block", only.toString()).getMessage();

        lore.addAll(TextUtils.wrap("7", onlyStr));

        boolean met = true;
        for (SpawnCondition condition : this.tier.getConditions()) {
            if (!condition.isMet(this.spawner)) {
                if (met) {
                    met = false;
                    lore.add("");
                    lore.add(this.plugin.getLocale().getMessage("interface.spawner.paused").getMessage());
                }
                lore.addAll(TextUtils.wrap("7", " » " + condition.getDescription()));
            }
        }

        if (this.spawner.getSpawnerStacks().size() == 1) {
            lore.add("");
            lore.add(this.plugin.getLocale().getMessage("interface.spawner.stats")
                    .processPlaceholder("amount", NumberUtils.formatNumber(this.spawner.getSpawnCount())).getMessage());
        }

        itemmeta.setLore(lore);
        spawnerItem.setItemMeta(itemmeta);

        double levelsCost = this.nextTier == null ? -1 : this.nextTier.getUpgradeCost(CostType.LEVELS);
        double economyCost = this.nextTier == null ? -1 : this.nextTier.getUpgradeCost(CostType.ECONOMY);

        ItemStack itemLevels = Settings.XP_ICON.getMaterial().parseItem();
        ItemMeta itemmetaXP = itemLevels.getItemMeta();
        itemmetaXP.setDisplayName(this.plugin.getLocale().getMessage("interface.spawner.upgradewithlevels").getMessage());
        ArrayList<String> loreXP = new ArrayList<>();
        if (this.nextTier != null) {
            loreXP.add(this.plugin.getLocale().getMessage("interface.spawner.upgradewithlevelslore")
                    .processPlaceholder("cost", Double.toString(levelsCost)).getMessage());
        } else {
            loreXP.add(this.plugin.getLocale().getMessage("event.upgrade.maxed").getMessage());
        }
        itemmetaXP.setLore(loreXP);
        itemLevels.setItemMeta(itemmetaXP);

        ItemStack itemECO = Settings.ECO_ICON.getMaterial().parseItem();
        ItemMeta itemmetaECO = itemECO.getItemMeta();
        itemmetaECO.setDisplayName(this.plugin.getLocale().getMessage("interface.spawner.upgradewitheconomy").getMessage());
        ArrayList<String> loreECO = new ArrayList<>();
        if (this.nextTier != null) {
            loreECO.add(this.plugin.getLocale().getMessage("interface.spawner.upgradewitheconomylore")
                    .processPlaceholder("cost", EconomyManager.formatEconomy(economyCost)).getMessage());
        } else {
            loreECO.add(this.plugin.getLocale().getMessage("event.upgrade.maxed").getMessage());
        }
        itemmetaECO.setLore(loreECO);
        itemECO.setItemMeta(itemmetaECO);

        setItem("spawner", 13, spawnerItem);

        if (this.player.hasPermission("epicspawners.convert") && this.data.isConvertible()) {
            setButton("convert", 4, GuiUtils.createButtonItem(Settings.CONVERT_ICON.getMaterial(),
                            this.plugin.getLocale().getMessage("interface.spawner.convert").getMessage()),
                    (event) -> this.guiManager.showGUI(this.player, new SpawnerConvertGui(this.plugin, this.stack, this.player)));
        }

        if (this.spawner.getSpawnerStacks().size() == 1) {
            GuiUtils.applyBoosted(22, this, this.plugin, this.player, this.spawner);
        }

        if (Settings.DISPLAY_HELP_BUTTON.getBoolean()) {
            ItemStack itemO = new ItemStack(Material.PAPER, 1);
            ItemMeta itemmetaO = itemO.getItemMeta();
            itemmetaO.setDisplayName(this.plugin.getLocale().getMessage("interface.spawner.tutorialtitle").getMessage());
            ArrayList<String> loreO = new ArrayList<>();
            String text = this.plugin.getLocale().getMessage("interface.spawner.tutorial").getMessage();

            int start = (14 * this.infoPage) - 14;
            int li = 1; // 12
            int added = 0;
            boolean max = false;

            String[] parts = text.split("\\|");
            for (String line : parts) {
                line = compileHow(this.player, line);
                if (line.equals(".") || line.isEmpty()) {
                    continue;
                }

                Matcher m = REGEX.matcher(line);
                while (m.find()) {
                    if (li > start) {
                        if (li < start + 15) {
                            loreO.add(TextUtils.formatText("&7" + m.group()));
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
                this.addInfo();
                return;
            }

            if (max) {
                loreO.add(this.plugin.getLocale().getMessage("interface.spawner.howtonext").getMessage());
            } else {
                loreO.add(this.plugin.getLocale().getMessage("interface.spawner.howtoback").getMessage());
            }

            itemmetaO.setLore(loreO);
            itemO.setItemMeta(itemmetaO);
            setButton("info", 8, itemO,
                    event -> {
                        this.infoPage++;
                        addInfo();
                    });
        }
        if (!this.onlyOneTier) {
            if (Settings.UPGRADE_WITH_LEVELS_ENABLED.getBoolean()) {
                setButton("levels", 11, itemLevels, event -> {
                    this.stack.upgrade(this.player, CostType.LEVELS);
                    this.spawner.overview(this.player);
                });
            }
            if (Settings.UPGRADE_WITH_ECONOMY_ENABLED.getBoolean()) {
                setButton("economy", 15, itemECO, event -> {
                    this.stack.upgrade(this.player, CostType.ECONOMY);
                    this.spawner.overview(this.player);
                });
            }
        }
    }

    private void runTask() {
        this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
            if (this.inventory != null && !this.inventory.getViewers().isEmpty()) {
                paint();
            }
        }, 5L, 5L);
    }

    private void addInfo() {
        ItemStack itemO = new ItemStack(Material.PAPER, 1);
        ItemMeta itemmetaO = itemO.getItemMeta();
        itemmetaO.setDisplayName(this.plugin.getLocale().getMessage("interface.spawner.tutorialtitle").getMessage());
        List<String> loreO = new ArrayList<>();
        String text = this.plugin.getLocale().getMessage("interface.spawner.tutorial").getMessage();

        int start = (14 * this.infoPage) - 14;
        int li = 1; // 12
        int added = 0;
        boolean max = false;

        String[] parts = text.split("\\|");
        for (String line : parts) {
            line = compileHow(this.player, line);
            if (line.equals(".") || line.isEmpty()) {
                continue;
            }

            Matcher m = REGEX.matcher(line);
            while (m.find()) {
                if (li > start) {
                    if (li < start + 15) {
                        loreO.add(TextUtils.formatText("&7" + m.group()));
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
            this.addInfo();
            return;
        }

        if (max) {
            loreO.add(this.plugin.getLocale().getMessage("interface.spawner.howtonext").getMessage());
        } else {
            loreO.add(this.plugin.getLocale().getMessage("interface.spawner.howtoback").getMessage());
        }

        itemmetaO.setLore(loreO);
        itemO.setItemMeta(itemmetaO);
        setItem("info", 8, itemO);
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
                                if (!p.hasPermission("epicspawners.combine." + this.data.getIdentifyingName()) && !p.hasPermission("epicspawners.combine." + this.data.getIdentifyingName())) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                    a++;
                                }
                            } else if (nu == 2) {
                                if (!Settings.UPGRADE_WITH_LEVELS_ENABLED.getBoolean()) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                    a++;
                                }
                            } else if (nu == 3) {
                                if (!Settings.UPGRADE_WITH_ECONOMY_ENABLED.getBoolean()) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                    a++;
                                }
                            }
                            break;
                        case "WATER":
                            if (nu == 1) {
                                if (!Settings.LIQUID_REPEL_RADIUS.getBoolean()) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                }
                            }
                            break;
                        case "REDSTONE":
                            if (nu == 1) {
                                if (!Settings.REDSTONE_ACTIVATE.getBoolean()) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                }
                            }
                            break;
                        case "OMNI":
                            if (nu == 1) {
                                if (!Settings.OMNI_SPAWNERS.getBoolean()) {
                                    text = text.replace(mi.group(), "");
                                } else {
                                    text = text.replace(mi.group(), a(a, mi.group()));
                                }
                            }
                            break;
                        case "DROP":
                            if (!Settings.MOB_KILLING_COUNT.getBoolean() || !p.hasPermission("epicspawners.Killcounter")) {
                                text = "";
                            } else {
                                text = text.replace("<TYPE>", this.tier.getDisplayName().toLowerCase());
                                this.stack.getSpawnerData().getKillDropGoal();
                                if (this.stack.getSpawnerData().getKillDropGoal() != 0) {
                                    text = text.replace("<AMT>", Integer.toString(this.stack.getSpawnerData().getKillDropGoal()));
                                } else {
                                    text = text.replace("<AMT>", Integer.toString(Settings.KILL_DROP_GOAL.getInt()));
                                }
                            }
                            if (nu == 1) {
                                if (Settings.COUNT_UNNATURAL_KILLS.getBoolean()) {
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
        text = text.replaceAll("[\\[\\]{}]", "");
        return text;
    }

    private String a(int a, String text) {
        return (a != 0 ? ", " : "") + text;
    }
}

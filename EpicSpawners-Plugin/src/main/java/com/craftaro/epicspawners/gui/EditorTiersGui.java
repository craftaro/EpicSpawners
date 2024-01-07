package com.craftaro.epicspawners.gui;

import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.input.ChatPrompt;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerStack;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.epicspawners.spawners.spawner.SpawnerDataImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EditorTiersGui extends Gui {
    private final EpicSpawners plugin;
    private final Player player;
    private final SpawnerData spawnerData;

    public EditorTiersGui(EpicSpawners plugin, Player player, SpawnerData spawnerData) {
        super(6);
        this.plugin = plugin;
        this.player = player;
        this.spawnerData = spawnerData;

        setTitle(spawnerData.getIdentifyingName());
        setOnClose(event -> plugin.getSpawnerManager().saveSpawnerDataToFile());
        setDefaultItem(null);

        showPage();
    }

    public void showPage() {
        reset();

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(XMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        // edges will be type 3
        mirrorFill(0, 2, true, true, glass3);
        mirrorFill(1, 1, true, true, glass3);

        // decorate corners with type 2
        mirrorFill(0, 0, true, true, glass2);
        mirrorFill(1, 0, true, true, glass2);
        mirrorFill(0, 1, true, true, glass2);

        setButton(8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR, "Back to Selector"),
                (event) -> this.plugin.getGuiManager().showGUI(this.player, new EditorSelectorGui(this.plugin, this.player)));

        List<SpawnerTier> tiersSource = this.spawnerData.getTiers();

        this.pages = (int) Math.max(1, Math.ceil(tiersSource.size() / ((double) 28)));

        // enable page event
        setNextPage(5, 7, GuiUtils.createButtonItem(XMaterial.ARROW, this.plugin.getLocale().getMessage("general.nametag.next").getMessage()));
        setPrevPage(5, 1, GuiUtils.createButtonItem(XMaterial.ARROW, this.plugin.getLocale().getMessage("general.nametag.back").getMessage()));
        setOnPage((event) -> showPage());

        List<SpawnerTier> tiers = tiersSource.stream().skip((this.page - 1) * 28).limit(28).collect(Collectors.toList());

        int num = 10;
        for (int i = 0; i < 28; i++) {
            num++;
            SpawnerTier tier = i < tiers.size() ? tiers.get(i) : null;
            if (num == 16 || num == 36) {
                num = num + 2;
            }

            if (tier == null) {
                setItem(num, null);
                continue;
            }

            if (this.acceptsItems) {
                setItem(num, GuiUtils.createButtonItem(tier.getDisplayItem() == XMaterial.AIR ? XMaterial.DIRT : tier.getDisplayItem(),
                        tier.getIdentifyingName()));
            } else {

                List<String> lore = new ArrayList<>();
                lore.add(TextUtils.formatText("&6&l" + tier.getDisplayName() + " &7(" + tier.getIdentifyingName() + ")"));
                lore.add(TextUtils.formatText("&7Left Click to &a&lEdit&7."));
                boolean canDelete = this.spawnerData.getTiers().size() != 1 || this.spawnerData.isCustom();
                if (canDelete) {
                    lore.add(TextUtils.formatText("&7Right Click to &c&lDestroy&7."));
                }


                setButton(num, GuiUtils.createButtonItem(tier.getDisplayItem() == XMaterial.AIR ? XMaterial.DIRT : tier.getDisplayItem(),
                                lore),
                        (event) -> {
                            if (event.clickType == ClickType.LEFT) {
                                this.guiManager.showGUI(this.player, new EditorOverviewGui(this.plugin, this.player, tier));
                            } else if (canDelete) {
                                this.player.sendMessage("Type \"yes\" to confirm this action.");
                                ChatPrompt.showPrompt(this.plugin, this.player, evnt -> {
                                    if (evnt.getMessage().equalsIgnoreCase("yes")) {
                                        this.player.sendMessage(TextUtils.formatText("&6" + tier.getIdentifyingName() + " &7 has been destroyed successfully"));
                                        this.spawnerData.removeTier(tier);
                                        this.plugin.getLootablesManager().getLootManager().removeLootable(tier.getFullyIdentifyingName());
                                    }
                                    this.plugin.getGuiManager().showGUI(this.player, new EditorTiersGui(this.plugin, this.player, this.spawnerData));
                                });
                            }
                        });
            }
        }

        setButton(5, 3, GuiUtils.createButtonItem(XMaterial.FIRE_CHARGE, TextUtils.formatText("&9&lEdit Settings")),
                (event) -> this.guiManager.showGUI(this.player, new EditorTierGeneralGui(this.plugin, this, this.spawnerData)));

        if (this.pages == 1) {
            setButton(5, 4, GuiUtils.createButtonItem(XMaterial.CHEST, TextUtils.formatText("&a&lUnlock Tiers",
                            "&7Currently: " + (this.acceptsItems ? "&aUnlocked" : "&cLocked") + "&7.",
                            "&7Re-lock to save changed.")),
                    (event) -> {
                        if (this.acceptsItems) {
                            Map<String, SpawnerTier> newTiers = new LinkedHashMap<>();

                            int slot = 10;
                            for (int i = 0; i < 28; i++) {
                                slot++;
                                if (slot == 16 || slot == 36) {
                                    slot = slot + 2;
                                }

                                int finalSlot = slot;
                                SpawnerTier tier = getItem(slot) != null ?
                                        tiers.stream().filter(t -> t.getIdentifyingName().equals(getItem(finalSlot).getItemMeta().getDisplayName())).findFirst().orElseGet(null) : null;

                                if (tier != null) {
                                    newTiers.put("Tier_" + (newTiers.size() + 1), tier);
                                }
                            }

                            for (PlacedSpawner spawner : this.plugin.getSpawnerManager().getSpawners()) {
                                boolean modified = false;
                                for (SpawnerStack stack : spawner.getSpawnerStacks()) {
                                    if (stack.getSpawnerData() != this.spawnerData) {
                                        continue;
                                    }

                                    modified = true;

                                    stack.setTier(newTiers.get(stack.getCurrentTier().getIdentifyingName()));
                                }
                                if (modified) {
                                    this.plugin.updateHologram(spawner);
                                    this.plugin.getDataManager().save(spawner);
                                }
                            }

                            for (Map.Entry<String, SpawnerTier> entry : newTiers.entrySet()) {
                                entry.getValue().setIdentifyingName(entry.getKey());
                            }

                            if (newTiers.size() == tiers.size()) {
                                this.spawnerData.replaceTiers(newTiers.values());
                            }

                            this.unlockedCells.clear();
                        } else {
                            setUnlockedRange(11, 38);
                            this.unlockedCells.remove(16);
                            this.unlockedCells.remove(17);
                            this.unlockedCells.remove(26);
                            this.unlockedCells.remove(27);
                        }
                        setAcceptsItems(!this.acceptsItems);
                        showPage();
                    });
        } else {
            setItem(5, 4, null);
        }

        setButton(5, 5, GuiUtils.createButtonItem(XMaterial.PAPER, TextUtils.formatText("&9&lNew Tier")),
                (event) -> {
                    this.spawnerData.addDefaultTier();
                    showPage();
                });
    }

    public static void openTiers(EpicSpawners plugin, Player player, SpawnerData spawnerData) {
        openTiers(plugin, player, spawnerData, false);
    }

    public static void openTiers(EpicSpawners plugin, Player player, SpawnerData spawnerData, boolean forced) {
        if (spawnerData == null) {
            String type;
            for (int i = 1; true; i++) {
                String temp = "Custom " + i;
                if (!plugin.getSpawnerManager().isSpawnerData(temp)) {
                    type = temp;
                    break;
                }
            }

            spawnerData = plugin.getSpawnerManager().addSpawnerData(type, new SpawnerDataImpl(type));
            spawnerData.addDefaultTier();
            spawnerData.setCustom(true);
        }

        int tierCount = spawnerData.getTiers().size();
        if (tierCount == 0) {
            spawnerData.addDefaultTier();
        }

        if (tierCount == 1 && !forced) {
            plugin.getGuiManager().showGUI(player, new EditorOverviewGui(plugin, player, spawnerData.getFirstTier()));
        } else {
            plugin.getGuiManager().showGUI(player, new EditorTiersGui(plugin, player, spawnerData));
        }

    }

    public static void openTiersInReverse(EpicSpawners plugin, Player player, SpawnerTier tier) {
        SpawnerData spawnerData = tier.getSpawnerData();
        int tierCount = spawnerData.getTiers().size();
        if (tierCount == 0) {
            spawnerData.addDefaultTier();
        }

        if (tierCount == 1) {
            plugin.getGuiManager().showGUI(player, new EditorSelectorGui(plugin, player));
        } else {
            plugin.getGuiManager().showGUI(player, new EditorTiersGui(plugin, player, spawnerData));
        }
    }
}

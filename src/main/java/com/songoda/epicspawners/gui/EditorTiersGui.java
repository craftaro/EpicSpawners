package com.songoda.epicspawners.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.input.ChatPrompt;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.spawner.PlacedSpawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.spawners.spawner.SpawnerTier;
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
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(CompatibleMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(CompatibleMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        // edges will be type 3
        mirrorFill(0, 2, true, true, glass3);
        mirrorFill(1, 1, true, true, glass3);

        // decorate corners with type 2
        mirrorFill(0, 0, true, true, glass2);
        mirrorFill(1, 0, true, true, glass2);
        mirrorFill(0, 1, true, true, glass2);

        setButton(8, GuiUtils.createButtonItem(CompatibleMaterial.OAK_DOOR, "Back to Selector"),
                (event) -> plugin.getGuiManager().showGUI(player, new EditorSelectorGui(plugin, player)));

        List<SpawnerTier> tiersSource = spawnerData.getTiers();

        pages = (int) Math.max(1, Math.ceil(tiersSource.size() / ((double) 28)));

        // enable page event
        setNextPage(5, 7, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("general.nametag.next").getMessage()));
        setPrevPage(5, 1, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("general.nametag.back").getMessage()));
        setOnPage((event) -> showPage());

        List<SpawnerTier> tiers = tiersSource.stream().skip((page - 1) * 28).limit(28).collect(Collectors.toList());

        int num = 10;
        for (int i = 0; i < 28; i++) {
            num++;
            SpawnerTier tier = i < tiers.size() ? tiers.get(i) : null;
            if (num == 16 || num == 36)
                num = num + 2;

            if (tier == null) {
                setItem(num, null);
                continue;
            }

            if (acceptsItems) {
                setItem(num, GuiUtils.createButtonItem(tier.getDisplayItem() == CompatibleMaterial.AIR ? CompatibleMaterial.DIRT : tier.getDisplayItem(),
                        tier.getIdentifyingName()));
            } else {

                List<String> lore = new ArrayList<>();
                lore.add(TextUtils.formatText("&6&l" + tier.getDisplayName() + " &7(" + tier.getIdentifyingName() + ")"));
                lore.add(TextUtils.formatText("&7Left Click to &a&lEdit&7."));
                boolean canDelete = spawnerData.getTiers().size() != 1 || spawnerData.isCustom();
                if (canDelete)
                    lore.add(TextUtils.formatText("&7Right Click to &c&lDestroy&7."));


                setButton(num, GuiUtils.createButtonItem(tier.getDisplayItem() == CompatibleMaterial.AIR ? CompatibleMaterial.DIRT : tier.getDisplayItem(),
                        lore),
                        (event) -> {
                            if (event.clickType == ClickType.LEFT)
                                guiManager.showGUI(player, new EditorOverviewGui(plugin, player, tier));
                            else if (canDelete) {
                                player.sendMessage("Type \"yes\" to confirm this action.");
                                ChatPrompt.showPrompt(plugin, player, evnt -> {
                                    if (evnt.getMessage().equalsIgnoreCase("yes")) {
                                        player.sendMessage(TextUtils.formatText("&6" + tier.getIdentifyingName() + " &7 has been destroyed successfully"));
                                        spawnerData.removeTier(tier);
                                        plugin.getLootablesManager().getLootManager().removeLootable(tier.getFullyIdentifyingName());
                                    }
                                    plugin.getGuiManager().showGUI(player, new EditorTiersGui(plugin, player, spawnerData));
                                });
                            }
                        });
            }
        }

        setButton(5, 3, GuiUtils.createButtonItem(CompatibleMaterial.FIRE_CHARGE, TextUtils.formatText("&9&lEdit Settings")),
                (event) -> guiManager.showGUI(player, new EditorTierGeneralGui(plugin, this, spawnerData)));

        if (pages == 1)
            setButton(5, 4, GuiUtils.createButtonItem(CompatibleMaterial.CHEST, TextUtils.formatText("&a&lUnlock Tiers",
                    "&7Currently: " + (acceptsItems ? "&aUnlocked" : "&cLocked") + "&7.",
                    "&7Re-lock to save changed.")),
                    (event) -> {
                        if (acceptsItems) {
                            Map<String, SpawnerTier> newTiers = new LinkedHashMap<>();

                            int slot = 10;
                            for (int i = 0; i < 28; i++) {
                                slot++;
                                if (slot == 16 || slot == 36)
                                    slot = slot + 2;

                                int finalSlot = slot;
                                SpawnerTier tier = getItem(slot) != null ?
                                        tiers.stream().filter(t -> t.getIdentifyingName().equals(getItem(finalSlot).getItemMeta().getDisplayName())).findFirst().orElseGet(null) : null;

                                if (tier != null)
                                    newTiers.put("Tier_" + (newTiers.size() + 1), tier);
                            }

                            for (PlacedSpawner spawner : plugin.getSpawnerManager().getSpawners()) {
                                boolean modified = false;
                                for (SpawnerStack stack : spawner.getSpawnerStacks()) {
                                    if (stack.getSpawnerData() != spawnerData)
                                        continue;

                                    modified = true;

                                    stack.setTier(newTiers.get(stack.getCurrentTier().getIdentifyingName()));
                                }
                                if (modified) {
                                    plugin.updateHologram(spawner);
                                    plugin.getDataManager().updateSpawner(spawner);
                                }
                            }

                            for (Map.Entry<String, SpawnerTier> entry : newTiers.entrySet())
                                entry.getValue().setIdentifyingName(entry.getKey());

                            if (newTiers.size() == tiers.size())
                                spawnerData.replaceTiers(newTiers.values());

                            unlockedCells.clear();
                        } else {
                            setUnlockedRange(11, 38);
                            unlockedCells.remove(16);
                            unlockedCells.remove(17);
                            unlockedCells.remove(26);
                            unlockedCells.remove(27);
                        }
                        setAcceptsItems(!acceptsItems);
                        showPage();
                    });
        else setItem(5, 4, null);

        setButton(5, 5, GuiUtils.createButtonItem(CompatibleMaterial.PAPER, TextUtils.formatText("&9&lNew Tier")),
                (event) -> {
                    spawnerData.addDefaultTier();
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

            spawnerData = plugin.getSpawnerManager().addSpawnerData(type, new SpawnerData(type));
            spawnerData.addDefaultTier();
            spawnerData.setCustom(true);
        }

        int tierCount = spawnerData.getTiers().size();
        if (tierCount == 0)
            spawnerData.addDefaultTier();

        if (tierCount == 1 && !forced)
            plugin.getGuiManager().showGUI(player, new EditorOverviewGui(plugin, player, spawnerData.getFirstTier()));
        else
            plugin.getGuiManager().showGUI(player, new EditorTiersGui(plugin, player, spawnerData));

    }

    public static void openTiersInReverse(EpicSpawners plugin, Player player, SpawnerTier tier) {
        SpawnerData spawnerData = tier.getSpawnerData();
        int tierCount = spawnerData.getTiers().size();
        if (tierCount == 0)
            spawnerData.addDefaultTier();

        if (tierCount == 1)
            plugin.getGuiManager().showGUI(player, new EditorSelectorGui(plugin, player));
        else
            plugin.getGuiManager().showGUI(player, new EditorTiersGui(plugin, player, spawnerData));

    }

}

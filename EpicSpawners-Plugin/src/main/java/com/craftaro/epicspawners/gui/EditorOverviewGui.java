package com.craftaro.epicspawners.gui;

import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.gui.AnvilGui;
import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.input.ChatPrompt;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.epicspawners.spawners.spawner.SpawnerDataImpl;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.api.utils.HeadType;
import com.craftaro.epicspawners.api.utils.HeadUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class EditorOverviewGui extends Gui {

    private final EpicSpawners plugin;
    private final Player player;
    private final SpawnerTier spawnerTier;
    private final SpawnerData spawnerData;

    public EditorOverviewGui(EpicSpawners plugin, Player player, SpawnerTier spawnerTier) {
        super(6);
        this.plugin = plugin;
        this.player = player;
        this.spawnerTier = spawnerTier;
        this.spawnerData = spawnerTier.getSpawnerData();

        setDefaultItem(GuiUtils.getBorderItem(Settings.GLASS_TYPE_1.getMaterial().parseItem()));

        setTitle(spawnerTier.getGuiTitle());

        paint();
        setOnClose(event -> plugin.getSpawnerManager().saveSpawnerDataToFile());
    }

    public void paint() {
        reset();

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(XMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        setItem(0, glass3);
        setItem(1, glass3);
        setItem(2, glass3);
        setItem(3, glass2);
        setItem(4, glass2);

        setItem(9, glass3);
        setItem(13, glass2);
        setItem(14, glass3);
        setItem(15, glass2);
        setItem(16, glass2);
        setItem(17, glass2);

        setItem(18, glass3);
        setItem(22, glass3);
        setItem(26, glass2);

        setItem(27, glass2);
        setItem(31, glass3);
        setItem(35, glass3);

        setItem(36, glass2);
        setItem(37, glass2);
        setItem(38, glass3);
        setItem(39, glass2);
        setItem(40, glass2);
        setItem(44, glass3);

        setItem(49, glass2);
        setItem(50, glass2);
        setItem(51, glass3);
        setItem(52, glass3);
        setItem(53, glass3);

        setButton(8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR,
                plugin.getLocale().getMessage("general.nametag.back").getMessage()),
                (event) -> EditorTiersGui.openTiersInReverse(plugin, player, spawnerTier));

        ItemStack item = HeadUtils.getTexturedSkull(spawnerTier);
        if (spawnerTier.getDisplayItem() != null && !spawnerTier.getDisplayItem().equals(XMaterial.AIR))
            item = spawnerTier.getDisplayItem().parseItem();

        ItemMeta itemmeta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(TextUtils.formatText("&7Left-Click to &9Change Tier Name&7."));
        lore.add(TextUtils.formatText("&7Right-Click to &bChange Tier Display Item&7."));
        lore.add(TextUtils.formatText("&6-----------------------------"));

        lore.add(TextUtils.formatText("&6Display Name: &7" + spawnerTier.getDisplayName() + "&7."));
        if (spawnerTier.getDisplayItem() != null) {
            lore.add(TextUtils.formatText("&6Display Item: &7" + spawnerTier.getDisplayItem().name() + "&7."));
        } else {
            if (!spawnerData.isCustom()) {
                lore.add(TextUtils.formatText("&6Display Item: &7Unavailable&7."));
            } else {
                lore.add(TextUtils.formatText("&6Display Item: &7Dirt&7."));
            }
        }
        lore.add(TextUtils.formatText("&6Config Name: &7" + spawnerTier.getIdentifyingName() + "&7."));
        itemmeta.setLore(lore);
        itemmeta.setDisplayName(spawnerTier.getCompiledDisplayName());
        item.setItemMeta(itemmeta);
        setButton(11, item, event -> {
            if (event.clickType == ClickType.RIGHT) {
                spawnerTier.setDisplayItem(XMaterial.matchXMaterial(player.getInventory().getItemInHand()));
                plugin.getLocale().newMessage("&7Display Item for &6" + spawnerTier.getIdentifyingName() + " &7set to &6" + player.getInventory().getItemInHand().getType().toString() + "&7.")
                        .sendPrefixedMessage(player);
                paint();
            } else if (event.clickType == ClickType.LEFT) {
                AnvilGui gui = new AnvilGui(player, this);
                gui.setTitle("Enter a display name.");
                gui.setAction(evnt -> {
                    spawnerTier.setDisplayName(gui.getInputText().trim());
                    player.closeInventory();
                }).setOnClose(e -> setTitle(spawnerTier.getGuiTitle()));

                plugin.getGuiManager().showGUI(player, gui);
            }
        });

        if (spawnerData.getTiers().size() != 1 || spawnerData.isCustom())
            setButton(29, GuiUtils.createButtonItem(XMaterial.TNT, TextUtils.formatText("&7Click to: &cDestroy This Tier")),
                    (event) -> {
                        player.sendMessage("Type \"yes\" to confirm this action.");
                        ChatPrompt.showPrompt(plugin, player, evnt -> {
                            if (evnt.getMessage().equalsIgnoreCase("yes")) {
                                player.sendMessage(TextUtils.formatText("&6" + spawnerTier.getIdentifyingName() + " Spawner &7 has been destroyed successfully"));
                                spawnerData.removeTier(spawnerTier);
                                if (spawnerData.getTiers().isEmpty())
                                    plugin.getSpawnerManager().removeSpawnerData(spawnerData.getIdentifyingName());
                                plugin.getLootablesManager().getLootManager().removeLootable(spawnerTier.getFullyIdentifyingName());
                            }
                        }).setOnClose(() -> {
                            if (plugin.getSpawnerManager().isSpawnerData(spawnerData.getIdentifyingName()))
                                plugin.getGuiManager().showGUI(player, new EditorTiersGui(plugin, player, spawnerData));
                            else
                                plugin.getGuiManager().showGUI(player, new EditorSelectorGui(plugin, player));
                        }).setTimeOut(player, 20L * 15L);
                    });

        setButton(23, GuiUtils.createButtonItem(XMaterial.LEVER, TextUtils.formatText("&9&lGeneral Settings")),
                event -> guiManager.showGUI(player, new EditorGeneralGui(plugin, this, spawnerTier)));

        setButton(24, GuiUtils.createButtonItem(XMaterial.BONE, TextUtils.formatText("&e&lDrop Settings")),
                event -> plugin.getGuiManager().showGUI(player, new EditorDropsGui(plugin, spawnerTier, this)));

        setButton(25, GuiUtils.createButtonItem(HeadUtils.getTexturedSkull(HeadType.OMNI), TextUtils.formatText("&a&lEntity Settings")),
                event -> guiManager.showGUI(player, new EditorEditGui(plugin, this, spawnerTier, EditorEditGui.EditType.ENTITY)));

        setButton(41, GuiUtils.createButtonItem(XMaterial.CHEST, TextUtils.formatText("&5&lItem Settings")),
                event -> guiManager.showGUI(player, new EditorEditGui(plugin, this, spawnerTier, EditorEditGui.EditType.ITEM)));

        setButton(32, GuiUtils.createButtonItem(XMaterial.GOLD_BLOCK, TextUtils.formatText("&c&lBlock Settings")),
                event -> guiManager.showGUI(player, new EditorEditGui(plugin, this, spawnerTier, EditorEditGui.EditType.BLOCK)));

        setButton(34, GuiUtils.createButtonItem(XMaterial.FIREWORK_ROCKET, TextUtils.formatText("&b&lParticle Settings")),
                event -> guiManager.showGUI(player, new EditorParticleGui(plugin, this, spawnerTier)));

        setButton(43, GuiUtils.createButtonItem(XMaterial.PAPER, TextUtils.formatText("&6&lCommand Settings")),
                event -> guiManager.showGUI(player, new EditorEditGui(plugin, this, spawnerTier, EditorEditGui.EditType.COMMAND)));

        if (spawnerData.getTiers().size() == 1)
            setButton(5, 0, GuiUtils.createButtonItem(XMaterial.FIRE_CHARGE, TextUtils.formatText("&6Go to tiered view.")),
                    event -> EditorTiersGui.openTiers(plugin, player, spawnerData, true));
    }
}

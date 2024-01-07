package com.craftaro.epicspawners.gui;

import com.craftaro.core.gui.AnvilGui;
import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.utils.NumberUtils;
import com.craftaro.core.utils.PlayerUtils;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.settings.Settings;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EditorTierGeneralGui extends Gui {
    private final EpicSpawners plugin;
    private final Gui back;
    private final SpawnerData spawnerData;

    public EditorTierGeneralGui(EpicSpawners plugin, Gui back, SpawnerData spawnerData) {
        super(3);
        this.plugin = plugin;
        this.back = back;
        this.spawnerData = spawnerData;

        setDefaultItem(GuiUtils.getBorderItem(Settings.GLASS_TYPE_1.getMaterial().parseItem()));
        setTitle(spawnerData.getIdentifyingName());
        setOnClose(event -> plugin.getSpawnerManager().saveSpawnerDataToFile());

        paint();
    }

    public void paint() {
        reset();

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(XMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        mirrorFill(0, 0, true, true, glass2);
        mirrorFill(0, 1, true, true, glass2);
        mirrorFill(0, 2, true, true, glass3);
        mirrorFill(1, 0, false, true, glass2);
        mirrorFill(1, 1, false, true, glass3);

        setButton(0, GuiUtils.createButtonItem(XMaterial.OAK_DOOR,
                        this.plugin.getLocale().getMessage("general.nametag.back").getMessage()),
                (event) -> this.guiManager.showGUI(event.player, this.back));

        setButton(10, GuiUtils.createButtonItem(XMaterial.SUNFLOWER, TextUtils.formatText("&6&lIn Shop",
                        "&7Currently: &a" + this.spawnerData.isInShop(),
                        "&7If this is true this spawner",
                        "&7will show up in the shop GUI.")),
                event -> {
                    this.spawnerData.setInShop(!this.spawnerData.isInShop());
                    paint();
                });

        setButton(16, GuiUtils.createButtonItem(XMaterial.FIRE_CHARGE, TextUtils.formatText("&a&lShop Price",
                        "&7Currently: &a" + this.spawnerData.getShopPrice(),
                        "&7This is the shop cost")),
                event -> {
                    Player player = event.player;
                    AnvilGui gui = new AnvilGui(player, this);
                    gui.setTitle("Goal: Ex. 19.99");
                    gui.setAction(evnt -> {
                        String msg = gui.getInputText().trim();
                        if (NumberUtils.isNumeric(msg)) {
                            this.spawnerData.setShopPrice(Double.parseDouble(msg));
                            player.closeInventory();
                        } else {
                            player.sendMessage(TextUtils.formatText("&CYou must enter a number."));
                        }
                    }).setOnClose(e -> paint());
                    this.guiManager.showGUI(player, gui);

                    PlayerUtils.sendMessages(player, TextUtils.formatText("&7Enter a custom shop cost for " + this.spawnerData.getIdentifyingName() + "&7.",
                            "&7Use &60 &7to use the default cost.",
                            "&7Example: &619.99&7."));
                });

        setButton(14, GuiUtils.createButtonItem(XMaterial.FIRE_CHARGE, TextUtils.formatText("&c&lCustom Kill Goal",
                        "&7Currently: &a" + this.spawnerData.getShopPrice(),
                        "&7This is the amount of kills",
                        "of this tiers ")),
                event -> {
                    Player player = event.player;
                    AnvilGui gui = new AnvilGui(player, this);
                    gui.setTitle("Goal: Ex. 19.99");
                    gui.setAction(evnt -> {
                        String msg = gui.getInputText().trim();
                        if (NumberUtils.isNumeric(msg)) {
                            this.spawnerData.setShopPrice(Double.parseDouble(msg));
                            player.closeInventory();
                        } else {
                            player.sendMessage(TextUtils.formatText("&CYou must enter a number."));
                        }
                    }).setOnClose(e -> paint());
                    this.guiManager.showGUI(player, gui);

                    PlayerUtils.sendMessages(player, TextUtils.formatText("&7Enter a custom shop cost for " + this.spawnerData.getIdentifyingName() + "&7.",
                            "&7Use &60 &7to use the default cost.",
                            "&7Example: &619.99&7."));
                });
    }
}

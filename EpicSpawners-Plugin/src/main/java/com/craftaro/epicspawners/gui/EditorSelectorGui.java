package com.craftaro.epicspawners.gui;

import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.api.utils.HeadUtils;
import com.craftaro.epicspawners.settings.Settings;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EditorSelectorGui extends Gui {
    private final EpicSpawners plugin;
    private final Player player;
    private Type shownType = Type.BOTH;
    private final List<SpawnerData> entities = new ArrayList<>();

    public EditorSelectorGui(EpicSpawners plugin, Player player) {
        super(6);
        this.plugin = plugin;
        this.player = player;

        this.entities.addAll(plugin.getSpawnerManager().getAllEnabledSpawnerData());
        setTitle("Spawner Selector");

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

        this.pages = (int) Math.max(1, Math.ceil(this.entities.size() / ((double) 28)));

        // enable page event
        setNextPage(5, 7, GuiUtils.createButtonItem(XMaterial.ARROW, this.plugin.getLocale().getMessage("general.nametag.next").getMessage()));
        setPrevPage(5, 1, GuiUtils.createButtonItem(XMaterial.ARROW, this.plugin.getLocale().getMessage("general.nametag.back").getMessage()));
        setOnPage((event) -> showPage());

        List<SpawnerData> data = this.entities.stream()
                .filter(s -> this.shownType == Type.BOTH
                        || this.shownType == Type.DEFAULT && !s.isCustom()
                        || this.shownType == Type.CUSTOM && s.isCustom()).skip((this.page - 1) * 28).limit(28).collect(Collectors.toList());

        setButton(8, GuiUtils.createButtonItem(XMaterial.OAK_DOOR,
                this.plugin.getLocale().getMessage("general.nametag.exit").getMessage()), (event) -> close());

        int num = 10;
        for (int i = 0; i < 28; i++) {
            num++;
            SpawnerData spawnerData = i < data.size() ? data.get(i) : null;
            if (num == 16 || num == 36)
                num = num + 2;

            if (spawnerData == null) {
                setItem(num, null);
                continue;
            }
            XMaterial mat = spawnerData.getDisplayItem();
            setButton(num, GuiUtils.createButtonItem(mat != null && !mat.equals(XMaterial.AIR) ? spawnerData.getDisplayItem().parseItem() : HeadUtils.getTexturedSkull(spawnerData),
                            TextUtils.formatText("&6&l" + spawnerData.getFirstTier().getDisplayName()), TextUtils.formatText("&7Click to &a&lEdit&7.")),
                    (event) -> EditorTiersGui.openTiers(this.plugin, this.player, spawnerData));
        }

        setButton(5, 5, GuiUtils.createButtonItem(XMaterial.COMPASS, TextUtils.formatText("&5&lShow: &7" + this.shownType.name())),
                (event) -> {
                    this.shownType = this.shownType.next();
                    showPage();
                });
        setButton(5, 6, GuiUtils.createButtonItem(XMaterial.PAPER, TextUtils.formatText("&9&lNew Spawner")),
                (event) -> EditorTiersGui.openTiers(this.plugin, this.player, null));
    }

    private enum Type {
        BOTH, CUSTOM, DEFAULT;

        public Type next() {
            return values()[(this.ordinal() != values().length - 1 ? this.ordinal() + 1 : 0)];
        }
    }
}

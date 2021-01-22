package com.songoda.epicspawners.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.CustomizableGui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.utils.HeadUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SpawnerConvertGui extends CustomizableGui {

    private final EpicSpawners plugin;
    private final SpawnerStack stack;
    private final Player player;
    private final List<SpawnerData> entities = new ArrayList<>();

    public SpawnerConvertGui(EpicSpawners plugin, SpawnerStack stack, Player player) {
        super(plugin, "convert");
        setRows(6);
        this.plugin = plugin;
        this.stack = stack;
        this.player = player;

        for (SpawnerData spawnerData : plugin.getSpawnerManager().getAllSpawnerData()) {
            if (!spawnerData.isConvertible()
                    || !spawnerData.isActive()
                    || !player.hasPermission("epicspawners.convert." + spawnerData.getIdentifyingName().replace(" ", "_")))
                continue;
            entities.add(spawnerData);
        }

        setTitle(plugin.getLocale().getMessage("interface.convert.title").getMessage());
        showPage();
    }

    public void showPage() {
        reset();

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(CompatibleMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(CompatibleMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        // edges will be type 3
        mirrorFill("mirrorfill_1", 0, 2, true, true, glass3);
        mirrorFill("mirrorfill_2", 1, 1, true, true, glass3);

        // decorate corners with type 2
        mirrorFill("mirrorfill_3", 0, 0, true, true, glass2);
        mirrorFill("mirrorfill_4", 1, 0, true, true, glass2);
        mirrorFill("mirrorfill_5",0, 1, true, true, glass2);

        pages = (int) Math.max(1, Math.ceil(entities.size() / ((double) 28)));

        // enable page event
        setNextPage(5, 7, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("general.nametag.next").getMessage()));
        setPrevPage(5, 1, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("general.nametag.back").getMessage()));
        setOnPage((event) -> showPage());

        // Sort entities by their shopOrder val
        entities.sort(Comparator.comparingInt(SpawnerData::getShopOrder));

        List<SpawnerData> data = entities.stream().skip((page - 1) * 28).limit(28).collect(Collectors.toList());

        int num = 11;
        for (SpawnerData spawnerData : data) {
            if (num == 16 || num == 36)
                num = num + 2;

            ItemStack item = HeadUtils.getTexturedSkull(spawnerData);

            if (spawnerData.getDisplayItem() != null) {
                CompatibleMaterial mat = spawnerData.getDisplayItem();
                if (!mat.isAir())
                    item = mat.getItem();
            }

            ItemMeta itemmeta = item.getItemMeta();
            String name = spawnerData.getFirstTier().getCompiledDisplayName();
            ArrayList<String> lore = new ArrayList<>();
            double price = spawnerData.getConvertPrice();

            lore.add(plugin.getLocale().getMessage("interface.shop.buyprice").processPlaceholder("cost", EconomyManager.formatEconomy(price)).getMessage());
            String loreString = plugin.getLocale().getMessage("interface.convert.lore").getMessage();
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                loreString = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, loreString.replace(" ", "_")).replace("_", " ");
            }
            lore.add(loreString);
            itemmeta.setLore(lore);
            itemmeta.setDisplayName(name);
            item.setItemMeta(itemmeta);
            setButton(num, item, event ->
                    stack.convert(spawnerData, player, false));
            num++;
        }

        setButton("back", 8, GuiUtils.createButtonItem(Settings.EXIT_ICON.getMaterial(),
                plugin.getLocale().getMessage("general.nametag.back").getMessage()),
                event -> guiManager.showGUI(player, new SpawnerOverviewGui(plugin, stack, player)));
    }
}

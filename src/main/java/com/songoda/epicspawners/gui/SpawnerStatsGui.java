package com.songoda.epicspawners.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.utils.HeadUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SpawnerStatsGui extends Gui {

    private final EpicSpawners plugin;
    private final Player player;
    int size = 0;

    public SpawnerStatsGui(EpicSpawners plugin, Player player) {
        super(6);
        this.plugin = plugin;
        this.player = player;

        for (Map.Entry<EntityType, Integer> entry : plugin.getPlayerDataManager().getPlayerData(player).getEntityKills().entrySet())
            if (plugin.getSpawnerManager().getSpawnerData(entry.getKey()).isActive())
                size++;

        this.setTitle(plugin.getLocale().getMessage("interface.spawnerstats.title").getMessage());

        showPage();
    }

    public void showPage() {
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

        // enable page event
        setNextPage(4, 7, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("general.nametag.next").getMessage()));
        setPrevPage(4, 1, GuiUtils.createButtonItem(CompatibleMaterial.ARROW, plugin.getLocale().getMessage("general.nametag.back").getMessage()));
        setOnPage((event) -> showPage());

        setButton(8, GuiUtils.createButtonItem(CompatibleMaterial.valueOf(plugin.getConfig().getString("Interfaces.Exit Icon")),
                plugin.getLocale().getMessage("general.nametag.exit").getMessage()), (event) -> player.closeInventory());

        this.pages = (int) Math.floor(size / 28.0);

        Set<Map.Entry<EntityType, Integer>> entries = plugin.getPlayerDataManager().getPlayerData(player).getEntityKills().entrySet();

        entries = entries.stream().skip((page - 1) * 28).limit(28).collect(Collectors.toSet());

        int num = 10;
        for (Map.Entry<EntityType, Integer> entry : entries) {
            if (num == 16 || num == 36)
                num = num + 2;
            int goal = Settings.KILL_GOAL.getInt();
            SpawnerData spawnerData = plugin.getSpawnerManager().getSpawnerData(entry.getKey());

            int customGoal = spawnerData.getKillGoal();
            if (customGoal != 0) goal = customGoal;

            setItem(num, GuiUtils.createButtonItem(HeadUtils.getTexturedSkull(spawnerData),
                    TextUtils.formatText("&6" + spawnerData.getIdentifyingName() + "&7: &e" + entry.getValue() + "&7/&e" + goal)));
            num++;
        }
    }
}

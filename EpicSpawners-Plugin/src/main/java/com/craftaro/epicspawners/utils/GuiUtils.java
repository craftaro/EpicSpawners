package com.craftaro.epicspawners.utils;

import com.craftaro.core.gui.CustomizableGui;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.core.utils.TimeUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.boosts.types.Boosted;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.gui.SpawnerBoostGui;
import com.craftaro.epicspawners.settings.Settings;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GuiUtils extends com.craftaro.core.gui.GuiUtils {
    public static void applyBoosted(int slot, CustomizableGui gui, EpicSpawners plugin, Player player, PlacedSpawner spawner) {
        if (!player.hasPermission("epicspawners.canboost")) {
            return;
        }

        List<String> lore = new ArrayList<>();

        List<Boosted> boosts = spawner.getBoosts();
        int boostTotal = boosts.stream().mapToInt(Boosted::getAmountBoosted).sum();
        long boostEnd = 0;
        for (Boosted boost : boosts) {
            if (boost.getEndTime() > boostEnd) {
                boostEnd = boost.getEndTime();
            }
        }

        if (!boosts.isEmpty()) {
            // ToDo: Make it display all boosts.
            String[] parts = plugin.getLocale().getMessage("interface.spawner.boostedstats")
                    .processPlaceholder("amount", Integer.toString(boostTotal))
                    .processPlaceholder("time", boostEnd == Long.MAX_VALUE
                            ? plugin.getLocale().getMessage("interface.spawner.boostednever")
                            : TimeUtils.makeReadable(boostEnd - System.currentTimeMillis()))
                    .getMessage().split("\\|");

            lore.addAll(TextUtils.formatText(parts));

            gui.setItem("boost", slot, createButtonItem(Settings.BOOST_ICON.getMaterial(), lore));
        } else {
            gui.setButton("boost", slot, createButtonItem(Settings.BOOST_ICON.getMaterial(),
                    spawner.getBoosts().stream().mapToInt(Boosted::getAmountBoosted).sum() == 0
                            ? plugin.getLocale().getMessage("interface.spawner.boost").getMessage()
                            : plugin.getLocale().getMessage("interface.spawner.cantboost").getMessage(), lore), event ->
                    plugin.getGuiManager().showGUI(player, new SpawnerBoostGui(plugin, spawner, player)));
        }
    }
}

package com.songoda.epicspawners.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Map;

public class GUISpawnerStats extends AbstractGUI {

    private final EpicSpawners plugin;

    public GUISpawnerStats(EpicSpawners plugin, Player player) {
        super(player);
        this.plugin = plugin;

        int size = 0;

        for (Map.Entry<EntityType, Integer> entry : plugin.getPlayerActionManager().getPlayerAction(player).getEntityKills().entrySet()) {
            if (plugin.getSpawnerManager().getSpawnerData(entry.getKey()).isActive())
                size++;
        }

        int slots = 54;
        if (size <= 9) {
            slots = 18;
        } else if (size <= 18) {
            slots = 27;
        } else if (size <= 27) {
            slots = 36;
        } else if (size <= 36) {
            slots = 45;
        }

        init(plugin.getLocale().getMessage("interface.spawnerstats.title").getMessage(), slots);
    }

    @Override
    public void constructGUI() {

        short place = 0;
        for (Map.Entry<EntityType, Integer> entry : plugin.getPlayerActionManager().getPlayerAction(player).getEntityKills().entrySet()) {
            int goal = plugin.getConfig().getInt("Spawner Drops.Kills Needed for Drop");

            SpawnerData spawnerData = plugin.getSpawnerManager().getSpawnerData(entry.getKey());

            int customGoal = spawnerData.getKillGoal();
            if (customGoal != 0) goal = customGoal;

            ItemStack it = CompatibleMaterial.PLAYER_HEAD.getItem();

            ItemStack item = plugin.getHeads().addTexture(it, spawnerData);

            ItemMeta itemmeta = item.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            itemmeta.setLore(lore);
            itemmeta.setDisplayName(Methods.formatText("&6" + spawnerData.getDisplayName() + "&7: &e" + entry.getValue() + "&7/&e" + goal));
            item.setItemMeta(itemmeta);
            inventory.setItem(place, item);

            place++;
        }
    }

    @Override
    protected void registerClickables() {
    }

    @Override
    protected void registerOnCloses() {

    }
}

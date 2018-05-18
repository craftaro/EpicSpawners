package com.songoda.epicspawners.utils;

import java.util.HashMap;
import java.util.Map;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.SpawnerData;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by songoda on 3/19/2017.
 */
public class Heads {

    private final EpicSpawnersPlugin instance;
    private final Map<String, String> textureURL = new HashMap<>();

    public Heads(EpicSpawnersPlugin instance) {
        this.instance = instance;

        try {
            for (HeadType type : HeadType.values()) {
                this.textureURL.put(type.name(), type.getUrl());
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public ItemStack addTexture(ItemStack item, SpawnerData spawnerData) {
        try {
            String headURL = textureURL.get(spawnerData.getIdentifyingName().toUpperCase().replace(" ", "_"));
            if (headURL == null) {
                headURL = textureURL.get("DROPPED_ITEM");
            }

            if (instance.isServerVersion(ServerVersion.V1_7)) {
                return (item = new ItemStack(Material.MOB_SPAWNER, 1));
            }

            return Arconix.pl().getApi().getGUI().addTexture(item, headURL);
        } catch (Exception e) {
            Debugger.runReport(e);
        }

        return item;
    }
}
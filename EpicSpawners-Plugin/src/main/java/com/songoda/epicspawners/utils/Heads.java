package com.songoda.epicspawners.utils;

import com.songoda.epicspawners.api.spawner.SpawnerData;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by songoda on 3/19/2017.
 */
public class Heads {

    private final Map<HeadType, String> textureURL = new EnumMap<>(HeadType.class);

    public Heads() {
        try {
            for (HeadType type : HeadType.values()) {
                this.textureURL.put(type, type.getUrl());
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public ItemStack addTexture(ItemStack item, SpawnerData spawnerData) {
        try {
            String headURL = textureURL.get(getHeadTypeOrDefault(spawnerData.getIdentifyingName().toUpperCase().replace(" ", "_")));

            return Methods.addTexture(item, headURL);
        } catch (Exception e) {
            Debugger.runReport(e);
        }

        return item;
    }

    private HeadType getHeadTypeOrDefault(String name) {
        try {
            return HeadType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return HeadType.DROPPED_ITEM;
        }
    }
}
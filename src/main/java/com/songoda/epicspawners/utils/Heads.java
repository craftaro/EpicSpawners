package com.songoda.epicspawners.utils;

import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by songoda on 3/19/2017.
 */
public class Heads {

    private final Map<HeadType, String> textureURL = new EnumMap<>(HeadType.class);

    public Heads() {
            for (HeadType type : HeadType.values()) {
                this.textureURL.put(type, type.getUrl());
            }
    }

    public ItemStack addTexture(ItemStack item, SpawnerData spawnerData) {
            String headURL = textureURL.get(getHeadTypeOrDefault(spawnerData.getIdentifyingName().toUpperCase().replace(" ", "_")));

            return Methods.addTexture(item, headURL);
    }

    private HeadType getHeadTypeOrDefault(String name) {
        try {
            return HeadType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return HeadType.DROPPED_ITEM;
        }
    }
}
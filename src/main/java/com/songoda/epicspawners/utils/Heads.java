package com.songoda.epicspawners.utils;

import com.songoda.core.compatibility.CompatibleMaterial;
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

    public ItemStack addTexture(ItemStack item, String name) {
        String headURL = textureURL.get(getHeadTypeOrDefault(name.toUpperCase().replace(" ", "_")));

        return Methods.addTexture(item, headURL);
    }

    public ItemStack addTexture(ItemStack item, SpawnerData spawnerData) {
        return addTexture(item, spawnerData.getIdentifyingName());
    }

    private HeadType getHeadTypeOrDefault(String name) {
        try {
            return HeadType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return HeadType.DROPPED_ITEM;
        }
    }

    public ItemStack getTexturedSkull(SpawnerData spawnerData) {
        return addTexture(CompatibleMaterial.PLAYER_HEAD.getItem(), spawnerData);
    }
}
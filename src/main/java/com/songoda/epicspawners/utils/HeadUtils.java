package com.songoda.epicspawners.utils;

import com.songoda.core.utils.ItemUtils;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerTier;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by songoda on 3/19/2017.
 */
public class HeadUtils {

    private static final Map<HeadType, String> textureURL = new EnumMap<>(HeadType.class);

    static {
        for (HeadType type : HeadType.values()) {
            textureURL.put(type, type.getUrl());
        }
    }

    private static HeadType getHeadTypeOrDefault(String name) {
        try {
            return HeadType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return HeadType.DROPPED_ITEM;
        }
    }

    public static ItemStack getTexturedSkull(SpawnerTier spawnerTier) {
        return getTexturedSkull(spawnerTier.getSpawnerData());
    }

    public static ItemStack getTexturedSkull(SpawnerData spawnerData) {
        return ItemUtils.getCustomHead(textureURL.get(getHeadTypeOrDefault(spawnerData.getIdentifyingName().toUpperCase().replace(" ", "_"))));
    }

    public static ItemStack getTexturedSkull(HeadType headType) {
        return ItemUtils.getCustomHead(headType.getUrl());
    }
}
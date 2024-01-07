package com.craftaro.epicspawners.api.utils;

import com.craftaro.third_party.com.cryptomorin.xseries.SkullUtils;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.EnumMap;
import java.util.Map;

public class HeadUtils {
    private static final Map<HeadType, String> TEXTURE_HASHES = new EnumMap<>(HeadType.class);

    static {
        for (HeadType type : HeadType.values()) {
            TEXTURE_HASHES.put(type, type.getUrlHash());
        }
    }

    public static ItemStack getTexturedSkull(SpawnerTier spawnerTier) {
        return getTexturedSkull(spawnerTier.getSpawnerData());
    }

    public static ItemStack getTexturedSkull(SpawnerData spawnerData) {
        HeadType headType = getHeadTypeOrDefault(spawnerData.getIdentifyingName().toUpperCase().replace(" ", "_"));
        return createSkullForSkinHash(TEXTURE_HASHES.get(headType));
    }

    public static ItemStack getTexturedSkull(HeadType headType) {
        return createSkullForSkinHash(headType.getUrlHash());
    }

    private static ItemStack createSkullForSkinHash(String textureHash) {
        ItemStack head = XMaterial.PLAYER_HEAD.parseItem();

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        SkullUtils.applySkin(meta, textureHash);
        head.setItemMeta(meta);

        return head;
    }

    private static HeadType getHeadTypeOrDefault(String name) {
        try {
            return HeadType.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return HeadType.DROPPED_ITEM;
        }
    }
}

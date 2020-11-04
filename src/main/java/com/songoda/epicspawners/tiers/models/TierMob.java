package com.songoda.epicspawners.tiers.models;

import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * This class is for handling
 * the live mobs with custom drops
 *
 * Made by CodePunisher with <3
 */
public class TierMob
{
    private final TierType tierType;                         // Tier type object associated with mob
    private final int spawnerLevel;                          // Spawner level

    /** Setting instance variables */
    public TierMob(TierType tierType, int spawnerLevel) {
        this.tierType = tierType;
        this.spawnerLevel = spawnerLevel;
    }

    /** Getters */
    public TierType getTierType() { return this.tierType; }
    public int getSpawnerLevel() { return this.spawnerLevel; }
    public List<ItemStack> getItems() { return this.tierType.getItemsFromLevel(getSpawnerLevel()); }
}
package com.songoda.epicspawners.tiers.models;

import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * This class object handles TierData
 *
 * Each TierType has a list of TierData objects
 * TierData has a number value (for the spawner level)
 * with a list of item stacks to drop (Yea the request on this is FUCKING INSANE)
 */
public class TierData
{
    private final int level;                             // Spawner level required
    private List<ItemStack> items;                       // Items for this particular level

    // Constructor for setting up values
    public TierData(int level, List<ItemStack> items) {
        this.level = level;
        this.items = items;
    }

    // My beautiful getters (don't talk shit about them, they have feelings)
    public int getLevel() { return this.level; }
    public List<ItemStack> getItems() { return this.items; }
    public void setItems(List<ItemStack> items) { this.items = items; }
}

package com.songoda.epicspawners.tiers.models;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class stores the tier type objects
 *
 * FYI: These objects are created on enable
 * from the YML file via the StorageManager class
 *
 * Class made by CodePunisher with <3
 */
public class TierType
{
    private final String entityType;                                               // Entity type of the tier type
    private String type;                                                           // The type (Random or All)
    private final List<TierData> tierData;                                         // Tier data list

    /** Creating the tier type object */
    public TierType(String entityType, String type, HashMap<Integer, List<ItemStack>> data) {
        // Just in case the string is incorrectly inputted from the user
        if (!type.equalsIgnoreCase("random") && !type.equalsIgnoreCase("all"))
            type = "all";

        // If the user doesn't add the under scores (doing it for them)
        if (entityType.contains(" "))
            entityType = entityType.replaceAll(" ", "_");

        this.entityType = entityType;
        this.type = type.toUpperCase();
        this.tierData = new ArrayList<>();

        // Setting up the tier data (using hash map because it makes the most sense for this)
        if (!data.isEmpty()) {
            for (Map.Entry<Integer, List<ItemStack>> entry : data.entrySet()) {
                this.tierData.add(new TierData(entry.getKey(), entry.getValue()));
            }
        }
    }

    /** BEAUTIFUL GETTERS */
    public String getEntityType() { return this.entityType; }
    public List<TierData> getTierData() { return this.tierData; }

    public String getType() { return this.type; }
    public void setType(String type) { this.type = type; }

    // Getting item stacks to drop based on integer
    public List<ItemStack> getItemsFromLevel(int level) {
        List<ItemStack> stack = null;

        for (TierData tierData : getTierData()) {
            if (level >= tierData.getLevel())
                stack = tierData.getItems();
        }

        return stack;
    }

    // This returns the entity type name
    // but it removes underscores and caps
    public String getPrettyName() {
        String prettyName = getEntityType();
        prettyName = prettyName.substring(0, 1).toUpperCase() + prettyName.substring(1).toLowerCase();

        if (prettyName.contains("_"))
            prettyName = prettyName.replaceAll("_", " ");

        return prettyName;
    }

    // Determines if tier data type contains a level
    public boolean tierDataHasValue(int level) {
        for (TierData tierData : getTierData()) {
            if (tierData.getLevel() == level)
                return true;
        }

        return false;
    }
}

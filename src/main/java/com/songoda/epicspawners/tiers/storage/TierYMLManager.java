package com.songoda.epicspawners.tiers.storage;

import com.songoda.core.configuration.Config;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.tiers.models.TierData;
import com.songoda.epicspawners.tiers.models.TierType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * This class handles YML storage system
 *
 * Class made by CodePunisher with <3
 */
public class TierYMLManager
{
    /** Instance of main class */
    private final EpicSpawners plugin = EpicSpawners.getInstance();

    private boolean enabled;                                         // If feature is enabled or not
    private boolean globalEnabled;                                   // If global feature is enabled or not
    private final Config tierConfig;                                 // Config to store the tiers in
    private final String path;                                       // Path for the yml file

    // Setting up default values
    public TierYMLManager() {
        this.tierConfig = new Config(this.plugin, "tiers.yml");
        this.path = "Tiers.";

        updateTierConfig(false);
    }

    /** Aren't these getters beautiful? Ikr */
    public Config getTierConfig() { return this.tierConfig; }
    public String getPath() { return this.path; }

    public boolean isEnabled() { return this.enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isGlobalEnabled() { return this.globalEnabled; }
    public void setGlobalEnabled(boolean globalEnabled) { this.globalEnabled = globalEnabled; }

    /**
     * Updating tier config
     * Storing tier type objects
     *
     * @param isBeingReloaded Clearing tier type objects
     */
    public void updateTierConfig(boolean isBeingReloaded) {
        getTierConfig().load();
        boolean brandNewFile = !getTierConfig().isSet("Tiers");

        // If the yml is being generated (only running once)
        if (brandNewFile) {
            // Default config shit
            getTierConfig().set("enabled", false);
            getTierConfig().set("Global.enabled", false);
            getTierConfig().set("Global.type", "ALL");
        }

        // Clearing map (then it gets updated)
        if (isBeingReloaded)
            plugin.getTierDataManager().clearTierTypes();

        // Saving file
        getTierConfig().options().copyDefaults(true);
        getTierConfig().save();

        // Updating booleans
        setEnabled(getTierConfig().getBoolean("enabled"));
        boolean globalSetting = getTierConfig().isSet("Global") && getTierConfig().getBoolean("Global." + "enabled");
        setGlobalEnabled(globalSetting);
        createTierTypes();

        if (!isEnabled() && isBeingReloaded) {
            //plugin.getTierSQLManager().removeTableValues();
        }
    }

    /**
     * This method creates the tier type
     * objects and stores them based on
     * the YML file
     *
     * Being ran async because it will
     * need to be ran on reloads as well
     */
    public void createTierTypes() {
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, ()-> {
            if (getTierConfig().isSet("Tiers")) {
                ConfigurationSection section = getTierConfig().getConfigurationSection("Tiers");

                if (section != null) {
                    for (String entity : section.getKeys(false)) {
                        createTierType(entity);
                    }
                }
            }

            if (getTierConfig().isSet("Global"))
                createTierType("Global");
        }, 5L);
    }

    /**
     * Method adds tier type to the yml file
     * Running async obviously because I'm not a monster
     *
     * @param entity entity to specify
     */
    public void addTypeToFile(String entity, TierType tierType) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, ()-> {
            // If the setting is global
            boolean isGlobalSetting = entity.equalsIgnoreCase("global");

            // Setting global boolean option (if the entity is global)
            if (isGlobalSetting)
                getTierConfig().set(path + entity + ".enabled", true);

            // This is because the global setting is stand alone (not under the "Tiers")
            String path = isGlobalSetting ? "Global" : getPath() + entity;

            // Setting type
            getTierConfig().set(path + ".type", tierType.getType());

            if (!tierType.getTierData().isEmpty()) {
                for (TierData tierData : tierType.getTierData()) {
                    int level = tierData.getLevel();
                    getTierConfig().set(path + ".levels." + level, tierData.getItems());
                }
            }

            getTierConfig().save();
        });
    }

    /**
     * Removes tier type from file
     * Running async
     *
     * @param type tier type to remove
     */
    public void removeTypeFromFile(String type) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, ()-> {
            String removeType = getConfigPath(type);
            if (removeType != null) {
                getTierConfig().set(getPath() + removeType, null);
                getTierConfig().save();
            }
        });
    }

    /**
     * Updating tier type in file (if it exists)
     *
     * @param tierType tier type to update
     */
    public void updateTierTypeInFile(TierType tierType) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, ()-> {
            String updateType = getConfigPath(tierType.getEntityType());

            if (updateType != null) {
                // Config object
                Config config = getTierConfig();
                boolean isGlobal = updateType.equalsIgnoreCase("global");

                // Incase the string is global (there is no path for global)
                String path = isGlobal ? updateType : getPath() + updateType;

                // Clearing levels path (so that it can get updated)
                if (config.isSet(path + ".levels"))
                    config.set(path + ".levels", null);

                // Updating config
                config.set(path + ".type", tierType.getType());
                for (TierData tierData : tierType.getTierData())
                    config.set(path + ".levels." + tierData.getLevel(), tierData.getItems());

                // Updating boolean (if it is global enabled)
                if (isGlobal)
                    config.set(path + ".enabled", isGlobalEnabled());

                // Saving config
                config.save();
            }
        });
    }

    /**
     * This creates a single tier type
     * and stores it from the file
     *
     * Made this for the tiers + global section
     * Plus it's easier to read
     */
    @SuppressWarnings("unchecked")
    private void createTierType(String string) {
        // Incase the string is global (there is no path for global)
        String path = string.equals("Global") ? string : getPath() + string;

        // The hash map for the entity  type
        HashMap<Integer, List<ItemStack>> tierMap = new HashMap<>();

        // Type (Random or All)
        String type = getTierConfig().getString(path + ".type");

        // Looping through levels (to add to map)
        if (getTierConfig().isSet(path + ".levels")) {
            for (String level : Objects.requireNonNull(getTierConfig().getConfigurationSection(path + ".levels")).getKeys(false)) {
                ConfigurationSection configSection = getTierConfig().getConfigurationSection(path + ".levels");

                if (configSection != null) {
                    List<ItemStack> items = (List<ItemStack>) configSection.getList(level, new ArrayList<>());
                    int requiredLevel = Integer.parseInt(level);
                    tierMap.put(requiredLevel, items);
                }
            }
        }

        // Storing object
        if (type != null) {
            String entityType = string.toUpperCase();
            TierType tierType = new TierType(entityType, type.toUpperCase(), tierMap);
            plugin.getTierDataManager().addTierType(entityType, tierType);
        }
    }

    /**
     * This method exists so that
     * I can add/remove config files based
     * on a string input
     *
     * The config doesn't default to checking
     * for equalsignorecase, so I kind of
     * gotta do it myself
     *
     * @param string tier type entity type input
     */
    public String getConfigPath(String string) {
        if (string.equalsIgnoreCase("global")) {
            if (getTierConfig().isSet("Global"))
                return "Global";
        }

        for (String mob : Objects.requireNonNull(getTierConfig().getConfigurationSection("Tiers")).getKeys(false)) {
            if (mob.equalsIgnoreCase(string))
                return mob;
        }

        return null;
    }
}

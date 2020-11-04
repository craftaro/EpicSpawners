package com.songoda.epicspawners.tiers.storage;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.tiers.models.TierMob;
import com.songoda.epicspawners.tiers.models.TierType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * This class stores the data that will only
 * exist throughout the life of the plugin
 *
 * I also through in a multi page handler
 * here, because I didn't want to make
 * a class for just it
 *
 * Also, I have a clear task built into
 * this (this is to avoid memory leaks)
 *
 * Class made by CodePunisher with <3
 */
public class TierDataManager
{
    // Instance of main class
    private final EpicSpawners plugin = EpicSpawners.getInstance();

    /** Stores all tier types */
    private final LinkedHashMap<String, TierType> tierTypes = new LinkedHashMap<>();

    /** Temporary mob storage for the custom drops */
    private final HashMap<UUID, TierMob> tierMobs = new HashMap<>();

    /** Methods for adding shit to maps */
    public void addTierType(String type, TierType tierType) { tierTypes.put(type, tierType); }
    public void addTierMob(UUID uuid, TierMob tierMob, boolean addToSQL) {
        tierMobs.put(uuid, tierMob);

        if (addToSQL)
            plugin.getTierSQLManager().addTierMob(uuid, tierMob);
    }

    /** Methods for removing shit from maps */
    public void removeTierType(String type) { tierTypes.remove(type); }
    public void removeTierMob(UUID uuid) { tierMobs.remove(uuid); }

    /** Methods for getting shit from maps */
    public TierType getTierType(String type) { return tierTypes.get(type); }
    public TierMob getTierMob(UUID uuid) { return tierMobs.get(uuid); }

    /** Methods for clearing maps */
    public void clearTierTypes() { this.tierTypes.clear(); }
    public void clearTierMobs() { this.tierMobs.clear(); }

    /**
     * @param type the entity type to check
     * @return if the map contains the entity key
     */
    public boolean containsEntity(String type) { return tierTypes.containsKey(type); }

    /** Returning tier types as a list */
    public List<TierType> getTierTypes() {
        List<TierType> list = new ArrayList<>(tierTypes.values());
        TierType tierType = getTierType("GLOBAL");
        list.remove(tierType);
        return list;
    }

    /** Returns the literal hash map */
    public HashMap<UUID, TierMob> getTierMobs() { return this.tierMobs; }

    /**
     * This method determines what
     * list to return based on the page value
     *
     * This is an easy way for me to
     * manage a multipage system (if it comes to that)
     *
     * @return list of (Whatever the fuck you want)
     */
    public <T> List<T> listFromPageCount(int page, int maxItems, List<T> referenceList) {
        int secondValue = page * maxItems;
        int firstValue = secondValue - maxItems;
        return referenceList.isEmpty() ? referenceList : referenceList.subList(firstValue, Math.min(referenceList.size(), secondValue));
    }

    /**
     * This clear task is essentially just to avoid
     * memory leaks, it is running async and it
     * should have virtually 0 performance cost
     *
     * An example of a memory leak is if clear lag
     * removes stored mobs (just one example)
     *
     * RUNS ONCE ON ENABLE
     */
    public void clearTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getTierYMLManager().isEnabled()) { // Only running code if feature is enabled
                    // Only running if the map itself isn't empty
                    if (!tierMobs.isEmpty()) {
                        // Objects to clear from map
                        List<UUID> uuidsToRemove = new ArrayList<>();

                        for (UUID uuid : tierMobs.keySet()) {
                            // So apparently this method isn't a thing
                            // in 1.8? I couldn't find a replacement
                            // This clear task is pretty damn important
                            Entity entity = Bukkit.getEntity(uuid);

                            if (entity == null || !entity.isValid() || entity.isDead())
                                uuidsToRemove.add(uuid);
                        }

                        for (UUID uuid : uuidsToRemove)
                            tierMobs.remove(uuid);
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1200L);
    }
}

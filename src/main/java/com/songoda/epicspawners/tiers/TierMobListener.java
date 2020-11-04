package com.songoda.epicspawners.tiers;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.api.events.SpawnerSpawnEvent;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.tiers.models.TierMob;
import com.songoda.epicspawners.tiers.models.TierType;
import com.songoda.epicspawners.tiers.storage.TierDataManager;
import com.songoda.epicspawners.tiers.storage.TierSQLManager;
import com.songoda.epicspawners.tiers.storage.TierYMLManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.UUID;

/**
 * Class made by CodePunisher with <3
 */
public class TierMobListener implements Listener
{
    /** Isntance of tier data manager class */
    private final TierDataManager tierDataManager = EpicSpawners.getInstance().getTierDataManager();

    /** Instance of tier storage manager class */
    private final TierYMLManager tierYMLManager = EpicSpawners.getInstance().getTierYMLManager();

    /** Instance of SQL storage manager class */
    private final TierSQLManager tierSQLManager = EpicSpawners.getInstance().getTierSQLManager();

    /**
     * This listener checks if the spawner type matches a
     * tier type (or if the setting is set to global)
     *
     * Then it checks if the spawner level matches a tier
     * type level
     *
     * If all the credentials are met, the spawned mob
     * gets stored so that it can drop custom loot!
     *
     * @param event spawner spawn event (using songodas event)
     */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEpicSpawnerSpawn(SpawnerSpawnEvent event) {
        if (tierYMLManager.isEnabled()) { // Not gonna run code unless feature is enabled
            Spawner spawner = event.getSpawner();
            EntityType type = event.getEntityType();
            int spawnerLevel = spawner.getSpawnerDataCount();
            UUID uuid = event.getEntity().getUniqueId();
            TierType tierType = tierDataManager.getTierType(type.name());

            // Storing the tier mob object
            if (tierType != null) {
                TierMob tierMob = new TierMob(tierType, spawnerLevel);
                tierDataManager.addTierMob(uuid, tierMob, true);
            } else { // Defaulting to the global setting (if it's enabled)
                if (tierYMLManager.isGlobalEnabled()) {
                    TierType global = tierDataManager.getTierType("GLOBAL");

                    if (global != null) {
                        TierMob tierMob = new TierMob(global, spawnerLevel);
                        tierDataManager.addTierMob(uuid, tierMob, true);
                    }
                }
            }
        }
    }

    /**
     * Simply changes the custom drops if
     * the mob has custom drops to drop
     */
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onMobDeath(EntityDeathEvent event) {
        if (tierYMLManager.isEnabled()) { // Not gonna run code unless feature is enabled
            Entity entity = event.getEntity();
            UUID uuid = entity.getUniqueId();

            // Tier mob object
            TierMob tierMob = tierDataManager.getTierMob(uuid);

            // If the tier mob exists (if it's stored)
            if (tierMob != null) {
                // Making sure the list isn't empty
                // This might happen if they have global
                // enabled, but don't have anything added
                if (!tierMob.getItems().isEmpty()) {
                    event.getDrops().clear();
                    String type = tierMob.getTierType().getType();

                    // Dropping all items
                    if (type.equalsIgnoreCase("all")) {
                        event.getDrops().addAll(tierMob.getItems());
                    } else { // Dropping a randomized item
                        int randomizer = new Random().nextInt(tierMob.getItems().size());
                        ItemStack randomItem = tierMob.getItems().get(randomizer);
                        event.getDrops().add(randomItem);
                    }
                }

                // Removing tier mob object
                tierDataManager.removeTierMob(uuid);
            }
        }
    }
}

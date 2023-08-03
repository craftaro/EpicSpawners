package com.craftaro.epicspawners.api.boosts;

import com.craftaro.epicspawners.api.boosts.types.Boosted;
import com.craftaro.epicspawners.api.boosts.types.BoostedPlayer;
import com.craftaro.epicspawners.api.boosts.types.BoostedSpawner;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public interface BoostManager {
    /**
     * Add a boost to the manager and save it to the database
     *
     * @param boosted The boost to add
     */
    void addBoost(Boosted boosted);

    /**
     * Remove a boost from the manager and delete it from the database
     *
     * @param boosted The boost to remove
     */
    void removeBoost(Boosted boosted);

    /**
     * Get all boosts
     *
     * @return All boosts
     */
    Set<Boosted> getBoosts();

    /**
     * Create a new boost for a player
     *
     * @param playerUUID The UUID of the player
     * @param amtBoosted The amount boosted
     * @param endTime    The time the boost ends
     */
    BoostedPlayer createBoostedPlayer(UUID playerUUID, int amtBoosted, long endTime);

    /**
     * Create a new boost for a player
     *
     * @param player     The player
     * @param amtBoosted The amount boosted
     * @param endTime    The time the boost ends
     */
    BoostedPlayer createBoostedPlayer(Player player, int amtBoosted, long endTime);

    /**
     * Create a new boost for a spawner
     *
     * @param location   The location of the spawner
     * @param amtBoosted The amount boosted
     * @param endTime    The time the boost ends
     */
    BoostedSpawner createBoostedSpawner(Location location, int amtBoosted, long endTime);
}

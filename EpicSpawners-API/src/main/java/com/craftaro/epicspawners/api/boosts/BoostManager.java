package com.craftaro.epicspawners.api.boosts;

import com.craftaro.epicspawners.api.boosts.types.Boosted;
import com.craftaro.epicspawners.api.boosts.types.BoostedPlayer;
import com.craftaro.epicspawners.api.boosts.types.BoostedSpawner;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface BoostManager {

    void addBoost(Boosted boosted);

    void removeBoost(Boosted boosted);

    Set<Boosted> getBoosts();

    void addBoosts(List<Boosted> boosts);

    void clearBoosts();

    BoostedPlayer createBoostedPlayer(UUID playerUUID, int amtBoosted, long endTime);

    BoostedPlayer createBoostedPlayer(Player player, int amtBoosted, long endTime);

    BoostedSpawner createBoostedSpawner(Location location, int amtBoosted, long endTime);
}

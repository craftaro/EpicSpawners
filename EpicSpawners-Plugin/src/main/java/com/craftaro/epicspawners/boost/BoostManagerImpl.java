package com.craftaro.epicspawners.boost;

import com.craftaro.epicspawners.api.boosts.BoostManager;
import com.craftaro.epicspawners.api.boosts.types.Boosted;
import com.craftaro.epicspawners.api.boosts.types.BoostedPlayer;
import com.craftaro.epicspawners.api.boosts.types.BoostedSpawner;
import com.craftaro.epicspawners.boost.types.BoostedPlayerImpl;
import com.craftaro.epicspawners.boost.types.BoostedSpawnerImpl;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BoostManagerImpl implements BoostManager {
    private final Set<Boosted> registeredBoosts = new HashSet<>();

    @Override
    public void addBoost(Boosted boosted) {
        synchronized (registeredBoosts) {
            this.registeredBoosts.add(boosted);
        }
    }

    @Override
    public void removeBoost(Boosted boosted) {
        synchronized (registeredBoosts) {
            this.registeredBoosts.remove(boosted);
        }
    }

    @Override
    public Set<Boosted> getBoosts() {
        return Collections.unmodifiableSet(registeredBoosts);
    }

    public void addBoosts(List<Boosted> boosts) {
        synchronized (registeredBoosts) {
            registeredBoosts.addAll(boosts);
        }
    }

    public void clearBoosts() {
        synchronized (registeredBoosts) {
            registeredBoosts.clear();
        }
    }

    @Override
    public BoostedPlayer createBoostedPlayer(UUID playerUUID, int amtBoosted, long endTime) {
        return new BoostedPlayerImpl(playerUUID, amtBoosted, endTime);
    }

    @Override
    public BoostedPlayer createBoostedPlayer(Player player, int amtBoosted, long endTime) {
        return new BoostedPlayerImpl(player, amtBoosted, endTime);
    }

    @Override
    public BoostedSpawner createBoostedSpawner(Location location, int amtBoosted, long endTime) {
        return new BoostedSpawnerImpl(location, amtBoosted, endTime);
    }
}

package com.craftaro.epicspawners.api.player;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;

import java.util.Map;

public interface PlayerData {
    void save();

    OfflinePlayer getPlayer();

    int addKilledEntity(EntityType type, int amount);

    void removeEntity(EntityType entity);

    Map<EntityType, Integer> getEntityKills();

    void deleteEntityKills(EntityType entityType);
}

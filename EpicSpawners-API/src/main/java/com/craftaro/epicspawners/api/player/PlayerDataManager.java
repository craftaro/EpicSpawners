package com.craftaro.epicspawners.api.player;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public interface PlayerDataManager {
    PlayerData getPlayerData(UUID uuid);

    PlayerData getPlayerData(Player player);

    boolean isPlayerData(Player player);

    Collection<PlayerData> getRegisteredPlayers();
}

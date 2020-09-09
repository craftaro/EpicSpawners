package com.songoda.epicspawners.player;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final Map<UUID, PlayerData> registeredPlayers = new HashMap<>();

    public PlayerData getPlayerData(UUID uuid) {
        return (uuid != null) ? registeredPlayers.computeIfAbsent(uuid, PlayerData::new) : null;
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public boolean isPlayerData(Player player) {
        return registeredPlayers.containsKey(player.getUniqueId());
    }

    public Collection<PlayerData> getRegisteredPlayers() {
        return Collections.unmodifiableCollection(registeredPlayers.values());
    }

}
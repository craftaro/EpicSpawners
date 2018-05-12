package com.songoda.epicspawners.player;

import org.bukkit.entity.Player;

import java.util.*;

public class PlayerActionManager {

    private final Map<UUID, PlayerData> registeredPlayers = new HashMap<>();

    public PlayerData getPlayerAction(UUID uuid) {
        return (uuid != null) ? registeredPlayers.computeIfAbsent(uuid, p -> new PlayerData(uuid)) : null;
    }

    public PlayerData getPlayerAction(Player player) {
        return getPlayerAction(player.getUniqueId());
    }

    public Collection<PlayerData> getRegisteredPlayers() {
        return Collections.unmodifiableCollection(registeredPlayers.values());
    }
}
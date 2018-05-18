package com.songoda.epicspawners.player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

public class PlayerActionManager {

    private final Map<UUID, PlayerData> registeredPlayers = new HashMap<>();

    public PlayerData getPlayerAction(UUID uuid) {
        return (uuid != null) ? registeredPlayers.computeIfAbsent(uuid, PlayerData::new) : null;
    }

    public PlayerData getPlayerAction(Player player) {
        return getPlayerAction(player.getUniqueId());
    }

    public Collection<PlayerData> getRegisteredPlayers() {
        return Collections.unmodifiableCollection(registeredPlayers.values());
    }

}
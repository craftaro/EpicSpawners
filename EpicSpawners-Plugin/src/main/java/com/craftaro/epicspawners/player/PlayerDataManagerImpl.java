package com.craftaro.epicspawners.player;

import com.craftaro.epicspawners.api.player.PlayerData;
import com.craftaro.epicspawners.api.player.PlayerDataManager;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManagerImpl implements PlayerDataManager {
    private final Map<UUID, PlayerDataImpl> registeredPlayers = new HashMap<>();

    @Override
    public PlayerDataImpl getPlayerData(UUID uuid) {
        return (uuid != null) ? this.registeredPlayers.computeIfAbsent(uuid, PlayerDataImpl::new) : null;
    }

    @Override
    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    @Override
    public boolean isPlayerData(Player player) {
        return this.registeredPlayers.containsKey(player.getUniqueId());
    }

    @Override
    public Collection<PlayerData> getRegisteredPlayers() {
        return Collections.unmodifiableCollection(this.registeredPlayers.values());
    }
}

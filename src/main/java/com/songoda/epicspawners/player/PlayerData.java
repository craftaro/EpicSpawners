package com.songoda.epicspawners.player;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerData {

    private final UUID playerUUID;

    private Map<EntityType, Integer> entityKills = new EnumMap<>(EntityType.class);

    PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(playerUUID);
    }

    public int addKilledEntity(EntityType type, int amount) {
        return entityKills.merge(type, amount, Integer::sum);
    }

    public void removeEntity(EntityType entity) {
        entityKills.remove(entity);
    }

    public Map<EntityType, Integer> getEntityKills() {
        return Collections.unmodifiableMap(entityKills);
    }

    @Override
    public int hashCode() {
        return 31 * (playerUUID == null ? 0 : playerUUID.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PlayerData)) return false;

        PlayerData other = (PlayerData) obj;
        return Objects.equals(playerUUID, other.playerUUID);
    }

}

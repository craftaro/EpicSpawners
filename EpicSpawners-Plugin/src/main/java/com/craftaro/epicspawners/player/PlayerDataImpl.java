package com.craftaro.epicspawners.player;

import com.craftaro.core.database.DataManager;
import com.craftaro.core.third_party.org.jooq.impl.DSL;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class PlayerDataImpl implements PlayerData {

    private final UUID playerUUID;

    private final Map<EntityType, Integer> entityKills;

    PlayerDataImpl(UUID playerUUID) {
        this.entityKills = new EnumMap<>(EntityType.class);
        this.playerUUID = playerUUID;
    }

    @Override
    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(playerUUID);
    }

    @Override
    public int addKilledEntity(EntityType type, int amount) {
        return entityKills.merge(type, amount, Integer::sum);
    }

    @Override
    public void removeEntity(EntityType entity) {
        entityKills.remove(entity);
    }

    @Override
    public Map<EntityType, Integer> getEntityKills() {
        return Collections.unmodifiableMap(entityKills);
    }

    @Override
    public void deleteEntityKills(EntityType entityType) {
        entityKills.remove(entityType);
    }

    @Override
    public void save() {
        DataManager dataManager = EpicSpawners.getInstance().getDataManager();
        dataManager.getDatabaseConnector().connectDSL(context -> {
            for (Map.Entry<EntityType, Integer> entry : entityKills.entrySet()) {
                context.insertInto(DSL.table(dataManager.getTablePrefix() + "entity_kills"))
                        .set(DSL.field("player"), playerUUID.toString())
                        .set(DSL.field("entity_type"), entry.getKey().name())
                        .set(DSL.field("amount"), entry.getValue())
                        .onDuplicateKeyUpdate()
                        .set(DSL.field("amount"), entry.getValue())
                        .execute();
            }
        });
    }

    @Override
    public int hashCode() {
        return 31 * (playerUUID == null ? 0 : playerUUID.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PlayerDataImpl)) return false;

        PlayerDataImpl other = (PlayerDataImpl) obj;
        return Objects.equals(playerUUID, other.playerUUID);
    }
}

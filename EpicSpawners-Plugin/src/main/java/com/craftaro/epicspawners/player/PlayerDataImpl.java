package com.craftaro.epicspawners.player;

import com.craftaro.core.database.DataManager;
import com.craftaro.third_party.org.jooq.InsertOnDuplicateSetMoreStep;
import com.craftaro.third_party.org.jooq.InsertResultStep;
import com.craftaro.third_party.org.jooq.Query;
import com.craftaro.third_party.org.jooq.Record;
import com.craftaro.third_party.org.jooq.TableRecord;
import com.craftaro.third_party.org.jooq.UpdatableRecord;
import com.craftaro.third_party.org.jooq.impl.DSL;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        return Bukkit.getOfflinePlayer(this.playerUUID);
    }

    @Override
    public int addKilledEntity(EntityType type, int amount) {
        return this.entityKills.merge(type, amount, Integer::sum);
    }

    @Override
    public void removeEntity(EntityType entity) {
        this.entityKills.remove(entity);
    }

    @Override
    public Map<EntityType, Integer> getEntityKills() {
        return Collections.unmodifiableMap(this.entityKills);
    }

    @Override
    public void deleteEntityKills(EntityType entityType) {
        this.entityKills.remove(entityType);
    }

    @Override
    public void save() {
        DataManager dataManager = EpicSpawners.getInstance().getDataManager();
        dataManager.getDatabaseConnector().connectDSL(context -> {
            List<Query> statements = new ArrayList<>();
            for (Map.Entry<EntityType, Integer> entry : this.entityKills.entrySet()) {
                Query query = DSL.insertInto(DSL.table(dataManager.getTablePrefix() + "entity_kills"))
                        .set(DSL.field("player"), this.playerUUID.toString())
                        .set(DSL.field("entity_type"), entry.getKey().name())
                        .set(DSL.field("count"), entry.getValue())
                        .onConflict(DSL.field("player"), DSL.field("entity_type"))
                        .doUpdate()
                        .set(DSL.field("count"), entry.getValue());
                statements.add(query);
            }

            context.batch(statements).execute();
        });
    }

    @Override
    public int hashCode() {
        return 31 * (this.playerUUID == null ? 0 : this.playerUUID.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PlayerDataImpl)) {
            return false;
        }

        PlayerDataImpl other = (PlayerDataImpl) obj;
        return Objects.equals(this.playerUUID, other.playerUUID);
    }
}

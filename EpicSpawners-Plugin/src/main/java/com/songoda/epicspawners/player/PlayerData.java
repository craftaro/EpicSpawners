package com.songoda.epicspawners.player;

import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.ESpawner;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;

import java.util.*;

public class PlayerData {

    private final UUID playerUUID;

    private ESpawner lastSpawner = null;
    private SpawnerData lastData = null;

    private MenuType inMenu = MenuType.NOT_IN;
    private Map<EntityType, Integer> entityKills = new EnumMap<>(EntityType.class);
    private int infoPage = 1;

    private int currentPage = 0;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public int getInfoPage() {
        return infoPage;
    }

    public void setInfoPage(int infoPage) {
        this.infoPage = infoPage;
    }

    public ESpawner getLastSpawner() {
        return lastSpawner;
    }

    public void setLastSpawner(ESpawner lastSpawner) {
        this.lastSpawner = lastSpawner;
    }

    public MenuType getInMenu() {
        return inMenu;
    }

    public void setInMenu(MenuType inMenu) {
        this.inMenu = inMenu;
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(playerUUID);
    }

    public int addKilledEntity(EntityType type) {
        if (entityKills == null) entityKills = new EnumMap<>(EntityType.class);
        return entityKills.merge(type, 1, Integer::sum);
    }

    public Map<EntityType, Integer> getEntityKills() {
        return Collections.unmodifiableMap(entityKills);
    }

    public void setEntityKills(Map<EntityType, Integer> entityKills) {
        this.entityKills = entityKills;
    }

    public SpawnerData getLastData() {
        return lastData;
    }

    public void setLastData(SpawnerData lastData) {
        this.lastData = lastData;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
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

package com.songoda.epicspawners.player;

import com.songoda.epicspawners.spawners.object.ESpawner;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;

import java.util.*;

public class PlayerData {

    private final UUID playerUUID;
    private ESpawner lastSpawner = null;
    private MenuType inMenu = MenuType.NOTIN;
    private Map<EntityType, Integer> entityKills = new EnumMap<>(EntityType.class);
    private int infoPage = 1;

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

    public void setEntityKills(Map<EntityType, Integer> entityKills) {
        this.entityKills = entityKills;
    }

    public int addKilledEntity(EntityType type) {
        if (entityKills == null) entityKills = new EnumMap<>(EntityType.class);
        return entityKills.merge(type,1, Integer::sum);
    }

    public Map<EntityType, Integer> getEntityKills() {
        return Collections.unmodifiableMap(entityKills);
    }
}

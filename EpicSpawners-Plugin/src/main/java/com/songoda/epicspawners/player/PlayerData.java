package com.songoda.epicspawners.player;

import com.songoda.epicspawners.spawners.object.ESpawner;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {

    private final UUID playerUUID;
    private ESpawner lastSpawner = null;
    private MenuType inMenu = MenuType.NOTIN;
    private Map<EntityType, Integer> entityKills = new HashMap<>();
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
        if (entityKills.containsKey(type)) {
            int amt = entityKills.get(type) + 1;
            entityKills.remove(type);
            entityKills.put(type, amt);
            return amt++;
        } else {
            return entityKills.put(type, 1);
        }
    }

    public Map<EntityType, Integer> getEntityKills() {
        return Collections.unmodifiableMap(entityKills);
    }
}

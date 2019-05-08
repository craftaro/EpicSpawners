package com.songoda.epicspawners.storage;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.boost.BoostData;
import com.songoda.epicspawners.player.PlayerData;
import com.songoda.epicspawners.utils.ConfigWrapper;
import com.songoda.epicspawners.utils.Methods;

import java.util.List;

public abstract class Storage {

    protected final EpicSpawnersPlugin instance;
    protected final ConfigWrapper dataFile;

    public Storage(EpicSpawnersPlugin instance) {
        this.instance = instance;
        this.dataFile = new ConfigWrapper(instance, "", "data.yml");
        this.dataFile.createNewFile(null, "EpicHeads Data File");
        this.dataFile.getConfig().options().copyDefaults(true);
        this.dataFile.saveConfig();
    }

    public abstract boolean containsGroup(String group);

    public abstract List<StorageRow> getRowsByGroup(String group);

    public abstract void prepareSaveItem(String group, StorageItem... items);

    public void updateData(EpicSpawnersPlugin instance) {
        // Save game data
        for (Spawner spawner : instance.getSpawnerManager().getSpawners()) {
            if (spawner.getFirstStack() == null
                    || spawner.getFirstStack().getSpawnerData() == null
                    || spawner.getLocation() == null
                    || spawner.getLocation().getWorld() == null) continue;

            StorageItem location = new StorageItem("location", Methods.serializeLocation(spawner.getLocation()));

            StringBuilder stacksStr = new StringBuilder();
            for (SpawnerStack stack : spawner.getSpawnerStacks()) {
                stacksStr.append(stack.getSpawnerData().getIdentifyingName()).append(":").append(stack.getStackSize()).append(";");
            }
            StorageItem stacks = new StorageItem("stacks", stacksStr.toString());

            StorageItem placedBy = spawner.getPlacedBy() != null ? new StorageItem("placedby", spawner.getPlacedBy().getUniqueId().toString()) : null;

            prepareSaveItem("spawners", location, stacks, new StorageItem("spawns", spawner.getSpawnCount()), placedBy);
        }

        for (BoostData boostData : instance.getBoostManager().getBoosts()) {
            prepareSaveItem("boosts", new StorageItem("endtime", String.valueOf(boostData.getEndTime())),
                    new StorageItem("boosttype", boostData.getBoostType().name()),
                    new StorageItem("data", boostData.getData()),
                    new StorageItem("amount", boostData.getAmtBoosted()));
        }

        for (PlayerData playerData : instance.getPlayerActionManager().getRegisteredPlayers()) {
            prepareSaveItem("players", new StorageItem("uuid", playerData.getPlayer().getUniqueId().toString()),
                    new StorageItem("entitykills", playerData.getEntityKills()));
        }
    }

    public abstract void doSave();

    public abstract void save();

    public abstract void makeBackup();

    public abstract void closeConnection();

}

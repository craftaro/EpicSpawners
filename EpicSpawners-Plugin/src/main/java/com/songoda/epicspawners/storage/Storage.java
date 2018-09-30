package com.songoda.epicspawners.storage;

import com.songoda.arconix.api.utils.ConfigWrapper;
import com.songoda.epicspawners.EpicSpawnersPlugin;

import java.util.List;

public abstract class Storage {

    protected final EpicSpawnersPlugin instance;
    protected final ConfigWrapper dataFile;

    public Storage(EpicSpawnersPlugin instance) {
        this.instance = instance;
        this.dataFile = new ConfigWrapper(instance, "", "data.yml");
        this.dataFile.createNewFile(null, "EpicSpawners Data File");
        this.dataFile.getConfig().options().copyDefaults(true);
        this.dataFile.saveConfig();
    }

    public abstract boolean containsGroup(String group);

    public abstract List<StorageRow> getRowsByGroup(String group);

    public abstract void clearFile();

    public abstract void saveItem(String group, StorageItem... items);

    public abstract void closeConnection();

}

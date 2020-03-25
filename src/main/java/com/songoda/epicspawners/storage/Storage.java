package com.songoda.epicspawners.storage;

import com.songoda.core.configuration.Config;
import com.songoda.epicspawners.EpicSpawners;

import java.util.List;

public abstract class Storage {

    protected final EpicSpawners plugin;
    protected final Config dataFile;

    public Storage(EpicSpawners plugin) {
        this.plugin = plugin;
        this.dataFile = new Config(plugin, "data.yml");
        this.dataFile.load();
    }

    public abstract boolean containsGroup(String group);

    public abstract List<StorageRow> getRowsByGroup(String group);

    public abstract void prepareSaveItem(String group, StorageItem... items);

    public void updateData(EpicSpawners plugin) {
        // We're not saving data anymore.
    }

    public abstract void doSave();

    public abstract void save();

    public abstract void makeBackup();

    public abstract void closeConnection();

}

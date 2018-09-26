package com.songoda.epicspawners.storage.types;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.storage.Storage;
import com.songoda.epicspawners.storage.StorageItem;
import com.songoda.epicspawners.storage.StorageRow;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageYaml extends Storage {

    public StorageYaml(EpicSpawnersPlugin instance) {
        super(instance);
    }

    @Override
    public boolean containsGroup(String group) {
        return dataFile.getConfig().contains("data." + group);
    }

    @Override
    public List<StorageRow> getRowsByGroup(String group) {
        List<StorageRow> rows = new ArrayList<>();
        ConfigurationSection currentSection = dataFile.getConfig().getConfigurationSection("data." + group);
        for (String key : currentSection.getKeys(false)) {

            Map<String, StorageItem> items = new HashMap<>();
            ConfigurationSection currentSection2 = dataFile.getConfig().getConfigurationSection("data." + group + "." + key);
            for (String key2 : currentSection2.getKeys(false)) {
                String path = "data." + group + "." + key + "." + key2;
                items.put(key2, new StorageItem(dataFile.getConfig().get(path) instanceof MemorySection
                        ? convertToInLineList(path) : dataFile.getConfig().get(path)));
            }
            if (items.isEmpty()) continue;
            StorageRow row = new StorageRow(key, items);
            rows.add(row);
        }
        return rows;
    }

    private String convertToInLineList(String path) {
        String converted = "";
        for (String key : dataFile.getConfig().getConfigurationSection(path).getKeys(false)) {
            converted += key + ":" + dataFile.getConfig().getInt(path + "." + key) + ";";
        }
        return converted;
    }

    @Override
    public void clearFile() {
        dataFile.getConfig().set("data", null);
    }

    @Override
    public void saveItem(String group, StorageItem... items) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null || items[i].asObject() == null) continue;
            dataFile.getConfig().set("data." + group + "." + items[0].asString() + "." + items[i].getKey(), items[i].asObject());
        }
    }

    @Override
    public void closeConnection() {
        dataFile.saveConfig();
    }
}

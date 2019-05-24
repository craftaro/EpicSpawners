package com.songoda.epicspawners.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * ConfigWrapper made by @clip
 */
public class ConfigWrapper {

    private final JavaPlugin plugin;
    private final String folderName, fileName;
    private FileConfiguration config;
    private File configFile;

    public ConfigWrapper(final JavaPlugin instance, final String folderName, final String fileName) {
        this.plugin = instance;
        this.folderName = folderName;
        this.fileName = fileName;
    }

    public void createNewFile(final String message, final String header) {
        reloadConfig();
        saveConfig();
        loadConfig(header);

        if (message != null) {
            plugin.getLogger().info(message);
        }
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public void loadConfig(final String header) {
        config.options().header(header);
        config.options().copyDefaults(true);
        saveConfig();
    }

    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder() + folderName, fileName);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (final IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }
}
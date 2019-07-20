package com.songoda.epicspawners.blacklist;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.utils.ConfigWrapper;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by songoda on 2/25/2017.
 */
public class BlacklistHandler {

    private ConfigWrapper blackFile = new ConfigWrapper(EpicSpawners.getInstance(), "", "blacklist.yml");

    public BlacklistHandler() {
        blackFile.createNewFile("Loading language file", "EpicSpawners.java blacklist file");
        loadBlacklistFile();
    }

    public boolean isBlacklisted(Player player, boolean notify) {
        boolean blacklisted = false;
        List<String> list = blackFile.getConfig().getStringList("settings.blacklist");
        String cWorld = player.getWorld().getName();
        for (String world : list) {
            if (!cWorld.equalsIgnoreCase(world)) continue;
            if (notify)
                EpicSpawners.getInstance().getLocale().getMessage("event.block.blacklisted")
                        .sendPrefixedMessage(player);
            blacklisted = true;
        }
        return blacklisted;
    }

    private void loadBlacklistFile() {
        List<String> list = new ArrayList<>();
        list.add("world2");
        list.add("world3");
        list.add("world4");
        list.add("world5");
        blackFile.getConfig().addDefault("settings.blacklist", list);

        blackFile.getConfig().options().copyDefaults(true);
        blackFile.saveConfig();
    }

    public void reload() {
        blackFile.reloadConfig();
        loadBlacklistFile();
    }
}
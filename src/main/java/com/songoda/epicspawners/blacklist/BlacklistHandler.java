package com.songoda.epicspawners.blacklist;

import com.songoda.core.configuration.Config;
import com.songoda.epicspawners.EpicSpawners;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by songoda on 2/25/2017.
 */
public class BlacklistHandler {

    private Config blackConfig = new Config(EpicSpawners.getInstance(), "blacklist.yml");

    public BlacklistHandler() {
        blackConfig.load();
        loadBlacklistFile();
    }

    public boolean isBlacklisted(Player player, boolean notify) {
        boolean blacklisted = false;
        List<String> list = blackConfig.getStringList("settings.blacklist");
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
        blackConfig.addDefault("settings.blacklist", list);

        blackConfig.options().copyDefaults(true);
        blackConfig.save();
    }

    public Config getBlackConfig() {
        return blackConfig;
    }

    public void reload() {
        blackConfig.load();
        loadBlacklistFile();
    }
}
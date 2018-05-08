package com.songoda.epicspawners.Handlers;

import com.songoda.arconix.api.utils.ConfigWrapper;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Utils.Debugger;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by songoda on 2/25/2017.
 */
public class BlacklistHandler {

    private ConfigWrapper blackFile = new ConfigWrapper(EpicSpawners.getInstance(), "", "main/resources/blacklist.yml");

    public BlacklistHandler() {
        try {
            blackFile.createNewFile("Loading language file", "EpicSpawners blacklist file");
            loadBlacklistFile();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public boolean isBlacklisted(Player p, boolean yell) {
        try {
            List<String> list = blackFile.getConfig().getStringList("settings.blacklist");
            String cworld = p.getWorld().getName();
            for (String world : list) {
                if (cworld.equalsIgnoreCase(world)) {
                    if (yell)
                        p.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.block.blacklisted"));
                    return true;
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return false;
    }

    private void loadBlacklistFile() {
        try {
            List<String> list = new ArrayList<>();
            list.add("world2");
            list.add("world3");
            list.add("world4");
            list.add("world5");
            blackFile.getConfig().addDefault("settings.blacklist", list);

            blackFile.getConfig().options().copyDefaults(true);
            blackFile.saveConfig();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void reload() {
        try {
            blackFile.createNewFile("Loading blacklist file", "EpicSpawnesrs.java blacklist file");
            loadBlacklistFile();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}

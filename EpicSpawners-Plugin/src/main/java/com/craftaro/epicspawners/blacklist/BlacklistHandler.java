package com.craftaro.epicspawners.blacklist;

import com.craftaro.core.configuration.Config;
import com.craftaro.epicspawners.EpicSpawners;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BlacklistHandler {
    private final Config blackConfig = new Config(EpicSpawners.getInstance(), "blacklist.yml");
    private final List<String> list;

    public BlacklistHandler() {
        blackConfig.load();
        loadBlacklistFile();
        list = blackConfig.getStringList("settings.blacklist")
                .stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    public boolean isBlacklisted(Player player, boolean notify) {
        if (list.contains(player.getWorld().getName().toLowerCase())) {
            if (notify) {
                EpicSpawners.getInstance().getLocale().getMessage("event.block.blacklisted").sendPrefixedMessage(player);
            }
            return true;
        }
        return false;
    }

    public boolean isBlacklisted(World world) {
        return list.contains(world.getName().toLowerCase());
    }

    private void loadBlacklistFile() {
        List<String> list = new ArrayList<>(Arrays.asList("world2", "world3", "world4", "world5"));
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

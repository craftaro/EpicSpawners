package com.songoda.epicspawners.Handlers;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by songo on 9/3/2017.
 */
public class BoostHandler {

    public BoostHandler() {
        try {
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(EpicSpawners.getInstance(), BoostHandler::animate, 2L, 9L);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public static void animate() {
        try {
            if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.boosts")) {
                ConfigurationSection cs = EpicSpawners.getInstance().dataFile.getConfig().getConfigurationSection("data.boosts");
                for (String key : cs.getKeys(false)) {
                    Location location = null;
                    boolean yes = false;
                    if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.boosts." + key + ".location")) {
                        location = Arconix.pl().getApi().serialize().unserializeLocation(EpicSpawners.getInstance().dataFile.getConfig().getString("data.boosts." + key + ".location"));
                        yes = true;
                        if (location.getBlock().getType() != Material.MOB_SPAWNER) {
                            yes = false;
                            EpicSpawners.getInstance().dataFile.getConfig().set("data.boosts." + key, null);
                        }
                    } else if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.boosts." + key + ".player")) {
                        String uuid = EpicSpawners.getInstance().dataFile.getConfig().getString("data.boosts." + key + ".player");
                        if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawnerstats")) {
                            ConfigurationSection cs2 = EpicSpawners.getInstance().dataFile.getConfig().getConfigurationSection("data.spawnerstats");
                            for (String key2 : cs2.getKeys(false)) {
                                if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawnerstats." + key2 + ".player")) {
                                    if (EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + key2 + ".player").equals(uuid)) {
                                        location = Arconix.pl().getApi().serialize().unserializeLocation(key2);
                                        yes = true;
                                    }
                                }
                            }
                        }
                    }
                    if (yes) {
                        location.add(.5, .5, .5);
                        float x = (float) (0 + (Math.random() * .75));
                        float y = (float) (0 + (Math.random() * 1));
                        float z = (float) (0 + (Math.random() * .75));
                        Arconix.pl().getApi().packetLibrary.getParticleManager().broadcastParticle(location, x, y, z, 0, "SPELL", 2);
                    }
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}

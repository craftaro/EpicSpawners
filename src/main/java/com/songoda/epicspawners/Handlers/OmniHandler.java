package com.songoda.epicspawners.Handlers;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Spawners.Spawner;
import com.songoda.epicspawners.Spawners.SpawnerItem;
import com.songoda.epicspawners.Utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * Created by songo on 5/17/2017.
 */
public class OmniHandler {

    public OmniHandler() {
        try {
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(EpicSpawners.getInstance(), OmniHandler::displayItems, 30L, 30L);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public static void displayItems() {
        try {
            if (EpicSpawners.getInstance().getConfig().getBoolean("Main.OmniSpawners Enabled")) {
                if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawnerstats")) {
                    ConfigurationSection cs = EpicSpawners.getInstance().dataFile.getConfig().getConfigurationSection("data.spawnerstats");
                    for (String key : cs.getKeys(false)) {
                        if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawnerstats." + key + ".type")) {
                            if (EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + key + ".type").equals("OMNI")) {

                                Location loc = Arconix.pl().getApi().serialize().unserializeLocation(key);
                                if (loc != null && loc.getWorld() != null) {
                                    int destx = loc.getBlockX() >> 4;
                                    int destz = loc.getBlockZ() >> 4;
                                    if (!loc.getWorld().isChunkLoaded(destx, destz)) {
                                        continue;
                                    }
                                    if (loc.getBlock().getType() == Material.MOB_SPAWNER) {
                                        Spawner eSpawner = new Spawner(loc);

                                        String last = null;
                                        String next = null;
                                        List<SpawnerItem> list = EpicSpawners.getInstance().getApi().convertFromList(EpicSpawners.getInstance().dataFile.getConfig().getStringList("data.spawnerstats." + key + ".entities"));
                                        for (SpawnerItem item : list) {
                                            if (item.getType().equals(eSpawner.getOmniState())) {
                                                last = item.getType();
                                            } else if (last != null && next == null) {
                                                next = item.getType();
                                            }
                                        }
                                        if (next == null) {
                                            next = list.get(0).getType();
                                        }
                                        EpicSpawners.getInstance().getApi().updateDisplayItem(next, loc);
                                        eSpawner.setOmniState(next);
                                    } else {
                                        EpicSpawners.getInstance().dataFile.getConfig().set("data.spawnerstats." + key + ".type", null);
                                        EpicSpawners.getInstance().dataFile.getConfig().set("data.spawnerstats." + key + ".entities", null);
                                    }
                                } else {
                                    EpicSpawners.getInstance().dataFile.getConfig().set("data.spawnerstats." + key + ".type", null);
                                    EpicSpawners.getInstance().dataFile.getConfig().set("data.spawnerstats." + key + ".entities", null);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}

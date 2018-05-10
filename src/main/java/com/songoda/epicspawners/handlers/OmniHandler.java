package com.songoda.epicspawners.handlers;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.object.Spawner;
import com.songoda.epicspawners.spawners.object.SpawnerData;
import com.songoda.epicspawners.spawners.object.SpawnerStack;
import com.songoda.epicspawners.utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by songo on 5/17/2017.
 */
@SuppressWarnings("ConstantConditions")
public class OmniHandler {

    public OmniHandler() {
        try {
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(EpicSpawners.getInstance(), OmniHandler::displayItems, 30L, 30L);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public static void displayItems() { //ToDo: Test this.
        try {
            EpicSpawners instance = EpicSpawners.getInstance();

            if (!EpicSpawners.getInstance().getConfig().getBoolean("Main.OmniSpawners Enabled")) return;

            for (Spawner spawner : instance.getSpawnerManager().getSpawnersInWorld().values()) {
                if (spawner.getSpawnerStacks().size() == 1) {
                    EpicSpawners.getInstance().getApi().updateDisplayItem(spawner, spawner.getFirstStack().getSpawnerData());
                    continue;
                }

                Location location = spawner.getLocation();
                if (location == null && location.getWorld() == null) continue;
                int destx = location.getBlockX() >> 4;
                int destz = location.getBlockZ() >> 4;
                if (!location.getWorld().isChunkLoaded(destx, destz)) {
                    continue;
                }
                if (location.getBlock().getType() != Material.MOB_SPAWNER) continue;
                String last = null;
                SpawnerData next = null;
                List<SpawnerStack> list = new ArrayList<>(spawner.getSpawnerStacks());
                for (SpawnerStack stack : list) {
                    if (stack.getSpawnerData().getName().equals(spawner.getOmniState())) {
                        last = stack.getSpawnerData().getName();
                    } else if (last != null && next == null) {
                        next = stack.getSpawnerData();
                    }
                }
                if (next == null) {
                    next = list.get(0).getSpawnerData();
                }
                EpicSpawners.getInstance().getApi().updateDisplayItem(spawner, next);
                spawner.setOmniState(next.getName());

            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}
package com.songoda.epicspawners.handlers;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Collection;

/**
 * Created by songoda on 3/12/2017.
 */
public class HologramHandler {

    private final EpicSpawnersPlugin instance;

    public HologramHandler(EpicSpawnersPlugin instance) {
        this.instance = instance;
        loadHolograms();
    }

    private void loadHolograms() {
        Collection<Spawner> spawners = instance.getSpawnerManager().getSpawners();
        if (spawners.size() == 0) return;

        for (Spawner spawner : spawners) {
            if (spawner.getLocation().getWorld() == null) continue;
            updateHologram(spawner);
        }
    }

    public void updateHologram(Spawner spawner) {
        try {
            if (spawner == null) return;

            Location location = spawner.getLocation().add(0.5, 1, 0.5);

            despawn(spawner.getLocation().getBlock());

            if (!instance.getConfig().getBoolean("Main.Spawners Have Holograms")) return;

            addHologram(location, spawner);

        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void despawn(Block b) {
        Location location = b.getLocation().add(0.5, 1, 0.5);
        Arconix.pl().getApi().packetLibrary.getHologramManager().despawnHologram(location);
    }

    private void addHologram(Location location, Spawner spawner) {
        try {
            int multi = spawner.getSpawnerDataCount();
            String name = Methods.compileName(instance.getSpawnerManager().getSpawnerData(spawner.getIdentifyingName()), multi, false);

            Arconix.pl().getApi().packetLibrary.getHologramManager().spawnHologram(location, name.trim());

        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void processChange(Block block) {
        try {
            if (block.getType() != Material.SPAWNER) return;
            Spawner spawner = instance.getSpawnerManager().getSpawnerFromWorld(block.getLocation());
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> instance.getHologramHandler().updateHologram(spawner), 1L);

        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}
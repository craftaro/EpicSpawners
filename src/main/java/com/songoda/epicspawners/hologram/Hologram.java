package com.songoda.epicspawners.hologram;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Collection;

public abstract class Hologram {

    protected final EpicSpawners plugin;

    Hologram(EpicSpawners plugin) {
        this.plugin = plugin;
    }

    public void loadHolograms() {
        Collection<Spawner> spawners = plugin.getSpawnerManager().getSpawners();
        if (spawners.size() == 0) return;

        for (Spawner spawner : spawners) {
            if (spawner.getWorld() == null) continue;
                add(spawner);
        }
    }

    public void unloadHolograms() {
        Collection<Spawner> spawners = plugin.getSpawnerManager().getSpawners();
        if (spawners.size() == 0) return;
        for (Spawner spawner : spawners) {
            if (spawner.getWorld() == null) continue;
                remove(spawner);
        }

    }

    public void add(Spawner spawner) {
        int multi = spawner.getSpawnerDataCount();
        if (spawner.getSpawnerStacks().size() == 0) return;
        String name = Methods.compileName(plugin.getSpawnerManager().getSpawnerData(spawner.getIdentifyingName()), multi, false).trim();

        add(spawner.getLocation(), name);
    }

    public void remove(Spawner spawner) {
        remove(spawner.getLocation());
    }

    public void update(Spawner spawner) {
        int multi = spawner.getSpawnerDataCount();
        if (spawner.getSpawnerStacks().size() == 0) return;
        String name = Methods.compileName(plugin.getSpawnerManager().getSpawnerData(spawner.getIdentifyingName()), multi, false).trim();

        update(spawner.getLocation(), name);
    }

    protected abstract void add(Location location, String line);

    protected abstract void remove(Location location);

    protected abstract void update(Location location, String line);

    public void processChange(Block block) {
        if (block.getType() != (plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER")))
            return;
        Spawner spawner = plugin.getSpawnerManager().getSpawnerFromWorld(block.getLocation());
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () ->
                plugin.getHologram().update(spawner), 1L);
    }
}

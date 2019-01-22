package com.songoda.epicspawners.hologram;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Collection;

public abstract class Hologram {

    protected final EpicSpawnersPlugin instance;

    Hologram(EpicSpawnersPlugin instance) {
        this.instance = instance;
    }

    public void loadHolograms() {
        Collection<Spawner> spawners = instance.getSpawnerManager().getSpawners();
        if (spawners.size() == 0) return;

        for (Spawner spawner : spawners) {
            if (spawner.getWorld() == null) continue;
                add(spawner);
        }
    }

    public void unloadHolograms() {
        Collection<Spawner> spawners = instance.getSpawnerManager().getSpawners();
        if (spawners.size() == 0) return;
        for (Spawner spawner : spawners) {
            if (spawner.getWorld() == null) continue;
                remove(spawner);
        }

    }

    public void add(Spawner spawner) {
        int multi = spawner.getSpawnerDataCount();
        if (spawner.getSpawnerStacks().size() == 0) return;
        String name = Methods.compileName(instance.getSpawnerManager().getSpawnerData(spawner.getIdentifyingName()), multi, false).trim();

        add(spawner.getLocation(), name);
    }

    public void remove(Spawner spawner) {
        remove(spawner.getLocation());
    }

    public void update(Spawner spawner) {
        int multi = spawner.getSpawnerDataCount();
        if (spawner.getSpawnerStacks().size() == 0) return;
        String name = Methods.compileName(instance.getSpawnerManager().getSpawnerData(spawner.getIdentifyingName()), multi, false).trim();

        update(spawner.getLocation(), name);
    }

    protected abstract void add(Location location, String line);

    protected abstract void remove(Location location);

    protected abstract void update(Location location, String line);

    public void processChange(Block block) {
            if (block.getType() != Material.SPAWNER) return;
            Spawner spawner = instance.getSpawnerManager().getSpawnerFromWorld(block.getLocation());
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () ->
                    instance.getHologram().update(spawner), 1L);
    }
}

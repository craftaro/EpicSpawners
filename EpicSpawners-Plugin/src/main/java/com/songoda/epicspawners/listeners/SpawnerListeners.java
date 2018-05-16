package com.songoda.epicspawners.listeners;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.spawners.object.ESpawnerData;
import com.songoda.epicspawners.spawners.object.ESpawnerStack;
import com.songoda.epicspawners.spawners.object.ESpawner;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.ServerVersion;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by songoda on 2/25/2017.
 */
public class SpawnerListeners implements Listener {

    private EpicSpawnersPlugin instance;

    public SpawnerListeners(EpicSpawnersPlugin instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onSpawn(SpawnerSpawnEvent e) {
        try {
            e.setCancelled(true);
            Location location = e.getSpawner().getLocation();

            if (!instance.getSpawnerManager().isSpawner(location)) {
                Spawner spawner = new ESpawner(location);
                instance.getSpawnerManager().addSpawnerToWorld(location, spawner);
                SpawnerData spawnerData = instance.getSpawnerManager().getSpawnerData(e.getEntityType().name());
                spawner.addSpawnerStack(new ESpawnerStack(spawnerData, 1));
            }

            Spawner spawner = instance.getSpawnerManager().getSpawnerFromWorld(e.getSpawner().getLocation());

            // Remove entity so we can do our own method.
            e.getEntity().remove();

            if (instance.isServerVersionAtLeast(ServerVersion.V1_9)) {
                for (Entity ee : e.getEntity().getPassengers()) {
                    ee.remove();
                }
            }

            spawner.spawn();
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        try {
            if (EpicSpawnersPlugin.getInstance().getConfig().getBoolean("entity.Hostile Mobs Attack Second")) {
                if (event.getEntity().getLastDamageCause() != null) {
                    if (event.getEntity().getLastDamageCause().getCause().name().equals("ENTITY_ATTACK")) {
                        return;
                    }
                }
                event.setCancelled(true);
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}

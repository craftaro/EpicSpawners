package com.songoda.epicspawners.listeners;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.ESpawner;
import com.songoda.epicspawners.spawners.spawner.ESpawnerStack;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;

/**
 * Created by songoda on 2/25/2017.
 */
public class SpawnerListeners implements Listener {

    private EpicSpawnersPlugin instance;

    public SpawnerListeners(EpicSpawnersPlugin instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onSpawn(SpawnerSpawnEvent event) {
        try {
            Entity entity = event.getEntity();
            if (entity.getVehicle() != null) {
                entity.getVehicle().remove();
                entity.remove();
            } else if (entity.getPassengers().size() != 0) {
                for (Entity e : entity.getPassengers()) {
                    e.remove();
                }
                entity.remove();
            }
            entity.remove();

            Location location = event.getSpawner().getLocation();

            if (!instance.getSpawnerManager().isSpawner(location)) {
                Spawner spawner = new ESpawner(location);
                instance.getSpawnerManager().addSpawnerToWorld(location, spawner);
                SpawnerData spawnerData = instance.getSpawnerManager().getSpawnerData(Methods.getTypeFromString(event.getEntityType().name()));
                spawner.addSpawnerStack(new ESpawnerStack(spawnerData, 1));
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        try {
            if (EpicSpawnersPlugin.getInstance().getConfig().getBoolean("entity.Hostile Mobs Attack Second")) {
                if (event.getEntity().getLastDamageCause() != null && event.getEntity().getLastDamageCause().getCause().name().equals("ENTITY_ATTACK")) {
                    return;
                }
                event.setCancelled(true);
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}

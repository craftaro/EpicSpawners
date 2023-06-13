package com.craftaro.epicspawners.listeners;

import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.epicspawners.spawners.spawner.PlacedSpawnerImpl;
import com.craftaro.epicspawners.spawners.spawner.SpawnerDataImpl;
import com.craftaro.epicspawners.spawners.spawner.SpawnerStackImpl;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;

/**
 * Created by songoda on 2/25/2017.
 */
public class SpawnerListeners implements Listener {

    private final EpicSpawners plugin;

    public SpawnerListeners(EpicSpawners plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawn(SpawnerSpawnEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.FIREWORK) return;
        if (entity.getVehicle() != null) {
            entity.getVehicle().remove();
            entity.remove();
        }

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) {
            if (entity.getPassengers().size() != 0) {
                for (Entity e : entity.getPassengers()) {
                    e.remove();
                }
                entity.remove();
            }
        }
        entity.remove();

        Location location = event.getSpawner().getLocation();

        if (!plugin.getSpawnerManager().isSpawner(location)) {
            PlacedSpawnerImpl spawner = new PlacedSpawnerImpl(location);
            plugin.getSpawnerManager().addSpawnerToWorld(location, spawner);
            SpawnerData spawnerData = plugin.getSpawnerManager().getSpawnerData(event.getEntityType().name());
            if (spawnerData == null) return;
            spawner.addSpawnerStack(new SpawnerStackImpl(spawner, spawnerData.getFirstTier(), 1));
            EpicSpawners.getInstance().getDataManager().createSpawner(spawner);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (!Settings.HOSTILE_MOBS_ATTACK_SECOND.getBoolean()) return;
        if (event.getEntity().getLastDamageCause() != null && event.getEntity().getLastDamageCause().getCause().name().equals("ENTITY_ATTACK"))
            return;
        event.setCancelled(true);
    }
}

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
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

    //ToDO: Boosting isn't implemented at all.
    //ToDo: Use this for all spawner things (Like items, commands and what not) instead of the old shit
    //ToDO: There is a weird error that is triggered when a spawner is not found in the config.
    private Map<Location, Date> lastSpawns = new HashMap<>();
    private Map<Location, Integer> timer = new HashMap<>();

    @EventHandler
    public void onSpawn(SpawnerSpawnEvent e) {
        try {
            e.setCancelled(true);
            long lastSpawn = 1001;
            if (lastSpawns.containsKey(e.getSpawner().getLocation())) {
                lastSpawn = (new Date()).getTime() - lastSpawns.get(e.getSpawner().getLocation()).getTime();
            }
            if (lastSpawn >= 1000) {
                lastSpawns.put(e.getSpawner().getLocation(), new Date());
            } else return;

            Location location = e.getSpawner().getLocation();

            // Remove entity so we can do our own method.
            e.getEntity().remove();

            if (location.getBlock().isBlockPowered() && instance.getConfig().getBoolean("Main.Redstone Power Deactivates Spawners"))
                return;

            if (!instance.getSpawnerManager().isSpawner(location)) {
                Spawner spawner = new ESpawner(location);
                instance.getSpawnerManager().addSpawnerToWorld(location, spawner);
                SpawnerData spawnerData = instance.getSpawnerManager().getSpawnerData(e.getEntityType().name());
                spawner.addSpawnerStack(new ESpawnerStack(spawnerData, 1));
            }

            Spawner spawner = instance.getSpawnerManager().getSpawnerFromWorld(e.getSpawner().getLocation());

            if (e.getSpawner().getSpawnedType() == EntityType.DROPPED_ITEM) {
                int amt = 0;
                if (!timer.containsKey(location)) {
                    timer.put(location, amt);
                    return;
                } else {
                    amt = timer.get(location);
                    amt = amt + 30;
                    timer.put(location, amt);
                }
                int delay = spawner.updateDelay();
                if (amt < delay) {
                    return;
                }
                timer.remove(location);
            }

            if (spawner.getFirstStack().getSpawnerData() == null) return;

            float x = (float) (0 + (Math.random() * .8));
            float y = (float) (0 + (Math.random() * .8));
            float z = (float) (0 + (Math.random() * .8));

            Location particleLocation = location.clone();
            particleLocation.add(.5, .5, .5);
            //ToDo: Only currently works for the first spawner type in the stack. this is not how it should work.
            SpawnerData spawnerData = spawner.getFirstStack().getSpawnerData();
            Arconix.pl().getApi().packetLibrary.getParticleManager().broadcastParticle(particleLocation, x, y, z, 0, spawnerData.getSpawnerSpawnParticle().getEffect(), spawnerData.getParticleDensity().getSpawnerSpawn());

            for (SpawnerStack stack : spawner.getSpawnerStacks()) {
                ((ESpawnerData)stack.getSpawnerData()).spawn(spawner, stack);
                //stack.getSpawnerData().spawn(location); // This method will spawn all methods at once.
            }
            Bukkit.getScheduler().runTaskLater(instance, spawner::updateDelay, 10);
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

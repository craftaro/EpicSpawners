package com.songoda.epicspawners.listeners;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.object.Spawner;
import com.songoda.epicspawners.spawners.object.SpawnerData;
import com.songoda.epicspawners.spawners.object.SpawnerStack;
import com.songoda.epicspawners.utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

    private EpicSpawners instance;

    public SpawnerListeners(EpicSpawners instance) {
        this.instance = instance;
    }

    //ToDO: Boosting isn't implemented at all.
    //ToDo: Use this for all spawner things (Like items, commands and what not) instead of the old shit
    //ToDO: There is a weird error that is triggered when a spawner is not found in the config.
    private Map<Location, Date> lastSpawns = new HashMap<>();

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
                Spawner spawner = new Spawner(location);
                instance.getSpawnerManager().addSpawnerToWorld(location, spawner);
                SpawnerData spawnerData = instance.getSpawnerManager().getSpawnerType(e.getEntityType().name());
                spawner.addSpawnerType(new SpawnerStack(spawnerData, 1));
            }

            Spawner spawner = instance.getSpawnerManager().getSpawnerFromWorld(e.getSpawner().getLocation());

            if (spawner.getFirstStack().getSpawnerData() == null) return;

            float x = (float) (0 + (Math.random() * .8));
            float y = (float) (0 + (Math.random() * .8));
            float z = (float) (0 + (Math.random() * .8));

            Location particleLocation = location.clone();
            particleLocation.add(.5, .5, .5);
            //ToDo: Only currently works for the first spawner type in the stack. this is not how it should work.
            SpawnerData spawnerData = spawner.getFirstStack().getSpawnerData();
            Arconix.pl().getApi().packetLibrary.getParticleManager().broadcastParticle(particleLocation, x, y, z, 0, spawnerData.getSpawnerSpawnParticle().getEffect(), spawnerData.getParticleAmount().getSpawnerSpawn());

            for (SpawnerStack stack : spawner.getSpawnerStacks()) {
                stack.getSpawnerData().spawn(spawner, stack);
                //stack.getSpawnerData().spawn(location); // This method will spawn all methods at once.
            }
            Bukkit.getScheduler().runTaskLater(instance, spawner::updateDelay, 1);
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        try {
            if (EpicSpawners.getInstance().getConfig().getBoolean("entity.Hostile Mobs Attack Second")) {
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

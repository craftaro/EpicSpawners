package com.songoda.epicspawners.Events;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.Entity.EPlayer;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Spawners.Spawner;
import com.songoda.epicspawners.Utils.Debugger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

/**
 * Created by songoda on 2/25/2017.
 */
public class EntityListeners implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlow(EntityExplodeEvent e) {
        try {
            if (!e.isCancelled()) {
                List<Block> destroyed = e.blockList();
                for (Block b : destroyed) {
                    if (b.getType() == Material.MOB_SPAWNER) {
                        Location spawnLocation = b.getLocation();
                        if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Prevent Spawners From Exploding"))
                            e.blockList().remove(b);
                        else if (e.getEntity() instanceof Creeper && EpicSpawners.getInstance().getConfig().getBoolean("Spawner Drops.Drop On Creeper Explosion") || e.getEntity() instanceof TNTPrimed && EpicSpawners.getInstance().getConfig().getBoolean("Spawner Drops.Drop On TNT Explosion")) {
                            int multi = 0;

                            Spawner spawner = new Spawner(b);
                            boolean canDrop = spawner.canBreak();

                            String locationStr = Arconix.pl().getApi().serialize().serializeLocation(b);
                            if (EpicSpawners.getInstance().dataFile.getConfig().getInt("data.spawner." + locationStr) != 0) {
                                multi = EpicSpawners.getInstance().dataFile.getConfig().getInt("data.spawner." + locationStr);
                                EpicSpawners.getInstance().dataFile.getConfig().set("data.spawner." + locationStr, null);
                            }

                            String type = spawner.spawnedType;
                            String chance = "";
                            if (e.getEntity() instanceof Creeper && EpicSpawners.getInstance().getConfig().getBoolean("Spawner Drops.Drop On Creeper Explosion"))
                                chance = EpicSpawners.getInstance().getConfig().getString("Spawner Drops.Chance On TNT Explosion");
                            else if (e.getEntity() instanceof TNTPrimed && EpicSpawners.getInstance().getConfig().getBoolean("Spawner Drops.Drop On TNT Explosion"))
                                chance = EpicSpawners.getInstance().getConfig().getString("Spawner Drops.Chance On Creeper Explosion");
                            int ch = Integer.parseInt(chance.replace("%", ""));
                            double rand = Math.random() * 100;
                            if (rand - ch < 0 || ch == 100) {
                                if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(b.getLocation()) + ".type")) {
                                    if (EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(b.getLocation()) + ".type").equals("OMNI")) {
                                        type = "Omni";
                                        multi = 100;
                                    }
                                }
                                if (canDrop)
                                    new EPlayer(null).dropSpawner(spawnLocation, multi, type);
                            }
                        }
                        EpicSpawners.getInstance().holo.processChange(b);
                        Location nloc = spawnLocation.clone();
                        nloc.add(.5, -.4, .5);
                        List<Entity> near = (List<Entity>) nloc.getWorld().getNearbyEntities(nloc, 8, 8, 8);
                        for (Entity ee : near) {
                            if (ee.getLocation().getX() == nloc.getX() && ee.getLocation().getY() == nloc.getY() && ee.getLocation().getZ() == nloc.getZ()) {
                                ee.remove();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        try {
            if (e.getEntity().getKiller() != null) {
                if (e.getEntity().getKiller() instanceof Player) {
                    Player p = e.getEntity().getKiller();
                    if (!EpicSpawners.getInstance().dataFile.getConfig().getBoolean("data.Entities." + e.getEntity().getUniqueId()) || EpicSpawners.getInstance().getConfig().getBoolean("Spawner Drops.Count Unnatural Kills Towards Spawner Drop")) {
                        new EPlayer(p).plus(e.getEntity(), 1);
                    }
                }
            }
            EpicSpawners.getInstance().dataFile.getConfig().set("data.Entities." + e.getEntity().getUniqueId(), null);
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    @EventHandler
    public void onDeath(CreatureSpawnEvent e) {
        try {
            if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL &&
                    e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CHUNK_GEN) {
                EpicSpawners.getInstance().dataFile.getConfig().set("data.Entities." + e.getEntity().getUniqueId(), true);
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}

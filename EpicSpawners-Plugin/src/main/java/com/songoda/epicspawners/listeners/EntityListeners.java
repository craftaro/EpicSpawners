package com.songoda.epicspawners.listeners;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.utils.Debugger;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by songoda on 2/25/2017.
 */
public class EntityListeners implements Listener {

    private final EpicSpawnersPlugin instance;

    public EntityListeners(EpicSpawnersPlugin instance) {
        this.instance = instance;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlow(EntityExplodeEvent e) {
        try {
            if (!e.isCancelled()) {
                List<Block> destroyed = e.blockList();
                Iterator<Block> it = destroyed.iterator();
                List<Block> toCancel = new ArrayList<>();
                while (it.hasNext()) {
                    Block b = it.next();
                    if (b.getType() != Material.SPAWNER) continue;

                    Location spawnLocation = b.getLocation();

                    if (instance.getConfig().getBoolean("Main.Prevent Spawners From Exploding"))
                        toCancel.add(b);
                    else if (e.getEntity() instanceof Creeper && instance.getConfig().getBoolean("Spawner Drops.Drop On Creeper Explosion")
                            || e.getEntity() instanceof TNTPrimed && instance.getConfig().getBoolean("Spawner Drops.Drop On TNT Explosion")) {

                        Spawner spawner = instance.getSpawnerManager().getSpawnerFromWorld(b.getLocation());

                        String chance = "";
                        if (e.getEntity() instanceof Creeper && instance.getConfig().getBoolean("Spawner Drops.Drop On Creeper Explosion"))
                            chance = instance.getConfig().getString("Spawner Drops.Chance On TNT Explosion");
                        else if (e.getEntity() instanceof TNTPrimed && instance.getConfig().getBoolean("Spawner Drops.Drop On TNT Explosion"))
                            chance = instance.getConfig().getString("Spawner Drops.Chance On Creeper Explosion");
                        int ch = Integer.parseInt(chance.replace("%", ""));
                        double rand = Math.random() * 100;
                        if (rand - ch < 0 || ch == 100) {
                            for (SpawnerStack stack : spawner.getSpawnerStacks()) {
                                ItemStack item = stack.getSpawnerData().toItemStack(1, stack.getStackSize());
                                spawnLocation.getWorld().dropItemNaturally(spawnLocation.clone().add(.5, 0, .5), item);
                            }
                            instance.getSpawnerManager().removeSpawnerFromWorld(spawnLocation);
                            instance.getHologramHandler().despawn(spawnLocation.getBlock());
                            instance.getAppearanceHandler().removeDisplayItem(spawner);
                        }
                    }
                    instance.getHologramHandler().processChange(b);
                    Location nloc = spawnLocation.clone();
                    nloc.add(.5, -.4, .5);
                    List<Entity> near = (List<Entity>) nloc.getWorld().getNearbyEntities(nloc, 8, 8, 8);
                    for (Entity ee : near) {
                        if (ee.getLocation().getX() == nloc.getX() && ee.getLocation().getY() == nloc.getY() && ee.getLocation().getZ() == nloc.getZ()) {
                            ee.remove();
                        }
                    }

                }

                for (Block block : toCancel) {
                    e.blockList().remove(block);
                }

            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        try {
            if (event.getEntity().hasMetadata("ES")) {
                SpawnerData spawnerData = instance.getSpawnerManager().getSpawnerData(event.getEntity().getMetadata("ES").get(0).asString());
                if (!spawnerData.getEntityDroppedItems().isEmpty()) {
                    event.getDrops().clear();
                }
                for (ItemStack itemStack : spawnerData.getEntityDroppedItems()) {
                    event.getDrops().add(itemStack);
                }
            }
            if (event.getEntity().getKiller() == null) return;
            Player player = event.getEntity().getKiller();
            if (!player.hasPermission("epicspawners.Killcounter") || !instance.getConfig().getBoolean("Spawner Drops.Allow Killing Mobs To Drop Spawners"))
                return;

            if (!instance.getSpawnManager().isNaturalSpawn(event.getEntity().getUniqueId()) && !instance.getConfig().getBoolean("Spawner Drops.Count Unnatural Kills Towards Spawner Drop"))
                return;

            int amt = instance.getPlayerActionManager().getPlayerAction(player).addKilledEntity(event.getEntityType());
            int goal = instance.getConfig().getInt("Spawner Drops.Kills Needed for Drop");

            SpawnerData spawnerData = instance.getSpawnerManager().getSpawnerData(event.getEntityType());

            if (!spawnerData.isActive()) return;

            int customGoal = spawnerData.getKillGoal();
            if (customGoal != 0) goal = customGoal;

            if (instance.getConfig().getInt("Spawner Drops.Alert Every X Before Drop") != 0
                    && amt % instance.getConfig().getInt("Spawner Drops.Alert Every X Before Drop") == 0
                    && amt != goal) {
                Arconix.pl().getApi().packetLibrary.getActionBarManager().sendActionBar(player, instance.getLocale().getMessage("event.goal.alert", goal - amt, spawnerData.getIdentifyingName()));
            }

            if (amt % goal == 0) {
                ItemStack item = spawnerData.toItemStack();
                event.getEntity().getLocation().getWorld().dropItemNaturally(event.getEntity().getLocation(), item);
                Arconix.pl().getApi().packetLibrary.getActionBarManager().sendActionBar(player, instance.getLocale().getMessage("event.goal.reached", spawnerData.getIdentifyingName()));
            }

        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}

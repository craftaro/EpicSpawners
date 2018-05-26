package com.songoda.epicspawners.listeners;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.events.SpawnerBreakEvent;
import com.songoda.epicspawners.api.events.SpawnerPlaceEvent;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.object.ESpawner;
import com.songoda.epicspawners.spawners.object.ESpawnerManager;
import com.songoda.epicspawners.spawners.object.ESpawnerStack;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Created by songoda on 2/25/2017.
 */
public class BlockListeners implements Listener {

    private final EpicSpawnersPlugin instance;

    public BlockListeners(EpicSpawnersPlugin instance) {
        this.instance = instance;

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent e) {
        try {
            if (doLiquidRepel(e.getBlock(), false)) e.setCancelled(true);
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    private boolean doLiquidRepel(Block block, boolean from) {
        int radius = instance.getConfig().getInt("Main.Spawner Repel Liquid Radius");
        if (radius == 0) return false;
        if (!from) radius++;
        int bx = block.getX();
        int by = block.getY();
        int bz = block.getZ();
        for (int fx = -radius; fx <= radius; fx++) {
            for (int fy = -radius; fy <= radius; fy++) {
                for (int fz = -radius; fz <= radius; fz++) {
                    Block b2 = block.getWorld().getBlockAt(bx + fx, by + fy, bz + fz);

                    if (from) {
                        if ((b2.getType().equals(Material.STATIONARY_LAVA) || b2.getType().equals(Material.LAVA))
                                || (b2.getType().equals(Material.STATIONARY_WATER) || b2.getType().equals(Material.WATER))) {
                            b2.setType(Material.AIR);
                        }
                    } else {
                        if (b2.getType().equals(Material.MOB_SPAWNER)) {
                            return true;
                        }
                    }

                }
            }
        }
        return false;
    }

    public boolean doForceCombine(Player player, ESpawner placedSpawner) {
        if (instance.getConfig().getInt("Main.Force Combine Radius") == 0) return false;

        for (Spawner spawner : instance.getSpawnerManager().getSpawners()) {
            if (spawner.getLocation().getWorld() == null
                    || spawner.getLocation().getWorld() != placedSpawner.getLocation().getWorld()
                    || spawner.getLocation() == placedSpawner.getLocation()
                    || spawner.getLocation().distance(placedSpawner.getLocation()) > instance.getConfig().getInt("Main.Force Combine Radius")
                    || !instance.getConfig().getBoolean("Main.OmniSpawners Enabled") && spawner.getSpawnerStacks().size() != 1) {
                continue;
            }

            if (instance.getConfig().getBoolean("Main.Deny Place On Force Combine"))
                player.sendMessage(instance.getLocale().getMessage("event.block.forcedeny"));
            else if (spawner.stack(player, placedSpawner.getFirstStack().getSpawnerData(), placedSpawner.getSpawnerDataCount()))
                player.sendMessage(instance.getLocale().getMessage("event.block.mergedistance"));
            return true;
        }
        return false;
    }

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent event) {
        //We are ignoring canceled inside the event so that it will still remove holograms when the event is canceled.
        if (!event.isCancelled()) {
            if (event.getBlock().getType() != Material.MOB_SPAWNER) return;

            Location location = event.getBlock().getLocation();
            ESpawner spawner = new ESpawner(event.getBlock().getLocation());

            SpawnerData spawnerData = instance.getSpawnerDataFromItem(event.getItemInHand());
            int spawnerStackSize = instance.getStackSizeFromItem(event.getItemInHand());
            spawner.addSpawnerStack(new ESpawnerStack(spawnerData, spawnerStackSize));
            
            Player player = event.getPlayer();
            
            SpawnerPlaceEvent placeEvent = new SpawnerPlaceEvent(player, spawner);
            Bukkit.getPluginManager().callEvent(placeEvent);
            if (event.isCancelled()) {
                return;
            }
            
            doLiquidRepel(event.getBlock(), true);

            if (instance.getBlacklistHandler().isBlacklisted(player, true)) {
                event.setCancelled(true);
            }

            if (doForceCombine(player, spawner)) {
                event.setCancelled(true);
                return;
            }

            if (spawnerData == null) {
                player.sendMessage(instance.getLocale().getMessage("event.block.broken"));
                event.setCancelled(true);
                return;
            }

            instance.getSpawnerManager().addSpawnerToWorld(location, spawner);

            if (instance.getConfig().getBoolean("Main.Alerts On Place And Break"))
                player.sendMessage(instance.getLocale().getMessage("event.block.place", Methods.compileName(spawnerData.getIdentifyingName(), spawner.getFirstStack().getStackSize(), false)));

            try {
                spawner.getCreatureSpawner().setSpawnedType(EntityType.valueOf(spawnerData.getIdentifyingName().toUpperCase().replace(" ", "_")));
            } catch (Exception ex) {
                spawner.getCreatureSpawner().setSpawnedType(EntityType.valueOf("PIG"));
            }
            spawner.getCreatureSpawner().update();

            spawner.setPlacedBy(player);

            instance.getHologramHandler().processChange(event.getBlock());

            instance.getHologramHandler().updateHologram(spawner);
            instance.getAppearanceHandler().updateDisplayItem(spawner, spawnerData);

            return;
        }

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> instance.getHologramHandler().processChange(event.getBlock()), 10L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    //Leave this on high or WorldGuard will not work...
    public void onBlockBreak(BlockBreakEvent event) {
        //We are ignoring canceled inside the event so that it will still remove holograms when the event is canceled.
        if (!event.isCancelled()) {

            Player player = event.getPlayer();

            if (event.getBlock().getType() != Material.MOB_SPAWNER) return;

            if (instance.getBlacklistHandler().isBlacklisted(event.getPlayer(), true)) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(true);

            Location location = event.getBlock().getLocation();

            if (!instance.getSpawnerManager().isSpawner(location)) {
                ESpawner spawner = new ESpawner(location);

                spawner.addSpawnerStack(new ESpawnerStack(instance.getSpawnerManager().getSpawnerData(spawner.getCreatureSpawner().getSpawnedType())));
                instance.getSpawnerManager().addSpawnerToWorld(location, spawner);
            }

            Spawner spawner = instance.getSpawnerManager().getSpawnerFromWorld(location);

            SpawnerBreakEvent breakEvent = new SpawnerBreakEvent(player, spawner);
            Bukkit.getPluginManager().callEvent(breakEvent);
            if (breakEvent.isCancelled()) {
                return;
            }

            int currentStackSize = spawner.getSpawnerDataCount();

            if (spawner.getFirstStack().getSpawnerData() == null) {
                event.getBlock().setType(Material.AIR);
                System.out.println("A corrupted spawner has been removed as its Type no longer exists.");
                instance.getSpawnerManager().removeSpawnerFromWorld(location);
                instance.getHologramHandler().updateHologram(spawner);
                instance.getAppearanceHandler().removeDisplayItem(spawner);
                return;
            }

            String type = spawner.getFirstStack().getSpawnerData().getIdentifyingName();

            boolean naturalOnly = instance.getConfig().getBoolean("Main.Only Charge Natural Spawners");

            if (spawner.getFirstStack().getSpawnerData().getPickupCost() != 0 && (!naturalOnly || spawner.getPlacedBy() == null)) {
                if (!((ESpawnerManager)instance.getSpawnerManager()).hasCooldown(spawner)) {
                    player.sendMessage(instance.getLocale().getMessage("event.block.chargebreak", spawner.getFirstStack().getSpawnerData().getPickupCost()));
                    ((ESpawnerManager)instance.getSpawnerManager()).addCooldown(spawner);
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> ((ESpawnerManager)instance.getSpawnerManager()).removeCooldown(spawner), 300L);
                    event.setCancelled(true);
                    return;
                }

                ((ESpawnerManager)instance.getSpawnerManager()).removeCooldown(spawner);
                //ToDO: Do this somewhere else.
                double cost = spawner.getFirstStack().getSpawnerData().getPickupCost();
                RegisteredServiceProvider<Economy> rsp = instance.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
                if (econ.has(player, cost)) {
                    econ.withdrawPlayer(player, cost);
                } else {
                    player.sendMessage(instance.getLocale().getMessage("event.block.cannotbreak"));
                    event.setCancelled(true);
                    return;
                }
            }

            if (spawner.unstack(event.getPlayer())) {
                if (instance.getConfig().getBoolean("Main.Alerts On Place And Break")) {
                    if (spawner.getSpawnerStacks().size() != 0) {
                        player.sendMessage(instance.getLocale().getMessage("event.downgrade.success", Integer.toString(spawner.getSpawnerDataCount())));
                    } else {
                        player.sendMessage(instance.getLocale().getMessage("event.block.break", Methods.compileName(type, currentStackSize, true)));
                    }
                }
            }

            instance.getHologramHandler().updateHologram(spawner);
            instance.getAppearanceHandler().removeDisplayItem(spawner);

            return;
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> instance.getHologramHandler().processChange(event.getBlock()), 10L);
    }
}
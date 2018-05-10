package com.songoda.epicspawners.listeners;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.object.Spawner;
import com.songoda.epicspawners.spawners.object.SpawnerData;
import com.songoda.epicspawners.spawners.object.SpawnerStack;
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

    private final EpicSpawners instance;

    public BlockListeners(EpicSpawners instance) {
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

    public boolean doForceCombine(Player player, Spawner placedSpawner) {
        if (instance.getConfig().getInt("Main.Force Combine Radius") == 0) return false;

        for (Spawner spawner : instance.getSpawnerManager().getSpawnersInWorld().values()) {
            if (spawner.getLocation().getWorld() == null
                    || spawner.getLocation().getWorld() != placedSpawner.getLocation().getWorld()
                    || spawner.getLocation() == placedSpawner.getLocation()
                    || spawner.getLocation().distance(placedSpawner.getLocation()) > instance.getConfig().getInt("Main.Force Combine Radius")
                    || !instance.getConfig().getBoolean("Main.OmniSpawners Enabled") && spawner.getSpawnerStacks().size() != 1) {
                continue;
            }

            if (instance.getConfig().getBoolean("Main.Deny Place On Force Combine"))
                player.sendMessage(instance.getLocale().getMessage("event.block.forcedeny"));
            else if (spawner.stackFinal(player, placedSpawner.getName(), placedSpawner.getSpawnerMultiplier()))
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

            doLiquidRepel(event.getBlock(), true);

            if (instance.getBlacklistHandler().isBlacklisted(event.getPlayer(), true)) {
                event.setCancelled(true);
            }

            Location location = event.getBlock().getLocation();

            Spawner spawner = new Spawner(event.getBlock().getLocation());

            String spawnerName = instance.getApi().getIType(event.getItemInHand());

            int spawnerStackSize = instance.getApi().getIMulti(event.getItemInHand());

            SpawnerData spawnerData = instance.getSpawnerManager().getSpawnerType(Methods.getTypeFromString(spawnerName).toLowerCase());

            spawner.addSpawnerType(new SpawnerStack(spawnerData, spawnerStackSize));

            if (doForceCombine(event.getPlayer(), spawner)) {
                event.setCancelled(true);
                return;
            }

            if (spawnerData == null) {
                event.getPlayer().sendMessage(instance.getLocale().getMessage("event.block.broken"));
                event.setCancelled(true);
                return;
            }

            instance.getSpawnerManager().addSpawnerToWorld(location, spawner);

            if (instance.getConfig().getBoolean("Main.Alerts On Place And Break"))
                event.getPlayer().sendMessage(instance.getLocale().getMessage("event.block.place", Methods.compileName(spawnerData.getName(), spawner.getFirstStack().getStackSize(), false)));

            try {
                spawner.getCreatureSpawner().setSpawnedType(EntityType.valueOf(spawnerName.toUpperCase().replace(" ", "_")));
            } catch (Exception ex) {
                spawner.getCreatureSpawner().setSpawnedType(EntityType.valueOf("DROPPED_ITEM"));
            }
            spawner.getCreatureSpawner().update();

            spawner.setPlacedBy(event.getPlayer());

            instance.getHologramHandler().processChange(event.getBlock());

            instance.getHologramHandler().updateHologram(spawner);
            instance.getApi().updateDisplayItem(spawner, spawnerData);

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
            }

            event.setCancelled(true);

            Location location = event.getBlock().getLocation();

            if (!instance.getSpawnerManager().isSpawner(location)) {
                Spawner spawner = new Spawner(location);

                spawner.addSpawnerType(new SpawnerStack(instance.getSpawnerManager().getSpawnerType(Methods.getType(spawner.getCreatureSpawner().getSpawnedType())), 1));
                instance.getSpawnerManager().addSpawnerToWorld(location, spawner);
            }

            Spawner spawner = instance.getSpawnerManager().getSpawnerFromWorld(location);

            int currentStackSize = spawner.getSpawnerMultiplier();

            if (spawner.getFirstStack().getSpawnerData() == null) {
                event.getBlock().setType(Material.AIR);
                System.out.println("A corrupted spawner has been removed as its type no longer exists.");
                instance.getSpawnerManager().removeSpawnerFromWorld(location);
                instance.getHologramHandler().updateHologram(spawner);
                instance.getApi().removeDisplayItem(spawner);
                return;
            }

            String type = spawner.getFirstStack().getSpawnerData().getName();

            boolean naturalOnly = instance.getConfig().getBoolean("Main.Only Charge Natural Spawners");

            if (spawner.getFirstStack().getSpawnerData().getPickupCost() != 0 && (!naturalOnly || spawner.getPlacedBy() == null)) {
                if (!instance.getSpawnerManager().hasCooldown(spawner)) {
                    player.sendMessage(instance.getLocale().getMessage("event.block.chargebreak", spawner.getFirstStack().getSpawnerData().getPickupCost()));
                    instance.getSpawnerManager().addCooldown(spawner);
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> instance.getSpawnerManager().removeCooldown(spawner), 300L);
                    event.setCancelled(true);
                    return;
                } else {
                    instance.getSpawnerManager().removeCooldown(spawner);
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
            }

            if (spawner.unStack(event.getPlayer())) {
                if (instance.getConfig().getBoolean("Main.Alerts On Place And Break")) {
                    if (spawner.getSpawnerStacks().size() != 0) {
                        player.sendMessage(instance.getLocale().getMessage("event.downgrade.success", Integer.toString(spawner.getSpawnerMultiplier())));
                    } else {
                        player.sendMessage(instance.getLocale().getMessage("event.block.break", Methods.compileName(type, currentStackSize, true)));
                    }
                }
            }

            instance.getHologramHandler().updateHologram(spawner);
            instance.getApi().removeDisplayItem(spawner);

            return;
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> instance.getHologramHandler().processChange(event.getBlock()), 10L);
    }
}
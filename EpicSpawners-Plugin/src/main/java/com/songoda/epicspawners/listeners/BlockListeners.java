package com.songoda.epicspawners.listeners;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.References;
import com.songoda.epicspawners.api.events.SpawnerBreakEvent;
import com.songoda.epicspawners.api.events.SpawnerPlaceEvent;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.ESpawner;
import com.songoda.epicspawners.spawners.spawner.ESpawnerManager;
import com.songoda.epicspawners.spawners.spawner.ESpawnerStack;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
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
                        if ((b2.getType().equals(Material.LAVA) || b2.getType().equals(Material.LAVA))
                                || (b2.getType().equals(Material.WATER) || b2.getType().equals(Material.WATER))) {
                            b2.setType(Material.AIR);
                        }
                    } else {
                        if (b2.getType().equals(Material.SPAWNER)) {
                            return true;
                        }
                    }

                }
            }
        }
        return false;
    }

    private boolean doForceCombine(Player player, ESpawner placedSpawner) {
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
                player.sendMessage(References.getPrefix() + instance.getLocale().getMessage("event.block.forcedeny"));
            else if (spawner.stack(player, placedSpawner.getFirstStack().getSpawnerData(), placedSpawner.getSpawnerDataCount()))
                player.sendMessage(References.getPrefix() + instance.getLocale().getMessage("event.block.mergedistance"));
            return true;
        }
        return false;
    }

    private int maxSpawners(Player player) {
        int limit = -1;
        for (PermissionAttachmentInfo permissionAttachmentInfo : player.getEffectivePermissions()) {
            if (!permissionAttachmentInfo.getPermission().toLowerCase().startsWith("epicspawners.limit")) continue;
            limit = Integer.parseInt(permissionAttachmentInfo.getPermission().split("\\.")[2]);
        }
        if (limit == -1) limit = instance.getConfig().getInt("Main.Max Spawners Per Player");
        return limit;
    }

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent event) {
        //We are ignoring canceled inside the event so that it will still remove holograms when the event is canceled.
        if (!event.isCancelled()) {
            if (event.getBlock().getType() != Material.SPAWNER
                    || ((CreatureSpawner) event.getBlock().getState()).getSpawnedType() == EntityType.FIREWORK) return;


            Location location = event.getBlock().getLocation();
            ESpawner spawner = new ESpawner(event.getBlock().getLocation());

            SpawnerData spawnerData = instance.getSpawnerDataFromItem(event.getItemInHand());
            int spawnerStackSize = instance.getStackSizeFromItem(event.getItemInHand());
            spawner.addSpawnerStack(new ESpawnerStack(spawnerData, spawnerStackSize));

            Player player = event.getPlayer();

            if (!instance.getHookManager().canBuild(player, location)) {
                event.setCancelled(true);
                return;
            }

            doLiquidRepel(event.getBlock(), true);

            if (instance.getBlacklistHandler().isBlacklisted(player, true)) {
                event.setCancelled(true);
                return;
            }

            if (!player.hasPermission("epicspawners.place." + spawnerData.getIdentifyingName().replace(" ", "_"))) {
                event.setCancelled(true);
                return;
            }

            if (doForceCombine(player, spawner)) {
                event.setCancelled(true);
                return;
            }

            int amountPlaced = instance.getSpawnerManager().getAmountPlaced(player);
            int maxSpawners = maxSpawners(player);

            if (maxSpawners != -1 && amountPlaced > maxSpawners) {
                player.sendMessage(instance.getLocale().getMessage("event.spawner.toomany", maxSpawners));
                event.setCancelled(true);
                return;
            }

            SpawnerPlaceEvent placeEvent = new SpawnerPlaceEvent(player, spawner);
            Bukkit.getPluginManager().callEvent(placeEvent);
            if (placeEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            instance.getSpawnerManager().addSpawnerToWorld(location, spawner);

            if (instance.getConfig().getBoolean("Main.Alerts On Place And Break"))
                player.sendMessage(References.getPrefix() + instance.getLocale().getMessage("event.block.place", Methods.compileName(spawnerData, spawner.getFirstStack().getStackSize(), false)));

            CreatureSpawner creatureSpawner = spawner.getCreatureSpawner();
            if (creatureSpawner == null) return;

            try {
                creatureSpawner.setSpawnedType(EntityType.valueOf(spawnerData.getIdentifyingName().toUpperCase().replace(" ", "_")));
            } catch (Exception ex) {
                creatureSpawner.setSpawnedType(EntityType.DROPPED_ITEM);
            }
            creatureSpawner.setDelay(1);
            creatureSpawner.update();

            spawner.setPlacedBy(player);

            if (instance.getHologram() != null) {
                instance.getHologram().processChange(event.getBlock());
                instance.getHologram().add(spawner);
            }
            instance.getAppearanceHandler().updateDisplayItem(spawner, spawnerData);

            return;
        }

        if (instance.getHologram() != null)
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> instance.getHologram().processChange(event.getBlock()), 10L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    //Leave this on high or WorldGuard will not work...
    public void onBlockBreak(BlockBreakEvent event) {
        //We are ignoring canceled inside the event so that it will still remove holograms when the event is canceled.
        if (!event.isCancelled()) {

            Player player = event.getPlayer();

            if (event.getBlock().getType() != Material.SPAWNER
                    || ((CreatureSpawner) event.getBlock().getState()).getSpawnedType() == EntityType.FIREWORK) return;

            if (instance.getBlacklistHandler().isBlacklisted(event.getPlayer(), true)) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(true);

            Location location = event.getBlock().getLocation();

            if (!instance.getSpawnerManager().isSpawner(location)) {
                ESpawner spawner = new ESpawner(location);

                CreatureSpawner creatureSpawner = spawner.getCreatureSpawner();
                if (creatureSpawner == null) return;

                spawner.addSpawnerStack(new ESpawnerStack(instance.getSpawnerManager().getSpawnerData(creatureSpawner.getSpawnedType())));
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
                if (instance.getHologram() != null)
                    instance.getHologram().update(spawner);
                instance.getAppearanceHandler().removeDisplayItem(spawner);
                return;
            }

            boolean naturalOnly = instance.getConfig().getBoolean("Main.Only Charge Natural Spawners");

            if (spawner.getFirstStack().getSpawnerData().getPickupCost() != 0 && (!naturalOnly || spawner.getPlacedBy() == null)) {
                if (!((ESpawnerManager) instance.getSpawnerManager()).hasCooldown(spawner)) {
                    player.sendMessage(References.getPrefix() + instance.getLocale().getMessage("event.block.chargebreak", spawner.getFirstStack().getSpawnerData().getPickupCost()));
                    ((ESpawnerManager) instance.getSpawnerManager()).addCooldown(spawner);
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> ((ESpawnerManager) instance.getSpawnerManager()).removeCooldown(spawner), 300L);
                    event.setCancelled(true);
                    return;
                }

                ((ESpawnerManager) instance.getSpawnerManager()).removeCooldown(spawner);
                //ToDO: Do this somewhere else.
                double cost = spawner.getFirstStack().getSpawnerData().getPickupCost();
                RegisteredServiceProvider<Economy> rsp = instance.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
                if (econ.has(player, cost)) {
                    econ.withdrawPlayer(player, cost);
                } else {
                    player.sendMessage(References.getPrefix() + instance.getLocale().getMessage("event.block.cannotbreak"));
                    event.setCancelled(true);
                    return;
                }
            }

            SpawnerData firstData = spawner.getFirstStack().getSpawnerData();

            if (spawner.unstack(event.getPlayer())) {
                if (instance.getConfig().getBoolean("Main.Alerts On Place And Break")) {
                    if (spawner.getSpawnerStacks().size() != 0) {
                        player.sendMessage(References.getPrefix() + instance.getLocale().getMessage("event.downgrade.success", Integer.toString(spawner.getSpawnerDataCount())));
                    } else {
                        player.sendMessage(References.getPrefix() + instance.getLocale().getMessage("event.block.break", Methods.compileName(firstData, currentStackSize, true)));
                    }
                }
            }

            if (instance.getHologram() != null)
                instance.getHologram().update(spawner);

            instance.getAppearanceHandler().removeDisplayItem(spawner);

            return;
        }
        if (event.getBlock() == null || instance.getHologram() == null) return;

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> instance.getHologram().processChange(event.getBlock()), 10L);
    }
}
package com.songoda.epicspawners.listeners;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.References;
import com.songoda.epicspawners.api.events.SpawnerBreakEvent;
import com.songoda.epicspawners.api.events.SpawnerChangeEvent;
import com.songoda.epicspawners.api.events.SpawnerPlaceEvent;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.ServerVersion;
import com.songoda.epicspawners.utils.settings.Setting;
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

/**
 * Created by songoda on 2/25/2017.
 */
public class BlockListeners implements Listener {

    private final EpicSpawners plugin;

    public BlockListeners(EpicSpawners plugin) {
        this.plugin = plugin;

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent e) {
            if (doLiquidRepel(e.getBlock(), false)) e.setCancelled(true);
    }

    private boolean doLiquidRepel(Block block, boolean from) {
        int radius = plugin.getConfig().getInt("Main.Spawner Repel Liquid Radius");
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
                        if (b2.getType().equals(plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean doForceCombine(Player player, Spawner placedSpawner) {
        if (plugin.getConfig().getInt("Main.Force Combine Radius") == 0) return false;

        for (Spawner spawner : plugin.getSpawnerManager().getSpawners()) {
            if (spawner.getLocation().getWorld() == null
                    || spawner.getLocation().getWorld() != placedSpawner.getLocation().getWorld()
                    || spawner.getLocation() == placedSpawner.getLocation()
                    || spawner.getLocation().distance(placedSpawner.getLocation()) > plugin.getConfig().getInt("Main.Force Combine Radius")
                    || !plugin.getConfig().getBoolean("Main.OmniSpawners Enabled") && spawner.getSpawnerStacks().size() != 1) {
                continue;
            }

            if (plugin.getConfig().getBoolean("Main.Deny Place On Force Combine"))
                player.sendMessage(References.getPrefix() + plugin.getLocale().getMessage("event.block.forcedeny"));
            else if (spawner.stack(player, placedSpawner.getFirstStack().getSpawnerData(), placedSpawner.getSpawnerDataCount()))
                player.sendMessage(References.getPrefix() + plugin.getLocale().getMessage("event.block.mergedistance"));
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
        if (limit == -1) limit = plugin.getConfig().getInt("Main.Max Spawners Per Player");
        return limit;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerPlace(BlockPlaceEvent event) {
        //We are ignoring canceled inside the event so that it will still remove holograms when the event is canceled.
        if (!event.isCancelled()) {
            if (event.getBlock().getType() != (plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))
                    || ((CreatureSpawner) event.getBlock().getState()).getSpawnedType() == EntityType.FIREWORK) return;


            Location location = event.getBlock().getLocation();
            Spawner spawner = new Spawner(event.getBlock().getLocation());

            SpawnerData spawnerData = plugin.getSpawnerManager().getSpawnerData(event.getItemInHand());
            if (spawnerData == null) return;

            int spawnerStackSize = Methods.getStackSizeFromItem(event.getItemInHand());
            spawner.addSpawnerStack(new SpawnerStack(spawnerData, spawnerStackSize));

            Player player = event.getPlayer();

            doLiquidRepel(event.getBlock(), true);


            if (plugin.getBlacklistHandler().isBlacklisted(player, true)
                    || !player.hasPermission("epicspawners.place." + spawnerData.getIdentifyingName().replace(" ", "_"))
                    || doForceCombine(player, spawner)) {
                event.setCancelled(true);
                return;
            }

            int amountPlaced = plugin.getSpawnerManager().getAmountPlaced(player);
            int maxSpawners = maxSpawners(player);

            if (maxSpawners != -1 && amountPlaced > maxSpawners) {
                player.sendMessage(plugin.getLocale().getMessage("event.spawner.toomany", maxSpawners));
                event.setCancelled(true);
                return;
            }

            SpawnerPlaceEvent placeEvent = new SpawnerPlaceEvent(player, spawner);
            Bukkit.getPluginManager().callEvent(placeEvent);
            if (placeEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            plugin.getSpawnerManager().addSpawnerToWorld(location, spawner);

            if (plugin.getConfig().getBoolean("Main.Alerts On Place And Break"))
                player.sendMessage(References.getPrefix() + plugin.getLocale().getMessage("event.block.place", Methods.compileName(spawnerData, spawner.getFirstStack().getStackSize(), false)));

            CreatureSpawner creatureSpawner = spawner.getCreatureSpawner();
            if (creatureSpawner == null) return;

            try {
                creatureSpawner.setSpawnedType(EntityType.valueOf(spawnerData.getIdentifyingName().toUpperCase().replace(" ", "_")));
            } catch (Exception ex) {
                creatureSpawner.setSpawnedType(EntityType.EGG);
            }
            creatureSpawner.setDelay(1);
            creatureSpawner.update();

            spawner.setPlacedBy(player);

            if (plugin.getHologram() != null) {
                plugin.getHologram().processChange(event.getBlock());
                plugin.getHologram().add(spawner);
            }
            plugin.getAppearanceTask().updateDisplayItem(spawner, spawnerData);

            return;
        }

        if (plugin.getHologram() != null)
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.getHologram().processChange(event.getBlock()), 10L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    //Leave this on high or WorldGuard will not work...
    public void onBlockBreak(BlockBreakEvent event) {
        //We are ignoring canceled inside the event so that it will still remove holograms when the event is canceled.
        if (!event.isCancelled()) {

            Player player = event.getPlayer();

            if (event.getBlock().getType() != (plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))
                    || ((CreatureSpawner) event.getBlock().getState()).getSpawnedType() == EntityType.FIREWORK) return;

            if (plugin.getBlacklistHandler().isBlacklisted(event.getPlayer(), true)) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(true);

            Location location = event.getBlock().getLocation();

            if (!plugin.getSpawnerManager().isSpawner(location)) {
                Spawner spawner = new Spawner(location);

                CreatureSpawner creatureSpawner = spawner.getCreatureSpawner();
                if (creatureSpawner == null) return;

                spawner.addSpawnerStack(new SpawnerStack(plugin.getSpawnerManager().getSpawnerData(creatureSpawner.getSpawnedType())));
                plugin.getSpawnerManager().addSpawnerToWorld(location, spawner);
            }

            Spawner spawner = plugin.getSpawnerManager().getSpawnerFromWorld(location);

            if (spawner.getFirstStack().getSpawnerData() == null) {
                event.getBlock().setType(Material.AIR);
                System.out.println("A corrupted spawner has been removed as its Type no longer exists.");
                plugin.getSpawnerManager().removeSpawnerFromWorld(location);
                if (plugin.getHologram() != null)
                    plugin.getHologram().update(spawner);
                plugin.getAppearanceTask().removeDisplayItem(spawner);
                return;
            }

            int currentStackSize = spawner.getSpawnerDataCount();
            boolean destroyWholeStack = player.isSneaking() && Setting.SNEAK_FOR_STACK.getBoolean() || Setting.ONLY_DROP_STACKED.getBoolean();
            if (currentStackSize - 1 == 0 || destroyWholeStack) {
                SpawnerBreakEvent breakEvent = new SpawnerBreakEvent(player, spawner);
                Bukkit.getPluginManager().callEvent(breakEvent);
                if (breakEvent.isCancelled()) {
                    return;
                }
            } else {
                SpawnerChangeEvent changeEvent = new SpawnerChangeEvent(player, spawner, currentStackSize - 1, currentStackSize);
                Bukkit.getPluginManager().callEvent(changeEvent);
                if (changeEvent.isCancelled()) {
                    return;
                }
            }

            boolean naturalOnly = Setting.ONLY_CHARGE_NATURAL.getBoolean();

            if (spawner.getFirstStack().getSpawnerData().getPickupCost() != 0 && (!naturalOnly || spawner.getPlacedBy() == null)) {
                if (!plugin.getSpawnerManager().hasCooldown(spawner)) {
                    player.sendMessage(References.getPrefix() + plugin.getLocale().getMessage("event.block.chargebreak", spawner.getFirstStack().getSpawnerData().getPickupCost()));
                    plugin.getSpawnerManager().addCooldown(spawner);
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.getSpawnerManager().removeCooldown(spawner), 300L);
                    event.setCancelled(true);
                    return;
                }

                plugin.getSpawnerManager().removeCooldown(spawner);
                double cost = spawner.getFirstStack().getSpawnerData().getPickupCost();

                if (plugin.getEconomy().hasBalance(player, cost)) {
                    plugin.getEconomy().withdrawBalance(player, cost);
                } else {
                    player.sendMessage(References.getPrefix() + plugin.getLocale().getMessage("event.block.cannotbreak"));
                    event.setCancelled(true);
                    return;
                }
            }

            SpawnerData firstData = spawner.getFirstStack().getSpawnerData();

            if (spawner.unstack(event.getPlayer())) {
                if (plugin.getConfig().getBoolean("Main.Alerts On Place And Break")) {
                    if (spawner.getSpawnerStacks().size() != 0) {
                        player.sendMessage(References.getPrefix() + plugin.getLocale().getMessage("event.downgrade.success", Integer.toString(spawner.getSpawnerDataCount())));
                    } else {
                        player.sendMessage(References.getPrefix() + plugin.getLocale().getMessage("event.block.break", Methods.compileName(firstData, currentStackSize, true)));
                    }
                }
            }

            if (plugin.getHologram() != null)
                plugin.getHologram().update(spawner);

            plugin.getAppearanceTask().removeDisplayItem(spawner);

            return;
        }
        if (plugin.getHologram() == null) return;

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.getHologram().processChange(event.getBlock()), 10L);
    }
}
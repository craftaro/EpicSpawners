package com.songoda.epicspawners.listeners;

import com.songoda.core.compatibility.CompatibleHand;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.core.utils.ItemUtils;
import com.songoda.core.utils.PlayerUtils;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.api.events.SpawnerBreakEvent;
import com.songoda.epicspawners.api.events.SpawnerChangeEvent;
import com.songoda.epicspawners.api.events.SpawnerPlaceEvent;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
                        if (b2.getType().equals(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean doForceCombine(Player player, Spawner placedSpawner, BlockPlaceEvent event) {
        if (plugin.getConfig().getInt("Main.Force Combine Radius") == 0) return false;

        for (Spawner spawner : plugin.getSpawnerManager().getSpawners()) {
            if (spawner.getLocation().getWorld() == null
                    || spawner.getLocation().getWorld() != placedSpawner.getLocation().getWorld()
                    || spawner.getLocation() == placedSpawner.getLocation()
                    || spawner.getLocation().distance(placedSpawner.getLocation()) > plugin.getConfig().getInt("Main.Force Combine Radius")
                    || !plugin.getConfig().getBoolean("Main.OmniSpawners Enabled") && spawner.getSpawnerStacks().size() != 1) {
                continue;
            }

            CompatibleHand hand = CompatibleHand.getHand(event);
            if (plugin.getConfig().getBoolean("Main.Deny Place On Force Combine"))
                plugin.getLocale().getMessage("event.block.forcedeny").sendPrefixedMessage(player);
            else if (spawner.stack(player, placedSpawner.getFirstStack().getSpawnerData(), placedSpawner.getSpawnerDataCount(), hand)) {
                plugin.getLocale().getMessage("event.block.mergedistance").sendPrefixedMessage(player);
                if (hand == CompatibleHand.OFF_HAND)
                    ItemUtils.takeActiveItem(player, hand);
            }
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerPlace(BlockPlaceEvent event) {
        // We are ignoring canceled inside the event so that it will still remove holograms when the event is canceled.
        if (!event.isCancelled()) {
            if (event.getBlock().getType() != (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))
                    || ((CreatureSpawner) event.getBlock().getState()).getSpawnedType() == EntityType.FIREWORK) return;


            Location location = event.getBlock().getLocation();
            Spawner spawner = new Spawner(event.getBlock().getLocation());

            SpawnerData spawnerData = plugin.getSpawnerManager().getSpawnerData(event.getItemInHand());
            if (spawnerData == null) return;

            int spawnerStackSize = spawnerData.getStackSize(event.getItemInHand());
            spawner.addSpawnerStack(new SpawnerStack(spawner, spawnerData, spawnerStackSize));

            Player player = event.getPlayer();

            doLiquidRepel(event.getBlock(), true);


            if (plugin.getBlacklistHandler().isBlacklisted(player, true)
                    || !player.hasPermission("epicspawners.place." + spawnerData.getIdentifyingName().replace(" ", "_"))
                    || doForceCombine(player, spawner, event)) {
                event.setCancelled(true);
                return;
            }

            int amountPlaced = plugin.getSpawnerManager().getAmountPlaced(player);
            int maxSpawners = PlayerUtils.getNumberFromPermission(player, "epicspawners.limit",
                    plugin.getConfig().getInt("Main.Max Spawners Per Player"));

            if (maxSpawners != -1 && amountPlaced > maxSpawners) {
                player.sendMessage(plugin.getLocale().getMessage("event.spawner.toomany")
                        .processPlaceholder("amount", maxSpawners).getMessage());
                event.setCancelled(true);
                return;
            }

            CreatureSpawner creatureSpawner = spawner.getCreatureSpawner();
            if (creatureSpawner == null) return;

            SpawnerPlaceEvent placeEvent = new SpawnerPlaceEvent(player, spawner);
            Bukkit.getPluginManager().callEvent(placeEvent);
            if (placeEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }


            plugin.getSpawnerManager().addSpawnerToWorld(location, spawner);

            if (plugin.getConfig().getBoolean("Main.Alerts On Place And Break"))
                plugin.getLocale().getMessage("event.block.place")
                        .processPlaceholder("type", spawnerData.getCompiledDisplayName(spawner.getFirstStack().getStackSize()))
                        .sendPrefixedMessage(player);

            if (player.getGameMode() == GameMode.CREATIVE && Settings.CHARGE_FOR_CREATIVE.getBoolean())
                ItemUtils.takeActiveItem(player, CompatibleHand.getHand(event), 1);

            try {
                creatureSpawner.setSpawnedType(EntityType.valueOf(spawnerData.getIdentifyingName().toUpperCase().replace(" ", "_")));
            } catch (Exception ex) {
                creatureSpawner.setSpawnedType(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9) ? EntityType.EGG : EntityType.DROPPED_ITEM);
            }

            spawner.updateDelay();
            spawner.setPlacedBy(player);
            EpicSpawners.getInstance().getDataManager().createSpawner(spawner);

            plugin.processChange(event.getBlock());
            plugin.updateHologram(spawner);
            plugin.getAppearanceTask().updateDisplayItem(spawner, spawnerData);
            return;
        }

        //ToDo: Probably remove this.
        Bukkit.getServer().
                getScheduler().
                scheduleSyncDelayedTask(plugin, () -> plugin.processChange(event.getBlock()), 10L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    //Leave this on high or WorldGuard will not work...
    public void onBlockBreak(BlockBreakEvent event) {
        //We are ignoring canceled inside the event so that it will still remove holograms when the event is canceled.
        if (!event.isCancelled()) {

            Player player = event.getPlayer();

            if (CompatibleMaterial.getMaterial(event.getBlock().getType()) != CompatibleMaterial.SPAWNER
                    || ((CreatureSpawner) event.getBlock().getState()).getSpawnedType() == EntityType.FIREWORK) return;

            if (plugin.getBlacklistHandler().isBlacklisted(event.getPlayer(), true)) {
                event.setCancelled(true);
                return;
            }

            Location location = event.getBlock().getLocation();

            if (!plugin.getSpawnerManager().isSpawner(location)) {
                Spawner spawner = new Spawner(location);

                CreatureSpawner creatureSpawner = spawner.getCreatureSpawner();
                if (creatureSpawner == null) return;

                spawner.addSpawnerStack(new SpawnerStack(spawner, plugin.getSpawnerManager().getSpawnerData(creatureSpawner.getSpawnedType())));
                plugin.getSpawnerManager().addSpawnerToWorld(location, spawner);
                EpicSpawners.getInstance().getDataManager().createSpawner(spawner);
            }

            Spawner spawner = plugin.getSpawnerManager().getSpawnerFromWorld(location);

            if (spawner.getFirstStack().getSpawnerData() == null) {
                event.getBlock().setType(Material.AIR);
                System.out.println("A corrupted spawner has been removed as its Type no longer exists.");
                spawner.destroy(plugin);
                return;
            }

            int currentStackSize = spawner.getSpawnerDataCount();
            boolean destroyWholeStack = player.isSneaking() && Settings.SNEAK_FOR_STACK.getBoolean() || Settings.ONLY_DROP_STACKED.getBoolean();
            if (currentStackSize - 1 == 0 || destroyWholeStack) {
                SpawnerBreakEvent breakEvent = new SpawnerBreakEvent(player, spawner);
                Bukkit.getPluginManager().callEvent(breakEvent);
                if (breakEvent.isCancelled()) {
                    event.setCancelled(true);
                    return;
                }
            } else {
                SpawnerChangeEvent changeEvent = new SpawnerChangeEvent(player, spawner, currentStackSize - 1, currentStackSize);
                Bukkit.getPluginManager().callEvent(changeEvent);
                if (changeEvent.isCancelled()) {
                    event.setCancelled(true);
                    return;
                }
            }

            boolean naturalOnly = Settings.ONLY_CHARGE_NATURAL.getBoolean();

            if (spawner.getFirstStack().getSpawnerData().getPickupCost() != 0 && (!naturalOnly || spawner.getPlacedBy() == null)) {
                if (!plugin.getSpawnerManager().hasCooldown(spawner)) {
                    plugin.getLocale().getMessage("event.block.chargebreak")
                            .processPlaceholder("cost", EconomyManager.formatEconomy(spawner.getFirstStack().getSpawnerData().getPickupCost()))
                            .sendPrefixedMessage(player);
                    plugin.getSpawnerManager().addCooldown(spawner);
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.getSpawnerManager().removeCooldown(spawner), 300L);
                    event.setCancelled(true);
                    return;
                }

                plugin.getSpawnerManager().removeCooldown(spawner);
                double cost = spawner.getFirstStack().getSpawnerData().getPickupCost();

                if (EconomyManager.hasBalance(player, cost)) {
                    EconomyManager.withdrawBalance(player, cost);
                } else {
                    plugin.getLocale().getMessage("event.block.cannotbreak").sendPrefixedMessage(player);
                    event.setCancelled(true);
                    return;
                }
            }

            SpawnerData firstData = spawner.getFirstStack().getSpawnerData();

            CompatibleHand hand = CompatibleHand.getHand(event);
            if (hand.getItem(player).getType().name().endsWith("PICKAXE") && !player.hasPermission("epicspawners.nopickdamage"))
                hand.damageItem(player, spawner.getFirstStack().getSpawnerData().getPickDamage());

            if (spawner.unstack(event.getPlayer())) {
                if (event.getBlock().getType() != Material.AIR)
                    event.setCancelled(true);

                if (plugin.getConfig().getBoolean("Main.Alerts On Place And Break")) {
                    if (spawner.getSpawnerStacks().size() != 0) {
                        plugin.getLocale().getMessage("event.downgrade.success").processPlaceholder("level", Integer.toString(spawner.getSpawnerDataCount())).sendPrefixedMessage(player);
                    } else {
                        plugin.getLocale().getMessage("event.block.break").processPlaceholder("type", firstData.getCompiledDisplayName(currentStackSize)).sendPrefixedMessage(player);
                    }
                }
            }

            plugin.updateHologram(spawner);

            plugin.getAppearanceTask().removeDisplayItem(spawner);

            return;
        }

        //ToDo: Probably remove this.
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.processChange(event.getBlock()), 10L);
    }
}
package com.craftaro.epicspawners.listeners;

import com.craftaro.core.compatibility.CompatibleHand;
import com.craftaro.core.compatibility.CompatibleMaterial;
import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.core.hooks.EconomyManager;
import com.craftaro.core.utils.ItemUtils;
import com.craftaro.core.utils.PlayerUtils;
import com.craftaro.core.world.SItemStack;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.events.SpawnerBreakEvent;
import com.craftaro.epicspawners.api.events.SpawnerChangeEvent;
import com.craftaro.epicspawners.api.events.SpawnerPlaceEvent;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.epicspawners.spawners.spawner.PlacedSpawnerImpl;
import com.craftaro.epicspawners.spawners.spawner.SpawnerStackImpl;
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
        int radius = Settings.LIQUID_REPEL_RADIUS.getInt();
        if (radius == 0) return false;
        if (!from) radius++;
        int bx = block.getX();
        int by = block.getY();
        int bz = block.getZ();
        for (int fx = -radius; fx <= radius; fx++) {
            for (int fy = -radius; fy <= radius; fy++) {
                for (int fz = -radius; fz <= radius; fz++) {
                    Block foundBlock = block.getWorld().getBlockAt(bx + fx, by + fy, bz + fz);

                    if (from) {
                        if ((foundBlock.getType().equals(Material.LAVA) || foundBlock.getType().equals(Material.LAVA))
                                || (foundBlock.getType().equals(Material.WATER) || foundBlock.getType().equals(Material.WATER))) {
                            foundBlock.setType(Material.AIR);
                        }
                    } else if (CompatibleMaterial.getMaterial(foundBlock) == CompatibleMaterial.SPAWNER)
                        return true;
                }
            }
        }
        return false;
    }

    private boolean doForceCombine(Player player, PlacedSpawnerImpl placedSpawner, BlockPlaceEvent event) {
        int forceCombineRadius = Settings.FORCE_COMBINE_RADIUS.getInt();
        if (forceCombineRadius == 0) return false;

        for (PlacedSpawner spawner : plugin.getSpawnerManager().getSpawners()) {
            if (spawner.getLocation().getWorld() == null
                    || spawner.getLocation().getWorld() != placedSpawner.getLocation().getWorld()
                    || spawner.getLocation() == placedSpawner.getLocation()
                    || spawner.getLocation().distance(placedSpawner.getLocation()) > forceCombineRadius
                    || !Settings.OMNI_SPAWNERS.getBoolean() && spawner.getSpawnerStacks().size() != 1) {
                continue;
            }

            CompatibleHand hand = CompatibleHand.getHand(event);
            if (Settings.FORCE_COMBINE_DENY.getBoolean())
                plugin.getLocale().getMessage("event.block.forcedeny").sendPrefixedMessage(player);
            else if (spawner.stack(player, plugin.getSpawnerManager().getSpawnerTier(hand.getItem(player)), placedSpawner.getStackSize(), hand)) {
                plugin.getLocale().getMessage("event.block.mergedistance").sendPrefixedMessage(player);
                if (hand == CompatibleHand.OFF_HAND)
                    ItemUtils.takeActiveItem(player, hand);
            }
            return true;
        }
        return false;
    }

    private int getAmountInChunk(Block spawnerBlock) {
        int amountFound = 0;
        int chunkX = spawnerBlock.getX() >> 4;
        int chunkZ = spawnerBlock.getZ() >> 4;
        for (PlacedSpawner spawner : plugin.getSpawnerManager().getSpawners()) {
            if (spawner.getWorld() != spawnerBlock.getWorld()
                    || spawner.getX() >> 4 != chunkX
                    || spawner.getZ() >> 4 != chunkZ) continue;
            amountFound++;
        }
        return amountFound;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerPlace(BlockPlaceEvent event) {
        // We are ignoring canceled inside the event so that it will still remove holograms when the event is canceled.
        if (!event.isCancelled()) {
            Block block = event.getBlock();
            if (CompatibleMaterial.getMaterial(block) != CompatibleMaterial.SPAWNER
                    || ((CreatureSpawner) block.getState()).getSpawnedType() == EntityType.FIREWORK) return;

            Location location = block.getLocation();
            PlacedSpawnerImpl spawner = new PlacedSpawnerImpl(block.getLocation());

            SpawnerTier spawnerTier = plugin.getSpawnerManager().getSpawnerTier(event.getItemInHand());
            if (spawnerTier == null) return;

            int spawnerStackSize = spawnerTier.getStackSize(event.getItemInHand());
            spawner.addSpawnerStack(new SpawnerStackImpl(spawner, spawnerTier, spawnerStackSize));

            Player player = event.getPlayer();

            doLiquidRepel(block, true);

            if (plugin.getBlacklistHandler().isBlacklisted(player, true)
                    || !player.hasPermission("epicspawners.place." + spawnerTier.getSpawnerData().getIdentifyingName().replace(" ", "_"))
                    || doForceCombine(player, spawner, event)) {
                event.setCancelled(true);
                return;
            }

            int maxPerChunk = Settings.MAX_SPAWNERS_PER_CHUNK.getInt();
            if (maxPerChunk != -1 && getAmountInChunk(block) >= maxPerChunk) {
                plugin.getLocale().getMessage("event.block.chunklimit")
                        .processPlaceholder("amount", maxPerChunk)
                        .sendPrefixedMessage(player);
                event.setCancelled(true);
                return;
            }

            int amountPlaced = plugin.getSpawnerManager().getAmountPlaced(player);
            int maxSpawners = PlayerUtils.getNumberFromPermission(player, "epicspawners.limit", Settings.MAX_SPAWNERS.getInt());

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

            if (Settings.ALERT_PLACE_BREAK.getBoolean())
                plugin.getLocale().getMessage("event.block.place")
                        .processPlaceholder("type", spawnerTier.getCompiledDisplayName(false, spawner.getFirstStack().getStackSize()))
                        .sendPrefixedMessage(player);

            if (player.getGameMode() == GameMode.CREATIVE && Settings.CHARGE_FOR_CREATIVE.getBoolean())
                ItemUtils.takeActiveItem(player, CompatibleHand.getHand(event), 1);

            try {
                creatureSpawner.setSpawnedType(spawnerTier.getEntities().get(0));
            } catch (Exception ex) {
                creatureSpawner.setSpawnedType(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9) ? EntityType.EGG : EntityType.DROPPED_ITEM);
            }

            spawner.updateDelay();
            spawner.setPlacedBy(player);
            EpicSpawners.getInstance().getDataManager().save(spawner);

            plugin.processChange(block);
            plugin.createHologram(spawner);
            plugin.getAppearanceTask().updateDisplayItem(spawner, spawnerTier);
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
        Player player = event.getPlayer();
        Block block = event.getBlock();

        //We are ignoring canceled inside the event so that it will still remove holograms when the event is canceled.
        if (!event.isCancelled()) {
            if (CompatibleMaterial.getMaterial(block) != CompatibleMaterial.SPAWNER
                    || ((CreatureSpawner) block.getState()).getSpawnedType() == EntityType.FIREWORK) return;

            if (plugin.getBlacklistHandler().isBlacklisted(event.getPlayer(), true)) {
                event.setCancelled(true);
                return;
            }

            Location location = block.getLocation();

            if (!plugin.getSpawnerManager().isSpawner(location)) {
                PlacedSpawnerImpl spawner = new PlacedSpawnerImpl(location);

                CreatureSpawner creatureSpawner = spawner.getCreatureSpawner();
                if (creatureSpawner == null) return;

                spawner.addSpawnerStack(new SpawnerStackImpl(spawner, plugin.getSpawnerManager().getSpawnerData(creatureSpawner.getSpawnedType()).getFirstTier()));
                plugin.getSpawnerManager().addSpawnerToWorld(location, spawner);
                EpicSpawners.getInstance().getDataManager().save(spawner);
            }

            PlacedSpawner spawner = plugin.getSpawnerManager().getSpawnerFromWorld(location);

            if (spawner.getFirstStack().getSpawnerData() == null) {
                if (Settings.REMOVE_CORRUPTED_SPAWNERS.getBoolean()) {
                    block.setType(Material.AIR);
                    plugin.getLogger().warning("A corrupted spawner has been removed as its Type no longer exists.");
                    spawner.destroy();
                }
                return;
            }

            int currentStackSize = spawner.getStackSize();
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

            double cost = spawner.getFirstStack().getCurrentTier().getPickupCost();
            if (cost != 0.0 && (!naturalOnly || spawner.getPlacedBy() == null)) {
                if (!plugin.getSpawnerManager().hasCooldown(spawner)) {
                    plugin.getLocale().getMessage("event.block.chargebreak")
                            .processPlaceholder("cost", EconomyManager.formatEconomy(spawner.getFirstStack().getCurrentTier().getPickupCost()))
                            .sendPrefixedMessage(player);
                    plugin.getSpawnerManager().addCooldown(spawner);
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.getSpawnerManager().removeCooldown(spawner), 300L);
                    event.setCancelled(true);
                    return;
                }

                plugin.getSpawnerManager().removeCooldown(spawner);

                if (EconomyManager.hasBalance(player, cost)) {
                    EconomyManager.withdrawBalance(player, cost);
                } else {
                    plugin.getLocale().getMessage("event.block.cannotbreak").sendPrefixedMessage(player);
                    event.setCancelled(true);
                    return;
                }
            }

            SpawnerTier firstTier = spawner.getFirstStack().getCurrentTier();
            CompatibleHand hand = CompatibleHand.getHand(event);

            short damage = spawner.getFirstStack().getCurrentTier().getPickDamage();

            if (spawner.unstack(event.getPlayer(), hand)) {
                if (block.getType() != Material.AIR)
                    event.setCancelled(true);

                if (Settings.ALERT_PLACE_BREAK.getBoolean()) {
                    if (spawner.getSpawnerStacks().size() != 0) {
                        plugin.getLocale().getMessage("event.downgrade.success").processPlaceholder("size", Integer.toString(spawner.getStackSize())).sendPrefixedMessage(player);
                    } else {
                        plugin.getLocale().getMessage("event.block.break").processPlaceholder("type", firstTier.getCompiledDisplayName(false, currentStackSize)).sendPrefixedMessage(player);
                    }
                }

                if (hand.getItem(player).getType().name().endsWith("PICKAXE") && !player.hasPermission("epicspawners.nopickdamage"))
                    new SItemStack(hand.getItem(player)).addDamage(player, damage);
            }

            plugin.updateHologram(spawner);
            plugin.getAppearanceTask().removeDisplayItem(spawner);

            return;
        }

        //ToDo: Probably remove this.
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.processChange(block), 10L);
    }
}

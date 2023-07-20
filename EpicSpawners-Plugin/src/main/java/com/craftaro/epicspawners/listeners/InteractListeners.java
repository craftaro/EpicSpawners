package com.craftaro.epicspawners.listeners;

import com.craftaro.core.compatibility.CompatibleHand;
import com.craftaro.core.compatibility.CompatibleMaterial;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.core.hooks.ProtectionManager;
import com.craftaro.core.utils.ItemUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.events.SpawnerAccessEvent;
import com.craftaro.epicspawners.api.events.SpawnerChangeEvent;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerStack;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.epicspawners.spawners.spawner.PlacedSpawnerImpl;
import com.craftaro.epicspawners.spawners.spawner.SpawnerManager;
import com.craftaro.epicspawners.spawners.spawner.SpawnerStackImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;

import java.util.Optional;

/**
 * Created by songoda on 2/25/2017.
 */
public class InteractListeners implements Listener {

    private final EpicSpawners plugin;

    public InteractListeners(EpicSpawners plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerInteractEventEgg(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();

        int radius = Settings.LIQUID_REPEL_RADIUS.getInt();
        if (item != null
                && item.getType().equals(Material.WATER_BUCKET)
                && radius != 0) {
            int bx = block.getX();
            int by = block.getY();
            int bz = block.getZ();
            for (int fx = -radius; fx <= radius; fx++) {
                for (int fy = -radius; fy <= radius; fy++) {
                    for (int fz = -radius; fz <= radius; fz++) {
                        Block b = event.getClickedBlock().getWorld().getBlockAt(bx + fx, by + fy, bz + fz);
                        if (b.getType() == XMaterial.SPAWNER.parseMaterial()) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }

        if (item == null || item.getType() == Material.AIR) return;

        if (block == null || block.getType() != XMaterial.SPAWNER.parseMaterial()) return;

        //Check if the item is a spawn egg
        try {
            CompatibleMaterial.getEntityForSpawnEgg(XMaterial.matchXMaterial(item));
        } catch (IllegalArgumentException ex) {
            return;
        }

        event.setCancelled(true);

        if (plugin.getBlacklistHandler().isBlacklisted(player, true)) return;

        SpawnerManager spawnerManager = plugin.getSpawnerManager();

        if (!plugin.getSpawnerManager().isSpawner(block.getLocation()))
            createMissingSpawner(block.getLocation(), player);

        PlacedSpawner spawner = spawnerManager.getSpawnerFromWorld(block.getLocation());

        SpawnerTier blockType = spawnerManager.getSpawnerData(spawner.getCreatureSpawner().getSpawnedType()).getFirstTier();

        if (!Settings.EGGS_CONVERT_SPAWNERS.getBoolean()
                || !spawner.getFirstStack().getSpawnerData().isActive()
                || (spawner.getPlacedBy() == null && Settings.DISABLE_NATURAL_SPAWNERS.getBoolean())) {
            event.setCancelled(true);
            return;
        }

        int bmulti = spawner.getStackSize();
        int amt = player.getInventory().getItemInHand().getAmount();
        EntityType itype;

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
            itype = EntityType.valueOf(item.getType().name().replace("_SPAWN_EGG", "")
                    .replace("MOOSHROOM", "MUSHROOM_COW")
                    .replace("ZOMBIE_PIGMAN", "PIG_ZOMBIE"));
        } else {
            itype = ((SpawnEgg) item.getData()).getSpawnedType();
        }

        SpawnerTier itemType = plugin.getSpawnerManager().getSpawnerData(itype).getFirstTier();

        if (!player.hasPermission("epicspawners.egg." + itype) && !player.hasPermission("epicspawners.egg.*")) {
            event.setCancelled(true);
            return;
        }
        if (amt < bmulti) {
            plugin.getLocale().getMessage("event.egg.needmore")
                    .processPlaceholder("amount", bmulti).sendPrefixedMessage(player);
            event.setCancelled(true);
            return;
        }
        SpawnerChangeEvent e = new SpawnerChangeEvent(player, spawner, blockType, itemType);
        Bukkit.getPluginManager().callEvent(e);
        if (e.isCancelled()) {
            return;
        }

        if (Settings.USE_PROTECTION_PLUGINS.getBoolean() && !ProtectionManager.canInteract(player, block.getLocation())) {
            player.sendMessage(plugin.getLocale().getMessage("event.general.protected").getPrefixedMessage());
            return;
        }

        if (blockType.equals(itemType)) {
            plugin.getLocale().getMessage("event.egg.sametype")
                    .processPlaceholder("type", blockType.getIdentifyingName()).sendPrefixedMessage(player);
            return;
        }

        CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();

        if (Settings.GIVE_OLD_EGG.getBoolean() && creatureSpawner.getSpawnedType() != EntityType.fromId(item.getDurability())) {
            Optional<XMaterial> oldEgg = XMaterial.matchXMaterial(creatureSpawner.getSpawnedType().name() + "_SPAWN_EGG");
            oldEgg.ifPresent(xMaterial -> player.getInventory().addItem(xMaterial.parseItem()));
        }

        SpawnerStack stack = spawner.getFirstStack().setTier(plugin.getSpawnerManager().getSpawnerData(itype).getFirstTier());
        plugin.getDataManager().save(stack);
        try {
            spawner.getCreatureSpawner().setSpawnedType(EntityType.valueOf(plugin.getSpawnerManager().getSpawnerData(itype).getIdentifyingName().toUpperCase()));
        } catch (Exception e2) {
            spawner.getCreatureSpawner().setSpawnedType(EntityType.DROPPED_ITEM);
        }
        spawner.getCreatureSpawner().update();
        
        plugin.processChange(block);
        ItemUtils.takeActiveItem(player, CompatibleHand.getHand(event), bmulti);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerInteractEvent(PlayerArmorStandManipulateEvent event) {
        if (plugin.getSpawnerManager().isSpawner(event.getRightClicked().getLocation().getBlock().getRelative(BlockFace.UP).getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerInteractEvent(PlayerInteractEvent event) {
        if (CompatibleHand.getHand(event) == CompatibleHand.OFF_HAND) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;
        Location location = block.getLocation();
        ItemStack item = event.getItem();

        boolean isSpawner = block.getType() == XMaterial.SPAWNER.parseMaterial();

        if (isSpawner && !plugin.getSpawnerManager().isSpawner(location))
            createMissingSpawner(location, player);

        if (event.getClickedBlock() == null
                || event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (item != null && item.getType().name().contains("SPAWN_EGG") && item.getType().name().equals("MONSTER_EGG"))
            return;

        if (isSpawner && XMaterial.SPAWNER.equals(XMaterial.matchXMaterial(item))) {
            PlacedSpawner spawner = plugin.getSpawnerManager().getSpawnerFromWorld(location);

            if (spawner.getPlacedBy() == null && Settings.DISABLE_NATURAL_SPAWNERS.getBoolean()) return;

            if (!player.isSneaking()) {
                SpawnerTier spawnerTier = plugin.getSpawnerManager().getSpawnerTier(item);
                if (player.hasPermission("epicspawners.stack." + spawnerTier.getIdentifyingName()) || player.hasPermission("epicspawners.stack.*")) {
                    if (Settings.USE_PROTECTION_PLUGINS.getBoolean() && !ProtectionManager.canInteract(player, block.getLocation())) {
                        player.sendMessage(plugin.getLocale().getMessage("event.general.protected").getPrefixedMessage());
                        return;
                    }

                    spawner.stack(player, spawnerTier, spawnerTier.getStackSize(item), CompatibleHand.getHand(event));
                    plugin.updateHologram(spawner);
                    event.setCancelled(true);
                }
            }
        } else if (isSpawner && !plugin.getBlacklistHandler().isBlacklisted(player, false)) {
            PlacedSpawner spawner = plugin.getSpawnerManager().getSpawnerFromWorld(location);
            if (!player.isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (spawner.getPlacedBy() == null && Settings.DISABLE_NATURAL_SPAWNERS.getBoolean()) return;

                SpawnerAccessEvent accessEvent = new SpawnerAccessEvent(player, spawner);
                Bukkit.getPluginManager().callEvent(accessEvent);
                if (accessEvent.isCancelled()) {
                    return;
                }

                if (Settings.USE_PROTECTION_PLUGINS.getBoolean() && !ProtectionManager.canInteract(player, block.getLocation())) {
                    player.sendMessage(plugin.getLocale().getMessage("event.general.protected").getPrefixedMessage());
                    return;
                }

                spawner.overview(player);
                plugin.processChange(block);
                event.setCancelled(true);
            }
        }
    }

    private PlacedSpawnerImpl createMissingSpawner(Location location, Player player) {
        PlacedSpawnerImpl spawner = new PlacedSpawnerImpl(location);
        spawner.setPlacedBy(player);
        CreatureSpawner creatureSpawner = spawner.getCreatureSpawner();
        if (creatureSpawner == null) return null;

        spawner.addSpawnerStack(new SpawnerStackImpl(spawner, plugin.getSpawnerManager().getSpawnerData(creatureSpawner.getSpawnedType()).getFirstTier(), 1));
        plugin.getSpawnerManager().addSpawnerToWorld(location, spawner);
        EpicSpawners.getInstance().getDataManager().save(spawner);
        return spawner;
    }
}

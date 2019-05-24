package com.songoda.epicspawners.listeners;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.api.events.SpawnerChangeEvent;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerManager;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.ServerVersion;
import com.songoda.epicspawners.utils.settings.Setting;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Created by songoda on 2/25/2017.
 */
public class InteractListeners implements Listener {

    private final EpicSpawners plugin;

    public InteractListeners(EpicSpawners plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void PlayerInteractEventEgg(PlayerInteractEvent e) {
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        Player p = e.getPlayer();
        Block b = e.getClickedBlock();
        ItemStack i = e.getItem();

        Material is = null;
        if (e.getItem() != null) {
            is = i.getType();
        }

        int radius = plugin.getConfig().getInt("Main.Spawners Repel Liquid Radius");
        if (e.getItem() != null
                && is.equals(Material.WATER_BUCKET)
                && radius != 0) {
            Block block = e.getClickedBlock();
            int bx = block.getX();
            int by = block.getY();
            int bz = block.getZ();
            for (int fx = -radius; fx <= radius; fx++) {
                for (int fy = -radius; fy <= radius; fy++) {
                    for (int fz = -radius; fz <= radius; fz++) {
                        Block b2 = e.getClickedBlock().getWorld().getBlockAt(bx + fx, by + fy, bz + fz);
                        if (b2.getType().equals(plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))) {
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }

        if (is == null || is == Material.AIR) return;

        if (e.getClickedBlock().getType() == (plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER")) && is.toString().contains("SPAWN_EGG") && plugin.getBlacklistHandler().isBlacklisted(p, true))
            e.setCancelled(true);
        if (!(e.getClickedBlock().getType() == (plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER")) && is.toString().contains("SPAWN_EGG")) && !plugin.getBlacklistHandler().isBlacklisted(p, true)) {
            return;
        }

        SpawnerManager spawnerManager = plugin.getSpawnerManager();
        Spawner spawner = spawnerManager.getSpawnerFromWorld(b.getLocation());

        SpawnerData blockType = spawnerManager.getSpawnerData(spawner.getCreatureSpawner().getSpawnedType());

        if (!Setting.EGGS_CONVERT_SPAWNERS.getBoolean()
                || !spawner.getFirstStack().getSpawnerData().isActive()) {  //ToDo When you redo eggs make it so that if you use one on an omni
            e.setCancelled(true);
            return;
        }

        int bmulti = spawner.getSpawnerDataCount();
        int amt = p.getInventory().getItemInHand().getAmount();
        EntityType itype = EntityType.valueOf(i.getType().name().replace("_SPAWN_EGG", "").replace("MOOSHROOM", "MUSHROOM_COW"));

        SpawnerData itemType = plugin.getSpawnerManager().getSpawnerData(itype);

        if (!p.hasPermission("epicspawners.egg." + itype) && !p.hasPermission("epicspawners.egg.*")) {
            e.setCancelled(true);
            return;
        }
        if (amt < bmulti) {
            p.sendMessage(plugin.getLocale().getMessage("event.egg.needmore", bmulti));
            e.setCancelled(true);
            return;
        }
        SpawnerChangeEvent event = new SpawnerChangeEvent(p, spawner, blockType, itemType);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        if (blockType.equals(itemType)) {
            p.sendMessage(plugin.getLocale().getMessage("event.egg.sametype", blockType.getIdentifyingName()));
            return;
        }
        spawner.getFirstStack().setSpawnerData(plugin.getSpawnerManager().getSpawnerData(itype));
        try {
            spawner.getCreatureSpawner().setSpawnedType(EntityType.valueOf(plugin.getSpawnerManager().getSpawnerData(itype).getIdentifyingName().toUpperCase()));
        } catch (Exception e2) {
            spawner.getCreatureSpawner().setSpawnedType(EntityType.DROPPED_ITEM);
        }
        spawner.getCreatureSpawner().update();

        if (plugin.getHologram() != null)
            plugin.getHologram().processChange(b);
        if (p.getGameMode() != GameMode.CREATIVE) {
            Methods.takeItem(p, bmulti - 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void PlayerInteractEvent(PlayerArmorStandManipulateEvent event) {
        if (plugin.getSpawnerManager().isSpawner(event.getRightClicked().getLocation().getBlock().getRelative(BlockFace.UP).getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void PlayerInteractEvent(PlayerInteractEvent event) {
        if (plugin.isServerVersionAtLeast(ServerVersion.V1_9)) {
            if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        }


        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Location location = block.getLocation();
        ItemStack item = event.getItem();

        if (block.getType() == (plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))) {
            if (!plugin.getSpawnerManager().isSpawner(location)) {
                Spawner spawner = new Spawner(location);

                CreatureSpawner creatureSpawner = spawner.getCreatureSpawner();
                if (creatureSpawner == null) return;

                spawner.addSpawnerStack(new SpawnerStack(plugin.getSpawnerManager().getSpawnerData(creatureSpawner.getSpawnedType()), 1));
                plugin.getSpawnerManager().addSpawnerToWorld(location, spawner);
            }
        }

        if (event.getClickedBlock() == null
                || event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Material is = null;
        if (event.getItem() != null) {
            is = item.getType();
        }
        if (is != null && is.name().contains("SPAWN_EGG"))
            return;
        if (event.getClickedBlock().getType() == (plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER")) && is == (plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER")) && !plugin.getBlacklistHandler().isBlacklisted(player, true)) {

            Spawner spawner = plugin.getSpawnerManager().getSpawnerFromWorld(location);
            if (!player.isSneaking()) {
                SpawnerData spawnerData = plugin.getSpawnerDataFromItem(item);
                if (player.hasPermission("epicspawners.stack." + spawnerData.getIdentifyingName()) || player.hasPermission("epicspawners.stack.*")) {
                    spawner.preStack(player, item);
                    if (plugin.getHologram() != null)
                        plugin.getHologram().update(spawner);
                    event.setCancelled(true);
                }
            }
        } else if (event.getClickedBlock().getType() == (plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER")) && !plugin.getBlacklistHandler().isBlacklisted(player, false)) {
            if (!player.isSneaking() || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Spawner spawner = plugin.getSpawnerManager().getSpawnerFromWorld(location);

                spawner.overview(player);
                if (plugin.getHologram() != null)
                    plugin.getHologram().processChange(block);
                event.setCancelled(true);
            }
        }
    }
}
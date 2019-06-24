package com.songoda.epicspawners.listeners;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.api.events.SpawnerChangeEvent;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerManager;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.Reflection;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;

/**
 * Created by songoda on 2/25/2017.
 */
public class InteractListeners implements Listener {

    private final EpicSpawners plugin;

    public InteractListeners(EpicSpawners plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PlayerInteractEventEgg(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        ItemStack item = event.getItem();

        Material is = null;
        if (event.getItem() != null) {
            is = item.getType();
        }

        int radius = plugin.getConfig().getInt("Main.Spawners Repel Liquid Radius");
        if (event.getItem() != null
                && is.equals(Material.WATER_BUCKET)
                && radius != 0) {
            int bx = block.getX();
            int by = block.getY();
            int bz = block.getZ();
            for (int fx = -radius; fx <= radius; fx++) {
                for (int fy = -radius; fy <= radius; fy++) {
                    for (int fz = -radius; fz <= radius; fz++) {
                        Block b2 = event.getClickedBlock().getWorld().getBlockAt(bx + fx, by + fy, bz + fz);
                        if (b2.getType().equals(plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }

        if (is == null || is == Material.AIR) return;

        if (event.getClickedBlock().getType() != (plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))
                || !is.toString().contains(plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? "SPAWN_EGG" : "MONSTER_EGG"))
            return;

            event.setCancelled(true);

        if (plugin.getBlacklistHandler().isBlacklisted(player, true)) return;

        SpawnerManager spawnerManager = plugin.getSpawnerManager();

        if (!plugin.getSpawnerManager().isSpawner(block.getLocation()))
            createSpawner(block.getLocation());

        Spawner spawner = spawnerManager.getSpawnerFromWorld(block.getLocation());

        SpawnerData blockType = spawnerManager.getSpawnerData(spawner.getCreatureSpawner().getSpawnedType());

        if (!Setting.EGGS_CONVERT_SPAWNERS.getBoolean()
                || !spawner.getFirstStack().getSpawnerData().isActive()
                || (spawner.getPlacedBy() == null && Setting.DISABLE_NATURAL_SPAWNERS.getBoolean())) {
            event.setCancelled(true);
            return;
        }

        int bmulti = spawner.getSpawnerDataCount();
        int amt = player.getInventory().getItemInHand().getAmount();
        EntityType itype;

        if (plugin.isServerVersionAtLeast(ServerVersion.V1_13))
            itype = EntityType.valueOf(item.getType().name().replace("_SPAWN_EGG", "").replace("MOOSHROOM", "MUSHROOM_COW"));
        else if (plugin.isServerVersionAtLeast(ServerVersion.V1_12)) {
            String str = Reflection.getNBTTagCompound(Reflection.getNMSItemStack(item)).toString();
            if (str.contains("minecraft:"))
                itype = EntityType.fromName(str.substring(str.indexOf("minecraft:") + 10, str.indexOf("\"}")));
            else
                itype = EntityType.fromName(str.substring(str.indexOf("EntityTag:{id:") + 15, str.indexOf("\"}")));
        } else
            itype = ((SpawnEgg) item.getData()).getSpawnedType();

        SpawnerData itemType = plugin.getSpawnerManager().getSpawnerData(itype);

        if (!player.hasPermission("epicspawners.egg." + itype) && !player.hasPermission("epicspawners.egg.*")) {
            event.setCancelled(true);
            return;
        }
        if (amt < bmulti) {
            player.sendMessage(plugin.getLocale().getMessage("event.egg.needmore", bmulti));
            event.setCancelled(true);
            return;
        }
        SpawnerChangeEvent e = new SpawnerChangeEvent(player, spawner, blockType, itemType);
        Bukkit.getPluginManager().callEvent(e);
        if (e.isCancelled()) {
            return;
        }
        if (blockType.equals(itemType)) {
            player.sendMessage(plugin.getLocale().getMessage("event.egg.sametype", blockType.getIdentifyingName()));
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
            plugin.getHologram().processChange(block);
        if (player.getGameMode() != GameMode.CREATIVE) {
            Methods.takeItem(player, bmulti - 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PlayerInteractEvent(PlayerArmorStandManipulateEvent event) {
        if (plugin.getSpawnerManager().isSpawner(event.getRightClicked().getLocation().getBlock().getRelative(BlockFace.UP).getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void PlayerInteractEvent(PlayerInteractEvent event) {
        if (plugin.isServerVersionAtLeast(ServerVersion.V1_9)) {
            if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Location location = block.getLocation();
        ItemStack item = event.getItem();

        if (block.getType() == (plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))) {
            if (!plugin.getSpawnerManager().isSpawner(location))
                createSpawner(location);
        }

        if (event.getClickedBlock() == null
                || event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Material is = null;
        if (event.getItem() != null) {
            is = item.getType();
        }
        if (is != null && is.name().contains("SPAWN_EGG") && is.name().equals("MONSTER_EGG"))
            return;
        if (event.getClickedBlock().getType() == (plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER")) && is == (plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER")) && !plugin.getBlacklistHandler().isBlacklisted(player, true)) {

            Spawner spawner = plugin.getSpawnerManager().getSpawnerFromWorld(location);

            if (spawner.getPlacedBy() == null && Setting.DISABLE_NATURAL_SPAWNERS.getBoolean()) return;

            if (!player.isSneaking()) {
                SpawnerData spawnerData = plugin.getSpawnerManager().getSpawnerData(item);
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

                if (spawner.getPlacedBy() == null && Setting.DISABLE_NATURAL_SPAWNERS.getBoolean()) return;

                spawner.overview(player);
                if (plugin.getHologram() != null)
                    plugin.getHologram().processChange(block);
                event.setCancelled(true);
            }
        }
    }

    public Spawner createSpawner(Location location) {
        Spawner spawner = new Spawner(location);

        CreatureSpawner creatureSpawner = spawner.getCreatureSpawner();
        if (creatureSpawner == null) return null;

        spawner.addSpawnerStack(new SpawnerStack(plugin.getSpawnerManager().getSpawnerData(creatureSpawner.getSpawnedType()), 1));
        plugin.getSpawnerManager().addSpawnerToWorld(location, spawner);
        return spawner;
    }
}
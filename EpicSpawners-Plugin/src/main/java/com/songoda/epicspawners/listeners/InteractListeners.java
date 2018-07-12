package com.songoda.epicspawners.listeners;

import com.songoda.arconix.api.methods.serialize.Serialize;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.events.SpawnerChangeEvent;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerManager;
import com.songoda.epicspawners.spawners.object.ESpawnerStack;
import com.songoda.epicspawners.spawners.object.ESpawner;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.Reflection;
import com.songoda.epicspawners.utils.ServerVersion;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;

/**
 * Created by songoda on 2/25/2017.
 */
public class InteractListeners implements Listener {

    private final EpicSpawnersPlugin instance;

    public InteractListeners(EpicSpawnersPlugin instance) {
        this.instance = instance;

    }

    @EventHandler(ignoreCancelled = true)
    public void PlayerInteractEventEgg(PlayerInteractEvent e) {
        try {

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

            int radius = EpicSpawnersPlugin.getInstance().getConfig().getInt("Main.Spawners Repel Liquid Radius");
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
                            if (b2.getType().equals(Material.MOB_SPAWNER)) {
                                e.setCancelled(true);
                            }
                        }
                    }
                }
            }

            if (e.getClickedBlock().getType() == Material.MOB_SPAWNER && is == Material.MONSTER_EGG && EpicSpawnersPlugin.getInstance().getBlacklistHandler().isBlacklisted(p, true))
                e.setCancelled(true);
            if (!(e.getClickedBlock().getType() == Material.MOB_SPAWNER && is == Material.MONSTER_EGG && !EpicSpawnersPlugin.getInstance().getBlacklistHandler().isBlacklisted(p, true))) {
                return;
            }
            SpawnerManager spawnerManager = instance.getSpawnerManager();
            Spawner spawner = spawnerManager.getSpawnerFromWorld(b.getLocation());

            SpawnerData blockType = spawnerManager.getSpawnerData(spawner.getCreatureSpawner().getSpawnedType());

            if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Convert Spawners With Eggs")
                    || !spawner.getFirstStack().getSpawnerData().isActive()) {  //ToDo When you redo eggs make it so that if you use one on an omni
                e.setCancelled(true);
                return;
            }

            int bmulti = spawner.getSpawnerDataCount();
            int amt = p.getInventory().getItemInHand().getAmount();
            EntityType itype;

            if (EpicSpawnersPlugin.getInstance().isServerVersion(ServerVersion.V1_7, ServerVersion.V1_8))
                itype = ((SpawnEgg) i.getData()).getSpawnedType();
            else {
                String str = Reflection.getNBTTagCompound(Reflection.getNMSItemStack(i)).toString();
                if (str.contains("minecraft:"))
                    itype = EntityType.fromName(str.substring(str.indexOf("minecraft:") + 10, str.indexOf("\"}")));
                else
                    itype = EntityType.fromName(str.substring(str.indexOf("EntityTag:{id:") + 15, str.indexOf("\"}")));
            }

            SpawnerData itemType = instance.getSpawnerManager().getSpawnerData(itype);

            if (!p.hasPermission("epicspawners.egg." + itype) && !p.hasPermission("epicspawners.egg.*")) {
                return;
            }
            if (amt < bmulti) {
                p.sendMessage(instance.getLocale().getMessage("event.egg.needmore", bmulti));
                e.setCancelled(true);
                return;
            }
            SpawnerChangeEvent event = new SpawnerChangeEvent(p, spawner, blockType, itemType);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            if (blockType.equals(itemType)) {
                p.sendMessage(instance.getLocale().getMessage("event.egg.sametype", blockType.getIdentifyingName()));
                return;
            }
            spawner.getFirstStack().setSpawnerData(instance.getSpawnerManager().getSpawnerData(itype));
            try {
                spawner.getCreatureSpawner().setSpawnedType(EntityType.valueOf(instance.getSpawnerManager().getSpawnerData(itype).getIdentifyingName().toUpperCase()));
            } catch (Exception e2) {
                spawner.getCreatureSpawner().setSpawnedType(EntityType.DROPPED_ITEM);
            }
            spawner.getCreatureSpawner().update();
            EpicSpawnersPlugin.getInstance().getHologramHandler().processChange(b);
            if (p.getGameMode() != GameMode.CREATIVE) {
                Methods.takeItem(p, bmulti - 1);
            }
        } catch (Exception ee) {
            Debugger.runReport(ee);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void PlayerInteractEvent(PlayerInteractEvent e) {
        try {
            if (Methods.isOffhand(e)) return;

            Player player = e.getPlayer();
            Block block = e.getClickedBlock();
            Location location = block.getLocation();
            ItemStack item = e.getItem();

            if (block.getType() == Material.MOB_SPAWNER) {
                if (!instance.getSpawnerManager().isSpawner(location)) {
                    ESpawner spawner = new ESpawner(location);

                    spawner.addSpawnerStack(new ESpawnerStack(instance.getSpawnerManager().getSpawnerData(spawner.getCreatureSpawner().getSpawnedType()), 1));
                    instance.getSpawnerManager().addSpawnerToWorld(location, spawner);
                }
            }

            if (e.getClickedBlock() == null
                    || e.getAction() != Action.RIGHT_CLICK_BLOCK
                    || !EpicSpawnersPlugin.getInstance().canBuild(e.getPlayer(), e.getClickedBlock().getLocation())) {
                return;
            }

            Material is = null;
            if (e.getItem() != null) {
                is = item.getType();
            }
            if (is == Material.MONSTER_EGG)
                return;
            if (e.getClickedBlock().getType() == Material.MOB_SPAWNER && is == Material.MOB_SPAWNER && !EpicSpawnersPlugin.getInstance().getBlacklistHandler().isBlacklisted(player, true)) {

                Spawner spawner = instance.getSpawnerManager().getSpawnerFromWorld(location);
                if (!player.isSneaking() && item.getItemMeta().getDisplayName() != null) {
                    SpawnerData spawnerData = instance.getSpawnerDataFromItem(item);
                    if (player.hasPermission("epicspawners.stack." + spawnerData.getIdentifyingName()) || player.hasPermission("epicspawners.stack.*")) {
                        spawner.preStack(player, item);
                        instance.getHologramHandler().updateHologram(spawner);
                        e.setCancelled(true);
                    }
                }
            } else if (e.getClickedBlock().getType() == Material.MOB_SPAWNER && !EpicSpawnersPlugin.getInstance().getBlacklistHandler().isBlacklisted(player, false)) {
                if (!player.isSneaking()) {
                    Spawner spawner = EpicSpawnersPlugin.getInstance().getSpawnerManager().getSpawnerFromWorld(location);

                    ((ESpawner)spawner).overview(player, 1);
                    EpicSpawnersPlugin.getInstance().getHologramHandler().processChange(block);
                    e.setCancelled(true);
                }
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}
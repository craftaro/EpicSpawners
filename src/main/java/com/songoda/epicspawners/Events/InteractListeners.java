package com.songoda.epicspawners.Events;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Spawners.Spawner;
import com.songoda.epicspawners.Spawners.SpawnerChangeEvent;
import com.songoda.epicspawners.Utils.Debugger;
import com.songoda.epicspawners.Utils.Methods;
import com.songoda.epicspawners.Utils.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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

    EpicSpawners instance;

    public InteractListeners(EpicSpawners instance) {
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
            int radius = instance.getConfig().getInt("Main.Spawner Repel Liquid Radius");
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

            if (e.getClickedBlock().getType() == Material.MOB_SPAWNER && is == Material.MONSTER_EGG && instance.blacklist.isBlacklisted(p, true))
                e.setCancelled(true);
            if (!(e.getClickedBlock().getType() == Material.MOB_SPAWNER && is == Material.MONSTER_EGG && !instance.blacklist.isBlacklisted(p, true))) {
                return;
            }
            Spawner eSpawner = new Spawner(b);
            String btype = Methods.getType(eSpawner.getSpawner().getSpawnedType());

            if (!instance.getConfig().getBoolean("Main.Convert Spawners With Eggs")
                    || !instance.spawnerFile.getConfig().getBoolean("Entities." + btype + ".Allowed")) {
                e.setCancelled(true);
                return;
            }

            int bmulti = 1;
            if (instance.dataFile.getConfig().getInt("data.spawner." + Arconix.pl().getApi().serialize().serializeLocation(b)) != 0)
                bmulti = instance.dataFile.getConfig().getInt("data.spawner." + Arconix.pl().getApi().serialize().serializeLocation(b));
            int amt = p.getInventory().getItemInHand().getAmount();
            EntityType itype;

            if (instance.v1_7 || instance.v1_8)
                itype = ((SpawnEgg) i.getData()).getSpawnedType();
            else {
                String str = Reflection.getNBTTagCompound(Reflection.getNMSItemStack(i)).toString();
                if (str.contains("minecraft:"))
                    itype = EntityType.fromName(str.substring(str.indexOf("minecraft:") + 10, str.indexOf("\"}")));
                else
                    itype = EntityType.fromName(str.substring(str.indexOf("EntityTag:{id:") + 15, str.indexOf("\"}")));
            }

            if (!p.hasPermission("epicspawners.egg." + itype) && !p.hasPermission("epicspawners.egg.*")) {
                return;
            }
            if (amt < bmulti) {
                p.sendMessage(instance.getLocale().getMessage("event.egg.needmore", bmulti));
                e.setCancelled(true);
                return;
            }
            SpawnerChangeEvent event = new SpawnerChangeEvent(b.getLocation(), p, btype, itype.name());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            if (btype.equals(Methods.getType(itype))) {
                p.sendMessage(instance.getLocale().getMessage("event.egg.sametype", btype));
                return;
            }
            eSpawner.getSpawner().setSpawnedType(itype);
            eSpawner.update();
            instance.holo.processChange(b);
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
            if (e.getClickedBlock().getType() == Material.MOB_SPAWNER) {
                instance.holo.processChange(e.getClickedBlock());
            }
            if (e.isCancelled()
                    || Methods.isOffhand(e)) {
                return;
            }
            Player p = e.getPlayer();
            Block b = e.getClickedBlock();
            ItemStack i = e.getItem();
            String loc = Arconix.pl().getApi().serialize().serializeLocation(b);
            if (instance.dataFile.getConfig().getString("data.blockshop." + loc) != null) {
                e.setCancelled(true);
                instance.shop.show(instance.dataFile.getConfig().getString("data.blockshop." + loc).toUpperCase(), 1, p);
                return;
            }
            if (!instance.hooks.canBuild(e.getPlayer(), e.getClickedBlock().getLocation())
                    || e.getClickedBlock() == null
                    || e.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            Material is = null;
            if (e.getItem() != null) {
                is = i.getType();
            }
            if (is == Material.MONSTER_EGG)
                return;
            if (e.getClickedBlock().getType() == Material.MOB_SPAWNER && is == Material.MOB_SPAWNER && !instance.blacklist.isBlacklisted(p, true)) {
                Spawner eSpawner = new Spawner(b);
                if (!p.isSneaking() && i.getItemMeta().getDisplayName() != null) {
                    String itype = instance.getApi().getIType(i);
                    if (p.hasPermission("epicspawners.combine." + itype) || p.hasPermission("epicspawners.combine.*")) {
                        eSpawner.processCombine(p, i, null);
                        e.setCancelled(true);
                    }
                }
            } else if (e.getClickedBlock().getType() == Material.MOB_SPAWNER && !instance.blacklist.isBlacklisted(p, false)) {
                if (!p.isSneaking()) {
                    Spawner eSpawner = new Spawner(b);
                    eSpawner.overView(p, 1);
                    instance.holo.processChange(b);
                    e.setCancelled(true);
                }
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}

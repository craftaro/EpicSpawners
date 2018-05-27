package com.songoda.epicspawners.handlers;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.spawners.object.ESpawner;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by songo on 5/17/2017.
 */
public class AppearanceHandler {

    public AppearanceHandler() {
        try {
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(EpicSpawnersPlugin.getInstance(), this::displayItems, 30L, 30L);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void displayItems() {
        try {
            EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();

            if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.OmniSpawners Enabled")) return;

            for (Spawner spawner : instance.getSpawnerManager().getSpawners()) {
                if (spawner.getSpawnerStacks().size() <= 1) {
                    updateDisplayItem(spawner, spawner.getFirstStack().getSpawnerData());
                    continue;
                }

                Location location = spawner.getLocation();
                if (location == null || location.getWorld() == null) continue;
                int destx = location.getBlockX() >> 4;
                int destz = location.getBlockZ() >> 4;
                if (!location.getWorld().isChunkLoaded(destx, destz)) {
                    continue;
                }
                if (location.getBlock().getType() != Material.MOB_SPAWNER) continue;
                String last = null;
                SpawnerData next = null;
                List<SpawnerStack> list = new ArrayList<>(spawner.getSpawnerStacks());
                for (SpawnerStack stack : list) {
                    if (stack.getSpawnerData().getIdentifyingName().equals(((ESpawner)spawner).getOmniState())) {
                        last = stack.getSpawnerData().getIdentifyingName();
                    } else if (last != null && next == null) {
                        next = stack.getSpawnerData();
                    }
                }
                if (next == null) {
                    next = list.get(0).getSpawnerData();
                }
                updateDisplayItem(spawner, next);
                ((ESpawner)spawner).setOmniState(next.getIdentifyingName());

            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }


    public void updateDisplayItem(Spawner spawner, SpawnerData spawnerData) {
        try {

            Location nloc = spawner.getLocation();
            nloc.add(.5, -.4, .5);
            removeDisplayItem(spawner);

            try {
                EntityType next = EntityType.valueOf(Methods.restoreType(spawnerData.getIdentifyingName()));
                spawner.getCreatureSpawner().setSpawnedType(next);
            } catch (Exception ex) {
                spawner.getCreatureSpawner().setSpawnedType(EntityType.DROPPED_ITEM);

                Location location = spawner.getLocation();

                location.setPitch(-360);

                ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(nloc, EntityType.ARMOR_STAND);
                as.setSmall(true);
                as.setVisible(false);
                as.setCustomNameVisible(false);
                as.setGravity(false);
                as.setCanPickupItems(false);
                as.setBasePlate(true);
                try {
                    if (spawner.getFirstStack().getSpawnerData().getDisplayItem() != null) {
                        as.setHelmet(new ItemStack(spawnerData.getDisplayItem()));
                    } else {
                        as.setHelmet(new ItemStack(Material.DIRT));
                    }
                } catch (Exception ee) {
                    as.setHelmet(new ItemStack(Material.DIRT));
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void removeDisplayItem(Spawner spawner) {
        Location nloc = spawner.getLocation();
        nloc.add(.5, -.4, .5);
        List<Entity> near = (List<Entity>) nloc.getWorld().getNearbyEntities(nloc, 8, 8, 8);
        for (Entity e : near) {
            if (e.getLocation().getX() == nloc.getX() && e.getLocation().getY() == nloc.getY() && e.getLocation().getZ() == nloc.getZ()) {
                e.remove();
            }
        }
    }

}
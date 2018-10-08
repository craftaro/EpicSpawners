package com.songoda.epicspawners.handlers;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.spawners.spawner.ESpawner;
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
                if (location.getBlock().getType() != Material.SPAWNER) continue;
                String last = null;
                SpawnerData next = null;
                List<SpawnerStack> list = new ArrayList<>(spawner.getSpawnerStacks());
                for (SpawnerStack stack : list) {
                    if (stack.getSpawnerData().getIdentifyingName().equals(((ESpawner) spawner).getOmniState())) {
                        last = stack.getSpawnerData().getIdentifyingName();
                    } else if (last != null && next == null) {
                        next = stack.getSpawnerData();
                    }
                }
                if (next == null) {
                    next = list.get(0).getSpawnerData();
                }
                updateDisplayItem(spawner, next);
                ((ESpawner) spawner).setOmniState(next.getIdentifyingName());

            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }


    public void updateDisplayItem(Spawner spawner, SpawnerData spawnerData) {
        try {

            Location location = spawner.getLocation();
            location.add(.5, -.4, .5);

            ItemStack itemStack = new ItemStack(Material.DIRT);
            if (spawner.getFirstStack().getSpawnerData().getDisplayItem() != null)
                itemStack.setType(spawnerData.getDisplayItem());

            ArmorStand entity = (ArmorStand)getDisplayItem(spawner);

            if (entity != null) {
                if (entity.getHelmet().getType() != itemStack.getType()) {
                    entity.remove();
                    return;
                } else {
                    return;
                }
            }

            try {
                EntityType next = EntityType.valueOf(Methods.restoreType(spawnerData.getIdentifyingName()));
                spawner.getCreatureSpawner().setSpawnedType(next);
            } catch (Exception ex) {
                spawner.getCreatureSpawner().setSpawnedType(EntityType.DROPPED_ITEM);

                location.setPitch(-360);
                ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
                as.setSmall(true);
                as.setCustomName("EpicSpawners-Display");
                as.setVisible(false);
                as.setCustomNameVisible(false);
                as.setGravity(false);
                as.setCanPickupItems(false);
                as.setBasePlate(true);
                as.setHelmet(itemStack);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void removeDisplayItem(Spawner spawner) {
        Entity entity = getDisplayItem(spawner);
        if (entity == null) return;
        entity.remove();
    }

    private Entity getDisplayItem(Spawner spawner) {
        Location location = spawner.getLocation();
        location.add(.5, -.4, .5);

        List<Entity> near = (List<Entity>) location.getWorld().getNearbyEntities(location, 1, 2, 1);
        near.removeIf(e -> (!(e.getCustomName().equalsIgnoreCase("EpicSpawners-Display"))));
        if (near.size() != 0) {
            if(Debugger.isDebug()){
                Bukkit.getLogger().info("Songoda-Debug: ArmorStand present");
            }
            return near.get(0);
        }
        return null;
    }

}
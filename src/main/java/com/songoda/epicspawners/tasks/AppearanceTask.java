package com.songoda.epicspawners.tasks;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.ServerVersion;
import com.songoda.epicspawners.utils.settings.Setting;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class AppearanceTask extends BukkitRunnable {

    private static AppearanceTask instance;
    private final EpicSpawners plugin;

    private AppearanceTask(EpicSpawners plugin) {
        this.plugin = plugin;
    }

    public static AppearanceTask startTask(EpicSpawners plugin) {
        if (instance == null) {
            instance = new AppearanceTask(plugin);
            instance.runTaskTimerAsynchronously(plugin, 0, 60);
        }

        return instance;
    }

    @Override
    public void run() {
        EpicSpawners instance = EpicSpawners.getInstance();

        for (Spawner spawner : new ArrayList<>(instance.getSpawnerManager().getSpawners())) {
            Location location = spawner.getLocation();
            if (location == null || location.getWorld() == null) continue;
            int destx = location.getBlockX() >> 4;
            int destz = location.getBlockZ() >> 4;
            if (!location.getWorld().isChunkLoaded(destx, destz)) {
                continue;
            }
            if (location.getBlock().getType() != (instance.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER")))
                continue;

            if (spawner.getSpawnerStacks().size() <= 1) {
                updateDisplayItem(spawner, spawner.getFirstStack().getSpawnerData());
                continue;
            }

            if (!Setting.OMNI_SPAWNERS.getBoolean()) continue;

            String last = null;
            SpawnerData next = null;
            List<SpawnerStack> list = new ArrayList<>(spawner.getSpawnerStacks());
            for (SpawnerStack stack : list) {
                if (stack.getSpawnerData().getIdentifyingName().equals(spawner.getOmniState())) {
                    last = stack.getSpawnerData().getIdentifyingName();
                } else if (last != null && next == null) {
                    next = stack.getSpawnerData();
                }
            }
            if (next == null) {
                next = list.get(0).getSpawnerData();
            }
            updateDisplayItem(spawner, next);
            spawner.setOmniState(next.getIdentifyingName());

            CreatureSpawner creatureSpawner = spawner.getCreatureSpawner();
            if (creatureSpawner == null) continue;

            creatureSpawner.update();
        }
    }

    public void updateDisplayItem(Spawner spawner, SpawnerData spawnerData) {
        Location location = spawner.getLocation();
        location.add(.5, -.4, .5);

        ItemStack itemStack = new ItemStack(Material.DIRT);
        if (spawner.getFirstStack().getSpawnerData().getDisplayItem() != null)
            itemStack.setType(spawnerData.getDisplayItem());

        List<Entity> entities = getDisplayItem(spawner);

        if (entities != null && !entities.isEmpty()) {
            for (Entity entity : new ArrayList<>(entities)) {
                if (entity == null) {
                    entities.remove(entity);
                    continue;
                }
                if (((ArmorStand) entity).getHelmet().getType() == itemStack.getType()) return;
                entity.remove();
            }
        }

        try {
            EntityType next = EntityType.valueOf(Methods.restoreType(spawnerData.getIdentifyingName()));
            spawner.getCreatureSpawner().setSpawnedType(next);
        } catch (Exception failure) {
            spawner.getCreatureSpawner().setSpawnedType(EntityType.DROPPED_ITEM);

            if (itemStack.getType() == Material.AIR) return;
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
    }

    public void removeDisplayItem(Spawner spawner) {
        List<Entity> entites = getDisplayItem(spawner);
        if (entites == null) return;
        for (Entity entity : entites) {
            entity.remove();
        }
    }

    private List<Entity> getDisplayItem(Spawner spawner) {
        Location location = spawner.getLocation();
        location.add(.5, -.4, .5);

        List<Entity> near = (List<Entity>) location.getWorld().getNearbyEntities(location, .1, .1, .1);
        if (near == null) return null;
        near.removeIf(entity -> entity == null || entity.getType() != EntityType.ARMOR_STAND || entity.getCustomName() == null || !entity.getCustomName().equalsIgnoreCase("EpicSpawners-Display"));
        if (near.size() != 0) {
            return near;
        }
        return null;
    }
}

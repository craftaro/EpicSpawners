package com.craftaro.epicspawners.tasks;

import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerStack;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.settings.Settings;
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

    private AppearanceTask() {
    }

    public static AppearanceTask startTask(EpicSpawners plugin) {
        if (instance == null) {
            instance = new AppearanceTask();
            instance.runTaskTimer(plugin, 50L, 60);
        }

        return instance;
    }

    @Override
    public void run() {
        EpicSpawners instance = EpicSpawners.getInstance();

        for (PlacedSpawner spawner : new ArrayList<>(instance.getSpawnerManager().getSpawners())) {
            Location location = spawner.getLocation();
            if (location == null || location.getWorld() == null) {
                continue;
            }
            int destx = location.getBlockX() >> 4;
            int destz = location.getBlockZ() >> 4;
            if (!location.getWorld().isChunkLoaded(destx, destz)) {
                continue;
            }
            if (XMaterial.matchXMaterial(location.getBlock().getType().name()).get() != XMaterial.SPAWNER) {
                continue;
            }

            if (spawner.getSpawnerStacks().size() <= 1) {
                updateDisplayItem(spawner, spawner.getFirstStack().getCurrentTier());
                continue;
            }

            if (!Settings.OMNI_SPAWNERS.getBoolean()) {
                continue;
            }

            String last = null;
            SpawnerTier next = null;
            List<SpawnerStack> list = new ArrayList<>(spawner.getSpawnerStacks());
            for (SpawnerStack stack : list) {
                if (stack.getCurrentTier().getIdentifyingName().equals(spawner.getOmniState())) {
                    last = stack.getCurrentTier().getIdentifyingName();
                } else if (last != null && next == null) {
                    next = stack.getCurrentTier();
                }
            }
            if (next == null) {
                next = spawner.getFirstTier();
            }

            updateDisplayItem(spawner, next);
            spawner.setOmniState(next.getIdentifyingName());

            CreatureSpawner creatureSpawner = spawner.getCreatureSpawner();
            if (creatureSpawner == null) {
                continue;
            }

            creatureSpawner.update();
        }
    }

    public void updateDisplayItem(PlacedSpawner spawner, SpawnerTier spawnerTier) {
        Location location = spawner.getLocation();

        if (!spawner.getWorld().isChunkLoaded(spawner.getX() >> 4, spawner.getZ() >> 4)) {
            return;
        }

        location.add(.5, -.4, .5);

        ItemStack itemStack = new ItemStack(Material.DIRT);
        if (spawnerTier.getDisplayItem() != null) {
            itemStack = spawnerTier.getDisplayItem().parseItem();
        }

        List<Entity> entities = getDisplayItem(spawner);

        if (entities != null && !entities.isEmpty()) {
            for (Entity entity : new ArrayList<>(entities)) {
                if (entity == null) {
                    entities.remove(entity);
                    continue;
                }
                if (((ArmorStand) entity).getHelmet().getType() == itemStack.getType()) {
                    return;
                }
                entity.remove();
            }
        }

        try {
            EntityType next = spawnerTier.getEntities().get(0);
            spawner.getCreatureSpawner().setSpawnedType(next);
        } catch (Exception failure) {
            spawner.getCreatureSpawner().setSpawnedType(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9) ? EntityType.EGG : EntityType.DROPPED_ITEM);
            if (itemStack.getType() == Material.AIR) {
                return;
            }
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

    public void removeDisplayItem(PlacedSpawner spawner) {
        List<Entity> entites = getDisplayItem(spawner);
        if (entites == null) {
            return;
        }
        for (Entity entity : entites) {
            entity.remove();
        }
    }

    private List<Entity> getDisplayItem(PlacedSpawner spawner) {
        Location location = spawner.getLocation();
        location.add(.5, -.4, .5);

        List<Entity> near = (List<Entity>) location.getWorld().getNearbyEntities(location, .1, .1, .1);
        near.removeIf(entity -> entity == null || entity.getType() != EntityType.ARMOR_STAND || entity.getCustomName() == null || !entity.getCustomName().equalsIgnoreCase("EpicSpawners-Display"));
        if (!near.isEmpty()) {
            return near;
        }
        return null;
    }
}

package com.songoda.epicspawners.spawners.spawner.option;

import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public class SpawnOptionItem implements SpawnOption {

    private final Random random;
    private final ItemStack[] items;

    public SpawnOptionItem(ItemStack... items) {
        this.items = items;
        this.random = new Random();
    }

    public SpawnOptionItem(Collection<ItemStack> items) {
        this(items.toArray(new ItemStack[items.size()]));
    }


    public void spawn(SpawnerData data, SpawnerStack stack, Spawner spawner) {
        Location location = spawner.getLocation();
        if (location == null || location.getWorld() == null) return;

        World world = location.getWorld();
        Location spawnLocation = location.clone().add(0.5, 0.9, 0.5);
        location.getWorld().playSound(location, Sound.ENTITY_ITEM_PICKUP, 0.1F, 1F);

        int spawnerBoost = spawner.getBoost();
        for (int i = 0; i < spawner.getSpawnerDataCount() + spawnerBoost; i++) {
            for (ItemStack item : items) {
                if (item == null || item.getType() == Material.AIR) continue;
                Item droppedItem = world.dropItem(spawnLocation, item);
                spawner.setSpawnCount(spawner.getSpawnCount() + 1);

                double dx = -.2 + (.2 - -.2) * random.nextDouble();
                double dy = 0 + (.5 - 0) * random.nextDouble();
                double dz = -.2 + (.2 - -.2) * random.nextDouble();

                droppedItem.setVelocity(new Vector(dx, dy, dz));
            }
        }
    }


    public SpawnOptionType getType() {
        return SpawnOptionType.ITEM;
    }


    public int hashCode() {
        return 31 * (items != null ? items.hashCode() : 0);
    }


    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof SpawnOptionItem)) return false;

        SpawnOptionItem other = (SpawnOptionItem) object;
        return Arrays.equals(items, other.items);
    }

}
package com.songoda.epicspawners.spawners.object.option;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.spawners.object.SpawnOptionType;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

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

    @Override
    public void spawn(SpawnerData data, SpawnerStack stack, Spawner spawner) {
        Location location = spawner.getLocation();
        if (location == null || location.getWorld() == null) return;

        World world = location.getWorld();
        Location spawnLocation = location.clone().add(0.5, 0.9, 0.5);

        for (int i = 0; i < spawner.getSpawnerDataCount(); i++) {
            for (ItemStack item : items) {
                Item droppedItem = world.dropItem(spawnLocation, item);
                spawner.setSpawnCount(spawner.getSpawnCount() + 1);

                double dx = -.2 + (.2 - -.2) * random.nextDouble();
                double dy = 0 + (.5 - 0) * random.nextDouble();
                double dz = -.2 + (.2 - -.2) * random.nextDouble();

                droppedItem.setVelocity(new Vector(dx, dy, dz));
            }
        }
    }

    @Override
    public SpawnOptionType getType() {
        return SpawnOptionType.ITEM;
    }

    @Override
    public int hashCode() {
        return 31 * (items != null ? items.hashCode() : 0);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof SpawnOptionItem)) return false;

        SpawnOptionItem other = (SpawnOptionItem) object;
        return Arrays.equals(items, other.items);
    }

}
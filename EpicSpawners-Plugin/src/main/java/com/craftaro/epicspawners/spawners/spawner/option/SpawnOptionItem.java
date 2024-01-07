package com.craftaro.epicspawners.spawners.spawner.option;

import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.third_party.com.cryptomorin.xseries.XSound;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.boosts.types.Boosted;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerStack;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.api.spawners.spawner.option.SpawnOption;
import com.craftaro.epicspawners.api.spawners.spawner.option.SpawnOptionType;
import org.bukkit.Location;
import org.bukkit.Material;
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
        this(items.toArray(new ItemStack[0]));
    }

    @Override
    public void spawn(SpawnerTier data, SpawnerStack stack, PlacedSpawner spawner) {
        Location location = spawner.getLocation();
        if (location == null || location.getWorld() == null) {
            return;
        }

        World world = location.getWorld();
        Location spawnLocation = location.clone().add(0.5, 0.9, 0.5);

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) {
            XSound.ENTITY_ITEM_PICKUP.play(location, 0.1F, 1F);
        }


        int spawnerBoost = spawner.getBoosts().stream().mapToInt(Boosted::getAmountBoosted).sum();
        for (int i = 0; i < stack.getStackSize() + spawnerBoost; i++) {
            for (ItemStack item : this.items) {
                if (item == null || item.getType() == Material.AIR) {
                    continue;
                }
                Item droppedItem = world.dropItem(spawnLocation, item);
                spawner.setSpawnCount(spawner.getSpawnCount() + 1);


                double dx = -.2 + (.2 - -.2) * this.random.nextDouble();
                double dy = 0 + (.5 - 0) * this.random.nextDouble();
                double dz = -.2 + (.2 - -.2) * this.random.nextDouble();

                droppedItem.setVelocity(new Vector(dx, dy, dz));
            }
        }

        EpicSpawners.getInstance().getDataManager().save(spawner);
    }

    public SpawnOptionType getType() {
        return SpawnOptionType.ITEM;
    }

    public int hashCode() {
        return 31 * (this.items != null ? Arrays.hashCode(this.items) : 0);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SpawnOptionItem)) {
            return false;
        }

        SpawnOptionItem other = (SpawnOptionItem) obj;
        return Arrays.equals(this.items, other.items);
    }
}

package com.songoda.epicspawners.spawners.object.option;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.spawners.object.SpawnOptionType;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class SpawnOptionBlock implements SpawnOption {

    private static final int MAX_SEARCH_COUNT = 250;
    private static final int SPAWN_RADIUS = 3;

    private final Random random;
    private final Material[] blocks;

    public SpawnOptionBlock(Material... blocks) { //ToDo: convertOverview to  MaterialData as to support different types of wool and what not.
        this.blocks = blocks;
        this.random = new Random();
    }

    public SpawnOptionBlock(Collection<Material> blocks) {
        this(blocks.toArray(new Material[blocks.size()]));
    }

    @Override
    public void spawn(SpawnerData data, SpawnerStack stack, Spawner spawner) {
        Location location = spawner.getLocation();
        if (location == null || location.getWorld() == null) return;

        for (int i = 0; i < spawner.getSpawnerDataCount(); i++) {
            for (Material material : blocks) {
                int searchIndex = 0;
                while (searchIndex++ <= MAX_SEARCH_COUNT) {
                    spawner.setSpawnCount(spawner.getSpawnCount() + 1);
                    double xOffset = random.nextInt((SPAWN_RADIUS * 2) + 1) - SPAWN_RADIUS;
                    double yOffset = random.nextInt((SPAWN_RADIUS * 2) + 1) - SPAWN_RADIUS;
                    double zOffset = random.nextInt((SPAWN_RADIUS * 2) + 1) - SPAWN_RADIUS;

                    // Get block at offset
                    location.add(xOffset, yOffset, zOffset);
                    Block spawnBlock = location.getBlock();
                    location.subtract(xOffset, yOffset, zOffset);

                    // If block isn't air, try for another block
                    if (spawnBlock.getType() != Material.AIR) continue;

                    // Set Type and data for valid air block
                    spawnBlock.setType(material);
                    break;
                }
            }
        }
    }

    @Override
    public SpawnOptionType getType() {
        return SpawnOptionType.BLOCK;
    }

    @Override
    public int hashCode() {
        return 31 * (blocks != null ? blocks.hashCode() : 0);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof SpawnOptionBlock)) return false;

        SpawnOptionBlock other = (SpawnOptionBlock) object;
        return Arrays.equals(blocks, other.blocks);
    }

}
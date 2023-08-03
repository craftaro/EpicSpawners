package com.craftaro.epicspawners.spawners;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpawnManager {
    private final Set<UUID> unnaturalSpawns = new HashSet<>();

    public boolean isNaturalSpawn(UUID uuid) {
        return !this.unnaturalSpawns.contains(uuid);
    }

    public void addUnnaturalSpawn(UUID uuid) {
        this.unnaturalSpawns.add(uuid);
    }
}

package com.songoda.epicspawners.boost;

import java.util.HashSet;
import java.util.Set;

public class BoostManager {

    // These are the boosted spawners currently in the game world.
    private final Set<BoostData> boostedSpawners = new HashSet<>();

    public void addBoostToSpawner(BoostData data) {
        boostedSpawners.add(data);
    }

    public void removeBoostFromSpawner(BoostData data) {
        boostedSpawners.remove(data);
    }

    public Set<BoostData> getBoosts() {
        return boostedSpawners;
    }
}

package com.songoda.epicspawners.boost;

import java.util.ArrayList;
import java.util.List;

public class BoostManager {

    // These are the boosted spawners currently in the game world.
    private final List<BoostData> boostedSpawners = new ArrayList<>();

    public void addBoostToSpawner(BoostData data) {
        boostedSpawners.add(data);
    }

    public void removeBoostFromSpawner(BoostData data) {
        boostedSpawners.remove(data);
    }

    public List<BoostData> getBoosts() {
        return boostedSpawners;
    }
}

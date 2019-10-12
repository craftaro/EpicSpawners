package com.songoda.epicspawners.boost;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BoostManager {

    private final Set<BoostData> boostedSpawners = new HashSet<>();

    public void addBoostToSpawner(BoostData data) {
        this.boostedSpawners.add(data);
    }

    public void removeBoostFromSpawner(BoostData data) {
        this.boostedSpawners.remove(data);
    }

    public Set<BoostData> getBoosts() {
        return Collections.unmodifiableSet(boostedSpawners);
    }
}

package com.songoda.epicspawners.boost.types;

import org.bukkit.Location;

public class BoostedSpawner extends Boosted {

    private final Location location;

    public BoostedSpawner(Location location, int amtBoosted, long endTime) {
        super(amtBoosted, endTime);
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}

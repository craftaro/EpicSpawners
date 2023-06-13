package com.craftaro.epicspawners.boost.types;

import com.craftaro.epicspawners.api.boosts.types.BoostedSpawner;
import org.bukkit.Location;

public class BoostedSpawnerImpl extends BoostedImpl implements BoostedSpawner {

    private final Location location;

    public BoostedSpawnerImpl(Location location, int amtBoosted, long endTime) {
        super(amtBoosted, endTime);
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location;
    }
}

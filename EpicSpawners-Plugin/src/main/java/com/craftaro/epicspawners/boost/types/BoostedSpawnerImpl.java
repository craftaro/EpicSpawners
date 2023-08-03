package com.craftaro.epicspawners.boost.types;

import com.craftaro.core.database.Data;
import com.craftaro.core.database.SerializedLocation;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.boosts.types.BoostedSpawner;
import org.bukkit.Location;

import java.util.LinkedHashMap;
import java.util.Map;

public class BoostedSpawnerImpl extends BoostedImpl implements BoostedSpawner {
    private final Location location;

    /**
     * Default constructor used for database loading.
     */
    public BoostedSpawnerImpl() {
        super(0, 0);
        this.location = null;
    }

    public BoostedSpawnerImpl(Location location, int amtBoosted, long endTime) {
        super(amtBoosted, endTime);
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public int getId() {
        return EpicSpawners.getInstance().getSpawnerManager().getSpawner(getLocation()).getId();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>(SerializedLocation.of(location));
        map.put("amount", getAmountBoosted());
        map.put("end_time", getEndTime());
        return map;
    }

    @Override
    public Data deserialize(Map<String, Object> map) {
        return new BoostedSpawnerImpl(SerializedLocation.of(map),
                (int) map.get("amount"), (long) map.get("end_time"));
    }

    @Override
    public String getTableName() {
        return "boosted_spawners";
    }
}

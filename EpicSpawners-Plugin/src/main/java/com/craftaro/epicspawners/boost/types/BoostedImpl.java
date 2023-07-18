package com.craftaro.epicspawners.boost.types;

import com.craftaro.core.database.Data;
import com.craftaro.epicspawners.api.boosts.types.Boosted;

import java.util.Map;

public class BoostedImpl implements Boosted {

    private final int amountBoosted;
    private final long endTime;

    public BoostedImpl(int amountBoosted, long endTime) {
        this.amountBoosted = amountBoosted;
        this.endTime = endTime;
    }

    @Override
    public int getAmountBoosted() {
        return amountBoosted;
    }

    @Override
    public long getEndTime() {
        return endTime;
    }

    @Override
    public Map<String, Object> serialize() {
        return null;
    }

    @Override
    public Data deserialize(Map<String, Object> map) {
        return null;
    }

    @Override
    public String getTableName() {
        return null;
    }
}

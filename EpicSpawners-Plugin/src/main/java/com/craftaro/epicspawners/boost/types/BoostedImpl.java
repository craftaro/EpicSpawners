package com.craftaro.epicspawners.boost.types;

import com.craftaro.core.database.Data;
import com.craftaro.epicspawners.api.boosts.types.Boosted;

import java.util.Map;

public abstract class BoostedImpl implements Boosted {

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

}

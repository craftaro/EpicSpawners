package com.craftaro.epicspawners.boost.types;

import com.craftaro.epicspawners.api.boosts.types.Boosted;

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

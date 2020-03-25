package com.songoda.epicspawners.boost.types;

public class Boosted {

    private final int amountBoosted;
    private final long endTime;

    public Boosted(int amountBoosted, long endTime) {
        this.amountBoosted = amountBoosted;
        this.endTime = endTime;
    }

    public int getAmountBoosted() {
        return amountBoosted;
    }

    public long getEndTime() {
        return endTime;
    }
}

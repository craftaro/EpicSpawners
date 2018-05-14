package com.songoda.epicspawners.boost;

public class BoostData {

    // The type of boost.
    private final BoostType boostType;

    // Mount to add to each spawn.
    private final int amtBoosted;

    // Amount to add to
    private final long endTime;

    // The data stored in the boost.
    private final Object data;

    public BoostData(BoostType boostType, int amtBoosted, long endTime, Object data) {
        this.boostType = boostType;
        this.amtBoosted = amtBoosted;
        this.endTime = endTime;
        this.data = data;
    }

    public BoostType getBoostType() {
        return boostType;
    }

    public int getAmtBoosted() {
        return amtBoosted;
    }

    public Object getData() {
        return data;
    }

    public long getEndTime() {
        return endTime;
    }
}

package com.songoda.epicspawners.boost;

public class BoostData {

    // The type of boost.
    private BoostType boostType;

    // Mount to add to each spawn.
    private int amtBoosted;

    // Amount to add to
    private Long endTime;

    // The data stored in the boost.
    private Object data;

    public BoostData(BoostType boostType, int amtBoosted, Long endTime, Object data) {
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

    public Long getEndTime() {
        return endTime;
    }
}

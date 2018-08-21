package com.songoda.epicspawners.boost;

import java.util.Objects;

public class BoostData {

    private final BoostType boostType;
    private final int amtBoosted;
    private final long endTime;
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

    @Override
    public int hashCode() {
        int result = 31 * amtBoosted;

        result = 31 * result + (boostType == null ? 0 : boostType.hashCode());
        result = 31 * result + (this.data == null ? 0 : data.hashCode());
        result = 31 * result + (int) (endTime ^ (endTime >>> 32));

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BoostData)) return false;

        BoostData other = (BoostData) obj;
        return amtBoosted == other.amtBoosted && boostType == other.boostType
                && endTime == other.endTime && Objects.equals(data, other.data);
    }

}

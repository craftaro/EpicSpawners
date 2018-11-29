package com.songoda.epicspawners.utils.gui;

public final class Range {

    private final int low, high;

    private Range(int low, int high) {
        this.low = low;
        this.high = high;
    }

    public static Range from(int from, int to) {
        return new Range(from, to);
    }

    public int getLow() {
        return low;
    }

    public int getHigh() {
        return high;
    }

    public boolean isWithin(int value) {
        return value >= low && value <= high;
    }

    public boolean isWithin(double value) {
        return value >= low && value <= high;
    }

    public boolean isWithin(float value) {
        return value >= low && value <= high;
    }

    @Override
    public int hashCode() {
        int result = 31 * Integer.hashCode(low);
        result += 31 * result + Integer.hashCode(high);
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof Range)) return false;

        Range other = (Range) object;
        return low == other.low && high == other.high;
    }

}
package com.songoda.epicspawners.spawners.object;

import java.util.Objects;

import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerStack;

/*
 * This is object that will contain SpawnerData
 * and the amount of that SpawnerData.
 */
public class ESpawnerStack implements SpawnerStack {

    // This is the instance of SpawnerData that
    // This SpawnerStack utilizes.
    private SpawnerData spawnerData;

    // The Amount of the defined SpawnerData in
    // this stack.
    private int stackSize;

    //Construct the class and define values.
    public ESpawnerStack(SpawnerData spawnerData) {
        this(spawnerData, 1);
    }

    public ESpawnerStack(SpawnerData spawnerData, int stackSize) {
        this.spawnerData = spawnerData;
        this.stackSize = stackSize;
    }

    @Override
    public SpawnerData getSpawnerData() {
        return spawnerData;
    }

    @Override
    public void setSpawnerData(SpawnerData spawnerData) {
        this.spawnerData = spawnerData;
    }

    @Override
    public int getStackSize() {
        return stackSize;
    }

    @Override
    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
    }

    @Override
    public int hashCode() {
        int result = 31 * (spawnerData == null ? 0 : spawnerData.hashCode());
        result = 31 * result + this.stackSize;
        
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ESpawnerStack)) return false;

        ESpawnerStack other = (ESpawnerStack) obj;
        return stackSize == other.stackSize && Objects.equals(spawnerData, other.spawnerData);
    }

}
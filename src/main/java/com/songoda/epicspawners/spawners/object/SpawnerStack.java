package com.songoda.epicspawners.spawners.object;

/*
 * This is object that will contain SpawnerData
 * and the amount of that SpawnerData.
 */
public class SpawnerStack {

    // This is the instance of SpawnerData that
    // This SpawnerStack utilizes.
    private SpawnerData spawnerData;

    // The Amount of the defined SpawnerData in
    // this stack.
    private int stackSize;

    //Construct the class and define values.
    public SpawnerStack(SpawnerData spawnerData, int stackSize) {
        this.spawnerData = spawnerData;
        this.stackSize = stackSize;
    }

    public SpawnerData getSpawnerData() {
        return spawnerData;
    }

    public void setSpawnerData(SpawnerData spawnerData) {
        this.spawnerData = spawnerData;
    }

    public int getStackSize() {
        return stackSize;
    }

    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
    }
}
package com.songoda.epicspawners.spawners.spawner;

import java.util.Objects;

/*
 * This is object that will contain SpawnerData
 * and the amount of that SpawnerData.
 */
public class SpawnerStack {

    // The spawner that owns this stack.
    private Spawner spawner;

    // This is the instance of SpawnerData that
    // This SpawnerStack utilizes.
    private SpawnerData spawnerData;

    // The Amount of the defined SpawnerData in
    // this stack.
    private int stackSize;

    //Construct the class and define values.
    public SpawnerStack(SpawnerData spawnerData) {
        this(spawnerData, 1);
    }

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


    public boolean hasSpawnerData() {
        return spawnerData != null;
    }


    public int getStackSize() {
        return stackSize;
    }


    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
    }


    public int hashCode() {
        int result = 31 * (spawnerData == null ? 0 : spawnerData.hashCode());
        result = 31 * result + this.stackSize;

        return result;
    }


    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SpawnerStack)) return false;

        SpawnerStack other = (SpawnerStack) obj;
        return stackSize == other.stackSize && Objects.equals(spawnerData, other.spawnerData);
    }

    public Spawner getSpawner() {
        return spawner;
    }

    public void setSpawner(Spawner spawner) {
        this.spawner = spawner;
    }

    @Override
    public String toString() {
        return "SpawnerStack{" +
                "spawnerData=" + spawnerData +
                ", stackSize=" + stackSize +
                '}';
    }
}
package com.songoda.epicspawners.storage;

public class StorageItem {

    private String key = null;

    private final Object object;

    public StorageItem(Object object) {
        this.object = object;
    }

    public StorageItem(String key, Object object) {
        this.key = key;
        this.object = object;
    }

    public String getKey() {
        return key;
    }

    public String asString() {
        return (String)object;
    }

    public boolean asBoolean() {
        return (boolean)object;
    }

    public int asInt() {
        return (int)object;
    }

    public Object asObject() {
        return object;
    }
}

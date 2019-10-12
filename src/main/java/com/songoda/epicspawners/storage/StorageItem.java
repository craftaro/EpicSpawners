package com.songoda.epicspawners.storage;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StorageItem {

    private final Object object;
    private String key = null;

    public StorageItem(Object object) {
        this.object = object;
    }

    public StorageItem(String key, Object object) {
        this.key = key;
        this.object = object;
    }

    public StorageItem(String key, List<Material> material) {
        StringBuilder builder = new StringBuilder();
        for (Material m : material) {
            builder.append(m.name()).append(";");
        }
        this.key = key;
        this.object = builder.toString();
    }
    
    public StorageItem(String key, Map<EntityType, Integer> entityKills) {
        String object;

        if (entityKills.isEmpty()) {
            object = "";
        } else {
            StringBuilder builder = new StringBuilder((entityKills.size() * 8) + 16);

            for (Map.Entry<EntityType, Integer> entry : entityKills.entrySet()) {
                builder.append(entry.getKey().name()).append(":").append(entry.getValue()).append(";");
            }

            object = builder.toString();
        }

        this.key = key;
        this.object = object;
    }

    public String getKey() {
        return key;
    }

    public String asString() {
        if (object == null) return null;
        return (String) object;
    }

    public boolean asBoolean() {
        if (object == null) return false;
        return (boolean) object;
    }

    public int asInt() {
        if (object == null) return 0;
        return (int) object;
    }

    public Object asObject() {
        return object;
    }

    public List<Material> asMaterialList() {
        List<Material> list = new ArrayList<>();
        if (object == null) return list;
        String[] stack = ((String) object).split(";");
        for (String item : stack) {
            if (item.equals("")) continue;
            list.add(Material.valueOf(item));
        }
        return list;
    }
}

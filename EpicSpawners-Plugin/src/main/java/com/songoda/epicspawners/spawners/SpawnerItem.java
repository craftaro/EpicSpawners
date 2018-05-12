package com.songoda.epicspawners.spawners;

/**
 * Created by songo on 5/21/2017.
 */
public class SpawnerItem {

    private String type = "PIG";
    private int multi = 1;

    public SpawnerItem(String _type, int _multi) {
        if (type != null)
            type = _type;

        multi = _multi;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMulti() {
        return multi;
    }

    public void setMulti(int multi) {
        this.multi = multi;
    }
}
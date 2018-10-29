package com.songoda.epicspawners.spawners.editor;

import com.songoda.epicspawners.api.spawner.SpawnerData;

public class EditingData {

    private EditingMenu menu = EditingMenu.NOT_IN;
    private int newId = -1;
    private SpawnerData spawnerEditing = null;

    private String newSpawnerName = null;

    public EditingMenu getMenu() {
        return menu;
    }

    public void setMenu(EditingMenu menu) {
        this.menu = menu;
    }

    public SpawnerData getSpawnerEditing() {
        return spawnerEditing;
    }

    public void setSpawnerEditing(SpawnerData spawnerEditing) {
        this.spawnerEditing = spawnerEditing;
    }

    public int getNewId() {
        return newId;
    }

    public void setNewId(int newId) {
        this.newId = newId;
    }

    public String getNewSpawnerName() {
        return newSpawnerName;
    }

    public void setNewSpawnerName(String newSpawnerName) {
        this.newSpawnerName = newSpawnerName;
    }
}

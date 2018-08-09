package com.songoda.epicspawners.spawners.editor;

public class EditingData {

    private EditingMenu menu = EditingMenu.NOT_IN;
    private int spawnerSlot = 0, newId = -1;

    private String newSpawnerName = null;

    public EditingMenu getMenu() {
        return menu;
    }

    public void setMenu(EditingMenu menu) {
        this.menu = menu;
    }

    public int getSpawnerSlot() {
        return spawnerSlot;
    }

    public void setSpawnerSlot(int spawnerSlot) {
        this.spawnerSlot = spawnerSlot;
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

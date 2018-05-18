package com.songoda.epicspawners.spawners.editor;

public class EditingData {

    private EditingMenu menu = EditingMenu.NOT_IN;
    private int spawnerSlot = 0, newId = -1;

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

}

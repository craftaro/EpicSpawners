package com.songoda.epicspawners.spawners.editor;

public class EditingData {

    // This is the menu that the use is currently
    // located in.
    private EditingMenu menu = EditingMenu.NOTIN;

    // This is the slot in which the spawner
    // that is currently being edited is located.
    private int spawnerSlot = 0;

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
}

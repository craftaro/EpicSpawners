package com.songoda.epicspawners.utils.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface GUI {

    /**
     * Get this GUI's underlying Bukkit {@link Inventory}
     *
     * @return the Bukkit Inventory instance
     */
    public Inventory getInventory();

    /**
     * Open this GUI for the specified player
     *
     * @param player the player for whom to open the GUI
     */
    public void openFor(Player player);

    /**
     * Check whether the specified slot has an associated click action or not
     *
     * @param slot the slot to check
     * @return true if click action is present, false otherwise
     */
    public boolean hasClickAction(int slot);

    /**
     * Dispose of this GUI. Click actions will no longer be recognized and
     * any players viewing the GUI will be booted from viewing.
     */
    public void dispose();

}
package com.songoda.epicspawners.utils.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an action to be called upon clicking an object in the slot of a GUI.
 */
public interface Clickable {

    /**
     * Called when a GUI has been clicked. Whether the ItemStack at the slot clicked
     * is null or not will not affect the result of this method.
     *
     * @param player the player who performed the click
     * @param inventory the inventory in which was clicked (the GUI's inventory)
     * @param cursor the ItemStack on the player's cursor. May be null
     * @param slot the clicked inventory slot
     * @param type the type of click performed
     */
    public void click(Player player, Inventory inventory, ItemStack cursor, int slot, ClickType type);

}
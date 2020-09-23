package com.songoda.epicspawners.utils.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface Clickable {

    void onClick(Player player, Inventory inventory, ItemStack cursor, int slot, ClickType type);
}

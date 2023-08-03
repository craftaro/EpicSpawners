package com.craftaro.epicspawners.listeners;

import com.craftaro.core.compatibility.ServerVersion;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class InventoryListeners implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) {
            return;
        }

        if (event.getSlot() != 64537 &&
                event.getInventory().getType() == InventoryType.ANVIL &&
                event.getAction() != InventoryAction.NOTHING &&
                event.getCurrentItem().getType() != Material.AIR) {
            ItemStack item = event.getCurrentItem();
            if (item.getType() == (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))) {
                event.setCancelled(true);
            }
        }
    }
}

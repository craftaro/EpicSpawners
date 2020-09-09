package com.songoda.epicspawners.listeners;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.epicspawners.EpicSpawners;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

/**
 * Created by songoda on 2/25/2017.
 */
public class InventoryListeners implements Listener {

    private EpicSpawners plugin;

    public InventoryListeners(EpicSpawners plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;

        if (event.getSlot() != 64537) {
            if (event.getInventory().getType() == InventoryType.ANVIL) {
                if (event.getAction() != InventoryAction.NOTHING) {
                    if (event.getCurrentItem().getType() != Material.AIR) {
                        ItemStack item = event.getCurrentItem();
                        if (item.getType() == (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
}

package com.craftaro.epicspawners.listeners;

import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.spawners.spawner.PlacedSpawnerImpl;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by noahvdaa on 5/09/2021.
 */
public class WorldListeners implements Listener {

	private final EpicSpawners plugin;

	public WorldListeners(EpicSpawners plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent e) {
		// Unload previous spawners belonging to this world.
		for (PlacedSpawner ps : plugin.getSpawnerManager().getSpawners()) {
			if (e.getWorld().getName().equals(ps.getWorld().getName())) {
				plugin.getSpawnerManager().removeSpawnerFromWorld(ps);
			}
		}

		//Todo fix it
		// Load spawners back in.
//		plugin.getDataManager().getSpawners(new Consumer<Map<Location, PlacedSpawner>>() {
//			@Override
//			public void accept(Map<Location, PlacedSpawner> locationPlacedSpawnerMap) {
//				for(PlacedSpawner ps : locationPlacedSpawnerMap.values()){
//					if (e.getWorld().getName().equals(ps.getWorld().getName())) {
//						plugin.getSpawnerManager().addSpawnerToWorld(ps.getLocation(), ps);
//					}
//				}
//			}
//		});
	}

}

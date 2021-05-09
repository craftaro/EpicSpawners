package com.songoda.epicspawners.listeners;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.PlacedSpawner;
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

		// Load spawners back in.
		plugin.getDataManager().getSpawners(new Consumer<Map<Location, PlacedSpawner>>() {
			@Override
			public void accept(Map<Location, PlacedSpawner> locationPlacedSpawnerMap) {
				for(PlacedSpawner ps : locationPlacedSpawnerMap.values()){
					if (e.getWorld().getName().equals(ps.getWorld().getName())) {
						plugin.getSpawnerManager().addSpawnerToWorld(ps.getLocation(), ps);
					}
				}
			}
		});
	}

}

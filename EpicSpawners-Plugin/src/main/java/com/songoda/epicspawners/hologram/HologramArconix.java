package com.songoda.epicspawners.hologram;

import com.songoda.arconix.api.hologram.HologramObject;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import org.bukkit.Location;

public class HologramArconix extends Hologram {
    
    private com.songoda.arconix.api.packets.Hologram hologramManager;

    public HologramArconix(EpicSpawnersPlugin instance) {
        super(instance);
        this.hologramManager = Arconix.pl().getApi().packetLibrary.getHologramManager();
    }

    @Override
    public void add(Location location, String line) {
        fixLocation(location);
        HologramObject hologram = new HologramObject(null, location, line);
        hologramManager.addHologram(hologram);
    }

    @Override
    public void remove(Location location) {
        fixLocation(location);
        hologramManager.removeHologram(location, 1);
    }

    @Override
    public void update(Location location, String line) {
        fixLocation(location);
        HologramObject hologram = new HologramObject(null, location, line);
        hologramManager.addHologram(hologram);
    }

    private void fixLocation(Location location) {
        location.add(0.5, 1, 0.5);
    }
}

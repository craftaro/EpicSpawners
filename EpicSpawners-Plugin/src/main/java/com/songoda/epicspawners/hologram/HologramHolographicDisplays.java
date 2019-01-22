package com.songoda.epicspawners.hologram;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import org.bukkit.Location;


public class HologramHolographicDisplays extends Hologram {

    public HologramHolographicDisplays(EpicSpawnersPlugin instance) {
        super(instance);
    }

    @Override
    public void add(Location location, String line) {
        fixLocation(location);
        com.gmail.filoghost.holographicdisplays.api.Hologram hologram = HologramsAPI.createHologram(instance, location);
        hologram.appendTextLine(line);
    }

    @Override
    public void remove(Location location) {
        fixLocation(location);
        for (com.gmail.filoghost.holographicdisplays.api.Hologram hologram : HologramsAPI.getHolograms(instance)) {
            if (hologram.getX() != location.getX()
                    || hologram.getY() != location.getY()
                    || hologram.getZ() != location.getZ()) continue;
            hologram.delete();
        }
    }

    @Override
    public void update(Location location, String line) {
        fixLocation(location);
        for (com.gmail.filoghost.holographicdisplays.api.Hologram hologram : HologramsAPI.getHolograms(instance)) {
            if (hologram.getX() != location.getX()
                    || hologram.getY() != location.getY()
                    || hologram.getZ() != location.getZ()) continue;
            hologram.clearLines();
            hologram.appendTextLine(line);
        }
    }

    private void fixLocation(Location location) {
        location.add(0.5, 1.52, 0.5);
    }
}

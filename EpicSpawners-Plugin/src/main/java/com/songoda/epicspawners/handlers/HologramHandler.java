package com.songoda.epicspawners.handlers;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Created by songoda on 3/12/2017.
 */
public class HologramHandler {

    private final EpicSpawnersPlugin instance;

    public HologramHandler(EpicSpawnersPlugin instance) {
        this.instance = instance;
        loadHolograms();
    }

    public void loadHolograms() {
        if (instance.getConfig().getBoolean("Main.Spawners Have Holograms")) {

            Collection<Spawner> spawners = instance.getSpawnerManager().getSpawners();
            if (spawners.size() == 0) return;

            for (Spawner spawner : spawners) {
                if (spawner.getLocation().getWorld() == null) continue;
                updateHologram(spawner);
            }
        }
    }

    public void updateHologram(Spawner spawner) {
        try {
            if (spawner == null) return;
            Location olocation = spawner.getLocation();
            Location location = null;
            Block b = olocation.getBlock();
            if (b.getType() != Material.SPAWNER) return;
            String face = null;
            Collection<Entity> nearbyEntites = location.getWorld().getNearbyEntities(olocation, 5, 5, 5);
            for (Entity entity : nearbyEntites) {
                if (entity instanceof Player) {
                    face = Arconix.pl().getApi().getPlayer((Player) entity).getPlayerDirection();
                }
            }

            despawn(b);

            boolean go = true;
            if (face != null && b.getRelative(BlockFace.UP).getType() != Material.AIR) {
                if (b.getRelative(BlockFace.valueOf(face.toUpperCase())).getType() == Material.AIR) {
                    location = adjust(face.toUpperCase(), b);
                    go = false;
                }
            }

            if (go) {
                if (b.getRelative(BlockFace.UP).getType() == Material.AIR) {
                    location = adjust("UP", b);
                } else if (b.getRelative(BlockFace.NORTH).getType() == Material.AIR) {
                    location = adjust("NORTH", b);
                } else if (b.getRelative(BlockFace.SOUTH).getType() == Material.AIR) {
                    location = adjust("SOUTH", b);
                } else if (b.getRelative(BlockFace.EAST).getType() == Material.AIR) {
                    location = adjust("EAST", b);
                } else if (b.getRelative(BlockFace.WEST).getType() == Material.AIR) {
                    location = adjust("WEST", b);
                } else if (b.getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                    location = adjust("DOWN", b);
                }
            }

            if (location != null) {
                if ((boolean) instance.getConfig().get("Main.Spawners Have Holograms")) {
                    addHologram(location, spawner);
                }
            }

        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void despawn(Block b) {
        Arconix.pl().getApi().packetLibrary.getHologramManager().despawnHologram(adjust("UP", b));
        Arconix.pl().getApi().packetLibrary.getHologramManager().despawnHologram(adjust("NORTH", b));
        Arconix.pl().getApi().packetLibrary.getHologramManager().despawnHologram(adjust("SOUTH", b));
        Arconix.pl().getApi().packetLibrary.getHologramManager().despawnHologram(adjust("EAST", b));
        Arconix.pl().getApi().packetLibrary.getHologramManager().despawnHologram(adjust("WEST", b));
        Arconix.pl().getApi().packetLibrary.getHologramManager().despawnHologram(adjust("DOWN", b));
    }

    public Location adjust(String direction, Block b) {
        Location location = null;
        try {
            switch (direction) {
                case "UP":
                    location = b.getLocation().add(0.5, 1, 0.5);
                    break;
                case "DOWN":
                    location = b.getLocation().subtract(0, 0.75, 0);
                    location = location.add(0.5, 0, 0.5);
                    break;
                case "NORTH":
                    location = b.getLocation().subtract(0, 0, 0.5);
                    location = location.add(0.5, 0.15, 0);
                    break;
                case "SOUTH":
                    location = b.getLocation().add(0, 0, 1.5);
                    location = location.add(0.5, 0.15, 0);
                    break;
                case "EAST":
                    location = b.getLocation().add(1.5, 0, 0);
                    location = location.add(0, 0.15, 0.5);
                    break;
                case "WEST":
                    location = b.getLocation().subtract(0.5, 0, 0);
                    location = location.add(0, 0.15, 0.5);
                    break;
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return location;
    }

    public void addHologram(Location location, Spawner spawner) {
        try {
            int multi = spawner.getSpawnerDataCount();
            String name = Methods.compileName(instance.getSpawnerManager().getSpawnerData(spawner.getIdentifyingName()), multi, false);

            Arconix.pl().getApi().packetLibrary.getHologramManager().spawnHologram(location, name.trim());

        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void processChange(Block b) {
        try {
            Block spawner = null;
            if (b.getType() == Material.SPAWNER) {
                spawner = b;
            } else if (b.getRelative(BlockFace.UP).getType() == Material.SPAWNER) {
                spawner = b.getRelative(BlockFace.UP);
            } else if (b.getRelative(BlockFace.DOWN).getType() == Material.SPAWNER) {
                spawner = b.getRelative(BlockFace.DOWN);
            } else if (b.getRelative(BlockFace.NORTH).getType() == Material.SPAWNER) {
                spawner = b.getRelative(BlockFace.NORTH);
            } else if (b.getRelative(BlockFace.SOUTH).getType() == Material.SPAWNER) {
                spawner = b.getRelative(BlockFace.SOUTH);
            } else if (b.getRelative(BlockFace.WEST).getType() == Material.SPAWNER) {
                spawner = b.getRelative(BlockFace.WEST);
            } else if (b.getRelative(BlockFace.EAST).getType() == Material.SPAWNER) {
                spawner = b.getRelative(BlockFace.EAST);
            }
            if (spawner != null) {
                Spawner block = instance.getSpawnerManager().getSpawnerFromWorld(spawner.getLocation());
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> instance.getHologramHandler().updateHologram(block), 1L);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}
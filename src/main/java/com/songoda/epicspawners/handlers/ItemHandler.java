package com.songoda.epicspawners.handlers;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.Spawner;
import com.songoda.epicspawners.spawners.SpawnerItem;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by songo on 9/3/2017.
 */
public class ItemHandler {

    public ItemHandler() {
        try {
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(EpicSpawners.getInstance(), ItemHandler::dropItems, 10L, 20L);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void dropItems() {
        try {
            if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawner")) {
                ConfigurationSection cs3 = EpicSpawners.getInstance().dataFile.getConfig().getConfigurationSection("data.spawner");
                for (String key2 : cs3.getKeys(false)) {
                    Location spawnerLoc = Arconix.pl().getApi().serialize().unserializeLocation(key2);
                    int locx = spawnerLoc.getBlockX() >> 4;
                    int locz = spawnerLoc.getBlockZ() >> 4;
                    if (!spawnerLoc.getWorld().isChunkLoaded(locx, locz)) {
                        continue;
                    }

                    boolean playerFound = false;

                    Collection <Entity> nearbyEntite = Methods.getNearbyEntities(spawnerLoc.clone().add(0.5, 0.5, 0.5), 16, 16, 16);
                    if (nearbyEntite.size() >= 1) {
                        for (Entity ee : nearbyEntite) {
                            if (ee instanceof LivingEntity) {
                                if (ee instanceof Player) {
                                    playerFound = true;
                                }
                            }
                        }
                    }

                    if (playerFound) {
                        Spawner sp = new Spawner(spawnerLoc);
                        //if (EpicSpawners.getInstance().hooksFile.getConfig().contains("Entities." + sp.spawnedTypeU + ".items") || sp.spawnedTypeU.equalsIgnoreCase("OMNI")) {
                        if (EpicSpawners.getInstance().spawnerFile.getConfig().contains("Entities." + sp.spawnedType + ".items")) {
                            try {
                                String type = Methods.getType(sp.getSpawner().getSpawnedType());
                                if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(spawnerLoc) + ".type")) {
                                    if (!EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(spawnerLoc) + ".type").equals("OMNI")) {
                                        type = Methods.getTypeFromString(EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(spawnerLoc) + ".type"));
                                    }
                                }
                                List<SpawnerItem> list = EpicSpawners.getInstance().getApi().convertFromList(EpicSpawners.getInstance().dataFile.getConfig().getStringList("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(spawnerLoc) + ".entities"));
                                if (list.size() == 0)
                                    list.add(new SpawnerItem(type, 1));
                                for (SpawnerItem omni : list) {
                                    if (EpicSpawners.getInstance().spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(omni.getType()) + ".items")) {
                                        String key3 = key2 + omni.getType();

                                        int rate = EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(omni.getType()) + ".itemTickRate");
                                        if (EpicSpawners.getInstance().api.getSpawnerMultiplier(spawnerLoc) != 1)
                                            rate = rate / EpicSpawners.getInstance().api.getSpawnerMultiplier(spawnerLoc);

                                        long cur = (System.currentTimeMillis() / 1000);
                                        long next = cur + rate;

                                        if (EpicSpawners.getInstance().tickTracker.containsKey(key3)) {
                                            long goal = EpicSpawners.getInstance().tickTracker.get(key3);
                                            if (cur >= goal) {
                                                if (!sp.getSpawner().getBlock().isBlockPowered() && EpicSpawners.getInstance().getConfig().getBoolean("Main.Redstone Power Deactivates Spawners")) {
                                                    for (ItemStack item : (ArrayList<ItemStack>) EpicSpawners.getInstance().spawnerFile.getConfig().getList("Entities." + Methods.getTypeFromString(omni.getType()) + ".items")) {
                                                        Location loc = spawnerLoc.clone();
                                                        loc.add(.5, .9, .5);
                                                        Item it = loc.getWorld().dropItemNaturally(loc, item);

                                                        Random r = new Random();
                                                        double rx = -.2 + (.2 - -.2) * r.nextDouble();
                                                        r = new Random();
                                                        double ry = 0 + (.5 - 0) * r.nextDouble();
                                                        r = new Random();
                                                        double rz = -.2 + (.2 - -.2) * r.nextDouble();
                                                        it.setVelocity(new Vector(rx, ry, rz));
                                                    }
                                                }
                                                EpicSpawners.getInstance().tickTracker.put(key3, next);
                                            }
                                        } else
                                            EpicSpawners.getInstance().tickTracker.put(key3, next);
                                    }
                                }
                            } catch (Exception ignore) {
                            }
                        }
                        if (EpicSpawners.getInstance().spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(sp.spawnedType) + ".blocks")) {
                            for (String m : (ArrayList<String>) EpicSpawners.getInstance().spawnerFile.getConfig().getList("Entities." + Methods.getTypeFromString(sp.spawnedType) + ".blocks")) {

                                Material mat = Material.valueOf(m);
                                if (mat.isBlock()) {

                                    double x = 1;
                                    double y = 1;
                                    double z = 1;

                                    Location loc = spawnerLoc.clone();
                                    loc.add(.5, 0, .5);

                                    boolean good = false;
                                    int num = 0;
                                    while (!good && num != 150) {
                                        num ++;
                                        double testX = ThreadLocalRandom.current().nextDouble(-1, 1);
                                        double testY = ThreadLocalRandom.current().nextDouble(-1, 2);
                                        double testZ = ThreadLocalRandom.current().nextDouble(-1, 1);

                                        x = loc.getX() + testX * (double) 3;
                                        y = loc.getY() + testY;
                                        z = loc.getZ() + testZ * (double) 3;

                                        Location loc2 = new Location(loc.getWorld(), x, y, z);
                                        Methods.isAir(loc.getBlock().getType());
                                        Location loc3 = loc2.clone().subtract(0, 1, 0);

                                        if (Methods.isAir(loc2.getBlock().getType()) && !loc3.getBlock().getType().equals(Material.AIR))
                                            good = true;
                                    }
                                    if (good) {
                                        Location location = new Location(loc.getWorld(), x,y,z);
                                        location.getBlock().setType(mat);
                                    }


                                }
                            }
                        }
                        if (EpicSpawners.getInstance().spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(sp.spawnedType) + ".commands")) {
                            try {
                                String key3 = key2 + sp.spawnedType;

                                int rate = EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(sp.spawnedType) + ".commandTickRate");
                                if (EpicSpawners.getInstance().api.getSpawnerMultiplier(spawnerLoc) != 1)
                                    rate = rate / EpicSpawners.getInstance().api.getSpawnerMultiplier(spawnerLoc);

                                long cur = (System.currentTimeMillis() / 1000);
                                long next = cur + rate;

                                if (EpicSpawners.getInstance().tickTracker2.containsKey(key3)) {
                                    long goal = EpicSpawners.getInstance().tickTracker2.get(key3);
                                    if (cur >= goal) {
                                        if (!sp.getSpawner().getBlock().isBlockPowered() && EpicSpawners.getInstance().getConfig().getBoolean("Main.Redstone Power Deactivates Spawners")) {

                                            for (String cmd : (ArrayList<String>) EpicSpawners.getInstance().spawnerFile.getConfig().getList("Entities." + Methods.getTypeFromString(sp.spawnedType) + ".commands")) {
                                                Location loc = spawnerLoc.clone();
                                                loc.add(.5, 0, .5);

                                                double x = 1;
                                                double y = 1;
                                                double z = 1;

                                                boolean good = false;
                                                while (!good) {
                                                    double testX = ThreadLocalRandom.current().nextDouble(-1, 1);
                                                    double testY = ThreadLocalRandom.current().nextDouble(-1, 2);
                                                    double testZ = ThreadLocalRandom.current().nextDouble(-1, 1);

                                                    x = loc.getX() + testX * (double) 3;
                                                    y = loc.getY() + testY;
                                                    z = loc.getZ() + testZ * (double) 3;

                                                    Location loc2 = new Location(loc.getWorld(), x, y, z);
                                                    Methods.isAir(loc.getBlock().getType());
                                                    Location loc3 = loc2.clone().subtract(0, 1, 0);
                                                    if (Methods.isAir(loc2.getBlock().getType()) && !loc3.getBlock().getType().equals(Material.AIR))
                                                        good = true;
                                                }

                                                if (cmd.toLowerCase().contains("@x") || cmd.toLowerCase().contains("@y") || cmd.toLowerCase().contains("@z")) {
                                                    if (Methods.countEntitiesAroundLoation(spawnerLoc) > EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + sp.spawnedType + ".commandSpawnLimit") &&
                                                            EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + sp.spawnedType + ".commandSpawnLimit") != 0)
                                                        return;
                                                }
                                                boolean uP = true;
                                                if (cmd.toLowerCase().contains("@p")) {
                                                    uP = false;
                                                    List<String> arr = Arrays.asList(EpicSpawners.getInstance().getConfig().getString("Main.Radius To Search Around Spawner").split("x"));
                                                    Collection<Entity> nearbyEntitee = Methods.getNearbyEntities(loc.clone().add(0.5, 0.5, 0.5), Integer.parseInt(arr.get(0)), Integer.parseInt(arr.get(1)), Integer.parseInt(arr.get(2)));
                                                    if (nearbyEntitee.size() >= 1) {
                                                        for (Entity ee : nearbyEntitee) {
                                                            if (ee instanceof LivingEntity) {
                                                                if (ee instanceof Player) {
                                                                    uP = true;
                                                                    cmd = cmd.replace("@p", ee.getName());
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                cmd = cmd.replace("@x", Integer.toString((int) Math.ceil(x)));
                                                cmd = cmd.replace("@y", Integer.toString((int) Math.ceil(y)));
                                                cmd = cmd.replace("@z", Integer.toString((int) Math.ceil(z)));
                                                cmd = cmd.replace("@X", Integer.toString((int) Math.ceil(x)));
                                                cmd = cmd.replace("@Y", Integer.toString((int) Math.ceil(y)));
                                                cmd = cmd.replace("@Z", Integer.toString((int) Math.ceil(z)));

                                                if (uP)
                                                    Bukkit.getServer().dispatchCommand(EpicSpawners.getInstance().getServer().getConsoleSender(), cmd);

                                            }
                                        }
                                        EpicSpawners.getInstance().tickTracker2.put(key3, next);
                                    }
                                } else
                                    EpicSpawners.getInstance().tickTracker2.put(key3, next);
                            } catch (Exception ignore) {
                            }
                        }
                    }
                }
            }
        } catch (Exception ignore) {
        }
    }
}

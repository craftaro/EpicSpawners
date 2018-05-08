package com.songoda.epicspawners.Entity;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Spawners.SpawnerDropEvent;
import com.songoda.epicspawners.Spawners.SpawnerItem;
import com.songoda.epicspawners.Utils.Debugger;
import com.songoda.epicspawners.Utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by songoda on 2/25/2017.
 */
public class EPlayer {
    private Player p;

    public EPlayer(Player p) {
        this.p = p;
    }

    public void plus(Entity entity, int amt) {
        try {
            EpicSpawners instance = EpicSpawners.getInstance();

            if (instance.getConfig().getInt("Spawner Drops.Kills Needed for Drop") != 0 && instance.getConfig().getBoolean("Spawner Drops.Allow Killing Mobs To Drop Spawners") && p.hasPermission("epicspawners.Killcounter")) {
                String type = Methods.getType(entity.getType());
                if (instance.spawnerFile.getConfig().getBoolean("Entities." + type + ".Allowed")) {
                    String uuid = p.getUniqueId().toString();
                    int total = 0;
                    if (instance.dataFile.getConfig().getInt("data.kills." + uuid + "." + type) != 0)
                        total = instance.dataFile.getConfig().getInt("data.kills." + uuid + "." + type);
                    int goal = instance.getConfig().getInt("Spawner Drops.Kills Needed for Drop");
                    if (instance.spawnerFile.getConfig().getInt("Entities." + type + ".CustomGoal") != 0) {
                        goal = instance.spawnerFile.getConfig().getInt("Entities." + type + ".CustomGoal");
                    }
                    if (total > goal)
                        total = 1;
                    total = amt + total;

                    if (instance.getConfig().getInt("Spawner Drops.Alert Every X Before Drop") != 0) {
                        if (total % instance.getConfig().getInt("Spawner Drops.Alert Every X Before Drop") == 0 && total != goal) {
                            Arconix.pl().getApi().packetLibrary.getActionBarManager().sendActionBar(p, instance.getLocale().getMessage("event.goal.alert", goal - total, type));
                        }

                    }
                    if (total % goal == 0) {
                        dropSpawner(entity.getLocation(), 0, entity.getType().name());
                        instance.dataFile.getConfig().set("data.kills." + uuid + "." + type, 0);
                        Arconix.pl().getApi().packetLibrary.getActionBarManager().sendActionBar(p, instance.getLocale().getMessage("event.goal.reached", type));
                    } else
                        instance.dataFile.getConfig().set("data.kills." + uuid + "." + type, total);
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void dropSpawner(Location location, int multi, String type) {
        try {
            SpawnerDropEvent event = new SpawnerDropEvent(location, p);
            Bukkit.getPluginManager().callEvent(event);
            ItemStack item;

            if (!event.isCancelled()) {
                if (!type.toUpperCase().equals("OMNI")) {
                    item = EpicSpawners.getInstance().getApi().newSpawnerItem(Methods.restoreType(type), multi, 1);
                } else {
                    if (!p.isSneaking() || p.isSneaking() && !EpicSpawners.getInstance().getConfig().getBoolean("Main.Sneak To Recive A Stacked Spawner")) {
                        List<SpawnerItem> spawners = EpicSpawners.getInstance().getApi().convertFromList(EpicSpawners.getInstance().dataFile.getConfig().getStringList("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(location) + ".entities"));
                        List<ItemStack> items = EpicSpawners.getInstance().getApi().removeOmni(EpicSpawners.getInstance().getApi().newOmniSpawner(spawners));
                        item = items.get(0);
                        if (EpicSpawners.getInstance().getApi().getType(items.get(1)).equals("OMNI"))
                            EpicSpawners.getInstance().getApi().saveCustomSpawner(items.get(1), location.getBlock());
                    } else {
                        List<SpawnerItem> spawners = EpicSpawners.getInstance().getApi().convertFromList(EpicSpawners.getInstance().dataFile.getConfig().getStringList("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(location) + ".entities"));
                        item = EpicSpawners.getInstance().getApi().newOmniSpawner(spawners);
                    }
                }

                if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Add Spawner To Inventory On Drop") && p != null) {
                    if (p.getInventory().firstEmpty() == -1)
                        location.getWorld().dropItemNaturally(location, item);
                    else
                        p.getInventory().addItem(item);
                } else
                    location.getWorld().dropItemNaturally(location, item);
            }


        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public Player getP() {
        return p;
    }
}

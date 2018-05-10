package com.songoda.epicspawners.api;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.SpawnerItem;
import com.songoda.epicspawners.spawners.object.Spawner;
import com.songoda.epicspawners.spawners.object.SpawnerData;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by songoda on 3/17/2017.
 */
public class EpicSpawnersAPI {


    public int getSpawnerMultiplier(Location location) {
        int amt = 0;
        try {
            if (location.getBlock().getType() != Material.MOB_SPAWNER)
                return 0;
            else {
                return EpicSpawners.getInstance().getSpawnerManager().getSpawnerFromWorld(location).getSpawnerMultiplier();
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return amt;
    }

    public boolean setMultiplier(Location location, int multi) {
        try {
            EpicSpawners.getInstance().dataFile.getConfig().set("data.spawner." + Arconix.pl().getApi().serialize().serializeLocation(location), multi);
        } catch (Exception e) {
            Debugger.runReport(e);
            return false;
        }
        return true;
    }

    public String getSpawnerType(Block b) {
        return getSpawnerType(b.getLocation());
    }

    public String getSpawnerType(Location location) {
        return EpicSpawners.getInstance().getSpawnerManager().getSpawnerFromWorld(location).getName();
    }

    public void removeDisplayItem(Spawner spawner) {
        Location nloc = spawner.getLocation();
        nloc.add(.5, -.4, .5);
        List<Entity> near = (List<Entity>) nloc.getWorld().getNearbyEntities(nloc, 8, 8, 8);
        for (Entity e : near) {
            if (e.getLocation().getX() == nloc.getX() && e.getLocation().getY() == nloc.getY() && e.getLocation().getZ() == nloc.getZ()) {
                e.remove();
            }
        }
    }

    public void updateDisplayItem(Spawner spawner, SpawnerData spawnerData) {
        try {
            spawner.getCreatureSpawner().setSpawnedType(EntityType.DROPPED_ITEM);
            spawner.getCreatureSpawner().update();

            Location nloc = spawner.getLocation();
            nloc.add(.5, -.4, .5);
            removeDisplayItem(spawner);

            try {
                EntityType next = EntityType.valueOf(Methods.restoreType(spawnerData.getName()));
                spawner.getCreatureSpawner().setSpawnedType(next);
                spawner.getCreatureSpawner().update();
            } catch (Exception ex) {

                Location location = spawner.getLocation();

                location.setPitch(-360);

                ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(nloc, EntityType.ARMOR_STAND);
                as.setSmall(true);
                as.setVisible(false);
                as.setCustomNameVisible(false);
                as.setGravity(false);
                as.setCanPickupItems(false);
                as.setBasePlate(true);
                try {
                    if (spawner.getFirstStack().getSpawnerData().getDisplayItem() != null) {
                        as.setHelmet(new ItemStack(spawnerData.getDisplayItem()));
                    } else {
                        as.setHelmet(new ItemStack(Material.DIRT));
                    }
                } catch (Exception ee) {
                    as.setHelmet(new ItemStack(Material.DIRT));
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public ItemStack newSpawnerItem(SpawnerItem item, int amount) {
        return newSpawnerItem(item.getType(), item.getMulti(), amount);
    }

    public ItemStack newSpawnerItem(String type, int amount) {
        try {
            return newSpawnerItem(type, 0, amount);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public ItemStack newSpawnerItem(EntityType type, int amount) {
        try {
            try {
                return newSpawnerItem(type.name(), 0, amount);
            } catch (Exception e) {
                return newSpawnerItem("PIG", 0, amount);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public ItemStack newSpawnerItem(String type, int multi, int amount) {
        try {
            if (multi == 0)
                multi = 1;
            ItemStack item = new ItemStack(Material.MOB_SPAWNER, amount);
            ItemMeta itemmeta = item.getItemMeta();
            String name = Methods.compileName(type, multi, true);
            itemmeta.setDisplayName(name);

            /* BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
            CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();

            cs.setSpawnedType(type);
            bsm.setBlockState(cs);
            item.setItemMeta(bsm); */

            item.setItemMeta(itemmeta);
            return item;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public boolean isOmniBlock(Location location) {
        if (location != null) {
            if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(location) + ".type")) {
                return EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(location) + ".type").equals("OMNI");
            }
        }
        return false;
    }

    public void clearOmni(Location location) {
        try {
            EpicSpawners.getInstance().dataFile.getConfig().set("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(location) + ".entities", null);
            EpicSpawners.getInstance().dataFile.getConfig().set("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(location) + ".type", null);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void saveCustomSpawner(ItemStack item, Block b) {
        try {
            if (getIType(item).equals("OMNI")) {
                EpicSpawners.getInstance().dataFile.getConfig().set("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(b) + ".type", getIType(item));

                List<SpawnerItem> entities = getOmniList(item);

                List<String> ents = new ArrayList<>();
                for (SpawnerItem spawner : entities)
                    ents.add(spawner.getType() + "-" + spawner.getMulti());
                EpicSpawners.getInstance().dataFile.getConfig().set("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(b) + ".entities", ents);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public List<SpawnerItem> getOmniList(ItemStack item) {
        List<SpawnerItem> entities = new ArrayList<>();
        try {
            String[] arr = (item.getItemMeta().getLore().get(0).replace("§", "")).split(":");
            int num = 1;
            for (String str : arr) {
                if (arr.length != num) {
                    String[] arr2 = str.split("-");
                    entities.add(new SpawnerItem(arr2[0], Integer.parseInt(arr2[1])));
                }
                num++;
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return entities;
    }

    public int getIMulti(ItemStack item) {
        try {
            if (item.getItemMeta().getDisplayName().contains("§fSpaw§fner")) {
                if (item.getItemMeta().getDisplayName().contains("§fSpaw§fner §c")) {
                    String arr[] = item.getItemMeta().getDisplayName().split("§fSpaw§fner §c");
                    return Integer.parseInt(arr[1]);
                }
                return 1;
            } else {
                String arr[] = (item.getItemMeta().getDisplayName().replace("§", "")).split(":");
                return Integer.parseInt(arr[1]);
            }
        } catch (Exception e) {
            return 1;
        }
    }

    public String getType(ItemStack item) {
        if (getIType(item) != null && !getIType(item).equals(""))
            return Methods.restoreType(getIType(item));
        else
            return null;
    }

    public String getIType(ItemStack item) {
        try {
            if (item == null) return null;
            try {
                if (item.getItemMeta().getDisplayName().contains(":")) {
                    String arr[] = (item.getItemMeta().getDisplayName().replace("§", "")).split(":");
                    return arr[0].toLowerCase().replace("_", " ");
                }
                for (final EntityType value : EntityType.values()) {
                    if (!value.isSpawnable() || !value.isAlive()) continue;
                    String str = item.getItemMeta().getDisplayName().toLowerCase();
                    str = str.replace("_", " ");

                    if (str.contains(value.name().toLowerCase()))
                        return value.toString().toLowerCase().replace("_", " ");

                }
                return null;
            } catch (Exception e) {
                BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
                CreatureSpawner bs = (CreatureSpawner) bsm.getBlockState();
                return bs.getSpawnedType().toString();
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public List<SpawnerItem> convertFromList(List<String> list) {
        try {
            List<SpawnerItem> entities = new ArrayList<>();

            for (String ent : list) {
                String[] arr2 = ent.split("-");
                entities.add(new SpawnerItem(arr2[0], Integer.parseInt(arr2[1])));
            }
            return entities;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public List<ItemStack> removeSpawner(ItemStack item) {
        try {
            List<ItemStack> items = new ArrayList<>();

            int multi = getIMulti(item);

            if (multi > 1) {

                items.add(newSpawnerItem(getIType(item), multi - 1, 1));
                items.add(newSpawnerItem(getIType(item), 1, 1));
            }
            return items;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }


    public int removeBoosts(String type, String value) {
        int removes = 0;
        try {
            ConfigurationSection cs = EpicSpawners.getInstance().dataFile.getConfig().getConfigurationSection("data.boosts");
            for (String key : cs.getKeys(false)) {
                if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.boosts." + key + "." + type)) {
                    if (EpicSpawners.getInstance().dataFile.getConfig().getString("data.boosts." + key + "." + type).equals(value)) {
                        EpicSpawners.getInstance().dataFile.getConfig().set("data.boosts." + key, null);
                        removes++;
                    }
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return removes;
    }
}

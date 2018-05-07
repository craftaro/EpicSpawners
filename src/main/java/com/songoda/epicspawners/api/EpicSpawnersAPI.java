package com.songoda.epicspawners.api;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.Spawner;
import com.songoda.epicspawners.spawners.SpawnerItem;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.ChatColor;
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
                Spawner spawner = new Spawner(location);
                amt = 1;
                if (location.getBlock() != null)
                    amt = spawner.getMulti();
                return amt;
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
        Spawner eSpawner = new Spawner(location);
        return eSpawner.spawnedTypeU;
    }

    public ItemStack addOmniSpawner(SpawnerItem spawner, ItemStack item) {
        try {
            List<SpawnerItem> ents = getOmniList(item);

            boolean hit = false;
            boolean maxed = false;
            for (SpawnerItem ent : ents) {
                if (spawner.getType().equals(ent.getType())) {
                    if (ent.getMulti() == EpicSpawners.getInstance().getConfig().getInt("settings.Spawner-max")) {
                        maxed = true;
                    } else {
                        ent.setMulti(ent.getMulti() + spawner.getMulti());
                    }
                    hit = true;
                }
            }
            if (hit || ents.size() < EpicSpawners.getInstance().getConfig().getInt("Main.Max Spawners Inside A OmniSpawner")) {
                if (!hit) {
                    ents.add(spawner);
                }
                if (!maxed) {
                    return newOmniSpawner(ents);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            //Debugger.runReport(e);
        }
        return null;
    }

    public ItemStack newOmniSpawner(SpawnerItem spawner, SpawnerItem spawner2) {
        try {
            List<SpawnerItem> ents = new ArrayList<>();
            ents.add(spawner2);
            ents.add(spawner);
            return newOmniSpawner(ents);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public ItemStack newOmniSpawner(List<SpawnerItem> ents) {
        try {
            ItemStack item = new ItemStack(Material.MOB_SPAWNER, 1);
            ItemMeta meta = item.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            StringBuilder str = new StringBuilder();
            String str2 = getOmniString(ents);
            for (SpawnerItem spawner : ents) {
                str.append(spawner.getType()).append("-").append(spawner.getMulti()).append(":");
            }
            meta.setDisplayName(Methods.compileName("OMNI", 1, true));
            lore.add(Arconix.pl().getApi().format().formatText(Arconix.pl().getApi().format().convertToInvisibleString(str.toString()) + str2));
            meta.setLore(lore);
            item.setItemMeta(meta);
            return item;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public String getOmniString(List<SpawnerItem> ents) {
        try {
            StringBuilder str2 = new StringBuilder("&7");
            for (SpawnerItem spawner : ents) {
                str2.append(Methods.properType(spawner.getType())).append(" ").append(spawner.getMulti()).append("x, ");
            }
            return str2.substring(0, str2.length() - 2);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public void updateDisplayItem(String type, Location location) {
        try {
            Spawner eSpawner = new Spawner(location);
            eSpawner.getSpawner().setSpawnedType(EntityType.DROPPED_ITEM);
            eSpawner.update();

            Location nloc = location.clone();
            nloc.add(.5, -.4, .5);
            List<Entity> near = (List<Entity>) nloc.getWorld().getNearbyEntities(nloc, 8, 8, 8);
            for (Entity e : near) {
                if (e.getLocation().getX() == nloc.getX() && e.getLocation().getY() == nloc.getY() && e.getLocation().getZ() == nloc.getZ()) {
                    e.remove();
                }
            }

            try {
                EntityType next = EntityType.valueOf(Methods.restoreType(type));
                eSpawner.getSpawner().setSpawnedType(next);
                eSpawner.update();
            } catch (Exception ex) {

                location.setPitch(-360);

                ArmorStand as = (ArmorStand) location.getWorld().spawnEntity(nloc, EntityType.ARMOR_STAND);
                as.setSmall(true);
                as.setVisible(false);
                as.setCustomNameVisible(false);
                as.setGravity(false);
                as.setCanPickupItems(false);
                as.setBasePlate(true);
                try {
                    if (EpicSpawners.getInstance().spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(type) + ".Display-Item")) {
                        as.setHelmet(new ItemStack(Material.valueOf(EpicSpawners.getInstance().spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(type) + ".Display-Item"))));
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
            if (item != null) {
                try {
                    if (item.getItemMeta().getDisplayName().contains("§fSpaw§fner")) {
                        String[] arr = ChatColor.stripColor(item.getItemMeta().getDisplayName()).split(" Spawner");
                        String otype = arr[0];
                        return otype.replaceAll(" ", "_").toUpperCase();
                    } else if (item.getItemMeta().getDisplayName().contains(":")) {
                        String arr[] = (item.getItemMeta().getDisplayName().replace("§", "")).split(":");
                        return arr[0];
                    } else {
                        for (final EntityType value : EntityType.values()) {
                            if (value.isSpawnable() && value.isAlive()) {
                                String str = item.getItemMeta().getDisplayName().toLowerCase();
                                str = str.replace(" ", "_");
                                if (str.contains(value.name().toLowerCase())) {
                                    return value.toString();
                                }
                            }
                        }
                    }
                    return "";
                } catch (Exception e) {
                    BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
                    CreatureSpawner bs = (CreatureSpawner) bsm.getBlockState();
                    return bs.getSpawnedType().toString();
                }
            } else {
                return "";
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

    public List<ItemStack> removeOmni(ItemStack item) {
        try {
            List<ItemStack> items = new ArrayList<>();
            List<SpawnerItem> entities = getOmniList(item);

            items.add(newSpawnerItem(entities.get(entities.size() - 1), 1));

            entities.remove(entities.size() - 1);
            if (entities.size() == 1) {
                items.add(newSpawnerItem(entities.get(entities.size() - 1), 1));
            } else {
                items.add(newOmniSpawner(entities));
            }
            return items;
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

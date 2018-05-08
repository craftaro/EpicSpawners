package com.songoda.epicspawners.Spawners;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Utils.Debugger;
import com.songoda.epicspawners.Utils.Methods;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by songoda on 2/24/2017.
 */
public class Spawner {

    Location location = null;
    public String locationStr = null;
    public CreatureSpawner spawner = null;
    public String spawnedType = null;
    public String spawnedTypeU = null;

    public Spawner() {
    }

    public Spawner(Block b) {
        try {
            if (b != null) {
                if (b.getType() == Material.MOB_SPAWNER) {
                    String loc = Arconix.pl().getApi().serialize().serializeLocation(b);
                    defineBlockInformation(loc);
                }
            }

        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public Spawner(Location location) {
        try {
            if (location.getBlock() != null) {
                if (location.getBlock().getType() == Material.MOB_SPAWNER) {
                    String loc = Arconix.pl().getApi().serialize().serializeLocation(location.getBlock());
                    defineBlockInformation(loc);
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    private void defineBlockInformation(String loc) {
        try {
            locationStr = loc;
            location = Arconix.pl().getApi().serialize().unserializeLocation(loc);

            spawner = (CreatureSpawner) location.getBlock().getState();

            spawnedType = Methods.getType(spawner.getSpawnedType());
            spawnedTypeU = spawner.getSpawnedType().name();

            if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(location) + ".type")) {
                if (EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(location) + ".type").equals("OMNI")) {
                    spawnedType = "Omni";
                    spawnedTypeU = "OMNI";
                } else {
                    spawnedType = Methods.getTypeFromString(EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(location) + ".type"));
                    spawnedTypeU = EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(location) + ".type");
                }
            }

        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public boolean isSpawningOnFire() {
        try {
            return EpicSpawners.getInstance().spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(Methods.getTypeFromString(spawnedType)) + ".Spawn-On-Fire");
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return false;
    }

    public boolean isSpawningWithAI() {
        try {
            return EpicSpawners.getInstance().spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(Methods.getTypeFromString(spawnedType)) + ".Has-AI");
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return false;
    }

    public int getMulti() {
        try {
            return EpicSpawners.getInstance().dataFile.getConfig().getInt("data.spawner." + locationStr);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return 0;
    }

    public int getSpawns() {
        try {
            return EpicSpawners.getInstance().dataFile.getConfig().getInt("data.spawnerstats." + locationStr + ".spawns");
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return 0;
    }

    public void updateDelay() {
        try {
            if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Default Minecraft Spawner Cooldowns")) {
                int multi = getMulti();

                if (getMulti() == 0)
                    multi = 1;

                if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawnerstats." + locationStr + ".type")) {
                    if (EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + locationStr + ".type").equals("OMNI")) {
                        List<SpawnerItem> list = EpicSpawners.getInstance().getApi().convertFromList(EpicSpawners.getInstance().dataFile.getConfig().getStringList("data.spawnerstats." + locationStr + ".entities"));
                        for (SpawnerItem ent : list) {
                            if (multi > ent.getMulti())
                                multi = ent.getMulti();
                        }
                    }
                }

                String equation = EpicSpawners.getInstance().getConfig().getString("Main.Equations.Cooldown Between Spawns");
                if (getSpawner() != null) {

                    int delay;
                    if (!EpicSpawners.getInstance().cache.containsKey(equation)) {
                        ScriptEngineManager mgr = new ScriptEngineManager();
                        ScriptEngine engine = mgr.getEngineByName("JavaScript");
                        Random rand = new Random();
                        equation = equation.replace("{DEFAULT}", Integer.toString(rand.nextInt(800) + 200));
                        equation = equation.replace("{MULTI}", Integer.toString(multi));
                        delay = (int) Math.round(Double.parseDouble(engine.eval(equation).toString()));
                        EpicSpawners.getInstance().cache.put(equation, delay);
                    } else {
                        delay = EpicSpawners.getInstance().cache.get(equation);
                    }

                    getSpawner().setDelay(delay);
                    update();
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public boolean processCombine(Player p, ItemStack item, ItemStack item2) {
        try {
            EpicSpawners instance = EpicSpawners.getInstance();

            if (instance.getConfig().getBoolean("Main.OmniSpawners Enabled")) {
                if (instance.getApi().getIType(item).equals("OMNI") && !instance.getApi().isOmniBlock(location) || instance.getApi().getIType(item2).equals("OMNI") && !instance.getApi().isOmniBlock(location)) {
                    if (instance.getApi().getIType(item).equals("OMNI") && instance.getApi().getIType(item2).equals("OMNI")) {
                        return false;
                    } else {
                        if (instance.getApi().getType(item).equals("OMNI")) {
                            if (instance.getApi().addOmniSpawner(new SpawnerItem(instance.getApi().getType(item2), instance.getApi().getIMulti(item2)), item) != null) {
                                item2.setItemMeta(instance.getApi().addOmniSpawner(new SpawnerItem(instance.getApi().getType(item2), instance.getApi().getIMulti(item2)), item).getItemMeta());
                                return true;
                            }
                        } else {
                            if (instance.getApi().addOmniSpawner(new SpawnerItem(instance.getApi().getType(item), instance.getApi().getIMulti(item)), item2) != null) {
                                item2.setItemMeta(instance.getApi().addOmniSpawner(new SpawnerItem(instance.getApi().getType(item), instance.getApi().getIMulti(item)), item2).getItemMeta());
                                return true;
                            }
                        }
                        return false;
                    }
                }
            }

            int bmulti;
            String btype;
            if (item2 == null) {
                if (instance.dataFile.getConfig().contains("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(getSpawner().getBlock()) + ".type"))
                    btype = instance.dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(getSpawner().getBlock()) + ".type");
                else
                    btype = getSpawner().getSpawnedType().name();
                bmulti = getMulti();
            } else {
                btype = instance.getApi().getType(item2);
                bmulti = instance.getApi().getIMulti(item2);
            }

            if (instance.getApi().isOmniBlock(location)) {
                if (instance.getConfig().getBoolean("Main.OmniSpawners Enabled")) {
                    if (instance.getApi().getIType(item).equals("OMNI")) {
                        p.sendMessage(instance.getLocale().getMessage("event.omni.cannotmerge"));
                        return false;
                    }
                    List<SpawnerItem> spawners = instance.getApi().convertFromList(instance.dataFile.getConfig().getStringList("data.spawnerstats." + locationStr + ".entities"));

                    if (instance.getApi().addOmniSpawner(new SpawnerItem(instance.getApi().getType(item), instance.getApi().getIMulti(item)), instance.getApi().newOmniSpawner(spawners)) == null) {
                        p.sendMessage(instance.getLocale().getMessage("event.omni.combinefail"));
                    } else {
                        instance.getApi().saveCustomSpawner(instance.getApi().addOmniSpawner(new SpawnerItem(instance.getApi().getType(item), instance.getApi().getIMulti(item)), instance.getApi().newOmniSpawner(spawners)), location.getBlock());
                        p.sendMessage(instance.getLocale().getMessage("event.omni.combinesuccess"));
                        if (item2 == null) {
                            if (p.getItemInHand().getType().equals(Material.MOB_SPAWNER))
                                Methods.takeItem(p, 1);
                        }
                    }
                }
            } else {
                int imulti = instance.getApi().getIMulti(item);
                int newMulti = bmulti + imulti;

                String itype = instance.getApi().getType(item);

                if (!btype.equals(itype)) {

                    if (instance.getConfig().getBoolean("Main.OmniSpawners Enabled")) {
                        if (item2 != null) {
                            item2.setItemMeta(instance.getApi().newOmniSpawner(new SpawnerItem(btype, bmulti), new SpawnerItem(itype, imulti)).getItemMeta()); //here
                            return true;
                        } else {
                            instance.getApi().saveCustomSpawner(instance.getApi().newOmniSpawner(new SpawnerItem(btype, bmulti), new SpawnerItem(itype, imulti)), location.getBlock());
                            instance.holo.processChange(location.getBlock());
                            if (p.getItemInHand().getType().equals(Material.MOB_SPAWNER))
                                Methods.takeItem(p, 1);
                            return false;
                        }
                    } else {
                        p.sendMessage(instance.getLocale().getMessage("&7These spawners cannot be combined because they're different mobs."));
                    }
                } else {
                    if (newMulti <= instance.getConfig().getInt("Main.Spawner Max Upgrade")) {
                        if (p.getGameMode() != GameMode.CREATIVE) {
                            if (p.getItemInHand().getType().equals(Material.MOB_SPAWNER))
                                Methods.takeItem(p, 1);
                        }
                        upgradeFinal(p, bmulti, newMulti, item, item2);
                        return true;
                    } else {
                        if (bmulti == instance.getConfig().getInt("Main.Spawner Max Upgrade")) {
                            p.sendMessage(instance.getLocale().getMessage("event.upgrade.maxed"));
                        } else {
                            if (p.getGameMode() != GameMode.CREATIVE) {
                                if (p.getItemInHand().getType().equals(Material.MOB_SPAWNER))
                                    Methods.takeItem(p, 1);
                            }
                            int newamt = imulti - (instance.getConfig().getInt("Main.Spawner Max Upgrade") - bmulti);
                            ItemStack spawnerItem = new ItemStack(Material.MOB_SPAWNER);
                            ItemMeta itemMeta = spawnerItem.getItemMeta();
                            String name = Methods.compileName(itype, newamt, true);
                            itemMeta.setDisplayName(name);
                            spawnerItem.setItemMeta(itemMeta);
                            p.getInventory().addItem(spawnerItem);
                            upgradeFinal(p, bmulti, instance.getConfig().getInt("Main.Spawner Max Upgrade"), item, item2);
                            return true;
                        }
                    }
                }
            }

        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return false;
    }

    public void upgrade(Player p, String type) {
        try {
            int multi = getMulti();
            if (multi == 0) {
                multi = 1;
            }
            String typ = spawnedType;


            int cost = getCost(type, typ, multi);

            boolean maxed = false;

            if (multi == EpicSpawners.getInstance().getConfig().getInt("Main.Spawner Max Upgrade")) {
                maxed = true;
            }
            if (maxed) {
                p.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.upgrade.maxed"));
            } else {
                if (type.equals("ECO")) {
                    if (EpicSpawners.getInstance().getServer().getPluginManager().getPlugin("Vault") != null) {
                        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = EpicSpawners.getInstance().getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                        net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
                        if (econ.has(p, cost)) {
                            econ.withdrawPlayer(p, cost);
                            upgradeFinal(p, multi, multi + 1, null, null);
                        } else {
                            p.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
                        }
                    } else {
                        p.sendMessage("Vault is not installed.");
                    }
                } else if (type.equals("XP")) {
                    if (p.getLevel() >= cost || p.getGameMode() == GameMode.CREATIVE) {
                        if (p.getGameMode() != GameMode.CREATIVE) {
                            p.setLevel(p.getLevel() - cost);
                        }
                        upgradeFinal(p, multi, multi + 1, null, null);
                    } else {
                        p.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
                    }
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public boolean canBreak() {
        try {
            if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Only Drop Placed Spawners")) {
                return EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawner." + locationStr);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return true;
    }

    public boolean canCharge() {
        try {
            if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Only Charge Natural Spawners")) {
                if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawner." + locationStr)) {
                    return false;
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return true;
    }

    public void upgradeFinal(Player p, int oldMulti, int multi, ItemStack item, ItemStack item2) {
        try {
            String amt = Integer.toString(multi);

            if ((EpicSpawners.getInstance().getConfig().getBoolean("Main.Named Spawners Tiers")) && multi >= 0) {
                if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Named Spawners Tiers") && EpicSpawners.getInstance().getConfig().getStringList("Main.Tier Names").size() + 1 > multi) {
                    amt = EpicSpawners.getInstance().getConfig().getStringList("Main.Tier Names").get(multi - 1);
                }
            }
            if (multi != EpicSpawners.getInstance().getConfig().getInt("Main.Spawner Max Upgrade")) {
                p.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.upgrade.success", amt));
            } else {
                p.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.upgrade.successmaxed", amt));
            }
            if (item2 == null) {
                SpawnerChangeEvent event = new SpawnerChangeEvent(location, p, multi, oldMulti);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return;
                }

                EpicSpawners.getInstance().dataFile.getConfig().set("data.spawner." + locationStr, multi);
                Location loc = location.clone();
                loc.setX(loc.getX() + .5);
                loc.setY(loc.getY() + .5);
                loc.setZ(loc.getZ() + .5);
                if (!EpicSpawners.getInstance().v1_8 && !EpicSpawners.getInstance().v1_7) {
                    p.getWorld().spawnParticle(org.bukkit.Particle.valueOf(EpicSpawners.getInstance().getConfig().getString("Main.Upgrade Particle Type")), loc, 100, .5, .5, .5);
                } else {
                    p.getWorld().playEffect(loc, org.bukkit.Effect.valueOf(EpicSpawners.getInstance().getConfig().getString("Main.Upgrade Particle Type")), 1, 0);
                    //Doesn't resolve --Nova
                    //p.getWorld().spigot().playEffect(loc, org.bukkit.Effect.valueOf(EpicSpawners.getInstance().getConfig().getString("Main.Upgrade Particle Type")), 1, 0, (float) 1, (float) 1, (float) 1, 1, 100, 10);
                }
                float x = (float) (0 + (Math.random() * 1));
                float y = (float) (0 + (Math.random() * 2));
                float z = (float) (0 + (Math.random() * 1));
                Arconix.pl().getApi().packetLibrary.getParticleManager().broadcastParticle(loc, x, y, z, 0, EpicSpawners.getInstance().getConfig().getString("Entity.Spawn Particle Effect"), 15);
            } else {
                ItemMeta meta = item.getItemMeta().clone();
                meta.setDisplayName(Methods.compileName(EpicSpawners.getInstance().getApi().getType(item.clone()), multi, true));
                item2.setItemMeta(meta);
            }
            if (!EpicSpawners.getInstance().getConfig().getBoolean("Main.Sounds Enabled")) {
                return;
            }
            if (multi != EpicSpawners.getInstance().getConfig().getInt("Main.Spawner Max Upgrade")) {
                if (!EpicSpawners.getInstance().v1_8 && !EpicSpawners.getInstance().v1_7) {
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6F, 15.0F);
                } else {
                    p.playSound(p.getLocation(), Sound.valueOf("LEVEL_UP"), 2F, 15.0F);
                }
            } else {
                if (!EpicSpawners.getInstance().v1_10 && !EpicSpawners.getInstance().v1_9 && !EpicSpawners.getInstance().v1_8 && !EpicSpawners.getInstance().v1_7) {
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2F, 25.0F);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_CHIME, 2F, 25.0F);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(EpicSpawners.getInstance(), () -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_CHIME, 1.2F, 35.0F), 5L);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(EpicSpawners.getInstance(), () -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_CHIME, 1.8F, 35.0F), 10L);
                } else {
                    p.playSound(p.getLocation(), Sound.valueOf("LEVEL_UP"), 2F, 25.0F);
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public Date getBoostEnd() {
        try {
            if (!EpicSpawners.getInstance().dataFile.getConfig().contains("data.boosts")) {
                return null;
            }
            ConfigurationSection cs = EpicSpawners.getInstance().dataFile.getConfig().getConfigurationSection("data.boosts");
            for (String key : cs.getKeys(true)) {
                if (!EpicSpawners.getInstance().dataFile.getConfig().contains("data.boosts." + key + ".boosted")) {
                    continue;
                }
                long endd = EpicSpawners.getInstance().dataFile.getConfig().getLong("data.boosts." + key + ".end");
                Date end;
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(endd);
                end = calendar.getTime();
                return end;
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public String getOmniState() {
        try {
            if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawnerstats." + locationStr + ".OmniState")) {
                return EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + locationStr + ".OmniState");
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public void setOmniState(String state) {
        try {
            EpicSpawners.getInstance().dataFile.getConfig().set("data.spawnerstats." + locationStr + ".OmniState", state);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public int getBoost() {
        try {
            if (!EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawnerstats." + locationStr + ".player")) {
                return 0;
            }
            OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + locationStr + ".player")));
            if (!EpicSpawners.getInstance().dataFile.getConfig().contains("data.boosts")) {
                return 0;
            }
            ConfigurationSection cs = EpicSpawners.getInstance().dataFile.getConfig().getConfigurationSection("data.boosts");
            for (String key : cs.getKeys(false)) {
                if (!EpicSpawners.getInstance().dataFile.getConfig().contains("data.boosts." + key + ".boosted")) {
                    continue;
                }

                int boost = EpicSpawners.getInstance().dataFile.getConfig().getInt("data.boosts." + key + ".boosted");

                Date today = new Date();
                Date end = getBoostEnd();
                if (today.after(end)) {
                    EpicSpawners.getInstance().dataFile.getConfig().set("data.boosts." + key, null);
                    continue;
                }

                boolean go = false;
                if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.boosts." + key + ".player")) {
                    OfflinePlayer p2 = Bukkit.getOfflinePlayer(UUID.fromString(EpicSpawners.getInstance().dataFile.getConfig().getString("data.boosts." + key + ".player")));
                    if (p2.equals(p))
                        go = true;
                } else if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.boosts." + key + ".faction")) {
                    String id = EpicSpawners.getInstance().dataFile.getConfig().getString("data.boosts." + key + ".faction");
                    if (EpicSpawners.getInstance().hooks.isInFaction(id, location))
                        go = true;
                } else if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.boosts." + key + ".town")) {
                    String id = EpicSpawners.getInstance().dataFile.getConfig().getString("data.boosts." + key + ".town");
                    if (EpicSpawners.getInstance().hooks.isInTown(id, location))
                        go = true;
                } else if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.boosts." + key + ".island")) {
                    String id = EpicSpawners.getInstance().dataFile.getConfig().getString("data.boosts." + key + ".island");
                    if (EpicSpawners.getInstance().hooks.isInIsland(id, location))
                        go = true;
                } else if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.boosts." + key + ".location")) {
                    String loc = EpicSpawners.getInstance().dataFile.getConfig().getString("data.boosts." + key + ".location");
                    if (loc.equals(locationStr))
                        go = true;
                }
                if (go) {
                    return boost;
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return 0;
    }

    public void downgradeFinal(Player p, int multi, int oldMulti, String type) {
        try {
            EpicSpawners.getInstance().holo.processChange(location.getBlock());
            if (multi >= 1) {
                EpicSpawners.getInstance().dataFile.getConfig().set("data.spawner." + locationStr, multi);

                if (type.equals("Omni")) {
                    List<SpawnerItem> spawners = EpicSpawners.getInstance().getApi().convertFromList(EpicSpawners.getInstance().dataFile.getConfig().getStringList("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(location) + ".entities"));
                    List<SpawnerItem> items = EpicSpawners.getInstance().getApi().getOmniList(EpicSpawners.getInstance().getApi().newOmniSpawner(spawners));
                    List<ItemStack> items2 = EpicSpawners.getInstance().getApi().removeOmni(EpicSpawners.getInstance().getApi().newOmniSpawner(spawners));
                    if (items.size() != 0) {
                        p.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.omni.takespawner", Methods.compileName(items.get(items.size() - 1).getType(), items.get(items.size() - 1).getMulti(), true)));
                        EpicSpawners.getInstance().getApi().clearOmni(location);
                        EpicSpawners.getInstance().dataFile.getConfig().set("data.spawner." + Arconix.pl().getApi().serialize().serializeLocation(location), EpicSpawners.getInstance().getApi().getIMulti(items2.get(1)));


                        boolean isCustom = false;
                        try {
                            setSpawner(EntityType.valueOf(type));
                        } catch (Exception ex) {
                            isCustom = true;
                        }
                        if (isCustom) {
                            setSpawner(EntityType.valueOf("DROPPED_ITEM"));
                            EpicSpawners.getInstance().dataFile.getConfig().set("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(location) + ".type", EpicSpawners.getInstance().getApi().getIType(items2.get(1)));
                        } else {
                            getSpawner().setSpawnedType(EntityType.valueOf(EpicSpawners.getInstance().getApi().getIType(items2.get(1))));
                        }
                        spawner.update();
                    }
                } else {
                    String amt = Integer.toString(multi);

                    if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Named Spawners Tiers")) {
                        if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Named Spawners Tiers") && EpicSpawners.getInstance().getConfig().getStringList("Main.Tier Names").size() + 1 > multi) {
                            amt = EpicSpawners.getInstance().getConfig().getStringList("Main.Tier Names").get(multi - 1);
                        }
                    }
                    p.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.downgrade.success", Arconix.pl().getApi().format().formatText(amt)));
                }
            } else {
                SpawnerChangeEvent event = new SpawnerChangeEvent(location, p, multi, oldMulti);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return;
                }
                if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Alerts On Place And Break")) {

                    p.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.block.break", Methods.compileName(type, oldMulti, true)));
                }
                EpicSpawners.getInstance().dataFile.getConfig().set("data.spawnerstats." + locationStr, null);
                EpicSpawners.getInstance().dataFile.getConfig().set("data.spawner." + locationStr, null);

                Location nloc = location.clone();
                nloc.add(.5, -.4, .5);

                List<Entity> near = (List<Entity>) nloc.getWorld().getNearbyEntities(nloc, 8, 8, 8);
                for (Entity e : near) {
                    if (e.getLocation().getX() == nloc.getX() && e.getLocation().getY() == nloc.getY() && e.getLocation().getZ() == nloc.getZ()) {
                        e.remove();
                    }
                }
            }
            if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Sounds Enabled")) {
                if (!EpicSpawners.getInstance().v1_8 && !EpicSpawners.getInstance().v1_7) {
                    p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.6F, 15.0F);
                } else {
                    p.playSound(p.getLocation(), Sound.valueOf("ARROW_HIT"), 0.6F, 15.0F);
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public int getCost(String type, String entity, int multi) {
        try {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");

            int cost = 0;
            if (type.equals("ECO")) {
                if (EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + entity + ".Custom-ECO-Cost") != 0)
                    cost = EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + entity + ".Custom-ECO-Cost");
                else
                    cost = EpicSpawners.getInstance().getConfig().getInt("Main.Cost To Upgrade With Economy");
                if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Use Custom Equations for Upgrade Costs")) {
                    String math = EpicSpawners.getInstance().getConfig().getString("Main.Equations.Calculate Economy Upgrade Cost").replace("{ECOCost}", Integer.toString(cost)).replace("{Level}", Integer.toString(multi));
                    cost = (int) Math.round(Double.parseDouble(engine.eval(math).toString()));
                }
            } else if (type.equals("XP")) {
                if (EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + entity + ".Custom-XP-Cost") != 0)
                    cost = EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + entity + ".Custom-XP-Cost");
                else
                    cost = EpicSpawners.getInstance().getConfig().getInt("Main.Cost To Upgrade With XP");
                if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Use Custom Equations for Upgrade Costs")) {
                    String math = EpicSpawners.getInstance().getConfig().getString("Main.Equations.Calculate XP Upgrade Cost").replace("{XPCost}", Integer.toString(cost)).replace("{Level}", Integer.toString(multi));
                    cost = (int) Math.round(Double.parseDouble(engine.eval(math).toString()));
                }
            }
            return cost;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return 999999999;
    }

    public void overView(Player p, int infPage) {
        try {
            EpicSpawners instance = EpicSpawners.getInstance();
            if (p.hasPermission("epicspawners.view")) {
                int multi = getMulti();
                int spawns = getSpawns();
                String type = spawnedType;

                Inventory i = Bukkit.createInventory(null, 27, Arconix.pl().getApi().format().formatTitle(Methods.compileName(type, multi, false)));


                int showAmt = multi;
                if (showAmt > 64)
                    showAmt = 1;
                else if (showAmt == 0)
                    showAmt = 1;

                ItemStack item = new ItemStack(Material.SKULL_ITEM, showAmt, (byte) 3);
                try {
                    item = instance.heads.addTexture(item, type);
                } catch (Exception e) {
                    item = new ItemStack(Material.MOB_SPAWNER, showAmt);
                }


                if (instance.spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(type) + ".Display-Item") != null) {
                    Material mat = Material.valueOf(instance.spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(type) + ".Display-Item"));
                    if (!mat.equals(Material.AIR))
                        item = new ItemStack(mat, 1);
                }

                ItemMeta itemmeta = item.getItemMeta();
                itemmeta.setDisplayName(instance.getLocale().getMessage("interface.spawner.statstitle"));
                ArrayList<String> lore = new ArrayList<>();

                String spawnBlocks = instance.spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(Methods.getTypeFromString(type)) + ".Spawn-Block");

                List<String> blocks = Arrays.asList(spawnBlocks.split("\\s*,\\s*"));

                StringBuilder only = new StringBuilder(blocks.get(0));

                int num = 1;
                for (String block : blocks) {
                    if (num != 1)
                        only.append("&8, &6").append(Methods.getTypeFromString(block));
                    num++;
                }

                lore.add(instance.getLocale().getMessage("interface.spawner.stats", spawns, spawnedType));

                boolean omni = false;
                if (instance.dataFile.getConfig().contains("data.spawnerstats." + locationStr + ".type")) {
                    if (instance.dataFile.getConfig().getString("data.spawnerstats." + locationStr + ".type").equals("OMNI")) {
                        List<SpawnerItem> list = instance.getApi().convertFromList(instance.dataFile.getConfig().getStringList("data.spawnerstats." + locationStr + ".entities"));
                        lore.add(Arconix.pl().getApi().format().formatText(instance.getApi().getOmniString(list)));
                        omni = true;
                    }
                }
                lore.add(instance.getLocale().getMessage("interface.spawner.onlyspawnson", only.toString()));
                if (p.hasPermission("epicspawners.convert") && !omni) {
                    lore.add("");
                    lore.add(instance.getLocale().getMessage("interface.spawner.convert"));
                }
                if (p.hasPermission("epicspawners.canboost")) {
                    if (getBoost() == 0) {
                        if (!p.hasPermission("epicspawners.convert." + Methods.getTypeFromString(type)) && !p.hasPermission("epicspawners.convert." + type)) {
                            lore.add("");
                        }
                        lore.add(instance.getLocale().getMessage("interface.spawner.boost"));
                    }
                }
                if (getBoost() != 0) {

                    Date today = new Date();

                    String[] parts = instance.getLocale().getMessage("interface.spawner.boostedstats", Integer.toString(getBoost()), type, Arconix.pl().getApi().format().readableTime(getBoostEnd().getTime() - today.getTime())).split("\\|");
                    lore.add("");
                    for (String line : parts)
                        lore.add(Arconix.pl().getApi().format().formatText(line));
                }
                itemmeta.setLore(lore);
                item.setItemMeta(itemmeta);

                int xpCost = getCost("XP", type, multi);

                int ecoCost = getCost("ECO", type, multi);

                boolean maxed = false;
                if (multi == instance.getConfig().getInt("Main.Spawner Max Upgrade"))
                    maxed = true;

                ItemStack itemXP = new ItemStack(Material.valueOf(instance.getConfig().getString("Interfaces.XP Icon")), 1);
                ItemMeta itemmetaXP = itemXP.getItemMeta();
                itemmetaXP.setDisplayName(instance.getLocale().getMessage("interface.spawner.upgradewithxp"));
                ArrayList<String> loreXP = new ArrayList<>();
                if (!maxed)
                    loreXP.add(instance.getLocale().getMessage("interface.spawner.upgradewithxplore", Integer.toString(xpCost)));
                else
                    loreXP.add(instance.getLocale().getMessage("event.upgrade.maxed"));
                itemmetaXP.setLore(loreXP);
                itemXP.setItemMeta(itemmetaXP);

                ItemStack itemECO = new ItemStack(Material.valueOf(instance.getConfig().getString("Interfaces.Economy Icon")), 1);
                ItemMeta itemmetaECO = itemECO.getItemMeta();
                itemmetaECO.setDisplayName(instance.getLocale().getMessage("interface.spawner.upgradewitheconomy"));
                ArrayList<String> loreECO = new ArrayList<>();
                if (!maxed)
                    loreECO.add(instance.getLocale().getMessage("interface.spawner.upgradewitheconomylore", Arconix.pl().getApi().format().formatEconomy(ecoCost)));
                else
                    loreECO.add(instance.getLocale().getMessage("event.upgrade.maxed"));
                itemmetaECO.setLore(loreECO);
                itemECO.setItemMeta(itemmetaECO);

                int nu = 0;
                while (nu != 27) {
                    i.setItem(nu, Methods.getGlass());
                    nu++;
                }
                i.setItem(13, item);

                i.setItem(0, Methods.getBackgroundGlass(true));
                i.setItem(1, Methods.getBackgroundGlass(true));
                i.setItem(2, Methods.getBackgroundGlass(false));
                i.setItem(6, Methods.getBackgroundGlass(false));
                i.setItem(7, Methods.getBackgroundGlass(true));
                i.setItem(8, Methods.getBackgroundGlass(true));
                i.setItem(9, Methods.getBackgroundGlass(true));
                i.setItem(10, Methods.getBackgroundGlass(false));
                i.setItem(16, Methods.getBackgroundGlass(false));
                i.setItem(17, Methods.getBackgroundGlass(true));
                i.setItem(18, Methods.getBackgroundGlass(true));
                i.setItem(19, Methods.getBackgroundGlass(true));
                i.setItem(20, Methods.getBackgroundGlass(false));
                i.setItem(24, Methods.getBackgroundGlass(false));
                i.setItem(25, Methods.getBackgroundGlass(true));
                i.setItem(26, Methods.getBackgroundGlass(true));

                if (instance.getConfig().getBoolean("Main.Display Help Button In Spawner Overview")) {
                    ItemStack itemO = new ItemStack(Material.PAPER, 1);
                    ItemMeta itemmetaO = itemO.getItemMeta();
                    itemmetaO.setDisplayName(instance.getLocale().getMessage("interface.spawner.howtotitle"));
                    ArrayList<String> loreO = new ArrayList<>();
                    String text = EpicSpawners.getInstance().getLocale().getMessage("interface.spawner.howtoinfo");

                    int start = (14 * infPage) - 14;
                    int li = 1; // 12
                    int added = 0;
                    boolean max = false;

                    String[] parts = text.split("\\|");
                    for (String line : parts) {
                        line = compileHow(p, line);
                        if (line.equals(".") || line.equals("")) {

                        } else {
                            Pattern regex = Pattern.compile("(.{1,28}(?:\\s|$))|(.{0,28})", Pattern.DOTALL);
                            Matcher m = regex.matcher(line);
                            while (m.find()) {
                                if (li > start) {
                                    if (li < start + 15) {
                                        loreO.add(Arconix.pl().getApi().format().formatText("&7" + m.group()));
                                        added++;
                                    } else {
                                        max = true;
                                    }
                                }
                                li++;
                            }
                        }
                    }
                    if (added == 0) {
                        overView(p, 1);
                        instance.infPage.remove(p);
                        return;
                    }
                    if (max) {
                        loreO.add(instance.getLocale().getMessage("interface.spawner.howtonext"));
                    } else {
                        loreO.add(instance.getLocale().getMessage("interface.spawner.howtoback"));
                    }
                    itemmetaO.setLore(loreO);
                    itemO.setItemMeta(itemmetaO);
                    i.setItem(8, itemO);
                }
                if (!type.equals("Omni")) {
                    if (instance.spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(Methods.getTypeFromString(type)) + ".Upgradable")) {
                        if (instance.getConfig().getBoolean("Main.Upgrade With XP"))
                            i.setItem(11, itemXP);
                        if (instance.getConfig().getBoolean("Main.Upgrade With Economy"))
                            i.setItem(15, itemECO);
                    }
                }
                p.openInventory(i);
                instance.spawnerLoc.put(p, spawner.getBlock());
                instance.lastSpawner.put(p, spawner.getBlock());
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public String compileHow(Player p, String text) {
        try {
            Matcher m = Pattern.compile("\\{(.*?)\\}").matcher(text);
            while (m.find()) {
                Matcher mi = Pattern.compile("\\[(.*?)\\]").matcher(text);
                int nu = 0;
                int a = 0;
                String type = "";
                while (mi.find()) {
                    if (nu == 0) {
                        type = mi.group().replace("[", "").replace("]", "");
                        text = text.replace(mi.group(), "");
                    } else {
                        switch (type) {
                            case "LEVELUP":
                                if (nu == 1) {
                                    if (!p.hasPermission("epicspawners.combine." + spawner.getSpawnedType()) && !p.hasPermission("epicspawners.combine." + spawnedType)) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                        a++;
                                    }
                                } else if (nu == 2) {
                                    if (!EpicSpawners.getInstance().getConfig().getBoolean("Main.Upgrade With XP")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                        a++;
                                    }
                                } else if (nu == 3) {
                                    if (!EpicSpawners.getInstance().getConfig().getBoolean("Main.Upgrade With Economy")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                        a++;
                                    }
                                }
                                break;
                            case "WATER":
                                if (nu == 1) {
                                    if (!EpicSpawners.getInstance().getConfig().getBoolean("settings.spawners-repel-liquid")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                            case "INVSTACK":
                                if (nu == 1) {
                                    if (!EpicSpawners.getInstance().getConfig().getBoolean("Main.Allow Stacking Spawners In Survival Inventories")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                            case "REDSTONE":
                                if (nu == 1) {
                                    if (!EpicSpawners.getInstance().getConfig().getBoolean("Main.Redstone Power Deactivates Spawners")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                            case "OMNI":
                                if (nu == 1) {
                                    if (!EpicSpawners.getInstance().getConfig().getBoolean("Main.OmniSpawners Enabled")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                            case "DROP":
                                if (!EpicSpawners.getInstance().getConfig().getBoolean("Spawner Drops.Allow Killing Mobs To Drop Spawners") || !p.hasPermission("epicspawners.Killcounter")) {
                                    text = "";
                                } else {
                                    text = text.replace("<TYPE>", spawnedType.toLowerCase());
                                    if (EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(spawnedType) + ".CustomGoal") != 0)
                                        text = text.replace("<AMT>", Integer.toString(EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(spawnedType) + ".CustomGoal")));
                                    else
                                        text = text.replace("<AMT>", Integer.toString(EpicSpawners.getInstance().getConfig().getInt("Spawner Drops.Kills Needed for Drop")));
                                }
                                if (nu == 1) {
                                    if (EpicSpawners.getInstance().getConfig().getBoolean("Spawner Drops.Count Unnatural Kills Towards Spawner Drop")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                        }
                    }
                    nu++;
                }

            }
            text = text.replace("[", "").replace("]", "").replace("{", "").replace("}", "");
            return text;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public String a(int a, String text) {
        try {
            if (a != 0) {
                text = ", " + text;
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return text;
    }

    public void change(Player p, int page) {
        try {
            EpicSpawners.getInstance().page.put(p, page);

            List<String> entities = new ArrayList<>();

            int num = 0;
            int show = 0;
            int start = (page - 1) * 32;
            ConfigurationSection cs = EpicSpawners.getInstance().spawnerFile.getConfig().getConfigurationSection("Entities");
            for (String value : cs.getKeys(false)) {
                if (!value.toLowerCase().equals("omni")) {
                    if (EpicSpawners.getInstance().spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(value) + ".Convertible")) {
                        if (p.hasPermission("epicspawners.*") || p.hasPermission("epicspawners.convert.*") || p.hasPermission("epicspawners.convert." + Methods.getTypeFromString(value).replaceAll(" ", "_"))) {
                            if (num >= start) {
                                if (show <= 32) {
                                    entities.add(value);
                                    show++;
                                }
                            }
                        }
                        num++;
                    }
                }
            }

            int amt = entities.size();
            String title = EpicSpawners.getInstance().getLocale().getMessage("interface.convert.title");
            Inventory i = Bukkit.createInventory(null, 54, Arconix.pl().getApi().format().formatTitle(title));
            int max2 = 54;
            if (amt <= 7) {
                i = Bukkit.createInventory(null, 27, Arconix.pl().getApi().format().formatTitle(title));
                max2 = 27;
            } else if (amt <= 15) {
                i = Bukkit.createInventory(null, 36, Arconix.pl().getApi().format().formatTitle(title));
                max2 = 36;
            } else if (amt <= 25) {
                i = Bukkit.createInventory(null, 45, Arconix.pl().getApi().format().formatTitle(title));
                max2 = 45;
            }

            final int max22 = max2;
            int place = 10;
            for (String value : entities) {
                if (place == 17)
                    place++;
                if (place == (max22 - 18))
                    place++;
                ItemStack it = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);

                ItemStack item = EpicSpawners.getInstance().heads.addTexture(it, Methods.getTypeFromString(value));

                if (EpicSpawners.getInstance().spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(value) + ".Display-Item") != null) {
                    Material mat = Material.valueOf(EpicSpawners.getInstance().spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(value) + ".Display-Item"));
                    if (!mat.equals(Material.AIR))
                        item = new ItemStack(mat, 1);
                }

                ItemMeta itemmeta = item.getItemMeta();
                String name = Methods.compileName(value, 0, true);
                ArrayList<String> lore = new ArrayList<>();
                String per = EpicSpawners.getInstance().spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(value) + ".Convert-Price");
                double sprice = EpicSpawners.getInstance().spawnerFile.getConfig().getDouble("Entities." + Methods.getTypeFromString(value) + ".Shop-Price");

                int ch = Integer.parseInt(per.replace("%", ""));

                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");
                double price = Math.round(Double.parseDouble(engine.eval("(" + ch + " / 100) * " + sprice).toString()) * getMulti());

                lore.add(EpicSpawners.getInstance().getLocale().getMessage("interface.shop.buyprice", Arconix.pl().getApi().format().formatEconomy(price)));
                String loreString = EpicSpawners.getInstance().getLocale().getMessage("interface.convert.lore", Methods.getTypeFromString(Methods.getTypeFromString(value)));
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    loreString = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, loreString.replace(" ", "_")).replace("_", " ");
                }
                lore.add(loreString);
                itemmeta.setLore(lore);
                itemmeta.setDisplayName(name);
                item.setItemMeta(itemmeta);
                i.setItem(place, item);
                place++;
            }

            int max = (int) Math.ceil((double) num / (double) 36);
            num = 0;
            while (num != 9) {
                i.setItem(num, Methods.getGlass());
                num++;
            }
            int num2 = max2 - 9;
            while (num2 != max2) {
                i.setItem(num2, Methods.getGlass());
                num2++;
            }

            ItemStack exit = new ItemStack(Material.valueOf(EpicSpawners.getInstance().getConfig().getString("Interfaces.Exit Icon")), 1);
            ItemMeta exitmeta = exit.getItemMeta();
            exitmeta.setDisplayName(EpicSpawners.getInstance().getLocale().getMessage("general.nametag.exit"));
            exit.setItemMeta(exitmeta);

            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            ItemStack skull = head;
            if (!EpicSpawners.getInstance().v1_7)
                skull = Arconix.pl().getApi().getGUI().addTexture(head, "http://textures.minecraft.net/texture/1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b");
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            if (EpicSpawners.getInstance().v1_7)
                skullMeta.setOwner("MHF_ArrowRight");
            skull.setDurability((short) 3);
            skullMeta.setDisplayName(EpicSpawners.getInstance().getLocale().getMessage("general.nametag.next"));
            skull.setItemMeta(skullMeta);

            ItemStack head2 = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            ItemStack skull2 = head2;
            if (!EpicSpawners.getInstance().v1_7)
                skull2 = Arconix.pl().getApi().getGUI().addTexture(head2, "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23");
            SkullMeta skull2Meta = (SkullMeta) skull2.getItemMeta();
            if (EpicSpawners.getInstance().v1_7)
                skull2Meta.setOwner("MHF_ArrowLeft");
            skull2.setDurability((short) 3);
            skull2Meta.setDisplayName(EpicSpawners.getInstance().getLocale().getMessage("general.nametag.back"));
            skull2.setItemMeta(skull2Meta);

            i.setItem(8, exit);

            i.setItem(0, Methods.getBackgroundGlass(true));
            i.setItem(1, Methods.getBackgroundGlass(true));
            i.setItem(9, Methods.getBackgroundGlass(true));

            i.setItem(7, Methods.getBackgroundGlass(true));
            i.setItem(17, Methods.getBackgroundGlass(true));

            i.setItem(max22 - 18, Methods.getBackgroundGlass(true));
            i.setItem(max22 - 9, Methods.getBackgroundGlass(true));
            i.setItem(max22 - 8, Methods.getBackgroundGlass(true));

            i.setItem(max22 - 10, Methods.getBackgroundGlass(true));
            i.setItem(max22 - 2, Methods.getBackgroundGlass(true));
            i.setItem(max22 - 1, Methods.getBackgroundGlass(true));

            i.setItem(2, Methods.getBackgroundGlass(false));
            i.setItem(6, Methods.getBackgroundGlass(false));
            i.setItem(max22 - 7, Methods.getBackgroundGlass(false));
            i.setItem(max22 - 3, Methods.getBackgroundGlass(false));

            if (page != 1) {
                i.setItem(max22 - 8, skull2);
            }
            if (page != max) {
                i.setItem(max22 - 2, skull);
            }

            p.openInventory(i);
            EpicSpawners.getInstance().change.add(p);
            EpicSpawners.getInstance().spawnerLoc.put(p, spawner.getBlock());
            EpicSpawners.getInstance().lastSpawner.put(p, spawner.getBlock());
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }


    public void convert(String type, Player p) {
        try {
            if (EpicSpawners.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
                p.sendMessage("Vault is not installed.");
                return;
            }
            RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = EpicSpawners.getInstance().getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            net.milkbowl.vault.economy.Economy econ = rsp.getProvider();

            String per = EpicSpawners.getInstance().spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(type) + ".Convert-Price");
            double sprice = EpicSpawners.getInstance().spawnerFile.getConfig().getDouble("Entities." + Methods.getTypeFromString(type) + ".Shop-Price");

            int ch = Integer.parseInt(per.replace("%", ""));

            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            double price = Math.round(Double.parseDouble(engine.eval("(" + ch + " / 100) * " + sprice).toString())) * getMulti();

            if (!(econ.has(p, price) || p.isOp())) {
                p.sendMessage(EpicSpawners.getInstance().references.getPrefix() + EpicSpawners.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
                return;
            }
            SpawnerChangeEvent event = new SpawnerChangeEvent(location, p, spawnedType, type);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            EpicSpawners.getInstance().dataFile.getConfig().set("data.spawnerstats." + locationStr + ".type", Methods.getTypeFromString(type));
            try {
                getSpawner().setSpawnedType(EntityType.valueOf(type));
                update();
            } catch (Exception ignore) {
            }

            p.sendMessage(EpicSpawners.getInstance().references.getPrefix() + EpicSpawners.getInstance().getLocale().getMessage("event.convert.success"));

            EpicSpawners.getInstance().holo.processChange(location.getBlock());
            p.closeInventory();
            if (!p.isOp()) {
                econ.withdrawPlayer(p, price);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void playerBoost(Player p) {
        try {
            if (!p.hasPermission("epicspawners.canboost")) {
                return;
            }
            if (EpicSpawners.getInstance().boostAmt.containsKey(p)) {
                if (EpicSpawners.getInstance().boostAmt.get(p) > EpicSpawners.getInstance().getConfig().getInt("Spawner Boosting.Max Multiplier For A Spawner Boost")) {
                    EpicSpawners.getInstance().boostAmt.put(p, EpicSpawners.getInstance().getConfig().getInt("Spawner Boosting.Max Multiplier For A Spawner Boost"));
                    return;
                } else if (EpicSpawners.getInstance().boostAmt.get(p) < 1) {
                    EpicSpawners.getInstance().boostAmt.put(p, 1);
                }
            }

            int amt = 1;

            if (EpicSpawners.getInstance().boostAmt.containsKey(p))
                amt = EpicSpawners.getInstance().boostAmt.get(p);
            else
                EpicSpawners.getInstance().boostAmt.put(p, amt);


            int multi = EpicSpawners.getInstance().dataFile.getConfig().getInt("data.spawner." + locationStr);

            String type = Methods.getType(spawner.getSpawnedType());

            if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(location) + ".type")) {
                if (EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(location) + ".type").equals("OMNI"))
                    type = "Omni";
                else
                    type = EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(location) + ".type");
            }

            Inventory i = Bukkit.createInventory(null, 27, EpicSpawners.getInstance().getLocale().getMessage("interface.boost.title", Integer.toString(amt), Methods.compileName(type, multi, false)));

            int num = 0;
            while (num != 27) {
                i.setItem(num, Methods.getGlass());
                num++;
            }

            ItemStack coal = new ItemStack(Material.COAL);
            ItemMeta coalMeta = coal.getItemMeta();
            coalMeta.setDisplayName(EpicSpawners.getInstance().getLocale().getMessage("interface.boost.boostfor", "5"));
            ArrayList<String> coalLore = new ArrayList<>();
            coalLore.add(Arconix.pl().getApi().format().formatText("&7Costs &6&l" + Methods.getBoostCost(5, amt) + "."));
            coalMeta.setLore(coalLore);
            coal.setItemMeta(coalMeta);

            ItemStack iron = new ItemStack(Material.IRON_INGOT);
            ItemMeta ironMeta = iron.getItemMeta();
            ironMeta.setDisplayName(EpicSpawners.getInstance().getLocale().getMessage("interface.boost.boostfor", "15"));
            ArrayList<String> ironLore = new ArrayList<>();
            ironLore.add(Arconix.pl().getApi().format().formatText("&7Costs &6&l" + Methods.getBoostCost(15, amt) + "."));
            ironMeta.setLore(ironLore);
            iron.setItemMeta(ironMeta);

            ItemStack diamond = new ItemStack(Material.DIAMOND);
            ItemMeta diamondMeta = diamond.getItemMeta();
            diamondMeta.setDisplayName(EpicSpawners.getInstance().getLocale().getMessage("interface.boost.boostfor", "30"));
            ArrayList<String> diamondLore = new ArrayList<>();
            diamondLore.add(Arconix.pl().getApi().format().formatText("&7Costs &6&l" + Methods.getBoostCost(30, amt) + "."));
            diamondMeta.setLore(diamondLore);
            diamond.setItemMeta(diamondMeta);

            ItemStack emerald = new ItemStack(Material.EMERALD);
            ItemMeta emeraldMeta = emerald.getItemMeta();
            emeraldMeta.setDisplayName(EpicSpawners.getInstance().getLocale().getMessage("interface.boost.boostfor", "60"));
            ArrayList<String> emeraldLore = new ArrayList<>();
            emeraldLore.add(Arconix.pl().getApi().format().formatText("&7Costs &6&l" + Methods.getBoostCost(60, amt) + "."));
            emeraldMeta.setLore(emeraldLore);
            emerald.setItemMeta(emeraldMeta);

            i.setItem(10, coal);
            i.setItem(12, iron);
            i.setItem(14, diamond);
            i.setItem(16, emerald);

            i.setItem(0, Methods.getBackgroundGlass(true));
            i.setItem(1, Methods.getBackgroundGlass(true));
            i.setItem(2, Methods.getBackgroundGlass(false));
            i.setItem(6, Methods.getBackgroundGlass(false));
            i.setItem(7, Methods.getBackgroundGlass(true));
            i.setItem(8, Methods.getBackgroundGlass(true));
            i.setItem(9, Methods.getBackgroundGlass(true));
            i.setItem(17, Methods.getBackgroundGlass(true));
            i.setItem(18, Methods.getBackgroundGlass(true));
            i.setItem(19, Methods.getBackgroundGlass(true));
            i.setItem(20, Methods.getBackgroundGlass(false));
            i.setItem(24, Methods.getBackgroundGlass(false));
            i.setItem(25, Methods.getBackgroundGlass(true));
            i.setItem(26, Methods.getBackgroundGlass(true));

            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            ItemStack skull = head;
            if (!EpicSpawners.getInstance().v1_7)
                skull = Arconix.pl().getApi().getGUI().addTexture(head, "http://textures.minecraft.net/texture/1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b");
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            if (EpicSpawners.getInstance().v1_7)
                skullMeta.setOwner("MHF_ArrowRight");
            skull.setDurability((short) 3);
            skullMeta.setDisplayName(Arconix.pl().getApi().format().formatText("&6&l+1"));
            skull.setItemMeta(skullMeta);

            ItemStack head2 = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            ItemStack skull2 = head2;
            if (!EpicSpawners.getInstance().v1_7)
                skull2 = Arconix.pl().getApi().getGUI().addTexture(head2, "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23");
            SkullMeta skull2Meta = (SkullMeta) skull2.getItemMeta();
            if (EpicSpawners.getInstance().v1_7)
                skullMeta.setOwner("MHF_ArrowLeft");
            skull2.setDurability((short) 3);
            skull2Meta.setDisplayName(Arconix.pl().getApi().format().formatText("&6&l-1"));
            skull2.setItemMeta(skull2Meta);

            if (amt != 1) {
                i.setItem(0, skull2);
            }
            if (amt < EpicSpawners.getInstance().getConfig().getInt("Spawner Boosting.Max Multiplier For A Spawner Boost")) {
                i.setItem(8, skull);
            }

            p.openInventory(i);
            EpicSpawners.getInstance().boosting.add(p);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void purchaseBoost(Player p, int time) {
        try {
            int amt = EpicSpawners.getInstance().boostAmt.get(p);
            boolean yes = false;

            String un = EpicSpawners.getInstance().getConfig().getString("Spawner Boosting.Item Charged For A Boost");

            String[] parts = un.split(":");

            String type = parts[0];
            String multi = parts[1];
            int cost = Methods.boostCost(multi, time, amt);
            if (!type.equals("ECO") && !type.equals("XP")) {
                ItemStack stack = new ItemStack(Material.valueOf(type));
                int invAmt = Arconix.pl().getApi().getGUI().getAmount(p.getInventory(), stack);
                if (invAmt >= cost) {
                    stack.setAmount(cost);
                    Arconix.pl().getApi().getGUI().removeFromInventory(p.getInventory(), stack);
                    yes = true;
                } else {
                    p.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
                }
            } else if (type.equals("ECO")) {
                if (EpicSpawners.getInstance().getServer().getPluginManager().getPlugin("Vault") != null) {
                    RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = EpicSpawners.getInstance().getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                    net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
                    if (econ.has(p, cost)) {
                        econ.withdrawPlayer(p, cost);
                        yes = true;
                    } else {
                        p.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
                    }
                } else {
                    p.sendMessage("Vault is not installed.");
                }
            } else if (type.equals("XP")) {
                if (p.getLevel() >= cost || p.getGameMode() == GameMode.CREATIVE) {
                    if (p.getGameMode() != GameMode.CREATIVE) {
                        p.setLevel(p.getLevel() - cost);
                    }
                    yes = true;
                } else {
                    p.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
                }
            }
            if (yes) {
                Calendar c = Calendar.getInstance();
                Date currentDate = new Date();
                c.setTime(currentDate);
                c.add(Calendar.MINUTE, time);

                String uuid = UUID.randomUUID().toString();
                EpicSpawners.getInstance().dataFile.getConfig().set("data.boosts." + uuid + ".location", locationStr);
                EpicSpawners.getInstance().dataFile.getConfig().set("data.boosts." + uuid + ".boosted", amt);

                EpicSpawners.getInstance().dataFile.getConfig().set("data.boosts." + uuid + ".end", c.getTime().getTime());
                p.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.boost.applied"));
            }
            p.closeInventory();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void setSpawner(EntityType ent) {
        try {
            spawner = ((CreatureSpawner) spawner.getBlock().getState());
            spawner.setSpawnedType(ent);
            update();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public CreatureSpawner getSpawner() {
        try {
            return spawner;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public void update() {
        try {
            spawner.update();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}

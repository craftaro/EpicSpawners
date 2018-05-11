package com.songoda.epicspawners.spawners.object;


import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.boost.BoostData;
import com.songoda.epicspawners.boost.BoostType;
import com.songoda.epicspawners.player.MenuType;
import com.songoda.epicspawners.spawners.events.SpawnerChangeEvent;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
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

@SuppressWarnings("Duplicates")
public class Spawner {
    // This holds a placed creatureSpawner that exists in the game world.

    // Location of this creatureSpawner.
    private Location location;

    private int spawnCount;

    private String omniState = null;

    private UUID placedBy = null;

    private CreatureSpawner creatureSpawner;

    //Holds the different types of spawners contained by this creatureSpawner.
    private final Deque<SpawnerStack> spawnerStacks = new ArrayDeque<>();

    private final ScriptEngine engine;

    public Spawner(Location location) {
        this.location = location;
        this.creatureSpawner = (CreatureSpawner) location.getBlock().getState();
        ScriptEngineManager mgr = new ScriptEngineManager();
        this.engine = mgr.getEngineByName("JavaScript");
    }

    public void addSpawnerType(SpawnerStack spawnerStack) {
        this.spawnerStacks.addFirst(spawnerStack);
    }

    public Location getLocation() {
        return location.clone();
    }

    public CreatureSpawner getCreatureSpawner() {
        return creatureSpawner;
    }

    public SpawnerStack getFirstStack() {
        return spawnerStacks.getFirst();
    }

    public int getSpawnerMultiplier() {
        int multi = 0;
        for (SpawnerStack stack : spawnerStacks) {
            multi += stack.getStackSize();
        }
        return multi;
    }

    public void overview(Player p, int infoPage) {
        try {
            EpicSpawners instance = EpicSpawners.getInstance();
            if (!p.hasPermission("epicspawners.overview")) return;
            Inventory i = Bukkit.createInventory(null, 27, Arconix.pl().getApi().format().formatTitle(Methods.compileName(getName(), getSpawnerMultiplier(), false)));

            String type = getFirstStack().getSpawnerData().getName();

            int showAmt = getSpawnerMultiplier();
            if (showAmt > 64)
                showAmt = 1;
            else if (showAmt == 0)
                showAmt = 1;

            ItemStack item = new ItemStack(Material.SKULL_ITEM, showAmt, (byte) 3);
            if (spawnerStacks.size() != 1) {
                item = EpicSpawners.getInstance().getHeads().addTexture(item, "omni");
            } else {
                try {
                    item = EpicSpawners.getInstance().getHeads().addTexture(item, Methods.restoreType(type));
                } catch (Exception e) {
                    item = new ItemStack(Material.MOB_SPAWNER, showAmt);
                }
            }

            if (spawnerStacks.size() == 1 && spawnerStacks.getFirst().getSpawnerData().getDisplayItem() != null) {
                item.setType(spawnerStacks.getFirst().getSpawnerData().getDisplayItem());
            }

            ItemMeta itemmeta = item.getItemMeta();
            itemmeta.setDisplayName(instance.getLocale().getMessage("interface.spawner.statstitle"));
            ArrayList<String> lore = new ArrayList<>();

            if (spawnerStacks.size() != 1) {
                StringBuilder only = new StringBuilder("&6" + Methods.compileName(spawnerStacks.getFirst().getSpawnerData().getName(), spawnerStacks.getFirst().getStackSize(), false));

                int num = 1;
                for (SpawnerStack stack : spawnerStacks) {
                    if (num != 1)
                        only.append("&8, &6").append(Methods.compileName(stack.getSpawnerData().getName(), stack.getStackSize(), false));
                    num++;
                }

                lore.add(Arconix.pl().getApi().format().formatText(only.toString()));
            }

            List<Material> blocks = getFirstStack().getSpawnerData().getSpawnBlocksList();

            StringBuilder only = new StringBuilder(blocks.get(0).name());

            int num = 1;
            for (Material block : blocks) {
                if (num != 1)
                    only.append("&8, &6").append(Methods.getTypeFromString(block.name()));
                num++;
            }

            lore.add(instance.getLocale().getMessage("interface.spawner.onlyspawnson", only.toString()));

            lore.add(instance.getLocale().getMessage("interface.spawner.stats", spawnCount));
            if (p.hasPermission("epicspawners.convert") && spawnerStacks.size() == 1) {
                lore.add("");
                lore.add(instance.getLocale().getMessage("interface.spawner.convert"));
            }
            if (p.hasPermission("epicspawners.canboost")) {
                if (getBoost() == 0) {
                    if (!p.hasPermission("epicspawners.convert") || spawnerStacks.size() != 1) {
                        lore.add("");
                    }
                    lore.add(instance.getLocale().getMessage("interface.spawner.boost"));
                }
            }
            if (getBoost() != 0) {

                // ToDo: Make it display all boosts.
                String[] parts = instance.getLocale().getMessage("interface.spawner.boostedstats", Integer.toString(getBoost()), type, Arconix.pl().getApi().format().readableTime(getBoostEnd().getTime() - new Date().getTime())).split("\\|");
                lore.add("");
                for (String line : parts)
                    lore.add(Arconix.pl().getApi().format().formatText(line));
            }
            itemmeta.setLore(lore);
            item.setItemMeta(itemmeta);

            int xpCost = getCost("XP");

            int ecoCost = getCost("ECO");

            boolean maxed = false;
            if (getSpawnerMultiplier() == EpicSpawners.getInstance().getConfig().getInt("Main.Spawner Max Upgrade"))
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

            if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Display Help Button In Spawner Overview")) {
                ItemStack itemO = new ItemStack(Material.PAPER, 1);
                ItemMeta itemmetaO = itemO.getItemMeta();
                itemmetaO.setDisplayName(instance.getLocale().getMessage("interface.spawner.howtotitle"));
                ArrayList<String> loreO = new ArrayList<>();
                String text = EpicSpawners.getInstance().getLocale().getMessage("interface.spawner.howtoinfo");

                int start = (14 * infoPage) - 14;
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
                    overview(p, 1);
                    EpicSpawners.getInstance().getPlayerActionManager().getPlayerAction(p).setInfoPage(1);
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
            if (spawnerStacks.size() == 1) {
                if (getFirstStack().getSpawnerData().isUpgradeable()) {
                    if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Upgrade With XP"))
                        i.setItem(11, itemXP);
                    if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Upgrade With Economy"))
                        i.setItem(15, itemECO);
                }
            }
            p.openInventory(i);
            EpicSpawners.getInstance().getPlayerActionManager().getPlayerAction(p).setInMenu(MenuType.OVERVIEW);
            EpicSpawners.getInstance().getPlayerActionManager().getPlayerAction(p).setLastSpawner(this);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void playerBoost(Player p) {
        try {
            if (!p.hasPermission("epicspawners.canboost")) return;

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

            Inventory i = Bukkit.createInventory(null, 27, EpicSpawners.getInstance().getLocale().getMessage("interface.boost.title", Integer.toString(amt), Methods.compileName(getName(), getSpawnerMultiplier(), false)));

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
            EpicSpawners.getInstance().getPlayerActionManager().getPlayerAction(p).setInMenu(MenuType.PLAYERBOOST);
            EpicSpawners.getInstance().getPlayerActionManager().getPlayerAction(p).setLastSpawner(this);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void purchaseBoost(Player p, int time) {
        try {
            EpicSpawners instance = EpicSpawners.getInstance();

            int amt = instance.boostAmt.get(p);
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


                BoostData boostData = new BoostData(BoostType.LOCATION, amt, c.getTime().getTime(), location);
                instance.getBoostManager().addBoostToSpawner(boostData);
                p.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.boost.applied"));
            }
            p.closeInventory();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void convertOverview(Player p, int page) {
        try {
            EpicSpawners instance = EpicSpawners.getInstance();
            instance.page.put(p, page);

            List<SpawnerData> entities = new ArrayList<>();

            int num = 0;
            int show = 0;
            int start = (page - 1) * 32;
            for (SpawnerData spawnerData : instance.getSpawnerManager().getSpawnerTypes().values()) {
                if (spawnerData.getName().equalsIgnoreCase("omni")
                        || !spawnerData.isConvertible()
                        || !p.hasPermission("epicspawners.convert." + spawnerData.getName())) continue;
                if (num >= start) {
                    if (show <= 32) {
                        entities.add(spawnerData);
                        show++;
                    }
                }
                num++;
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
            for (SpawnerData spawnerData : entities) {
                if (place == 17)
                    place++;
                if (place == (max22 - 18))
                    place++;
                ItemStack it = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);

                ItemStack item = EpicSpawners.getInstance().getHeads().addTexture(it, Methods.getTypeFromString(spawnerData.getName()));

                if (spawnerData.getDisplayItem() != null) {
                    Material mat = spawnerData.getDisplayItem();
                    if (!mat.equals(Material.AIR))
                        item = new ItemStack(mat, 1);
                }

                ItemMeta itemmeta = item.getItemMeta();
                String name = Methods.compileName(spawnerData.getName(), 0, true);
                ArrayList<String> lore = new ArrayList<>();
                double price = spawnerData.getConvertPrice() * getSpawnerMultiplier();

                lore.add(EpicSpawners.getInstance().getLocale().getMessage("interface.shop.buyprice", Arconix.pl().getApi().format().formatEconomy(price)));
                String loreString = EpicSpawners.getInstance().getLocale().getMessage("interface.convert.lore", Methods.getTypeFromString(spawnerData.getName()));
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
            EpicSpawners.getInstance().getPlayerActionManager().getPlayerAction(p).setInMenu(MenuType.CONVERT);
            EpicSpawners.getInstance().getPlayerActionManager().getPlayerAction(p).setLastSpawner(this);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }


    public void convert(String type, Player p) {
        try {
            EpicSpawners instance = EpicSpawners.getInstance();
            if (EpicSpawners.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
                p.sendMessage("Vault is not installed.");
                return;
            }
            RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = EpicSpawners.getInstance().getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            net.milkbowl.vault.economy.Economy econ = rsp.getProvider();

            SpawnerData spawnerData = instance.getSpawnerManager().getSpawnerType(type);

            double price = spawnerData.getConvertPrice() * getSpawnerMultiplier();

            if (!(econ.has(p, price) || p.isOp())) {
                p.sendMessage(EpicSpawners.getInstance().references.getPrefix() + EpicSpawners.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
                return;
            }
            SpawnerChangeEvent event = new SpawnerChangeEvent(location, p, getFirstStack().getSpawnerData().getName(), type);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            spawnerStacks.getFirst().setSpawnerData(instance.getSpawnerManager().getSpawnerType(type));
            try {
                getCreatureSpawner().setSpawnedType(EntityType.valueOf(type.toUpperCase()));
            } catch (Exception e) {
                getCreatureSpawner().setSpawnedType(EntityType.DROPPED_ITEM);
            }
            getCreatureSpawner().update();

            p.sendMessage(EpicSpawners.getInstance().references.getPrefix() + EpicSpawners.getInstance().getLocale().getMessage("event.convert.success"));

            instance.getHologramHandler().updateHologram(this);
            p.closeInventory();
            if (!p.isOp()) {
                econ.withdrawPlayer(p, price);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public int getCost(String type) {
        try {
            int cost = 0;
            if (type.equals("ECO")) {
                if (getFirstStack().getSpawnerData().getUpgradeCostEconomy() != 0)
                    cost = (int) getFirstStack().getSpawnerData().getUpgradeCostEconomy();
                else
                    cost = EpicSpawners.getInstance().getConfig().getInt("Main.Cost To Upgrade With Economy");
                if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Use Custom Equations for Upgrade Costs")) {
                    String math = EpicSpawners.getInstance().getConfig().getString("Main.Equations.Calculate Economy Upgrade Cost").replace("{ECOCost}", Integer.toString(cost)).replace("{Level}", Integer.toString(getSpawnerMultiplier()));
                    cost = (int) Math.round(Double.parseDouble(engine.eval(math).toString()));
                }
            } else if (type.equals("XP")) {
                if (getFirstStack().getSpawnerData().getUpgradeCostExperience() != 0) {
                    cost = getFirstStack().getSpawnerData().getUpgradeCostExperience();
                } else
                    cost = EpicSpawners.getInstance().getConfig().getInt("Main.Cost To Upgrade With XP");
                if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Use Custom Equations for Upgrade Costs")) {
                    String math = EpicSpawners.getInstance().getConfig().getString("Main.Equations.Calculate XP Upgrade Cost").replace("{XPCost}", Integer.toString(cost)).replace("{Level}", Integer.toString(getSpawnerMultiplier()));
                    cost = (int) Math.round(Double.parseDouble(engine.eval(math).toString()));
                }
            }
            return cost;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return 999999999;
    }

    public String compileHow(Player p, String text) {
        try {
            Matcher m = Pattern.compile("\\{(.*?)}").matcher(text);
            while (m.find()) {
                Matcher mi = Pattern.compile("\\[(.*?)]").matcher(text);
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
                                    if (!p.hasPermission("epicspawners.combine." + getName()) && !p.hasPermission("epicspawners.combine." + getName())) {
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
                                    if (!EpicSpawners.getInstance().getConfig().getBoolean("settings.Spawners-repel-liquid")) {
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
                                    text = text.replace("<TYPE>", getName().toLowerCase());
                                    if (EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(getName()) + ".CustomGoal") != 0)
                                        text = text.replace("<AMT>", Integer.toString(EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(getName()) + ".CustomGoal")));
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

    public boolean unStack(Player player) {
        EpicSpawners instance = EpicSpawners.getInstance();
        SpawnerStack stack = spawnerStacks.getFirst();

        /*
        if ((player.getItemInHand().getItemMeta() == null || !player.getItemInHand().getItemMeta().hasEnchant(Enchantment.SILK_TOUCH))
                && !player.hasPermission("epicspawners.no-silk-drop")) {
        } */

        int amtToUnstack = 1;

        if (player.isSneaking() && EpicSpawners.getInstance().getConfig().getBoolean("Main.Sneak To Receive A Stacked Spawner")
                || instance.getConfig().getBoolean("Spawner Drops.Only Drop Stacked Spawners")) {
            amtToUnstack = stack.getStackSize();
        }

        if (instance.getConfig().getBoolean("Main.Sounds Enabled")) {
            if (!instance.v1_8 && !instance.v1_7) {
                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.6F, 15.0F);
            } else {
                player.playSound(player.getLocation(), Sound.valueOf("ARROW_HIT"), 0.6F, 15.0F);
            }
        }
        ItemStack item = instance.getApi().newSpawnerItem(stack.getSpawnerData().getName(), amtToUnstack, 1);

        if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Add Spawners To Inventory On Drop") && player.getInventory().firstEmpty() == -1)
            player.getInventory().addItem(item);
        else if (!instance.getConfig().getBoolean("Main.Only Drop Placed Spawner") || placedBy != null) { //ToDo: Clean this up.

            if (instance.getConfig().getBoolean("Spawner Drops.Drop On SilkTouch")
                    && player.getItemInHand() != null
                    && player.getItemInHand().hasItemMeta()
                    && player.getItemInHand().getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)
                    || player.hasPermission("epicspawners.no-silk-drop")) {

                int ch = Integer.parseInt(instance.getConfig().getString((placedBy != null
                        ? "Spawner Drops.Chance On Placed Silktouch" : "Spawner Drops.Chance On Natural Silktouch")).replace("%", ""));

                double rand = Math.random() * 100;

                if (rand - ch < 0 || ch == 100) {
                    if (instance.getConfig().getBoolean("Main.Add Spawners To Inventory On Drop") && player.getInventory().firstEmpty() != -1)
                        player.getInventory().addItem(item);
                    else
                        location.getWorld().dropItemNaturally(location.clone().add(.5, 0, .5), item);
                }
            }
        }

        if (stack.getStackSize() != amtToUnstack) {
            stack.setStackSize(stack.getStackSize() - 1);
            return true;
        }

        spawnerStacks.removeFirst();

        if (spawnerStacks.size() != 0) return true;

        location.getBlock().setType(Material.AIR);
        EpicSpawners.getInstance().getSpawnerManager().removeSpawnerFromWorld(location);
        instance.getHologramHandler().despawn(location.getBlock());
        return true;
    }

    public boolean stack(Player player, ItemStack itemStack) {
        EpicSpawnersAPI api = EpicSpawners.getInstance().getApi();

        String type = api.getIType(itemStack);
        int amt = api.getIMulti(itemStack);

        return stackFinal(player, type, amt);
    }

    public boolean stackFinal(Player player, String type, int amt) {
        EpicSpawners instance = EpicSpawners.getInstance();

        int max = instance.getConfig().getInt("Main.Spawner Max Upgrade");
        int currentStackSize = getSpawnerMultiplier();

        if (getSpawnerMultiplier() == max) {
            player.sendMessage(instance.getLocale().getMessage("event.upgrade.maxed", max));
            return false;
        }

        if ((getSpawnerMultiplier() + amt) > max) {
            ItemStack item = instance.getApi().newSpawnerItem(type, (getSpawnerMultiplier() + amt) - max, 1);
            if (player.getInventory().firstEmpty() == -1)
                location.getWorld().dropItemNaturally(location.clone().add(.5, 0, .5), item);
            else
                player.getInventory().addItem(item);

            amt = max - currentStackSize;
        }

        if (player.getGameMode() != GameMode.CREATIVE)
            Methods.takeItem(player, currentStackSize);

        for (SpawnerStack stack : spawnerStacks) {
            if (!stack.getSpawnerData().getName().toLowerCase().equals(type.toLowerCase())) continue;
            stack.setStackSize(stack.getStackSize() + amt);
            upgradeFinal(player, currentStackSize);
            return true;
        }

        if (!instance.getConfig().getBoolean("Main.OmniSpawners Enabled")) return false;

        SpawnerStack stack = new SpawnerStack(instance.getSpawnerManager().getSpawnerType(type), amt);

        spawnerStacks.push(stack);

        return true;
    }

    public void upgradeFinal(Player player, int oldStackSize) {
        try {
            int currentStackSize = getSpawnerMultiplier();

            if (getSpawnerMultiplier() != EpicSpawners.getInstance().getConfig().getInt("Main.Spawner Max Upgrade"))
                player.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.upgrade.success", currentStackSize));
            else
                player.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.upgrade.successmaxed", currentStackSize));

            SpawnerChangeEvent event = new SpawnerChangeEvent(location, player, currentStackSize, oldStackSize);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;

            Location loc = location.clone();
            loc.setX(loc.getX() + .5);
            loc.setY(loc.getY() + .5);
            loc.setZ(loc.getZ() + .5);
            if (!EpicSpawners.getInstance().v1_8 && !EpicSpawners.getInstance().v1_7) {
                player.getWorld().spawnParticle(org.bukkit.Particle.valueOf(EpicSpawners.getInstance().getConfig().getString("Main.Upgrade Particle Type")), loc, 100, .5, .5, .5);
            } else {
                player.getWorld().playEffect(loc, org.bukkit.Effect.valueOf(EpicSpawners.getInstance().getConfig().getString("Main.Upgrade Particle Type")), 1, 0);
                //Doesn't resolve... --Nova
                //player.getWorld().spigot().playEffect(loc, org.bukkit.Effect.valueOf(EpicSpawners.getInstance().getConfig().getString("Main.Upgrade Particle Type")), 1, 0, (float) 1, (float) 1, (float) 1, 1, 100, 10);
            }
            float x = (float) (0 + (Math.random() * 1));
            float y = (float) (0 + (Math.random() * 2));
            float z = (float) (0 + (Math.random() * 1));
            Arconix.pl().getApi().packetLibrary.getParticleManager().broadcastParticle(loc, x, y, z, 0, EpicSpawners.getInstance().getConfig().getString("entity.Spawn Particle Effect"), 15);
            if (!EpicSpawners.getInstance().getConfig().getBoolean("Main.Sounds Enabled")) {
                return;
            }
            if (currentStackSize != EpicSpawners.getInstance().getConfig().getInt("Main.Spawner Max Upgrade")) {
                if (!EpicSpawners.getInstance().v1_8 && !EpicSpawners.getInstance().v1_7) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6F, 15.0F);
                } else {
                    player.playSound(player.getLocation(), Sound.valueOf("LEVEL_UP"), 2F, 15.0F);
                }
            } else {
                if (!EpicSpawners.getInstance().v1_10 && !EpicSpawners.getInstance().v1_9 && !EpicSpawners.getInstance().v1_8 && !EpicSpawners.getInstance().v1_7) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2F, 25.0F);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_CHIME, 2F, 25.0F);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(EpicSpawners.getInstance(), () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_CHIME, 1.2F, 35.0F), 5L);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(EpicSpawners.getInstance(), () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_CHIME, 1.8F, 35.0F), 10L);
                } else {
                    player.playSound(player.getLocation(), Sound.valueOf("LEVEL_UP"), 2F, 25.0F);
                }
            }
            EpicSpawners.getInstance().getHologramHandler().updateHologram(this);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }


    public void upgrade(Player player, String type) {
        try {
            int cost = getCost(type);

            boolean maxed = false;

            if (getSpawnerMultiplier() == EpicSpawners.getInstance().getConfig().getInt("Main.Spawner Max Upgrade")) {
                maxed = true;
            }
            if (maxed) {
                player.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.upgrade.maxed"));
            } else {
                if (type.equals("ECO")) {
                    if (EpicSpawners.getInstance().getServer().getPluginManager().getPlugin("Vault") != null) {
                        RegisteredServiceProvider<Economy> rsp = EpicSpawners.getInstance().getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                        net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
                        if (econ.has(player, cost)) {
                            econ.withdrawPlayer(player, cost);
                            int oldMultiplier = getSpawnerMultiplier();
                            spawnerStacks.getFirst().setStackSize(spawnerStacks.getFirst().getStackSize() + 1);
                            upgradeFinal(player, oldMultiplier);
                        } else {
                            player.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
                        }
                    } else {
                        player.sendMessage("Vault is not installed.");
                    }
                } else if (type.equals("XP")) {
                    if (player.getLevel() >= cost || player.getGameMode() == GameMode.CREATIVE) {
                        if (player.getGameMode() != GameMode.CREATIVE) {
                            player.setLevel(player.getLevel() - cost);
                        }
                        int oldMultiplier = getSpawnerMultiplier();
                        spawnerStacks.getFirst().setStackSize(spawnerStacks.getFirst().getStackSize() + 1);
                        upgradeFinal(player, oldMultiplier);
                    } else {
                        player.sendMessage(EpicSpawners.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
                    }
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public int getBoost() {
        EpicSpawners instance = EpicSpawners.getInstance();
        if (placedBy == null) return 0;

        List<BoostData> boosts = instance.getBoostManager().getBoosts();

        if (boosts.size() == 0) return 0;

        int amountToBoost = 0;

        for (BoostData boostData : boosts) {
            if (System.currentTimeMillis() >= boostData.getEndTime()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> instance.getBoostManager().removeBoostFromSpawner(boostData), 1);
                continue;
            }

            switch (boostData.getBoostType()) {
                case LOCATION:
                    if (!location.equals(boostData.getData())) continue;
                    break;
                case PLAYER:
                    if (!placedBy.toString().equals(boostData.getData())) continue;
                    break;
                case FACTION:
                    if (!instance.getHookHandler().isInFaction((String) boostData.getData(), location)) continue;
                    break;
                case ISLAND:
                    if (!instance.getHookHandler().isInIsland((String) boostData.getData(), location)) continue;
                    break;
                case TOWN:
                    if (!instance.getHookHandler().isInTown((String) boostData.getData(), location)) continue;
                    break;
            }
            amountToBoost += boostData.getAmtBoosted();
        }
        return amountToBoost;
    }

    public Date getBoostEnd() { //ToDo: Wrong.
        EpicSpawners instance = EpicSpawners.getInstance();

        List<BoostData> boosts = instance.getBoostManager().getBoosts();

        if (boosts.size() == 0) return null;

        for (BoostData boostData : boosts) {
            if (System.currentTimeMillis() >= boostData.getEndTime()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> instance.getBoostManager().removeBoostFromSpawner(boostData), 1);
                continue;
            }

            switch (boostData.getBoostType()) {
                case LOCATION:
                    if (!location.equals(boostData.getData())) continue;
                    break;
                case PLAYER:
                    if (!placedBy.toString().equals(boostData.getData())) continue;
                    break;
                case FACTION:
                    if (!instance.getHookHandler().isInFaction((String) boostData.getData(), location)) continue;
                    break;
                case ISLAND:
                    if (!instance.getHookHandler().isInIsland((String) boostData.getData(), location)) continue;
                    break;
                case TOWN:
                    if (!instance.getHookHandler().isInTown((String) boostData.getData(), location)) continue;
                    break;
            }
            Date date = new Date();
            date.setTime(boostData.getEndTime());

            return date;
        }
        return null;
    }

    private int lastDelay = 0;
    private int lastMulti = 0;
    private static final Random rand = new Random();

    public void updateDelay() {
        try {
            if (EpicSpawners.getInstance().getConfig().getBoolean("Main.Default Minecraft Spawner Cooldowns")) {
                String equation = EpicSpawners.getInstance().getConfig().getString("Main.Equations.Cooldown Between Spawns");

                int max = 0;
                int min = 0;
                for (SpawnerStack stack : spawnerStacks) { //ToDo: You can probably do this only on spawner stack or upgrade.
                    String tickRate = stack.getSpawnerData().getTickRate();

                    String[] tick = tickRate.contains(":") ? tickRate.split(":") : new String[]{tickRate, tickRate};

                    int tickMin = Integer.parseInt(tick[1]);
                    int tickMax = Integer.parseInt(tick[0]);
                    if (max == 0 && min == 0) {
                        max = tickMax;
                        min = tickMin;
                        continue;
                    }
                    if ((max + min) < (tickMax + min)) {
                        max = tickMax;
                        min = tickMin;
                    }
                }

                int delay;
                if (!EpicSpawners.getInstance().cache.containsKey(equation) || (max + min) != lastDelay || getSpawnerMultiplier() != lastMulti) {
                    equation = equation.replace("{DEFAULT}", Integer.toString(rand.nextInt(max) + min));
                    equation = equation.replace("{MULTI}", Integer.toString(getSpawnerMultiplier()));
                    delay = (int) Math.round(Double.parseDouble(engine.eval(equation).toString()));
                    EpicSpawners.getInstance().cache.put(equation, delay);
                    lastDelay = max + min;
                    lastMulti = getSpawnerMultiplier();
                } else {
                    delay = EpicSpawners.getInstance().cache.get(equation);
                }

                getCreatureSpawner().setDelay(delay);
                getCreatureSpawner().update();
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public String getName() {
        String name = spawnerStacks.getFirst().getSpawnerData().getName();

        if (spawnerStacks.size() > 1)
            name = EpicSpawners.getInstance().getSpawnerManager().getSpawnerType("omni").getName();

        return name;
    }

    public String getDisplayName() {
        String name = spawnerStacks.getFirst().getSpawnerData().getDisplayName();

        if (spawnerStacks.size() > 1)
            name = EpicSpawners.getInstance().getSpawnerManager().getSpawnerType("omni").getDisplayName();

        return name;
    }

    public Collection<SpawnerStack> getSpawnerStacks() {
        return Collections.unmodifiableCollection(spawnerStacks);
    }

    public void clearSpawnerStacks() {
        spawnerStacks.clear();
    }

    public UUID getPlacedBy() {
        return placedBy;
    }

    public void setPlacedBy(Player placedBy) {
        this.placedBy = placedBy.getUniqueId();
    }

    public void setPlacedBy(UUID placedBy) {
        this.placedBy = placedBy;
    }

    public int getSpawnCount() {
        return spawnCount;
    }

    public void setSpawnCount(int spawnCount) {
        this.spawnCount = spawnCount;
    }

    public String getOmniState() {
        return omniState;
    }

    public void setOmniState(String omniState) {
        this.omniState = omniState;
    }
}

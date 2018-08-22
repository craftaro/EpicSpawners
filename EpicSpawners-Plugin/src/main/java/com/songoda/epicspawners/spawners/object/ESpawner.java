package com.songoda.epicspawners.spawners.object;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.api.methods.formatting.TimeComponent;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.CostType;
import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.api.events.SpawnerChangeEvent;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.api.spawner.condition.SpawnCondition;
import com.songoda.epicspawners.boost.BoostData;
import com.songoda.epicspawners.boost.BoostType;
import com.songoda.epicspawners.gui.GUISpawnerOverview;
import com.songoda.epicspawners.player.MenuType;
import com.songoda.epicspawners.player.PlayerData;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.gui.GUI;
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
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ESpawner implements Spawner {

    private static final Random rand = new Random();
    //Holds the different types of spawners contained by this creatureSpawner.
    private final Deque<SpawnerStack> spawnerStacks = new ArrayDeque<>();
    private final ScriptEngine engine;
    private Location location;
    private int spawnCount;
    private String omniState = null;
    private UUID placedBy = null;
    private CreatureSpawner creatureSpawner;
    //ToDo: Use this for all spawner things (Like items, commands and what not) instead of the old shit
    //ToDO: There is a weird error that is triggered when a spawner is not found in the config.
    private Map<Location, Date> lastSpawns = new HashMap<>();
    private int lastDelay = 0;
    private int lastMulti = 0;

    public ESpawner(Location location) {
        this.location = location;
        this.creatureSpawner = (CreatureSpawner) location.getBlock().getState();
        ScriptEngineManager mgr = new ScriptEngineManager();
        this.engine = mgr.getEngineByName("JavaScript");
    }

    @Override
    public void spawn() {
        EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();
        long lastSpawn = 1001;
        if (lastSpawns.containsKey(location)) {
            lastSpawn = (new Date()).getTime() - lastSpawns.get(location).getTime();
        }
        if (lastSpawn >= 1000) {
            lastSpawns.put(location, new Date());
        } else return;

        if (location.getBlock().isBlockPowered() && instance.getConfig().getBoolean("Main.Redstone Power Deactivates Spawners"))
            return;


        if (getFirstStack().getSpawnerData() == null) return;

        float x = (float) (0 + (Math.random() * .8));
        float y = (float) (0 + (Math.random() * .8));
        float z = (float) (0 + (Math.random() * .8));

        Location particleLocation = location.clone();
        particleLocation.add(.5, .5, .5);
        //ToDo: Only currently works for the first spawner Type in the stack. this is not how it should work.
        SpawnerData spawnerData = getFirstStack().getSpawnerData();
        Arconix.pl().getApi().packetLibrary.getParticleManager().broadcastParticle(particleLocation, x, y, z, 0, spawnerData.getSpawnerSpawnParticle().getEffect(), spawnerData.getParticleDensity().getSpawnerSpawn());

        for (SpawnerStack stack : getSpawnerStacks()) {
            ((ESpawnerData) stack.getSpawnerData()).spawn(this, stack);
        }
        Bukkit.getScheduler().runTaskLater(instance, this::updateDelay, 10);
    }

    @Override
    public void addSpawnerStack(SpawnerStack spawnerStack) {
        this.spawnerStacks.addFirst(spawnerStack);
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public int getX() {
        return location.getBlockX();
    }

    @Override
    public int getY() {
        return location.getBlockY();
    }

    @Override
    public int getZ() {
        return location.getBlockZ();
    }

    @Override
    public World getWorld() {
        return location.getWorld();
    }

    @Override
    public CreatureSpawner getCreatureSpawner() {
        return creatureSpawner;
    }

    @Override
    public SpawnerStack getFirstStack() {
        return spawnerStacks.getFirst();
    }

    @Override
    public int getSpawnerDataCount() {
        int multi = 0;
        for (SpawnerStack stack : spawnerStacks) {
            multi += stack.getStackSize();
        }
        return multi;
    }

    public void playerBoost(Player p) {
        try {
            if (!p.hasPermission("epicspawners.canboost")) return;

            if (EpicSpawnersPlugin.getInstance().boostAmt.containsKey(p)) {
                if (EpicSpawnersPlugin.getInstance().boostAmt.get(p) > EpicSpawnersPlugin.getInstance().getConfig().getInt("Spawner Boosting.Max Multiplier For A Spawner Boost")) {
                    EpicSpawnersPlugin.getInstance().boostAmt.put(p, EpicSpawnersPlugin.getInstance().getConfig().getInt("Spawner Boosting.Max Multiplier For A Spawner Boost"));
                    return;
                } else if (EpicSpawnersPlugin.getInstance().boostAmt.get(p) < 1) {
                    EpicSpawnersPlugin.getInstance().boostAmt.put(p, 1);
                }
            }

            int amt = 1;

            if (EpicSpawnersPlugin.getInstance().boostAmt.containsKey(p))
                amt = EpicSpawnersPlugin.getInstance().boostAmt.get(p);
            else
                EpicSpawnersPlugin.getInstance().boostAmt.put(p, amt);

            Inventory i = Bukkit.createInventory(null, 27, EpicSpawnersPlugin.getInstance().getLocale().getMessage("interface.boost.title", Integer.toString(amt), Methods.compileName(getIdentifyingData(), getSpawnerDataCount(), false)));

            int num = 0;
            while (num != 27) {
                i.setItem(num, Methods.getGlass());
                num++;
            }

            ItemStack coal = new ItemStack(Material.COAL);
            ItemMeta coalMeta = coal.getItemMeta();
            coalMeta.setDisplayName(EpicSpawnersPlugin.getInstance().getLocale().getMessage("interface.boost.boostfor", "5"));
            ArrayList<String> coalLore = new ArrayList<>();
            coalLore.add(TextComponent.formatText("&7Costs &6&l" + Methods.getBoostCost(5, amt) + "."));
            coalMeta.setLore(coalLore);
            coal.setItemMeta(coalMeta);

            ItemStack iron = new ItemStack(Material.IRON_INGOT);
            ItemMeta ironMeta = iron.getItemMeta();
            ironMeta.setDisplayName(EpicSpawnersPlugin.getInstance().getLocale().getMessage("interface.boost.boostfor", "15"));
            ArrayList<String> ironLore = new ArrayList<>();
            ironLore.add(TextComponent.formatText("&7Costs &6&l" + Methods.getBoostCost(15, amt) + "."));
            ironMeta.setLore(ironLore);
            iron.setItemMeta(ironMeta);

            ItemStack diamond = new ItemStack(Material.DIAMOND);
            ItemMeta diamondMeta = diamond.getItemMeta();
            diamondMeta.setDisplayName(EpicSpawnersPlugin.getInstance().getLocale().getMessage("interface.boost.boostfor", "30"));
            ArrayList<String> diamondLore = new ArrayList<>();
            diamondLore.add(TextComponent.formatText("&7Costs &6&l" + Methods.getBoostCost(30, amt) + "."));
            diamondMeta.setLore(diamondLore);
            diamond.setItemMeta(diamondMeta);

            ItemStack emerald = new ItemStack(Material.EMERALD);
            ItemMeta emeraldMeta = emerald.getItemMeta();
            emeraldMeta.setDisplayName(EpicSpawnersPlugin.getInstance().getLocale().getMessage("interface.boost.boostfor", "60"));
            ArrayList<String> emeraldLore = new ArrayList<>();
            emeraldLore.add(TextComponent.formatText("&7Costs &6&l" + Methods.getBoostCost(60, amt) + "."));
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

            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
            ItemStack skull = Arconix.pl().getApi().getGUI().addTexture(head, "http://textures.minecraft.net/texture/1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b");
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            skull.setDurability((short) 3);
            skullMeta.setDisplayName(TextComponent.formatText("&6&l+1"));
            skull.setItemMeta(skullMeta);

            ItemStack head2 = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
            ItemStack skull2 = Arconix.pl().getApi().getGUI().addTexture(head2, "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23");
            SkullMeta skull2Meta = (SkullMeta) skull2.getItemMeta();
            skull2.setDurability((short) 3);
            skull2Meta.setDisplayName(TextComponent.formatText("&6&l-1"));
            skull2.setItemMeta(skull2Meta);

            if (amt != 1) {
                i.setItem(0, skull2);
            }
            if (amt < EpicSpawnersPlugin.getInstance().getConfig().getInt("Spawner Boosting.Max Multiplier For A Spawner Boost")) {
                i.setItem(8, skull);
            }

            p.openInventory(i);
            EpicSpawnersPlugin.getInstance().getPlayerActionManager().getPlayerAction(p).setInMenu(MenuType.PLAYERBOOST);
            EpicSpawnersPlugin.getInstance().getPlayerActionManager().getPlayerAction(p).setLastSpawner(this);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    @Override
    public boolean checkConditions() {
        for (SpawnerStack stack : spawnerStacks) {
            for (SpawnCondition spawnCondition : stack.getSpawnerData().getConditions()) {
                if (!spawnCondition.isMet(this)) return false;
            }
        }
        return true;
    }

    public void purchaseBoost(Player p, int time) {
        try {
            EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();

            int amt = instance.boostAmt.get(p);
            boolean yes = false;

            String un = EpicSpawnersPlugin.getInstance().getConfig().getString("Spawner Boosting.Item Charged For A Boost");

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
                    p.sendMessage(EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
                }
            } else if (type.equals("ECO")) {
                if (EpicSpawnersPlugin.getInstance().getServer().getPluginManager().getPlugin("Vault") != null) {
                    RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = EpicSpawnersPlugin.getInstance().getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                    net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
                    if (econ.has(p, cost)) {
                        econ.withdrawPlayer(p, cost);
                        yes = true;
                    } else {
                        p.sendMessage(EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
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
                    p.sendMessage(EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
                }
            }
            if (yes) {
                Calendar c = Calendar.getInstance();
                Date currentDate = new Date();
                c.setTime(currentDate);
                c.add(Calendar.MINUTE, time);


                BoostData boostData = new BoostData(BoostType.LOCATION, amt, c.getTime().getTime(), location);
                instance.getBoostManager().addBoostToSpawner(boostData);
                p.sendMessage(EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.boost.applied"));
            }
            p.closeInventory();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void overview(Player player) {
        try {
            EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();
            if (!player.hasPermission("epicspawners.overview")) return;
            new GUISpawnerOverview(instance,this, player).openFor(player);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void convertOverview(Player player, int page) {
        try {
            EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();
            PlayerData playerData = instance.getPlayerActionManager().getPlayerAction(player);

            playerData.setCurrentPage(page);

            List<SpawnerData> entities = new ArrayList<>();

            int num = 0;
            int show = 0;
            int start = (page - 1) * 32;
            for (SpawnerData spawnerData : instance.getSpawnerManager().getAllSpawnerData()) {
                if (spawnerData.getIdentifyingName().equalsIgnoreCase("omni")
                        || !spawnerData.isConvertible()
                        || !player.hasPermission("epicspawners.convert." + spawnerData.getIdentifyingName())) continue;
                if (num >= start) {
                    if (show <= 32) {
                        entities.add(spawnerData);
                        show++;
                    }
                }
                num++;
            }

            int amt = entities.size();
            String title = instance.getLocale().getMessage("interface.convert.title");
            Inventory i = Bukkit.createInventory(null, 54, TextComponent.formatTitle(title));
            int max2 = 54;
            if (amt <= 7) {
                i = Bukkit.createInventory(null, 27, TextComponent.formatTitle(title));
                max2 = 27;
            } else if (amt <= 15) {
                i = Bukkit.createInventory(null, 36, TextComponent.formatTitle(title));
                max2 = 36;
            } else if (amt <= 25) {
                i = Bukkit.createInventory(null, 45, TextComponent.formatTitle(title));
                max2 = 45;
            }

            final int max22 = max2;
            int place = 10;
            for (SpawnerData spawnerData : entities) {
                if (place == 17)
                    place++;
                if (place == (max22 - 18))
                    place++;
                ItemStack it = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);

                ItemStack item = instance.getHeads().addTexture(it, spawnerData);

                if (spawnerData.getDisplayItem() != null) {
                    Material mat = spawnerData.getDisplayItem();
                    if (!mat.equals(Material.AIR))
                        item = new ItemStack(mat, 1);
                }

                ItemMeta itemmeta = item.getItemMeta();
                String name = Methods.compileName(spawnerData, 1, true);
                ArrayList<String> lore = new ArrayList<>();
                double price = spawnerData.getConvertPrice() * getSpawnerDataCount();

                lore.add(instance.getLocale().getMessage("interface.shop.buyprice", TextComponent.formatEconomy(price)));
                String loreString = instance.getLocale().getMessage("interface.convert.lore", Methods.getTypeFromString(spawnerData.getIdentifyingName()));
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    loreString = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, loreString.replace(" ", "_")).replace("_", " ");
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

            ItemStack exit = new ItemStack(Material.valueOf(instance.getConfig().getString("Interfaces.Exit Icon")), 1);
            ItemMeta exitmeta = exit.getItemMeta();
            exitmeta.setDisplayName(instance.getLocale().getMessage("general.nametag.exit"));
            exit.setItemMeta(exitmeta);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
            ItemStack skull = Arconix.pl().getApi().getGUI().addTexture(head, "http://textures.minecraft.net/texture/1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b");
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            skull.setDurability((short) 3);
            skullMeta.setDisplayName(instance.getLocale().getMessage("general.nametag.next"));
            skull.setItemMeta(skullMeta);

            ItemStack head2 = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
            ItemStack skull2 = Arconix.pl().getApi().getGUI().addTexture(head2, "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23");
            SkullMeta skull2Meta = (SkullMeta) skull2.getItemMeta();
            skull2.setDurability((short) 3);
            skull2Meta.setDisplayName(instance.getLocale().getMessage("general.nametag.back"));
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

            player.openInventory(i);

            playerData.setInMenu(MenuType.CONVERT);
            playerData.setLastSpawner(this);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void convert(SpawnerData type, Player p) {
        try {
            EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();
            if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
                p.sendMessage("Vault is not installed.");
                return;
            }

            RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = EpicSpawnersPlugin.getInstance().getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            net.milkbowl.vault.economy.Economy econ = rsp.getProvider();

            double price = type.getConvertPrice() * getSpawnerDataCount();

            if (!(econ.has(p, price) || p.isOp())) {
                p.sendMessage(EpicSpawnersPlugin.getInstance().references.getPrefix() + EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
                return;
            }
            SpawnerChangeEvent event = new SpawnerChangeEvent(p, this, getFirstStack().getSpawnerData(), type);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            this.spawnerStacks.getFirst().setSpawnerData(type);
            try {
                this.creatureSpawner.setSpawnedType(EntityType.valueOf(type.getIdentifyingName().toUpperCase()));
            } catch (Exception e) {
                this.creatureSpawner.setSpawnedType(EntityType.DROPPED_ITEM);
            }
            this.creatureSpawner.update();

            p.sendMessage(EpicSpawnersPlugin.getInstance().references.getPrefix() + EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.convert.success"));

            instance.getHologramHandler().updateHologram(this);
            p.closeInventory();
            if (!p.isOp()) {
                econ.withdrawPlayer(p, price);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public int getUpgradeCost(CostType type) {
        try {
            int cost = 0;
            if (type == CostType.ECONOMY) {
                if (getFirstStack().getSpawnerData().getUpgradeCostEconomy() != 0)
                    cost = (int) getFirstStack().getSpawnerData().getUpgradeCostEconomy();
                else
                    cost = EpicSpawnersPlugin.getInstance().getConfig().getInt("Main.Cost To Upgrade With Economy");
                if (EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Use Custom Equations for Upgrade Costs")) {
                    String math = EpicSpawnersPlugin.getInstance().getConfig().getString("Main.Equations.Calculate Economy Upgrade Cost").replace("{ECOCost}", Integer.toString(cost)).replace("{Level}", Integer.toString(getSpawnerDataCount()));
                    cost = (int) Math.round(Double.parseDouble(engine.eval(math).toString()));
                }
            } else if (type == CostType.EXPERIENCE) {
                if (getFirstStack().getSpawnerData().getUpgradeCostExperience() != 0) {
                    cost = getFirstStack().getSpawnerData().getUpgradeCostExperience();
                } else
                    cost = EpicSpawnersPlugin.getInstance().getConfig().getInt("Main.Cost To Upgrade With XP");
                if (EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Use Custom Equations for Upgrade Costs")) {
                    String math = EpicSpawnersPlugin.getInstance().getConfig().getString("Main.Equations.Calculate XP Upgrade Cost").replace("{XPCost}", Integer.toString(cost)).replace("{Level}", Integer.toString(getSpawnerDataCount()));
                    cost = (int) Math.round(Double.parseDouble(engine.eval(math).toString()));
                }
            }
            return cost;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return Integer.MAX_VALUE;
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
                                    if (!p.hasPermission("epicspawners.combine." + getIdentifyingName()) && !p.hasPermission("epicspawners.combine." + getIdentifyingName())) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                        a++;
                                    }
                                } else if (nu == 2) {
                                    if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Upgrade With XP")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                        a++;
                                    }
                                } else if (nu == 3) {
                                    if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Upgrade With Economy")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                        a++;
                                    }
                                }
                                break;
                            case "WATER":
                                if (nu == 1) {
                                    if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("settings.Spawners-repel-liquid")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                            case "INVSTACK":
                                if (nu == 1) {
                                    if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Allow Stacking Spawners In Survival Inventories")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                            case "REDSTONE":
                                if (nu == 1) {
                                    if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Redstone Power Deactivates Spawners")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                            case "OMNI":
                                if (nu == 1) {
                                    if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.OmniSpawners Enabled")) {
                                        text = text.replace(mi.group(), "");
                                    } else {
                                        text = text.replace(mi.group(), a(a, mi.group()));
                                    }
                                }
                                break;
                            case "DROP":
                                if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Spawner Drops.Allow Killing Mobs To Drop Spawners") || !p.hasPermission("epicspawners.Killcounter")) {
                                    text = "";
                                } else {
                                    text = text.replace("<TYPE>", getIdentifyingName().toLowerCase());
                                    if (EpicSpawnersPlugin.getInstance().spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(getIdentifyingName()) + ".CustomGoal") != 0)
                                        text = text.replace("<AMT>", Integer.toString(EpicSpawnersPlugin.getInstance().spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(getIdentifyingName()) + ".CustomGoal")));
                                    else
                                        text = text.replace("<AMT>", Integer.toString(EpicSpawnersPlugin.getInstance().getConfig().getInt("Spawner Drops.Kills Needed for Drop")));
                                }
                                if (nu == 1) {
                                    if (EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Spawner Drops.Count Unnatural Kills Towards Spawner Drop")) {
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

    private String a(int a, String text) {
        try {
            if (a != 0) {
                text = ", " + text;
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return text;
    }

    @Override
    public boolean unstack(Player player) {
        EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();
        SpawnerStack stack = spawnerStacks.getFirst();

        int stackSize = 1;

        if (player.isSneaking() && EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Sneak To Receive A Stacked Spawner")
                || instance.getConfig().getBoolean("Spawner Drops.Only Drop Stacked Spawners")) {
            stackSize = stack.getStackSize();
        }

        if (instance.getConfig().getBoolean("Main.Sounds Enabled")) {
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.6F, 15.0F);
        }
        ItemStack item = stack.getSpawnerData().toItemStack(1, stackSize);


        if (EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Add Spawners To Inventory On Drop") && player.getInventory().firstEmpty() == -1)
            player.getInventory().addItem(item);
        else if (!instance.getConfig().getBoolean("Main.Only Drop Placed Spawner") || placedBy != null) { //ToDo: Clean this up.

            if (instance.getConfig().getBoolean("Spawner Drops.Drop On SilkTouch")
                    && player.getItemInHand() != null
                    && player.getItemInHand().hasItemMeta()
                    && player.getItemInHand().getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)
                    && player.hasPermission("epicspawners.silkdrop." + stack.getSpawnerData().getIdentifyingName())
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

        if (stack.getStackSize() != stackSize) {
            stack.setStackSize(stack.getStackSize() - 1);
            return true;
        }

        spawnerStacks.removeFirst();

        if (spawnerStacks.size() != 0) return true;

        location.getBlock().setType(Material.AIR);
        EpicSpawnersPlugin.getInstance().getSpawnerManager().removeSpawnerFromWorld(location);
        instance.getHologramHandler().despawn(location.getBlock());
        return true;
    }

    @Override
    public boolean preStack(Player player, ItemStack itemStack) {
        return stack(player, EpicSpawnersAPI.getSpawnerDataFromItem(itemStack), EpicSpawnersAPI.getStackSizeFromItem(itemStack));
    }

    @Override
    @Deprecated
    public boolean stack(Player player, String type, int amt) {
        return stack(player, EpicSpawnersAPI.getSpawnerManager().getSpawnerData(type), amt);
    }

    @Override
    public boolean stack(Player player, SpawnerData data, int amount) {
        EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();

        int max = instance.getConfig().getInt("Main.Spawner Max Upgrade");
        int currentStackSize = getSpawnerDataCount();

        if (getSpawnerDataCount() == max) {
            player.sendMessage(instance.getLocale().getMessage("event.upgrade.maxed", max));
            return false;
        }

        if ((getSpawnerDataCount() + amount) > max) {
            ItemStack item = data.toItemStack(1, (getSpawnerDataCount() + amount) - max);
            if (player.getInventory().firstEmpty() == -1)
                location.getWorld().dropItemNaturally(location.clone().add(.5, 0, .5), item);
            else
                player.getInventory().addItem(item);

            amount = max - currentStackSize;
        }


        for (SpawnerStack stack : spawnerStacks) {
            if (!stack.getSpawnerData().equals(data)) continue;
            stack.setStackSize(stack.getStackSize() + amount);
            upgradeFinal(player, currentStackSize);

            if (player.getGameMode() != GameMode.CREATIVE)
                Methods.takeItem(player, amount);

            return true;
        }

        if (!instance.getConfig().getBoolean("Main.OmniSpawners Enabled") || !player.hasPermission("epicspawners.omni"))
            return false;

        ESpawnerStack stack = new ESpawnerStack(data, amount);
        spawnerStacks.push(stack);

        if (player.getGameMode() != GameMode.CREATIVE)
            Methods.takeItem(player, amount);

        return true;
    }

    private void upgradeFinal(Player player, int oldStackSize) {
        try {
            int currentStackSize = getSpawnerDataCount();

            if (getSpawnerDataCount() != EpicSpawnersPlugin.getInstance().getConfig().getInt("Main.Spawner Max Upgrade"))
                player.sendMessage(EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.upgrade.success", currentStackSize));
            else
                player.sendMessage(EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.upgrade.successmaxed", currentStackSize));

            SpawnerChangeEvent event = new SpawnerChangeEvent(player, this, currentStackSize, oldStackSize);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;

            Location loc = location.clone();
            loc.setX(loc.getX() + .5);
            loc.setY(loc.getY() + .5);
            loc.setZ(loc.getZ() + .5);
            player.getWorld().spawnParticle(org.bukkit.Particle.valueOf(EpicSpawnersPlugin.getInstance().getConfig().getString("Main.Upgrade Particle Type")), loc, 100, .5, .5, .5);


            if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Sounds Enabled")) {
                return;
            }
            if (currentStackSize != EpicSpawnersPlugin.getInstance().getConfig().getInt("Main.Spawner Max Upgrade")) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6F, 15.0F);
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2F, 25.0F);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 2F, 25.0F);
                Bukkit.getScheduler().scheduleSyncDelayedTask(EpicSpawnersPlugin.getInstance(), () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.2F, 35.0F), 5L);
                Bukkit.getScheduler().scheduleSyncDelayedTask(EpicSpawnersPlugin.getInstance(), () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.8F, 35.0F), 10L);
            }
            EpicSpawnersPlugin.getInstance().getHologramHandler().updateHologram(this);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void upgrade(Player player, CostType type) {
        try {
            int cost = getUpgradeCost(type);

            boolean maxed = false;

            if (getSpawnerDataCount() == EpicSpawnersPlugin.getInstance().getConfig().getInt("Main.Spawner Max Upgrade")) {
                maxed = true;
            }
            if (maxed) {
                player.sendMessage(EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.upgrade.maxed"));
            } else {
                if (type == CostType.ECONOMY) {
                    if (EpicSpawnersPlugin.getInstance().getServer().getPluginManager().getPlugin("Vault") != null) {
                        RegisteredServiceProvider<Economy> rsp = EpicSpawnersPlugin.getInstance().getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                        net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
                        if (econ.has(player, cost)) {
                            econ.withdrawPlayer(player, cost);
                            int oldMultiplier = getSpawnerDataCount();
                            spawnerStacks.getFirst().setStackSize(spawnerStacks.getFirst().getStackSize() + 1);
                            upgradeFinal(player, oldMultiplier);
                        } else {
                            player.sendMessage(EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
                        }
                    } else {
                        player.sendMessage("Vault is not installed.");
                    }
                } else if (type == CostType.EXPERIENCE) {
                    if (player.getLevel() >= cost || player.getGameMode() == GameMode.CREATIVE) {
                        if (player.getGameMode() != GameMode.CREATIVE) {
                            player.setLevel(player.getLevel() - cost);
                        }
                        int oldMultiplier = getSpawnerDataCount();
                        spawnerStacks.getFirst().setStackSize(spawnerStacks.getFirst().getStackSize() + 1);
                        upgradeFinal(player, oldMultiplier);
                    } else {
                        player.sendMessage(EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
                    }
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    @Override
    public int getBoost() {
        EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();
        if (placedBy == null) return 0;

        Set<BoostData> boosts = instance.getBoostManager().getBoosts();

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
                    if (!instance.isInFaction((String) boostData.getData(), location)) continue;
                    break;
                case ISLAND:
                    if (!instance.isInIsland((String) boostData.getData(), location)) continue;
                    break;
                case TOWN:
                    if (!instance.isInTown((String) boostData.getData(), location)) continue;
                    break;
            }
            amountToBoost += boostData.getAmtBoosted();
        }
        return amountToBoost;
    }

    @Override
    public Instant getBoostEnd() { //ToDo: Wrong.
        EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();

        Set<BoostData> boosts = instance.getBoostManager().getBoosts();

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
                    if (!instance.isInFaction((String) boostData.getData(), location)) continue;
                    break;
                case ISLAND:
                    if (!instance.isInIsland((String) boostData.getData(), location)) continue;
                    break;
                case TOWN:
                    if (!instance.isInTown((String) boostData.getData(), location)) continue;
                    break;
            }

            return Instant.ofEpochMilli(boostData.getEndTime());
        }
        return null;
    }

    @Override
    public int updateDelay() { //ToDO: Should be redesigned to work with spawner.setmaxdelay
        try {
            if (!EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Default Minecraft Spawner Cooldowns"))
                return 0;

            String equation = EpicSpawnersPlugin.getInstance().getConfig().getString("Main.Equations.Cooldown Between Spawns");

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
            if (!EpicSpawnersPlugin.getInstance().cache.containsKey(equation) || (max + min) != lastDelay || getSpawnerDataCount() != lastMulti) {
                equation = equation.replace("{DEFAULT}", Integer.toString(rand.nextInt(Math.max(max, 0) + min)));
                equation = equation.replace("{MULTI}", Integer.toString(getSpawnerDataCount()));
                try {
                    delay = (int) Math.round(Double.parseDouble(engine.eval(equation).toString()));
                } catch (IllegalArgumentException ex) {
                    delay = 30;
                }
                EpicSpawnersPlugin.getInstance().cache.put(equation, delay);
                lastDelay = max + min;
                lastMulti = getSpawnerDataCount();
            } else {
                delay = EpicSpawnersPlugin.getInstance().cache.get(equation);
            }

            if (getCreatureSpawner().getSpawnedType() != EntityType.DROPPED_ITEM)
                getCreatureSpawner().setDelay(delay);
            getCreatureSpawner().update();

            return delay;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return 999999;
    }

    @Override
    public String getIdentifyingName() {
        String name = spawnerStacks.getFirst().getSpawnerData().getIdentifyingName();

        if (spawnerStacks.size() > 1)
            name = EpicSpawnersPlugin.getInstance().getSpawnerManager().getSpawnerData("omni").getIdentifyingName();

        return name;
    }

    @Override
    public SpawnerData getIdentifyingData() {
        SpawnerData name = spawnerStacks.getFirst().getSpawnerData();

        if (spawnerStacks.size() > 1)
            name = EpicSpawnersPlugin.getInstance().getSpawnerManager().getSpawnerData("omni");

        return name;
    }

    @Override
    public String getDisplayName() {
        if (spawnerStacks.size() == 0) {
            return Methods.getTypeFromString(creatureSpawner.getSpawnedType().name());
        } else if (spawnerStacks.size() > 1) {
            return EpicSpawnersPlugin.getInstance().getSpawnerManager().getSpawnerData("omni").getDisplayName();
        }

        return spawnerStacks.getFirst().getSpawnerData().getDisplayName();
    }

    @Override
    public Collection<SpawnerStack> getSpawnerStacks() {
        return Collections.unmodifiableCollection(spawnerStacks);
    }

    @Override
    public void clearSpawnerStacks() {
        spawnerStacks.clear();
    }

    @Override
    public OfflinePlayer getPlacedBy() {
        if (placedBy == null) return null;
        return Bukkit.getOfflinePlayer(placedBy);
    }

    public void setPlacedBy(UUID placedBy) {
        this.placedBy = placedBy;
    }

    public void setPlacedBy(Player placedBy) {
        this.placedBy = placedBy.getUniqueId();
    }

    @Override
    public int getSpawnCount() {
        return spawnCount;
    }

    @Override
    public void setSpawnCount(int spawnCount) {
        this.spawnCount = spawnCount;
    }

    public String getOmniState() {
        return omniState;
    }

    public void setOmniState(String omniState) {
        this.omniState = omniState;
    }

    @Override
    public int hashCode() {
        int result = 31 * (location == null ? 0 : location.hashCode());
        result = 31 * result + (placedBy == null ? 0 : placedBy.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ESpawner)) return false;

        ESpawner other = (ESpawner) obj;
        return Objects.equals(location, other.location) && Objects.equals(placedBy, other.placedBy);
    }

    @Override
    public String toString() {
        return "ESpawner:{"
                + "Owner:\"" + placedBy + "\","
                + "Location:{"
                + "World:\"" + location.getWorld().getName() + "\","
                + "X:" + location.getBlockX() + ","
                + "Y:" + location.getBlockY() + ","
                + "Z:" + location.getBlockZ()
                + "},"
                + "StackCount:" + spawnerStacks.size()
                + "}";
    }

}

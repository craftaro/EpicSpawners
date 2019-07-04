package com.songoda.epicspawners.gui;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.References;
import com.songoda.epicspawners.boost.BoostData;
import com.songoda.epicspawners.boost.BoostType;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.ServerVersion;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import com.songoda.epicspawners.utils.settings.Setting;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class GUISpawnerBoost extends AbstractGUI {

    private final EpicSpawners plugin;
    private final Spawner spawner;
    private int amount = 1;

    GUISpawnerBoost(EpicSpawners plugin, Spawner spawner, Player player) {
        super(player);
        this.plugin = plugin;
        this.spawner = spawner;
        setUp();
    }

    private void setUp() {
        if (amount > Setting.MAX_PLAYER_BOOST.getInt()) {
            amount = Setting.MAX_PLAYER_BOOST.getInt();
            return;
        } else if (amount < 1) {
            amount = 1;
        }
        init(plugin.getLocale().getMessage("interface.boost.title", spawner.getDisplayName(), amount), 27);
    }

    @Override
    public void constructGUI() {
        if (!player.hasPermission("epicspawners.canboost")) return;

        int num = 0;
        while (num != 27) {
            inventory.setItem(num, Methods.getGlass());
            num++;
        }

        ItemStack coal = new ItemStack(Material.COAL);
        ItemMeta coalMeta = coal.getItemMeta();
        coalMeta.setDisplayName(plugin.getLocale().getMessage("interface.boost.boostfor", "5"));
        ArrayList<String> coalLore = new ArrayList<>();
        coalLore.add(Methods.formatText("&7Costs &6&l" + Methods.getBoostCost(5, amount) + "."));
        coalMeta.setLore(coalLore);
        coal.setItemMeta(coalMeta);

        ItemStack iron = new ItemStack(Material.IRON_INGOT);
        ItemMeta ironMeta = iron.getItemMeta();
        ironMeta.setDisplayName(plugin.getLocale().getMessage("interface.boost.boostfor", "15"));
        ArrayList<String> ironLore = new ArrayList<>();
        ironLore.add(Methods.formatText("&7Costs &6&l" + Methods.getBoostCost(15, amount) + "."));
        ironMeta.setLore(ironLore);
        iron.setItemMeta(ironMeta);

        ItemStack diamond = new ItemStack(Material.DIAMOND);
        ItemMeta diamondMeta = diamond.getItemMeta();
        diamondMeta.setDisplayName(plugin.getLocale().getMessage("interface.boost.boostfor", "30"));
        ArrayList<String> diamondLore = new ArrayList<>();
        diamondLore.add(Methods.formatText("&7Costs &6&l" + Methods.getBoostCost(30, amount) + "."));
        diamondMeta.setLore(diamondLore);
        diamond.setItemMeta(diamondMeta);

        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta emeraldMeta = emerald.getItemMeta();
        emeraldMeta.setDisplayName(plugin.getLocale().getMessage("interface.boost.boostfor", "60"));
        ArrayList<String> emeraldLore = new ArrayList<>();
        emeraldLore.add(Methods.formatText("&7Costs &6&l" + Methods.getBoostCost(60, amount) + "."));
        emeraldMeta.setLore(emeraldLore);
        emerald.setItemMeta(emeraldMeta);

        inventory.setItem(10, coal);
        inventory.setItem(12, iron);
        inventory.setItem(14, diamond);
        inventory.setItem(16, emerald);

        inventory.setItem(0, Methods.getBackgroundGlass(true));
        inventory.setItem(1, Methods.getBackgroundGlass(true));
        inventory.setItem(2, Methods.getBackgroundGlass(false));
        inventory.setItem(6, Methods.getBackgroundGlass(false));
        inventory.setItem(7, Methods.getBackgroundGlass(true));
        inventory.setItem(8, Methods.getBackgroundGlass(true));
        inventory.setItem(9, Methods.getBackgroundGlass(true));
        inventory.setItem(17, Methods.getBackgroundGlass(true));
        inventory.setItem(18, Methods.getBackgroundGlass(true));
        inventory.setItem(19, Methods.getBackgroundGlass(true));
        inventory.setItem(20, Methods.getBackgroundGlass(false));
        inventory.setItem(24, Methods.getBackgroundGlass(false));
        inventory.setItem(25, Methods.getBackgroundGlass(true));
        inventory.setItem(26, Methods.getBackgroundGlass(true));

        createButton(4, Material.valueOf(plugin.getConfig().getString("Interfaces.Exit Icon")),
                plugin.getLocale().getMessage("general.nametag.back"));

        createButton(8, Methods.addTexture(new ItemStack(plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3),
                "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"),
                plugin.getLocale().getMessage("general.nametag.back"));

        ItemStack head = new ItemStack(plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3);
        ItemStack skull = Methods.addTexture(head, "http://textures.minecraft.net/texture/1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b");
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skull.setDurability((short) 3);
        skullMeta.setDisplayName(Methods.formatText("&6&l+1"));
        skull.setItemMeta(skullMeta);

        ItemStack head2 = new ItemStack(plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3);
        ItemStack skull2 = Methods.addTexture(head2, "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23");
        SkullMeta skull2Meta = (SkullMeta) skull2.getItemMeta();
        skull2.setDurability((short) 3);
        skull2Meta.setDisplayName(Methods.formatText("&6&l-1"));
        skull2.setItemMeta(skull2Meta);

        if (amount != 1) {
            inventory.setItem(0, skull2);
        }
        if (amount < Setting.MAX_PLAYER_BOOST.getInt()) {
            inventory.setItem(8, skull);
        }
    }

    @Override
    protected void registerClickables() {
        resetClickables();


        registerClickable(4, (player, inventory, cursor, slot, type) -> spawner.overview(player));


        registerClickable(0, (player, inventory, cursor, slot, type) -> {
            amount--;
            setUp();
            constructGUI();
        });

        registerClickable(8, (player, inventory, cursor, slot, type) -> {
            amount++;
            setUp();
            constructGUI();
        });

        registerClickable(10, (player, inventory, cursor, slot, type) ->
                purchaseBoost(player, 5, amount));

        registerClickable(12, (player, inventory, cursor, slot, type) ->
                purchaseBoost(player, 15, amount));

        registerClickable(14, (player, inventory, cursor, slot, type) ->
                purchaseBoost(player, 30, amount));

        registerClickable(16, (player, inventory, cursor, slot, type) ->
                purchaseBoost(player, 60, amount));
    }

    private void purchaseBoost(Player player, int time, int amt) {
            Location location = spawner.getLocation();
            player.closeInventory();
        EpicSpawners instance = plugin;

        String un = plugin.getConfig().getString("Spawner Boosting.Item Charged For A Boost");

            String[] parts = un.split(":");

            String type = parts[0];
            String multi = parts[1];
            int cost = Methods.boostCost(multi, time, amt);
            if (!type.equals("ECO") && !type.equals("XP")) {
                ItemStack stack = new ItemStack(Material.valueOf(type));
                int invAmt = Methods.getAmountInInventory(player.getInventory(), stack);
                if (invAmt >= cost) {
                    stack.setAmount(cost);
                    Methods.removeFromInventory(player.getInventory(), stack);
                } else {
                    player.sendMessage(References.getPrefix() + plugin.getLocale().getMessage("event.upgrade.cannotafford"));
                    return;
                }
            } else if (type.equals("ECO")) {
                if (plugin.getEconomy() != null) {
                    if (plugin.getEconomy().hasBalance(player, cost)) {
                        plugin.getEconomy().withdrawBalance(player, cost);
                    } else {
                        player.sendMessage(References.getPrefix() + plugin.getLocale().getMessage("event.upgrade.cannotafford"));
                        return;
                    }
                } else {
                    player.sendMessage("Economy not enabled.");
                    return;
                }
            } else if (type.equals("XP")) {
                if (player.getLevel() >= cost || player.getGameMode() == GameMode.CREATIVE) {
                    if (player.getGameMode() != GameMode.CREATIVE) {
                        player.setLevel(player.getLevel() - cost);
                    }
                } else {
                    player.sendMessage(References.getPrefix() + plugin.getLocale().getMessage("event.upgrade.cannotafford"));
                    return;
                }
            }
            Calendar c = Calendar.getInstance();
            Date currentDate = new Date();
            c.setTime(currentDate);
            c.add(Calendar.MINUTE, time);


            BoostData boostData = new BoostData(BoostType.LOCATION, amt, c.getTime().getTime(), location);
            instance.getBoostManager().addBoostToSpawner(boostData);
            player.sendMessage(References.getPrefix() + plugin.getLocale().getMessage("event.boost.applied"));
            player.playSound(location, Sound.ENTITY_VILLAGER_YES, 1, 1);
    }

    @Override
    protected void registerOnCloses() {

    }
}

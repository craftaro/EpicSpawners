package com.songoda.epicspawners.gui;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.ESpawner;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GUISpawnerShop extends AbstractGUI {

    private final EpicSpawnersPlugin plugin;
    private List<SpawnerData> entities;
    private int page = 1;
    private int max = 0;
    private int totalAmount = 0;
    private int slots = 0;

    public GUISpawnerShop(EpicSpawnersPlugin plugin, Player player) {
        super(player);
        this.plugin = plugin;
        setUp();
    }
    
    private void setUp() {
        int show = 0;
        int start = (page - 1) * 32;
        entities = new ArrayList<>();
        totalAmount = 0;
        for (SpawnerData spawnerData : plugin.getSpawnerManager().getAllSpawnerData()) {
            if (!spawnerData.isInShop() || !spawnerData.isActive()) continue;
            if (!spawnerData.getIdentifyingName().toLowerCase().equals("omni")
                    && player.hasPermission("epicspawners.shop." + Methods.getTypeFromString(spawnerData.getIdentifyingName()).replaceAll(" ", "_"))) {
                if (totalAmount >= start) {
                    if (show <= 32) {
                        entities.add(spawnerData);
                        show++;
                    }
                }
            }
            totalAmount++;
        }

        int size = entities.size();
        if (size == 24 || size == 25) size = 26;
        slots = 54;
        if (size <= 7) {
            slots = 27;
        } else if (size <= 15) {
            slots = 36;
        } else if (size <= 25) {
            slots = 45;
        }

        init(plugin.getLocale().getMessage("interface.shop.title"), slots);
    }

    @Override
    public void constructGUI() {
        inventory.clear();
        resetClickables();
        registerClickables();

        max = (int) Math.ceil((double) totalAmount / (double) 32);

        int place = 10;
        for (SpawnerData spawnerData : entities) {
            if (place == 17 || place == (slots - 18)) place++;
            if (place == 18 && slots == 36) place++;

            ItemStack it = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
            ItemStack item = plugin.getHeads().addTexture(it, spawnerData);

            if (spawnerData.getDisplayItem() != null) {
                Material mat = spawnerData.getDisplayItem();
                if (mat != Material.AIR)
                    item = new ItemStack(mat, 1);
            }

            ItemMeta itemmeta = item.getItemMeta();
            String name = Methods.compileName(spawnerData, 1, true);
            ArrayList<String> lore = new ArrayList<>();
            double price = spawnerData.getShopPrice();
            lore.add(Methods.formatText(plugin.getLocale().getMessage("interface.shop.buyprice", Methods.formatEconomy(price))));
            String loreString = plugin.getLocale().getMessage("interface.shop.lore", Methods.getTypeFromString(Methods.getTypeFromString(spawnerData.getDisplayName())));
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                loreString = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, loreString.replace(" ", "_")).replace("_", " ");
            }
            lore.add(loreString);
            itemmeta.setLore(lore);
            itemmeta.setDisplayName(name);
            item.setItemMeta(itemmeta);
            inventory.setItem(place, item);

            registerClickable(place, (player, inventory, cursor, slot, type) ->
                    new GUIShopItem(plugin, this, spawnerData, player));

            place++;
        }

        int max = (int) Math.ceil((double) totalAmount / (double) 36);
        int num = 0;
        while (num != 9) {
            inventory.setItem(num, Methods.getGlass());
            num++;
        }
        int num2 = slots - 9;
        while (num2 != slots) {
            inventory.setItem(num2, Methods.getGlass());
            num2++;
        }

        ItemStack exit = new ItemStack(Material.valueOf(plugin.getConfig().getString("Interfaces.Exit Icon")), 1);
        ItemMeta exitmeta = exit.getItemMeta();
        exitmeta.setDisplayName(plugin.getLocale().getMessage("general.nametag.exit"));
        exit.setItemMeta(exitmeta);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
        ItemStack skull = Methods.addTexture(head, "http://textures.minecraft.net/texture/1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b");
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skull.setDurability((short) 3);
        skullMeta.setDisplayName(plugin.getLocale().getMessage("general.nametag.next"));
        skull.setItemMeta(skullMeta);

        ItemStack head2 = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
        ItemStack skull2 = Methods.addTexture(head2, "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23");
        SkullMeta skull2Meta = (SkullMeta) skull2.getItemMeta();
        skull2.setDurability((short) 3);
        skull2Meta.setDisplayName(plugin.getLocale().getMessage("general.nametag.back"));
        skull2.setItemMeta(skull2Meta);

        inventory.setItem(8, exit);

        inventory.setItem(0, Methods.getBackgroundGlass(true));
        inventory.setItem(1, Methods.getBackgroundGlass(true));
        inventory.setItem(9, Methods.getBackgroundGlass(true));

        inventory.setItem(7, Methods.getBackgroundGlass(true));
        inventory.setItem(17, Methods.getBackgroundGlass(true));

        inventory.setItem(slots - 18, Methods.getBackgroundGlass(true));
        inventory.setItem(slots - 9, Methods.getBackgroundGlass(true));
        inventory.setItem(slots - 8, Methods.getBackgroundGlass(true));

        inventory.setItem(slots - 10, Methods.getBackgroundGlass(true));
        inventory.setItem(slots - 2, Methods.getBackgroundGlass(true));
        inventory.setItem(slots - 1, Methods.getBackgroundGlass(true));

        inventory.setItem(2, Methods.getBackgroundGlass(false));
        inventory.setItem(6, Methods.getBackgroundGlass(false));
        inventory.setItem(slots - 7, Methods.getBackgroundGlass(false));
        inventory.setItem(slots - 3, Methods.getBackgroundGlass(false));

        if (page != 1) {
            inventory.setItem(slots - 8, skull2);
        }
        if (page != max) {
            inventory.setItem(slots - 2, skull);
        }
    }

    @Override
    protected void registerClickables() {
        registerClickable(8, (player, inventory, cursor, slot, type) -> player.closeInventory());

        registerClickable(slots - 8, (player, inventory, cursor, slot, type) -> {
            if (page == 1) return;
            page--;
            setUp();
            constructGUI();
        });

        registerClickable(slots - 2, (player, inventory, cursor, slot, type) -> {
            if (page == max) return;
            page++;
            setUp();
            constructGUI();
        });
    }

    @Override
    protected void registerOnCloses() {

    }
}

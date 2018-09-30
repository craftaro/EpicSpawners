package com.songoda.epicspawners.spawners.shop;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.player.MenuType;
import com.songoda.epicspawners.player.PlayerData;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by songoda on 3/10/2017.
 */
public class Shop {

    private final EpicSpawnersPlugin instance;

    public Shop(EpicSpawnersPlugin instance) {
        this.instance = instance;
    }

    public void open(Player player, int page) {
        try {
            PlayerData playerData = instance.getPlayerActionManager().getPlayerAction(player);
            playerData.setCurrentPage(page);

            List<SpawnerData> entities = new ArrayList<>();

            int num = 0, show = 0;
            int start = (page - 1) * 33;

            for (SpawnerData spawnerData : instance.getSpawnerManager().getAllSpawnerData()) {
                if (!spawnerData.isInShop() || !spawnerData.isActive())  continue;
                if (!spawnerData.getIdentifyingName().toLowerCase().equals("omni")
                        && player.hasPermission("epicspawners.shop." + Methods.getTypeFromString(spawnerData.getIdentifyingName()).replaceAll(" ", "_"))) {
                    if (num >= start && show <= 33) {
                        entities.add(spawnerData);
                        show++;
                    }
                }
                num++;
            }

            int amount = entities.size();
            String title = TextComponent.formatTitle(instance.getLocale().getMessage("interface.shop.title"));
            Inventory inventory = Bukkit.createInventory(null, 54, title);
            int max2 = 54;

            if (amount <= 7) {
                inventory = Bukkit.createInventory(null, 27, title);
                max2 = 27;
            } else if (amount <= 15) {
                inventory = Bukkit.createInventory(null, 36, title);
                max2 = 36;
            } else if (amount <= 25) {
                inventory = Bukkit.createInventory(null, 45, title);
                max2 = 45;
            }

            int max22 = max2;
            int place = 10;
            for (SpawnerData spawnerData : entities) {
                if (place == 17 || place == (max22 - 18)) place++;

                ItemStack it = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
                ItemStack item = instance.getHeads().addTexture(it, spawnerData);

                if (spawnerData.getDisplayItem() != null) {
                    Material mat = spawnerData.getDisplayItem();
                    if (mat != Material.AIR)
                        item = new ItemStack(mat, 1);
                }

                ItemMeta itemmeta = item.getItemMeta();
                String name = Methods.compileName(spawnerData, 1, true);
                ArrayList<String> lore = new ArrayList<>();
                double price = spawnerData.getShopPrice();
                lore.add(TextComponent.formatText(instance.getLocale().getMessage("interface.shop.buyprice", TextComponent.formatEconomy(price))));
                String loreString = instance.getLocale().getMessage("interface.shop.lore", Methods.getTypeFromString(Methods.getTypeFromString(spawnerData.getDisplayName())));
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    loreString = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, loreString.replace(" ", "_")).replace("_", " ");
                }
                lore.add(loreString);
                itemmeta.setLore(lore);
                itemmeta.setDisplayName(name);
                item.setItemMeta(itemmeta);
                inventory.setItem(place, item);

                place++;
            }

            int max = (int) Math.ceil((double) num / (double) 36);
            num = 0;
            while (num != 9) {
                inventory.setItem(num, Methods.getGlass());
                num++;
            }
            int num2 = max2 - 9;
            while (num2 != max2) {
                inventory.setItem(num2, Methods.getGlass());
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

            inventory.setItem(8, exit);

            inventory.setItem(0, Methods.getBackgroundGlass(true));
            inventory.setItem(1, Methods.getBackgroundGlass(true));
            inventory.setItem(9, Methods.getBackgroundGlass(true));

            inventory.setItem(7, Methods.getBackgroundGlass(true));
            inventory.setItem(17, Methods.getBackgroundGlass(true));

            inventory.setItem(max22 - 18, Methods.getBackgroundGlass(true));
            inventory.setItem(max22 - 9, Methods.getBackgroundGlass(true));
            inventory.setItem(max22 - 8, Methods.getBackgroundGlass(true));

            inventory.setItem(max22 - 10, Methods.getBackgroundGlass(true));
            inventory.setItem(max22 - 2, Methods.getBackgroundGlass(true));
            inventory.setItem(max22 - 1, Methods.getBackgroundGlass(true));

            inventory.setItem(2, Methods.getBackgroundGlass(false));
            inventory.setItem(6, Methods.getBackgroundGlass(false));
            inventory.setItem(max22 - 7, Methods.getBackgroundGlass(false));
            inventory.setItem(max22 - 3, Methods.getBackgroundGlass(false));

            if (page != 1) {
                inventory.setItem(max22 - 8, skull2);
            }
            if (page != max) {
                inventory.setItem(max22 - 2, skull);
            }

            player.openInventory(inventory);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void show(int amt, Player player) {
        try {
            PlayerData playerData = instance.getPlayerActionManager().getPlayerAction(player);
            SpawnerData spawnerData = playerData.getLastData();
            Inventory inventory = Bukkit.createInventory(null, 45, TextComponent.formatTitle(instance.getLocale().getMessage("interface.shop.spawnershoptitle", Methods.compileName(spawnerData, 1, false))));

            int num = 0;
            while (num != 9) {
                inventory.setItem(num, Methods.getGlass());
                num++;
            }

            num = 36;
            while (num != 45) {
                inventory.setItem(num, Methods.getGlass());
                num++;
            }

            inventory.setItem(1, Methods.getBackgroundGlass(true));
            inventory.setItem(9, Methods.getBackgroundGlass(true));

            inventory.setItem(7, Methods.getBackgroundGlass(true));
            inventory.setItem(17, Methods.getBackgroundGlass(true));

            inventory.setItem(27, Methods.getBackgroundGlass(true));
            inventory.setItem(36, Methods.getBackgroundGlass(true));
            inventory.setItem(37, Methods.getBackgroundGlass(true));

            inventory.setItem(35, Methods.getBackgroundGlass(true));
            inventory.setItem(43, Methods.getBackgroundGlass(true));
            inventory.setItem(44, Methods.getBackgroundGlass(true));

            inventory.setItem(2, Methods.getBackgroundGlass(false));
            inventory.setItem(6, Methods.getBackgroundGlass(false));
            inventory.setItem(38, Methods.getBackgroundGlass(false));
            inventory.setItem(42, Methods.getBackgroundGlass(false));

            double price = spawnerData.getShopPrice() * amt;

            ItemStack it = new ItemStack(Material.PLAYER_HEAD, amt, (byte) 3);

            ItemStack item = EpicSpawnersPlugin.getInstance().getHeads().addTexture(it, spawnerData);


            if (spawnerData.getDisplayItem() != null) {
                Material mat = spawnerData.getDisplayItem();
                if (!mat.equals(Material.AIR))
                    item = new ItemStack(mat, 1);
            }

            item.setAmount(amt);
            ItemMeta itemmeta = item.getItemMeta();
            String name = Methods.compileName(spawnerData, 1, false);
            itemmeta.setDisplayName(name);
            ArrayList<String> lore = new ArrayList<>();
            lore.add(instance.getLocale().getMessage("interface.shop.buyprice", TextComponent.formatEconomy(price)));
            itemmeta.setLore(lore);
            item.setItemMeta(itemmeta);
            inventory.setItem(22, item);


            ItemStack plus = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1, (short) 5);
            ItemMeta plusmeta = plus.getItemMeta();
            plusmeta.setDisplayName(instance.getLocale().getMessage("interface.shop.add1"));
            plus.setItemMeta(plusmeta);
            if (item.getAmount() + 1 <= 64) {
                inventory.setItem(15, plus);
            }

            plus = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 10, (short) 5);
            plusmeta.setDisplayName(instance.getLocale().getMessage("interface.shop.add10"));
            plus.setItemMeta(plusmeta);
            if (item.getAmount() + 10 <= 64) {
                inventory.setItem(33, plus);
            }

            plus = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 64, (short) 5);
            plusmeta.setDisplayName(instance.getLocale().getMessage("interface.shop.set64"));
            plus.setItemMeta(plusmeta);
            if (item.getAmount() != 64) {
                inventory.setItem(25, plus);
            }

            ItemStack minus = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1, (short) 14);
            ItemMeta minusmeta = minus.getItemMeta();
            minusmeta.setDisplayName(instance.getLocale().getMessage("interface.shop.remove1"));
            minus.setItemMeta(minusmeta);
            if (item.getAmount() != 1) {
                inventory.setItem(11, minus);
            }

            minus = new ItemStack(Material.RED_STAINED_GLASS_PANE, 10, (short) 14);
            minusmeta.setDisplayName(instance.getLocale().getMessage("interface.shop.remove10"));
            minus.setItemMeta(minusmeta);
            if (item.getAmount() - 10 >= 0) {
                inventory.setItem(29, minus);
            }

            minus = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1, (short) 14);
            minusmeta.setDisplayName(instance.getLocale().getMessage("interface.shop.set1"));
            minus.setItemMeta(minusmeta);
            if (item.getAmount() != 1) {
                inventory.setItem(19, minus);
            }

            ItemStack exit = new ItemStack(Material.valueOf(EpicSpawnersPlugin.getInstance().getConfig().getString("Interfaces.Exit Icon")), 1);
            ItemMeta exitmeta = exit.getItemMeta();
            exitmeta.setDisplayName(instance.getLocale().getMessage("general.nametag.exit"));
            exit.setItemMeta(exitmeta);
            inventory.setItem(8, exit);

            ItemStack head2 = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
            ItemStack skull2 = Arconix.pl().getApi().getGUI().addTexture(head2, "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23");
            SkullMeta skull2Meta = (SkullMeta) skull2.getItemMeta();
            skull2.setDurability((short) 3);
            skull2Meta.setDisplayName(instance.getLocale().getMessage("general.nametag.back"));
            skull2.setItemMeta(skull2Meta);

            inventory.setItem(0, skull2);

            ItemStack buy = new ItemStack(Material.valueOf(EpicSpawnersPlugin.getInstance().getConfig().getString("Interfaces.Buy Icon")), 1);
            ItemMeta buymeta = buy.getItemMeta();
            buymeta.setDisplayName(instance.getLocale().getMessage("general.nametag.confirm"));
            buy.setItemMeta(buymeta);
            inventory.setItem(40, buy);

            player.openInventory(inventory);

            player.openInventory(inventory);
            playerData.setInMenu(MenuType.SHOP);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void confirm(Player player, int amount) {
        try {
            SpawnerData spawnerData = instance.getPlayerActionManager().getPlayerAction(player).getLastData();
            if (EpicSpawnersPlugin.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
                player.sendMessage("Vault is not installed.");
                return;
            }
            RegisteredServiceProvider<Economy> rsp = EpicSpawnersPlugin.getInstance().getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
            double price = spawnerData.getShopPrice() * amount;
            if (!player.isOp() && !econ.has(player, price)) {
                player.sendMessage(EpicSpawnersPlugin.getInstance().getReferences().getPrefix() + instance.getLocale().getMessage("event.shop.cannotafford"));
                return;
            }
            ItemStack item = spawnerData.toItemStack(amount);


            player.getInventory().addItem(item);

            player.sendMessage(EpicSpawnersPlugin.getInstance().getReferences().getPrefix() + instance.getLocale().getMessage("event.shop.purchasesuccess"));


            if (!player.isOp()) {
                econ.withdrawPlayer(player, price);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}

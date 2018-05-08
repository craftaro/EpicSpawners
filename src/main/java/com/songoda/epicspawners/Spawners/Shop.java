package com.songoda.epicspawners.Spawners;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Utils.Debugger;
import com.songoda.epicspawners.Utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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

    private EpicSpawners instance;

    public Shop(EpicSpawners instance) {
        this.instance = instance;
    }

    public void open(Player p, int page) {
        try {
            EpicSpawners.getInstance().page.put(p, page);

            List<String> entities = new ArrayList<>();

            int num = 0;
            int show = 0;
            int start = (page - 1) * 32;
            ConfigurationSection cs = EpicSpawners.getInstance().spawnerFile.getConfig().getConfigurationSection("Entities");
            for (String value : cs.getKeys(false)) {
                if (!value.toLowerCase().equals("omni")
                        && EpicSpawners.getInstance().spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(value) + ".In-Shop")
                        && (p.hasPermission("epicspawners.*") || p.hasPermission("epicspawners.shop.*")
                        || p.hasPermission("epicspawners.shop." + Methods.getTypeFromString(value).replaceAll(" ", "_")))) {
                    if (num >= start && show <= 32) {
                        entities.add(value);
                        show++;
                    }
                }
                num++;


            }

            int amt = entities.size();
            String title = Arconix.pl().getApi().format().formatTitle(instance.getLocale().getMessage("interface.shop.title"));
            Inventory i = Bukkit.createInventory(null, 54, title);
            int max2 = 54;
            if (amt <= 7) {
                i = Bukkit.createInventory(null, 27, title);
                max2 = 27;
            } else if (amt <= 15) {
                i = Bukkit.createInventory(null, 36, title);
                max2 = 36;
            } else if (amt <= 25) {
                i = Bukkit.createInventory(null, 45, title);
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
                double price = EpicSpawners.getInstance().spawnerFile.getConfig().getDouble("Entities." + Methods.getTypeFromString(value) + ".Shop-Price");
                lore.add(Arconix.pl().getApi().format().formatText(instance.getLocale().getMessage("interface.shop.buyprice", Arconix.pl().getApi().format().formatEconomy(price))));
                String loreString = instance.getLocale().getMessage("interface.shop.lore", Methods.getTypeFromString(Methods.getTypeFromString(value)));
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
            exitmeta.setDisplayName(instance.getLocale().getMessage("general.nametag.exit"));
            exit.setItemMeta(exitmeta);

            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            ItemStack skull = head;
            if (!EpicSpawners.getInstance().v1_7)
                skull = Arconix.pl().getApi().getGUI().addTexture(head, "http://textures.minecraft.net/texture/1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b");
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            if (EpicSpawners.getInstance().v1_7)
                skullMeta.setOwner("MHF_ArrowRight");
            skull.setDurability((short) 3);
            skullMeta.setDisplayName(instance.getLocale().getMessage("general.nametag.next"));
            skull.setItemMeta(skullMeta);

            ItemStack head2 = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            ItemStack skull2 = head2;
            if (!EpicSpawners.getInstance().v1_7)
                skull2 = Arconix.pl().getApi().getGUI().addTexture(head2, "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23");
            SkullMeta skull2Meta = (SkullMeta) skull2.getItemMeta();
            if (EpicSpawners.getInstance().v1_7)
                skull2Meta.setOwner("MHF_ArrowLeft");
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

            p.openInventory(i);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void show(String type, int amt, Player p) {
        try {
            Inventory i = Bukkit.createInventory(null, 45, Arconix.pl().getApi().format().formatTitle(instance.getLocale().getMessage("interface.shop.spawnershowtitle", Methods.compileName(type, 1, false))));

            int num = 0;
            while (num != 9) {
                i.setItem(num, Methods.getGlass());
                num++;
            }

            num = 36;
            while (num != 45) {
                i.setItem(num, Methods.getGlass());
                num++;
            }

            i.setItem(1, Methods.getBackgroundGlass(true));
            i.setItem(9, Methods.getBackgroundGlass(true));

            i.setItem(7, Methods.getBackgroundGlass(true));
            i.setItem(17, Methods.getBackgroundGlass(true));

            i.setItem(27, Methods.getBackgroundGlass(true));
            i.setItem(36, Methods.getBackgroundGlass(true));
            i.setItem(37, Methods.getBackgroundGlass(true));

            i.setItem(35, Methods.getBackgroundGlass(true));
            i.setItem(43, Methods.getBackgroundGlass(true));
            i.setItem(44, Methods.getBackgroundGlass(true));

            i.setItem(2, Methods.getBackgroundGlass(false));
            i.setItem(6, Methods.getBackgroundGlass(false));
            i.setItem(38, Methods.getBackgroundGlass(false));
            i.setItem(42, Methods.getBackgroundGlass(false));

            double price = EpicSpawners.getInstance().spawnerFile.getConfig().getDouble("Entities." + Methods.getTypeFromString(type) + ".Shop-Price") * amt;

            ItemStack it = new ItemStack(Material.SKULL_ITEM, amt, (byte) 3);


            ItemStack item = EpicSpawners.getInstance().heads.addTexture(it, Methods.getTypeFromString(type));


            if (EpicSpawners.getInstance().spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(type) + ".Display-Item") != null) {
                Material mat = Material.valueOf(EpicSpawners.getInstance().spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(type) + ".Display-Item"));
                if (!mat.equals(Material.AIR))
                    item = new ItemStack(mat, 1);
            }

            item.setAmount(amt);
            ItemMeta itemmeta = item.getItemMeta();
            String name = Methods.compileName(type, 0, false);
            itemmeta.setDisplayName(name);
            ArrayList<String> lore = new ArrayList<>();
            lore.add(instance.getLocale().getMessage("interface.shop.buyprice", Arconix.pl().getApi().format().formatEconomy(price)));
            itemmeta.setLore(lore);
            item.setItemMeta(itemmeta);
            i.setItem(22, item);

            ItemStack plus = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
            ItemMeta plusmeta = plus.getItemMeta();
            plusmeta.setDisplayName(instance.getLocale().getMessage("interface.shop.add1"));
            plus.setItemMeta(plusmeta);
            if (item.getAmount() + 1 <= 64) {
                i.setItem(15, plus);
            }

            plus = new ItemStack(Material.STAINED_GLASS_PANE, 10, (short) 5);
            plusmeta.setDisplayName(instance.getLocale().getMessage("interface.shop.add10"));
            plus.setItemMeta(plusmeta);
            if (item.getAmount() + 10 <= 64) {
                i.setItem(33, plus);
            }

            plus = new ItemStack(Material.STAINED_GLASS_PANE, 64, (short) 5);
            plusmeta.setDisplayName(instance.getLocale().getMessage("interface.shop.set64"));
            plus.setItemMeta(plusmeta);
            if (item.getAmount() != 64) {
                i.setItem(25, plus);
            }

            ItemStack minus = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
            ItemMeta minusmeta = minus.getItemMeta();
            minusmeta.setDisplayName(instance.getLocale().getMessage("interface.shop.remove1"));
            minus.setItemMeta(minusmeta);
            if (item.getAmount() != 1) {
                i.setItem(11, minus);
            }

            minus = new ItemStack(Material.STAINED_GLASS_PANE, 10, (short) 14);
            minusmeta.setDisplayName(instance.getLocale().getMessage("interface.shop.remove10"));
            minus.setItemMeta(minusmeta);
            if (item.getAmount() - 10 >= 0) {
                i.setItem(29, minus);
            }

            minus = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
            minusmeta.setDisplayName(instance.getLocale().getMessage("interface.shop.set1"));
            minus.setItemMeta(minusmeta);
            if (item.getAmount() != 1) {
                i.setItem(19, minus);
            }

            ItemStack exit = new ItemStack(Material.valueOf(EpicSpawners.getInstance().getConfig().getString("Interfaces.Exit Icon")), 1);
            ItemMeta exitmeta = exit.getItemMeta();
            exitmeta.setDisplayName(instance.getLocale().getMessage("general.nametag.exit"));
            exit.setItemMeta(exitmeta);
            i.setItem(8, exit);

            ItemStack head2 = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            ItemStack skull2 = head2;
            if (!EpicSpawners.getInstance().v1_7)
                skull2 = Arconix.pl().getApi().getGUI().addTexture(head2, "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23");
            SkullMeta skull2Meta = (SkullMeta) skull2.getItemMeta();
            if (EpicSpawners.getInstance().v1_7)
                skull2Meta.setOwner("MHF_ArrowLeft");
            skull2.setDurability((short) 3);
            skull2Meta.setDisplayName(instance.getLocale().getMessage("general.nametag.back"));
            skull2.setItemMeta(skull2Meta);

            i.setItem(0, skull2);

            ItemStack buy = new ItemStack(Material.valueOf(EpicSpawners.getInstance().getConfig().getString("Interfaces.Buy Icon")), 1);
            ItemMeta buymeta = buy.getItemMeta();
            buymeta.setDisplayName(instance.getLocale().getMessage("general.nametag.confirm"));
            buy.setItemMeta(buymeta);
            i.setItem(40, buy);

            p.openInventory(i);
            EpicSpawners.getInstance().inShow.put(p, type);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void confirm(Player p, int amt) {
        try {
            String type = EpicSpawners.getInstance().inShow.get(p);
            if (EpicSpawners.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {
                p.sendMessage("Vault is not installed.");
                return;
            }
            RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = EpicSpawners.getInstance().getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
            double price = EpicSpawners.getInstance().spawnerFile.getConfig().getDouble("Entities." + Methods.getTypeFromString(type) + ".Shop-Price") * amt;
            if (!p.isOp() && !econ.has(p, price)) {
                p.sendMessage(EpicSpawners.getInstance().references.getPrefix() + instance.getLocale().getMessage("event.shop.cannotafford"));
                return;
            }
            ItemStack item = EpicSpawners.getInstance().getApi().newSpawnerItem(type, amt);
            p.getInventory().addItem(item);

            p.sendMessage(EpicSpawners.getInstance().references.getPrefix() + instance.getLocale().getMessage("event.shop.purchasesuccess"));

            if (!p.isOp()) {
                econ.withdrawPlayer(p, price);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}

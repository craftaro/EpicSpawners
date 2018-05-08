package com.songoda.epicspawners.Spawners;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Utils.Debugger;
import com.songoda.epicspawners.Utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by songo on 9/3/2017.
 */
public class SpawnerEditor {

    EpicSpawners instance;

    public SpawnerEditor(EpicSpawners instance) {
        this.instance = instance;
    }

    public void open(Player p, int page) {
        try {
            EpicSpawners.getInstance().itemEditorInstance = new ArrayList<>();
            EpicSpawners.getInstance().entityEditorInstance = new ArrayList<>();
            EpicSpawners.getInstance().commandEditorInstance = new ArrayList<>();
            EpicSpawners.getInstance().editing.remove(p);
            EpicSpawners.getInstance().isEntityInstanceSaved = false;
            EpicSpawners.getInstance().isItemInstanceSaved = false;
            EpicSpawners.getInstance().isBlockInstanceSaved = false;
            EpicSpawners.getInstance().isCommandInstanceSaved = false;
            EpicSpawners.getInstance().newSpawnerName = "";
            EpicSpawners.getInstance().page.put(p, page);

            List<String> entities = new ArrayList<>();

            int num = 0;
            int show = 0;
            int start = (page - 1) * 32;

            ConfigurationSection cs = EpicSpawners.getInstance().spawnerFile.getConfig().getConfigurationSection("Entities");
            for (String key : cs.getKeys(false)) {
                if (num >= start && !key.equals("Omni")) {
                    if (show <= 32) {
                        key = key.toUpperCase().replace(" ", "_");
                        entities.add(key);
                        show++;
                    }
                }
                num++;
            }

            int max = (int) Math.ceil((double) num / (double) 36);
            int amt = entities.size();
            Inventory i = Bukkit.createInventory(null, 54, Arconix.pl().getApi().format().formatTitle("Spawner Editor"));
            int max2 = 54;
            if (amt <= 7) {
                i = Bukkit.createInventory(null, 27, Arconix.pl().getApi().format().formatTitle("Spawner Editor"));
                max2 = 27;
            } else if (amt <= 15) {
                i = Bukkit.createInventory(null, 36, Arconix.pl().getApi().format().formatTitle("Spawner Editor"));
                max2 = 36;
            } else if (amt <= 25) {
                i = Bukkit.createInventory(null, 45, Arconix.pl().getApi().format().formatTitle("Spawner Editor"));
                max2 = 45;
            }

            ItemStack exit = new ItemStack(Material.WOOD_DOOR, 1);
            ItemMeta exitMeta = exit.getItemMeta();
            exitMeta.setDisplayName(instance.getLocale().getMessage("general.nametag.exit"));
            exit.setItemMeta(exitMeta);
            i.setItem(8, exit);


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
            skull2Meta.setDisplayName(instance.getLocale().getMessage("general.nametag.last"));
            skull2.setItemMeta(skull2Meta);

            final int max22 = max2;
            int place = 10;
            int dis = start + 1;
            for (String value : entities) {
                if (place == 17)
                    place++;
                if (place == (max22 - 18))
                    place++;
                ItemStack it = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);

                ItemStack item = EpicSpawners.getInstance().heads.addTexture(it, Methods.getTypeFromString(value));

                ItemMeta itemmeta = item.getItemMeta();
                String name = Methods.compileName(value, 0, false);
                ArrayList<String> lore = new ArrayList<>();
                lore.add(Arconix.pl().getApi().format().formatText("&7Click to &a&lEdit&7."));
                lore.add(Arconix.pl().getApi().format().convertToInvisibleString(Integer.toString(dis)));
                itemmeta.setLore(lore);
                itemmeta.setDisplayName(name);
                item.setItemMeta(itemmeta);
                i.setItem(place, item);
                place++;
                dis++;
            }

            num = 0;
            while (num != 8) {
                i.setItem(num, Methods.getGlass());
                num++;
            }
            num = max22 - 9;
            while (num != max22) {
                i.setItem(num, Methods.getGlass());
                num++;
            }

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

            ItemStack newSpawner = new ItemStack(Material.PAPER, 1);
            ItemMeta newSpawnerMeta = newSpawner.getItemMeta();
            newSpawnerMeta.setDisplayName(Arconix.pl().getApi().format().formatText("&9&lNew Spawner"));
            newSpawner.setItemMeta(newSpawnerMeta);
            i.setItem(max22 - 4, newSpawner);

            p.openInventory(i);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void overview(Player p, int id) {
        try {
            if (!EpicSpawners.getInstance().isEntityInstanceSaved)
                EpicSpawners.getInstance().entityEditorInstance = new ArrayList<>();
            if (!EpicSpawners.getInstance().isItemInstanceSaved)
                EpicSpawners.getInstance().itemEditorInstance = new ArrayList<>();
            if (!EpicSpawners.getInstance().isBlockInstanceSaved)
                EpicSpawners.getInstance().blockEditorInstance = new ArrayList<>();
            if (!EpicSpawners.getInstance().isCommandInstanceSaved)
                EpicSpawners.getInstance().commandEditorInstance = new ArrayList<>();
            EpicSpawners.getInstance().subediting.remove(p);

            int csp = 1;
            ConfigurationSection cs = EpicSpawners.getInstance().spawnerFile.getConfig().getConfigurationSection("Entities");
            for (String key : cs.getKeys(false)) {
                if (key.contains("Custom"))
                    csp++;
            }

            String type = "Custom " + csp;

            if (!EpicSpawners.getInstance().newSpawnerName.equals(""))
                type = EpicSpawners.getInstance().newSpawnerName;

            if (id != 0)
                type = getType(id);
            else
                EpicSpawners.getInstance().newSpawnerName = type;
            String name;

            name = Methods.compileName(type, 0, false);
            Inventory i = Bukkit.createInventory(null, 54, Arconix.pl().getApi().format().formatTitle(Arconix.pl().getApi().format().formatText("&8Editing: " + name + "&8.")));

            int num = 0;
            while (num != 54) {
                i.setItem(num, Methods.getGlass());
                num++;
            }

            i.setItem(0, Methods.getBackgroundGlass(false));
            i.setItem(1, Methods.getBackgroundGlass(false));
            i.setItem(2, Methods.getBackgroundGlass(false));
            i.setItem(3, Methods.getBackgroundGlass(true));
            i.setItem(4, Methods.getBackgroundGlass(true));

            i.setItem(9, Methods.getBackgroundGlass(false));
            i.setItem(13, Methods.getBackgroundGlass(true));
            i.setItem(14, Methods.getBackgroundGlass(false));
            i.setItem(15, Methods.getBackgroundGlass(true));
            i.setItem(16, Methods.getBackgroundGlass(true));
            i.setItem(17, Methods.getBackgroundGlass(true));

            i.setItem(18, Methods.getBackgroundGlass(false));
            i.setItem(22, Methods.getBackgroundGlass(false));
            i.setItem(26, Methods.getBackgroundGlass(true));

            i.setItem(27, Methods.getBackgroundGlass(true));
            i.setItem(31, Methods.getBackgroundGlass(false));
            i.setItem(35, Methods.getBackgroundGlass(false));

            i.setItem(36, Methods.getBackgroundGlass(true));
            i.setItem(37, Methods.getBackgroundGlass(true));
            i.setItem(38, Methods.getBackgroundGlass(false));
            i.setItem(39, Methods.getBackgroundGlass(true));
            i.setItem(40, Methods.getBackgroundGlass(true));
            i.setItem(44, Methods.getBackgroundGlass(false));

            i.setItem(49, Methods.getBackgroundGlass(true));
            i.setItem(50, Methods.getBackgroundGlass(true));
            i.setItem(51, Methods.getBackgroundGlass(false));
            i.setItem(52, Methods.getBackgroundGlass(false));
            i.setItem(53, Methods.getBackgroundGlass(false));


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

            i.setItem(8, skull2);

            ItemStack it = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);

            ItemStack item = EpicSpawners.getInstance().heads.addTexture(it, type);

            ItemMeta itemmeta = item.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            lore.add(Arconix.pl().getApi().format().formatText("&7Left-Click to &9Change Spawner Name&7."));
            lore.add(Arconix.pl().getApi().format().formatText("&7Middle-Click to &bChange Spawner Display Item&7."));
            if (EpicSpawners.getInstance().getConfig().getBoolean("settings.beta-features"))
                lore.add(Arconix.pl().getApi().format().formatText("&7Right-Click to &9Change Spawner Head&7."));
            lore.add(Arconix.pl().getApi().format().formatText("&6-----------------------------"));

            lore.add(Arconix.pl().getApi().format().formatText("&6Display Name: &7" + EpicSpawners.getInstance().spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(type) + ".Display-Name") + "&7."));
            if (EpicSpawners.getInstance().spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(type) + ".Display-Item")) {
                lore.add(Arconix.pl().getApi().format().formatText("&6Display Item: &7" + EpicSpawners.getInstance().spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(type) + ".Display-Item") + "&7."));
            } else {
                if (!name.contains("Custom")) {
                    lore.add(Arconix.pl().getApi().format().formatText("&6Display Item: &7Unavailable&7."));
                } else {
                    lore.add(Arconix.pl().getApi().format().formatText("&6Display Item: &7Dirt&7."));
                }
            }
            lore.add(Arconix.pl().getApi().format().formatText("&6Config Name: &7" + type + "&7."));
            itemmeta.setLore(lore);
            itemmeta.setDisplayName(name);
            item.setItemMeta(itemmeta);
            i.setItem(11, item);

            ItemStack destroy = new ItemStack(Material.TNT);
            ItemMeta destroymeta = destroy.getItemMeta();
            boolean dont = false;
            for (final EntityType val : EntityType.values()) {
                if (val.isSpawnable() && val.isAlive()) {
                    if (val.name().equals(type)) {
                        dont = true;
                    }
                }
            }
            lore = new ArrayList<>();
            if (!dont) {
                destroymeta.setDisplayName(Arconix.pl().getApi().format().formatText("&7Left-Click to: &cDisable Spawner"));
                lore.add(Arconix.pl().getApi().format().formatText("&7Right-Click to: &cDestroy Spawner"));
            } else
                destroymeta.setDisplayName(Arconix.pl().getApi().format().formatText("&7Click to: &cDisable Spawner"));
            lore.add(Arconix.pl().getApi().format().formatText("&6---------------------------"));
            if (!EpicSpawners.getInstance().spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(type) + ".Allowed"))
                lore.add(Arconix.pl().getApi().format().formatText("&6Currently:&c Disabled."));
            else
                lore.add(Arconix.pl().getApi().format().formatText("&6Currently:&a Enabled."));
            destroymeta.setLore(lore);

            destroy.setItemMeta(destroymeta);
            i.setItem(28, destroy);

            ItemStack save = new ItemStack(Material.REDSTONE);
            ItemMeta savemeta = save.getItemMeta();
            savemeta.setDisplayName(Arconix.pl().getApi().format().formatText("&a&lSave Spawner"));
            save.setItemMeta(savemeta);
            i.setItem(30, save);

            ItemStack settings = new ItemStack(Material.LEVER);
            ItemMeta settingsmeta = settings.getItemMeta();
            settingsmeta.setDisplayName(Arconix.pl().getApi().format().formatText("&9&lGeneral Settings"));
            settings.setItemMeta(settingsmeta);
            i.setItem(23, settings);

            ItemStack it2 = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);

            ItemStack entity = EpicSpawners.getInstance().heads.addTexture(it2, "OMNI");
            ItemMeta entitymeta = entity.getItemMeta();
            entitymeta.setDisplayName(Arconix.pl().getApi().format().formatText("&a&lEntity Settings"));
            entity.setItemMeta(entitymeta);
            i.setItem(25, entity);

            ItemStack item2 = new ItemStack(Material.CHEST);
            ItemMeta item2meta = item2.getItemMeta();
            item2meta.setDisplayName(Arconix.pl().getApi().format().formatText("&5&lItem Settings"));
            item2.setItemMeta(item2meta);
            i.setItem(41, item2);

            ItemStack item3 = new ItemStack(Material.GOLD_BLOCK);
            ItemMeta item3meta = item3.getItemMeta();
            item3meta.setDisplayName(Arconix.pl().getApi().format().formatText("&c&lBlock Settings"));
            item3.setItemMeta(item3meta);
            i.setItem(33, item3);

            ItemStack command = new ItemStack(Material.PAPER);
            ItemMeta commandmeta = command.getItemMeta();
            commandmeta.setDisplayName(Arconix.pl().getApi().format().formatText("&6&lCommand Settings"));
            command.setItemMeta(commandmeta);
            i.setItem(43, command);

            p.openInventory(i);
            EpicSpawners.getInstance().editing.put(p, id);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public String getType(int id) {
        String type = "PIG";
        try {
            ConfigurationSection cs = EpicSpawners.getInstance().spawnerFile.getConfig().getConfigurationSection("Entities");
            int num = 1;
            for (String key : cs.getKeys(false)) {
                if (!key.equals("Omni")) {
                    if (num == id) {
                        key = key.toUpperCase().replace(" ", "_");
                        type = key;
                    }
                    num++;
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return type;
    }

    @SuppressWarnings("unchecked")
    public void editor(Player p, String type) {
        try {
            EpicSpawners.getInstance().subediting.put(p, type);
            String typee;
            if (EpicSpawners.getInstance().editing.get(p) != 0)
                typee = getType(EpicSpawners.getInstance().editing.get(p));
            else
                typee = EpicSpawners.getInstance().newSpawnerName;
            String name = Methods.compileName(typee, 0, false);
            Inventory i = Bukkit.createInventory(null, 54, Arconix.pl().getApi().format().formatTitle(Arconix.pl().getApi().format().formatText(name + "&8 " + type + " &8Settings.")));

            int num = 0;
            while (num != 54) {
                i.setItem(num, Methods.getGlass());
                num++;
            }
            List<ItemStack> itemList = new ArrayList<>();
            List<String> entityList = new ArrayList<>();
            List<String> commandList = new ArrayList<>();
            List<String> blockList = new ArrayList<>();
            switch (type) {
                case "Item":
                    if (EpicSpawners.getInstance().itemEditorInstance.size() == 0) {
                        //EpicSpawners.getInstance().isItemInstanceSaved = false;
                    }
                    if (EpicSpawners.getInstance().isItemInstanceSaved) {
                        itemList = EpicSpawners.getInstance().itemEditorInstance;
                    } else {
                        if (EpicSpawners.getInstance().spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(typee) + ".items")) {
                            List<ItemStack> list = (List<ItemStack>) EpicSpawners.getInstance().spawnerFile.getConfig().getList("Entities." + Methods.getTypeFromString(typee) + ".items");
                            if (list != null)
                                itemList = list;
                        }
                    }
                    break;
                case "Entity":
                    if (EpicSpawners.getInstance().isEntityInstanceSaved) {
                        entityList = EpicSpawners.getInstance().entityEditorInstance;
                    } else {
                        List<String> list = (List<String>) EpicSpawners.getInstance().spawnerFile.getConfig().getList("Entities." + Methods.getTypeFromString(typee) + ".entities");
                        if (list != null)
                            entityList = list;
                        EpicSpawners.getInstance().entityEditorInstance = entityList;
                    }
                    break;
                case "Command":
                    if (EpicSpawners.getInstance().isCommandInstanceSaved) {
                        commandList = EpicSpawners.getInstance().commandEditorInstance;
                    } else {
                        List<String> list = (List<String>) EpicSpawners.getInstance().spawnerFile.getConfig().getList("Entities." + Methods.getTypeFromString(typee) + ".commands");
                        if (list != null)
                            commandList = list;
                        EpicSpawners.getInstance().commandEditorInstance = commandList;
                    }
                    break;
                case "Block":
                    if (EpicSpawners.getInstance().isBlockInstanceSaved) {
                        blockList = EpicSpawners.getInstance().blockEditorInstance;
                    } else {
                        List<String> list = (List<String>) EpicSpawners.getInstance().spawnerFile.getConfig().getList("Entities." + Methods.getTypeFromString(typee) + ".blocks");
                        if (list != null)
                            blockList = list;
                        EpicSpawners.getInstance().blockEditorInstance = blockList;
                    }
                    break;
            }

            num = 10;
            int spot = 0;
            while (num != 26) {
                if (num == 17)
                    num = num + 2;

                if (itemList.size() >= spot + 1 && type.equals("Item")) {
                    i.setItem(num, itemList.get(spot));
                } else if (blockList.size() >= spot + 1 && type.equals("Block")) {
                    i.setItem(num, new ItemStack(Material.valueOf(blockList.get(spot))));
                } else if (entityList.size() >= spot + 1 && type.equals("Entity")) {
                    ItemStack it = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
                    ItemStack item = EpicSpawners.getInstance().heads.addTexture(it, entityList.get(spot));
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(Arconix.pl().getApi().format().formatText("&e" + Methods.getTypeFromString(entityList.get(spot))));
                    item.setItemMeta(meta);
                    i.setItem(num, item);
                } else if (commandList.size() >= spot + 1 && type.equals("Command")) {
                    ItemStack parseStack = new ItemStack(Material.PAPER, 1);
                    ItemMeta meta = parseStack.getItemMeta();
                    meta.setDisplayName(Arconix.pl().getApi().format().formatText("&a/" + commandList.get(spot)));
                    parseStack.setItemMeta(meta);
                    i.setItem(num, parseStack);
                } else {
                    i.setItem(num, new ItemStack(Material.AIR));
                }
                spot++;
                num++;
            }

            i.setItem(1, Methods.getBackgroundGlass(true));
            i.setItem(7, Methods.getBackgroundGlass(true));
            i.setItem(8, Methods.getBackgroundGlass(true));

            i.setItem(9, Methods.getBackgroundGlass(true));
            i.setItem(17, Methods.getBackgroundGlass(true));

            i.setItem(36, Methods.getBackgroundGlass(false));
            i.setItem(37, Methods.getBackgroundGlass(false));
            i.setItem(38, Methods.getBackgroundGlass(false));
            i.setItem(42, Methods.getBackgroundGlass(false));
            i.setItem(43, Methods.getBackgroundGlass(false));
            i.setItem(44, Methods.getBackgroundGlass(false));

            i.setItem(45, Methods.getBackgroundGlass(true));
            i.setItem(46, Methods.getBackgroundGlass(true));
            i.setItem(47, Methods.getBackgroundGlass(false));
            i.setItem(51, Methods.getBackgroundGlass(false));
            i.setItem(52, Methods.getBackgroundGlass(true));
            i.setItem(53, Methods.getBackgroundGlass(true));

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

            if (type.equals("Item") || type.equals("Command") || type.equals("Block")) {
                ItemStack command = new ItemStack(Material.LEVER);
                ItemMeta commandmeta = command.getItemMeta();
                commandmeta.setDisplayName(Arconix.pl().getApi().format().formatText("&7Left-Click to &9Modify Global Tick Rate&7."));
                ArrayList<String> lore = new ArrayList<>();
                lore.add(Arconix.pl().getApi().format().formatText("&7(1 tick is half a second)"));
                switch (type) {
                    case "Command":
                        lore.add(Arconix.pl().getApi().format().formatText("&7Currently: &c" + EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(typee) + ".commandTickRate")));
                        break;
                    case "Item":
                        lore.add(Arconix.pl().getApi().format().formatText("&7Currently: &c" + EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(typee) + ".itemTickRate")));
                        break;
                    case "Block":
                        lore.add(Arconix.pl().getApi().format().formatText("&7Currently: &c" + EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(typee) + ".blockTickRate")));
                        break;
                }
                lore.add(Arconix.pl().getApi().format().formatText("&7Right-Click to &bModify Amount&7."));
                commandmeta.setLore(lore);
                command.setItemMeta(commandmeta);
                i.setItem(40, command);
                if (type.equals("Command")) {
                    ItemStack item = new ItemStack(Material.WATCH);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(Arconix.pl().getApi().format().formatText("&bSpawn Limit"));
                    lore = new ArrayList<>();
                    lore.add(Arconix.pl().getApi().format().formatText("&7Currently: &c" + EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(typee) + ".commandSpawnLimit")));
                    lore.add("");
                    lore.add(Arconix.pl().getApi().format().formatText("&7This is the spawn limit for entities you spawn"));
                    lore.add(Arconix.pl().getApi().format().formatText("&7from this spawner. Set to &60 &7to disable this."));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    i.setItem(49, item);
                }
            }

            if (!type.equals("Item") && !type.equals("Block")) {
                ItemStack add;
                String addName;
                if (type.equals("Command")) {
                    add = new ItemStack(Material.PAPER);
                    addName = "&6Add Command";
                } else {
                    add = new ItemStack(Material.MONSTER_EGG);
                    addName = "&6Add Entity";
                }
                ItemMeta addmeta = add.getItemMeta();
                addmeta.setDisplayName(Arconix.pl().getApi().format().formatText(addName));

                add.setItemMeta(addmeta);
                i.setItem(39, add);
            }

            ItemStack save = new ItemStack(Material.REDSTONE);
            ItemMeta savemeta = save.getItemMeta();
            savemeta.setDisplayName(Arconix.pl().getApi().format().formatText("&aSave"));
            ArrayList<String> lore = new ArrayList<>();
            lore.add(Arconix.pl().getApi().format().formatText(""));
            lore.add(Arconix.pl().getApi().format().formatText("&7This only saves the instance you will"));
            lore.add(Arconix.pl().getApi().format().formatText("&7need to click save on the main screen"));
            lore.add(Arconix.pl().getApi().format().formatText("&7to finalize changes."));
            savemeta.setLore(lore);
            save.setItemMeta(savemeta);
            if (!type.equals("Item"))
                i.setItem(41, save);
            else
                i.setItem(49, save);

            p.openInventory(i);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void basicSettings(Player p) {
        try {
            EpicSpawners.getInstance().subediting.put(p, "basic");
            String typee;
            if (EpicSpawners.getInstance().editing.get(p) != 0)
                typee = getType(EpicSpawners.getInstance().editing.get(p));
            else
                typee = EpicSpawners.getInstance().newSpawnerName;
            String name = Methods.compileName(typee, 0, false);
            Inventory i = Bukkit.createInventory(null, 45, Arconix.pl().getApi().format().formatTitle(Arconix.pl().getApi().format().formatText(name + " &8Settings.")));
            int num = 0;
            while (num != 45) {
                i.setItem(num, Methods.getGlass());
                num++;
            }

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

            i.setItem(1, Methods.getBackgroundGlass(true));
            i.setItem(2, Methods.getBackgroundGlass(false));
            i.setItem(9, Methods.getBackgroundGlass(true));

            i.setItem(6, Methods.getBackgroundGlass(false));
            i.setItem(7, Methods.getBackgroundGlass(true));
            i.setItem(8, Methods.getBackgroundGlass(true));
            i.setItem(17, Methods.getBackgroundGlass(true));

            i.setItem(27, Methods.getBackgroundGlass(true));

            i.setItem(35, Methods.getBackgroundGlass(true));
            i.setItem(36, Methods.getBackgroundGlass(true));
            i.setItem(37, Methods.getBackgroundGlass(true));
            i.setItem(38, Methods.getBackgroundGlass(false));

            i.setItem(42, Methods.getBackgroundGlass(false));
            i.setItem(43, Methods.getBackgroundGlass(true));
            i.setItem(44, Methods.getBackgroundGlass(true));


            ItemStack item2 = new ItemStack(Material.DOUBLE_PLANT);
            ItemMeta item2meta = item2.getItemMeta();
            item2meta.setDisplayName(Arconix.pl().getApi().format().formatText("&6&lShop Price"));

            ArrayList<String> lore = new ArrayList<>();
            lore.add(Arconix.pl().getApi().format().formatText("&7Currently: &a" + EpicSpawners.getInstance().spawnerFile.getConfig().getDouble("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".Shop-Price")));

            lore.add(Arconix.pl().getApi().format().formatText("&7This is the price of the"));
            lore.add(Arconix.pl().getApi().format().formatText("&7spawner in the shop."));
            item2meta.setLore(lore);

            item2.setItemMeta(item2meta);
            i.setItem(19, item2);

            item2 = new ItemStack(Material.DIAMOND);
            item2meta = item2.getItemMeta();
            item2meta.setDisplayName(Arconix.pl().getApi().format().formatText("&6&lIn Shop"));
            lore = new ArrayList<>();
            lore.add(Arconix.pl().getApi().format().formatText("&7Currently: &a" + EpicSpawners.getInstance().spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".In-Shop")));

            lore.add(Arconix.pl().getApi().format().formatText("&7If this is true this spawner"));
            lore.add(Arconix.pl().getApi().format().formatText("&7will show up in the shop GUI."));
            item2meta.setLore(lore);
            item2.setItemMeta(item2meta);
            i.setItem(20, item2);

            item2 = new ItemStack(Material.FIREBALL);
            item2meta = item2.getItemMeta();
            item2meta.setDisplayName(Arconix.pl().getApi().format().formatText("&c&lSpawn On Fire"));
            lore = new ArrayList<>();
            lore.add(Arconix.pl().getApi().format().formatText("&7Currently: &a" + EpicSpawners.getInstance().spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".Spawn-On-Fire")));

            lore.add(Arconix.pl().getApi().format().formatText("&7If this is true this spawner"));
            lore.add(Arconix.pl().getApi().format().formatText("&7will spawn entities on fire."));
            item2meta.setLore(lore);
            item2.setItemMeta(item2meta);
            i.setItem(22, item2);

            item2 = new ItemStack(Material.HOPPER);
            item2meta = item2.getItemMeta();
            item2meta.setDisplayName(Arconix.pl().getApi().format().formatText("&5&lUpgradable"));
            lore = new ArrayList<>();
            lore.add(Arconix.pl().getApi().format().formatText("&7Currently: &a" + EpicSpawners.getInstance().spawnerFile.getConfig().getBoolean("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".Upgradable")));

            lore.add(Arconix.pl().getApi().format().formatText("&7Setting this to true will define"));
            lore.add(Arconix.pl().getApi().format().formatText("&7whether or not this spawner is"));
            lore.add(Arconix.pl().getApi().format().formatText("&7upgradable."));
            item2meta.setLore(lore);
            item2.setItemMeta(item2meta);
            i.setItem(13, item2);

            item2 = new ItemStack(Material.DOUBLE_PLANT);
            item2meta = item2.getItemMeta();
            item2meta.setDisplayName(Arconix.pl().getApi().format().formatText("&6&lCustom ECO cost"));
            lore = new ArrayList<>();
            lore.add(Arconix.pl().getApi().format().formatText("&7Currently: &a" + EpicSpawners.getInstance().spawnerFile.getConfig().getDouble("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".Custom-ECO-Cost")));

            lore.add(Arconix.pl().getApi().format().formatText("&7This is the custom Economy cost"));
            lore.add(Arconix.pl().getApi().format().formatText("&7to upgrade this spawner."));
            item2meta.setLore(lore);
            item2.setItemMeta(item2meta);
            i.setItem(24, item2);

            item2 = new ItemStack(Material.EXP_BOTTLE);
            item2meta = item2.getItemMeta();
            item2meta.setDisplayName(Arconix.pl().getApi().format().formatText("&5&lCustom XP cost"));
            lore = new ArrayList<>();
            lore.add(Arconix.pl().getApi().format().formatText("&7Currently: &a" + EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".Custom-XP-Cost")));

            lore.add(Arconix.pl().getApi().format().formatText("&7This is the custom XP cost"));
            lore.add(Arconix.pl().getApi().format().formatText("&7to upgrade this spawner."));
            item2meta.setLore(lore);
            item2.setItemMeta(item2meta);
            i.setItem(25, item2);

            item2 = new ItemStack(Material.DOUBLE_PLANT);
            item2meta = item2.getItemMeta();
            item2meta.setDisplayName(Arconix.pl().getApi().format().formatText("&5&lCustom Goal"));
            lore = new ArrayList<>();
            lore.add(Arconix.pl().getApi().format().formatText("&7Currently: &a" + EpicSpawners.getInstance().spawnerFile.getConfig().getInt("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".CustomGoal")));

            lore.add(Arconix.pl().getApi().format().formatText("&7If this is set to anything "));
            lore.add(Arconix.pl().getApi().format().formatText("&7but 0 the default kill goal "));
            lore.add(Arconix.pl().getApi().format().formatText("&7will be adjusted for this spawner."));
            item2meta.setLore(lore);
            item2.setItemMeta(item2meta);
            i.setItem(30, item2);

            item2 = new ItemStack(Material.DIAMOND);
            item2meta = item2.getItemMeta();
            item2meta.setDisplayName(Arconix.pl().getApi().format().formatText("&b&lPickup Cost"));
            lore = new ArrayList<>();
            lore.add(Arconix.pl().getApi().format().formatText("&7Currently: &a" + EpicSpawners.getInstance().spawnerFile.getConfig().getDouble("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(p))) + ".Pickup-cost")));

            lore.add(Arconix.pl().getApi().format().formatText("&7Setting this to anything but 0"));
            lore.add(Arconix.pl().getApi().format().formatText("&7will allow you to charge players"));
            lore.add(Arconix.pl().getApi().format().formatText("&7for breaking this spawner."));
            item2meta.setLore(lore);
            item2.setItemMeta(item2meta);
            i.setItem(32, item2);

            int ty = EpicSpawners.getInstance().editing.get(p);
            p.openInventory(i);
            EpicSpawners.getInstance().editing.put(p, ty);
        } catch (Exception e) {
            Debugger.runReport(e);
        }

    }

    public void save(Player p) {
        try {
            String type;
            if (EpicSpawners.getInstance().editing.get(p) != 0)
                type = getType(EpicSpawners.getInstance().editing.get(p));
            else
                type = EpicSpawners.getInstance().newSpawnerName;
            EpicSpawners.getInstance().subediting.put(p, type);

            String name = Methods.getTypeFromString(type);
            if (EpicSpawners.getInstance().isItemInstanceSaved) {
                EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + name + ".items", EpicSpawners.getInstance().itemEditorInstance);
            }
            if (EpicSpawners.getInstance().isEntityInstanceSaved) {
                EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + name + ".entities", EpicSpawners.getInstance().entityEditorInstance);
            }
            if (EpicSpawners.getInstance().isCommandInstanceSaved) {
                EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + name + ".commands", EpicSpawners.getInstance().commandEditorInstance);
            }
            if (EpicSpawners.getInstance().isBlockInstanceSaved) {
                EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + name + ".blocks", EpicSpawners.getInstance().blockEditorInstance);
            }
            EpicSpawners.getInstance().processDefault(name);
            EpicSpawners.getInstance().spawnerFile.saveConfig();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void alterSetting(Player p, String type) {
        try {
            int typ = EpicSpawners.getInstance().editing.get(p);
            String entity = getType(typ);
            p.sendMessage("");
            switch (type) {
                case "Shop-Price":
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Enter a sale price for &6" + Methods.getTypeFromString(entity) + "&7."));
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Example: &619.99&7."));
                    break;
                case "Custom-ECO-Cost":
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Enter a custom eco cost for " + Methods.getTypeFromString(entity) + "&7."));
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Use &60 &7to use the default cost."));
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Example: &619.99&7."));
                    break;
                case "Custom-XP-Cost":
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Enter a custom xp cost for " + Methods.getTypeFromString(entity) + "&7."));
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Use &60 &7to use the default cost."));
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Example: &625&7."));
                    break;
                case "Pickup-cost":
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Enter a pickup cost for " + Methods.getTypeFromString(entity) + "&7."));
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Use &60 &7to disable."));
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Example: &719.99&6."));
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Example: &625&7."));
                    break;
                case "CustomGoal":
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Enter a custom goal for " + Methods.getTypeFromString(entity) + "&7."));
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Use &60 &7to use the default price."));
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Example: &35&6."));
                    break;
            }
            p.sendMessage("");
            EpicSpawners.getInstance().chatEditing.put(p, type);
            p.closeInventory();
            EpicSpawners.getInstance().editing.put(p, typ);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public List<ItemStack> getItems(Player p) {
        try {
            ItemStack[] items2 = p.getOpenInventory().getTopInventory().getContents();
            //items2 = Arrays.copyOf(items2, items2.length - 9);

            List<ItemStack> items = new ArrayList<>();

            int num = 0;
            for (ItemStack item : items2) {
                if (num >= 10 && num <= 25 && num != 17 && num != 18 && item != null) {
                    items.add(items2[num]);
                }
                num++;
            }
            return items;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }

    public void saveInstance(Player p, List<ItemStack> items) {
        try {
            switch (EpicSpawners.getInstance().subediting.get(p)) {
                case "Item":
                    EpicSpawners.getInstance().itemEditorInstance = items;
                    EpicSpawners.getInstance().isItemInstanceSaved = true;
                    break;
                case "Block": {
                    List<String> list = new ArrayList<>();
                    for (ItemStack item : items) {
                        String name = item.getType().name();
                        list.add(name);
                    }

                    EpicSpawners.getInstance().blockEditorInstance = list;
                    EpicSpawners.getInstance().isBlockInstanceSaved = true;
                    break;
                }
                case "Entity": {
                    List<String> list = new ArrayList<>();
                    for (ItemStack item : items) {
                        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).toUpperCase().replace(" ", "_");
                        list.add(name);
                    }
                    EpicSpawners.getInstance().entityEditorInstance = list;
                    EpicSpawners.getInstance().isEntityInstanceSaved = true;
                    break;
                }
                case "Command": {
                    List<String> list = new ArrayList<>();
                    for (ItemStack item : items) {
                        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).substring(1);
                        list.add(name);
                    }
                    EpicSpawners.getInstance().commandEditorInstance = list;
                    EpicSpawners.getInstance().isCommandInstanceSaved = true;
                    break;
                }
            }
            p.sendMessage(Arconix.pl().getApi().format().formatText(EpicSpawners.getInstance().references.getPrefix() + "&7Instance Saved."));
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }


    public void addEntityInit(Player p) {
        try {
            int type = EpicSpawners.getInstance().editing.get(p);
            p.sendMessage("");
            StringBuilder list = new StringBuilder();
            for (final EntityType value : EntityType.values()) {
                if (value.isSpawnable() && value.isAlive()) {
                    list.append(value.toString()).append("&7, &6");
                }
            }
            p.sendMessage(Arconix.pl().getApi().format().formatText("&6" + list));
            p.sendMessage("Enter an entity type.");
            p.sendMessage("");
            EpicSpawners.getInstance().chatEditing.put(p, "addEntity");
            p.closeInventory();
            EpicSpawners.getInstance().editing.put(p, type);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void destroy(Player p) {
        try {
            int type = EpicSpawners.getInstance().editing.get(p);

            p.sendMessage("");
            p.sendMessage(Arconix.pl().getApi().format().formatText("&cAre you sure you want to destroy &6" + getType(type) + "&7."));
            p.sendMessage(Arconix.pl().getApi().format().formatText("&7Type &l&6CONFIRM &7to continue. Otherwise type anything else to cancel."));
            p.sendMessage("");
            EpicSpawners.getInstance().chatEditing.put(p, "destroy");
            p.closeInventory();
            EpicSpawners.getInstance().editing.put(p, type);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void destroyFinal(Player p, String msg) {
        try {
            int type = EpicSpawners.getInstance().editing.get(p);

            EpicSpawners.getInstance().chatEditing.remove(p);
            EpicSpawners.getInstance().editing.put(p, type);
            if (msg.toLowerCase().equals("confirm")) {
                p.sendMessage(Arconix.pl().getApi().format().formatText("&6" + getType(type) + " Spawner &7 has been destroyed successfully"));
                EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(getType(type)), null);
                open(p, 1);
            } else {
                p.sendMessage(Arconix.pl().getApi().format().formatText("&7Action canceled..."));
                overview(p, EpicSpawners.getInstance().editing.get(p));
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void editChatInit(Player p) {
        try {
            int type = EpicSpawners.getInstance().editing.get(p);
            String entity = getType(type);

            p.sendMessage("");
            switch (EpicSpawners.getInstance().subediting.get(p)) {
                case "Item":
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Enter a tick rate for &6" + Methods.getTypeFromString(entity) + "&7."));
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7This is the amount of ticks that will pass in between every item spawn."));
                    break;
                case "Block":
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Enter a tick rate for &6" + Methods.getTypeFromString(entity) + "&7."));
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7This is the amount of ticks that will pass in between every block spawn."));
                    break;
                case "Command":
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7Enter a tick rate for &6" + Methods.getTypeFromString(entity) + "&7."));
                    p.sendMessage(Arconix.pl().getApi().format().formatText("&7This is the amount of ticks that will pass in between every command."));
                    break;
            }
            p.sendMessage("");
            EpicSpawners.getInstance().chatEditing.put(p, "tick");
            p.closeInventory();
            EpicSpawners.getInstance().editing.put(p, type);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void editSpawnLimit(Player p) {
        try {
            if (EpicSpawners.getInstance().subediting.get(p).equals("Command")) {
                int type = EpicSpawners.getInstance().editing.get(p);
                String entity = getType(type);

                p.sendMessage("");
                p.sendMessage(Arconix.pl().getApi().format().formatText("&7Enter a spawn limit for &6" + Methods.getTypeFromString(entity) + "&7."));
                p.sendMessage("");
                EpicSpawners.getInstance().chatEditing.put(p, "spawnLimit");
                p.closeInventory();
                EpicSpawners.getInstance().editing.put(p, type);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void createCommand(Player p) {
        try {
            int type = EpicSpawners.getInstance().editing.get(p);
            p.sendMessage("");
            p.sendMessage(Arconix.pl().getApi().format().formatText("&7Please type a command. Example: &6eco give @p 1000&7."));
            p.sendMessage(Arconix.pl().getApi().format().formatText("&7You can use @X @Y and @Z for random X Y and Z coordinates around the spawner."));
            p.sendMessage(Arconix.pl().getApi().format().formatText("&7If you're getting command output try &6/gamerule sendCommandFeedback false&7."));
            p.sendMessage(Arconix.pl().getApi().format().formatText("&7do not include a &a/"));
            p.sendMessage("");

            EpicSpawners.getInstance().chatEditing.put(p, "Command");
            p.closeInventory();
            EpicSpawners.getInstance().editing.put(p, type);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void addCommand(Player p, String cmd) {
        try {
            if (!EpicSpawners.getInstance().commandEditorInstance.contains(cmd)) {
                EpicSpawners.getInstance().commandEditorInstance.add(cmd);
            }
            EpicSpawners.getInstance().isCommandInstanceSaved = true;
            editor(p, "Command");
            EpicSpawners.getInstance().chatEditing.remove(p);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void editSpawnerName(Player p) {
        try {
            int type = EpicSpawners.getInstance().editing.get(p);
            String entity = getType(type);

            p.sendMessage("");
            p.sendMessage(Arconix.pl().getApi().format().formatText("&7Enter a display name for &6" + Methods.getTypeFromString(entity) + "&7."));
            p.sendMessage("");
            EpicSpawners.getInstance().chatEditing.put(p, "name");
            p.closeInventory();
            EpicSpawners.getInstance().editing.put(p, type);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void saveSpawnerName(Player p, String name) {
        try {
            String entity = getType(EpicSpawners.getInstance().editing.get(p));
            EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(entity) + ".Display-Name", name);
            overview(p, EpicSpawners.getInstance().editing.get(p));
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void addEntity(Player p, String ent) {
        try {
            if (!EpicSpawners.getInstance().entityEditorInstance.contains(ent)) {
                EpicSpawners.getInstance().entityEditorInstance.add(ent);
            }
            EpicSpawners.getInstance().isEntityInstanceSaved = true;
            editor(p, "Entity");
            EpicSpawners.getInstance().chatEditing.remove(p);

        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void saveChatEdit(Player p, int amt) {
        try {
            String entity = getType(EpicSpawners.getInstance().editing.get(p));
            switch (EpicSpawners.getInstance().subediting.get(p)) {
                case "Item":
                    EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(entity) + ".itemTickRate", amt);
                    break;
                case "Command":
                    EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(entity) + ".commandTickRate", amt);
                    break;
                case "Block":
                    EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(entity) + ".blockTickRate", amt);
                    break;
            }
            switch (EpicSpawners.getInstance().subediting.get(p)) {
                case "Item":
                    editor(p, "Item");
                    break;
                case "Command":
                    editor(p, "Command");
                    break;
                case "Block":
                    editor(p, "Block");
                    break;
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }
}

<<<<<<< HEAD
package com.songoda.epicspawners.spawners.editor;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.listeners.ChatListeners;
import com.songoda.epicspawners.player.PlayerData;
import com.songoda.epicspawners.spawners.spawner.ESpawnerData;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SpawnerEditor {

    private final EpicSpawnersPlugin instance;

    private final Map<UUID, EditingData> userEditingData = new HashMap<>();

    public SpawnerEditor(EpicSpawnersPlugin instance) {
        this.instance = instance;
    }

    public void openSpawnerSelector(Player player, int page) {
        try {
            this.userEditingData.remove(player.getUniqueId());
            EditingData editingData = new EditingData();
            editingData.setMenu(EditingMenu.SPAWNER_SELECTOR);
            editingData.setNewSpawnerName(null);

            PlayerData playerData = instance.getPlayerActionManager().getPlayerAction(player);

            playerData.setCurrentPage(page);

            List<SpawnerData> entities = new ArrayList<>();

            int num = 0, show = 0;
            int start = (page - 1) * 32;

            for (SpawnerData spawnerData : instance.getSpawnerManager().getAllSpawnerData()) {
                if (num >= start && !spawnerData.getIdentifyingName().equalsIgnoreCase("omni")) {
                    if (show <= 32) {
                        entities.add(spawnerData);
                        show++;
                    }
                }
                num++;
            }

            int max = (int) Math.ceil((double) num / (double) 32);
            int amt = entities.size();
            if (amt == 24 || amt == 25) amt = 26;
            Inventory inventory = Bukkit.createInventory(null, 54, TextComponent.formatTitle("Spawner Editor"));
            int max2 = 54;
            if (amt <= 7) {
                inventory = Bukkit.createInventory(null, 27, TextComponent.formatTitle("Spawner Editor"));
                max2 = 27;
            } else if (amt <= 15) {
                inventory = Bukkit.createInventory(null, 36, TextComponent.formatTitle("Spawner Editor"));
                max2 = 36;
            } else if (amt <= 25) {
                inventory = Bukkit.createInventory(null, 45, TextComponent.formatTitle("Spawner Editor"));
                max2 = 45;
            }

            inventory.setItem(8, Methods.createButton(Material.OAK_DOOR, instance.getLocale().getMessage("general.nametag.exit")));

            ItemStack next = Methods.createButton(Arconix.pl().getApi().getGUI().addTexture(new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3), "http://textures.minecraft.net/texture/1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b"), instance.getLocale().getMessage("general.nametag.next"));

            ItemStack back = Methods.createButton(Arconix.pl().getApi().getGUI().addTexture(new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3), "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"), instance.getLocale().getMessage("general.nametag.back"));


            int max22 = max2;
            int place = 10;

            for (SpawnerData spawnerData : entities) {
                if (place == 17 || place == (max22 - 18)) place++;

                ItemStack icon = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
                ItemStack item = instance.getHeads().addTexture(icon, spawnerData);
                if (spawnerData.getDisplayItem() != null) {
                    item.setType(spawnerData.getDisplayItem());
                }

                String name = Methods.compileName(spawnerData, 1, false);
                inventory.setItem(place, Methods.createButton(icon, TextComponent.convertToInvisibleString(spawnerData.getDisplayName() + ":") + name,
                        "&7Click to &a&lEdit&7."));

                place++;
            }

            ItemStack glass = Methods.getGlass();
            for (int i = 0; i < 8; i++) {
                inventory.setItem(i, glass);
            }

            for (int i = max22 - 9; i < max22; i++) {
                inventory.setItem(i, glass);
            }

            ItemStack glassType2 = Methods.getBackgroundGlass(true), glassType3 = Methods.getBackgroundGlass(false);
            inventory.setItem(0, glassType2);
            inventory.setItem(1, glassType2);
            inventory.setItem(9, glassType2);

            inventory.setItem(7, glassType2);
            inventory.setItem(17, glassType2);

            inventory.setItem(max22 - 18, glassType2);
            inventory.setItem(max22 - 9, glassType2);
            inventory.setItem(max22 - 8, glassType2);

            inventory.setItem(max22 - 10, glassType2);
            inventory.setItem(max22 - 2, glassType2);
            inventory.setItem(max22 - 1, glassType2);

            inventory.setItem(2, glassType3);
            inventory.setItem(6, glassType3);
            inventory.setItem(max22 - 7, glassType3);
            inventory.setItem(max22 - 3, glassType3);

            if (page != 1) {
                inventory.setItem(max22 - 8, back);
            }

            if (page != max) {
                inventory.setItem(max22 - 2, next);
            }


            inventory.setItem(max22 - 4, Methods.createButton(Material.PAPER, "&9&lNew Spawner"));

            player.openInventory(inventory);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void overview(Player player, SpawnerData spawnerData) {
        try {
            EditingData editingData = getEditingData(player);

            int csp = 1;
            for (SpawnerData spawnerData2 : instance.getSpawnerManager().getAllSpawnerData()) {
                if (spawnerData2.getIdentifyingName().toLowerCase().contains("custom"))
                    csp++;
            }
            String type = "Custom " + (editingData.getNewSpawnerName() != null ? editingData.getNewSpawnerName() : csp);

            if (spawnerData != null)
                type = spawnerData.getIdentifyingName();
            else
                editingData.setNewSpawnerName(type);

            String name;

            if (!instance.getSpawnerManager().isSpawnerData(type.toLowerCase())) {
                spawnerData = new ESpawnerData(0, type, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                ((ESpawnerData) spawnerData).addDefaultConditions();
                instance.getSpawnerManager().addSpawnerData(type, spawnerData);
            }
            spawnerData = instance.getSpawnerManager().getSpawnerData(type);

            name = Methods.compileName(spawnerData, 1, false);
            Inventory i = Bukkit.createInventory(null, 54, TextComponent.formatTitle(TextComponent.formatText("&8Editing: " + name + "&8.")));

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

            i.setItem(8, Methods.createButton(Arconix.pl().getApi().getGUI().addTexture(new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3), "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"), instance.getLocale().getMessage("general.nametag.back")));

            ItemStack it = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);

            ItemStack item = instance.getHeads().addTexture(it, spawnerData);
            if (spawnerData.getDisplayItem() != null) {
                item.setType(spawnerData.getDisplayItem());
            }

            ItemMeta itemmeta = item.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            lore.add(TextComponent.formatText("&7Left-Click to &9Change Spawner Name&7."));
            lore.add(TextComponent.formatText("&7Middle-Click to &bChange Spawner Display Item&7."));
            if (instance.getConfig().getBoolean("settings.beta-features"))
                lore.add(TextComponent.formatText("&7Right-Click to &9Change Spawner Head&7."));
            lore.add(TextComponent.formatText("&6-----------------------------"));

            lore.add(TextComponent.formatText("&6Display Name: &7" + spawnerData.getDisplayName() + "&7."));
            if (spawnerData.getDisplayItem() != null) {
                lore.add(TextComponent.formatText("&6Display Item: &7" + spawnerData.getDisplayItem().name() + "&7."));
            } else {
                if (!name.contains("Custom")) {
                    lore.add(TextComponent.formatText("&6Display Item: &7Unavailable&7."));
                } else {
                    lore.add(TextComponent.formatText("&6Display Item: &7Dirt&7."));
                }
            }
            lore.add(TextComponent.formatText("&6Config Name: &7" + type + "&7."));
            itemmeta.setLore(lore);
            itemmeta.setDisplayName(name);
            item.setItemMeta(itemmeta);
            i.setItem(11, item);

            boolean dont = false;
            for (final EntityType val : EntityType.values()) {
                if (val.isSpawnable() && val.isAlive()) {
                    if (val.name().equals(Methods.restoreType(type))) {
                        dont = true;
                    }
                }
            }

            lore = new ArrayList<>();
            if (!dont) lore.add(TextComponent.formatText("&7Right-Click to: &cDestroy Spawner"));
            lore.add(TextComponent.formatText("&6---------------------------"));
            lore.add(TextComponent.formatText(spawnerData.isActive() ? "&6Currently:&a Enabled." : "&6Currently:&c Disabled."));

            i.setItem(29, Methods.createButton(Material.TNT, "&7Left-Click to: &cDisable Spawner", lore));

            i.setItem(23, Methods.createButton(Material.LEVER, "&9&lGeneral Settings"));
            i.setItem(24, Methods.createButton(Material.BONE, "&e&lDrop Settings"));

            i.setItem(25, Methods.createButton(instance.getHeads().addTexture(new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3), instance.getSpawnerManager().getSpawnerData("omni")), "&a&lEntity Settings"));

            i.setItem(41, Methods.createButton(Material.CHEST, "&5&lItem Settings"));
            i.setItem(32, Methods.createButton(Material.GOLD_BLOCK, "&c&lBlock Settings"));
            i.setItem(34, Methods.createButton(Material.FIREWORK_ROCKET, "&b&lParticle Settings"));
            i.setItem(43, Methods.createButton(Material.PAPER, "&6&lCommand Settings"));

            player.openInventory(i);
            editingData.setMenu(EditingMenu.OVERVIEW);
            editingData.setSpawnerEditing(spawnerData);
            editingData.setNewId(-1);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public SpawnerData getType(String name) {
        SpawnerData type = instance.getSpawnerManager().getSpawnerData("pig");
        try {
            name = name.replace(String.valueOf(ChatColor.COLOR_CHAR), "").split(":")[0];
            return instance.getSpawnerManager().getSpawnerData(name);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return type;
    }

    public void particleEditor(Player player) {
        EditingData editingData = userEditingData.get(player.getUniqueId());
        SpawnerData spawnerData = editingData.getSpawnerEditing();

        String name = Methods.compileName(spawnerData, 1, false);
        Inventory i = Bukkit.createInventory(null, 45, TextComponent.formatTitle(TextComponent.formatText(name + " &8Particle &8Settings.")));

        int num = 0;
        while (num != 45) {
            i.setItem(num, Methods.getGlass());
            num++;
        }

        i.setItem(0, Methods.createButton(Arconix.pl().getApi().getGUI().addTexture(new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3), "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"), instance.getLocale().getMessage("general.nametag.back")));

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

        i.setItem(20, Methods.createButton(Material.ENDER_PEARL, "&5&lParticle Types",
                "&7Entity Spawn Particle: &a" + spawnerData.getEntitySpawnParticle().name(),
                "&cLeft-Click to change.",
                "&7Spawner Spawn Particle: &a" + spawnerData.getSpawnerSpawnParticle().name(),
                "&cMiddle-Click to change.",
                "&7Effect Particle: &a" + spawnerData.getSpawnEffectParticle().name(),
                "&cRight-Click to change."));

        i.setItem(22, Methods.createButton(Material.FIREWORK_ROCKET, "&6&lSpawner Effect",
                "&7Particle Effect: &a" + spawnerData.getParticleEffect().name(),
                "&cLeft-Click to change.",
                "&7Particle Effect For Boosted Only: &a" + spawnerData.isParticleEffectBoostedOnly(),
                "&cRight-Click to change."));

        i.setItem(24, Methods.createButton(Material.COMPARATOR, "&6&lPerformance",
                "&7Currently: &a" + spawnerData.getParticleDensity().name() + " &cClick to change."));

        player.openInventory(i);
        editingData.setMenu(EditingMenu.PARTICLE);
    }

    public void editor(Player player, EditingMenu editingMenu) {
        try {
            EditingData editingData = userEditingData.get(player.getUniqueId());
            SpawnerData spawnerData = editingData.getSpawnerEditing();

            String name = Methods.compileName(spawnerData, 1, false);
            Inventory i = Bukkit.createInventory(null, 54, TextComponent.formatTitle(TextComponent.formatText(name + "&8 " + editingMenu + " &8Settings.")));

            int num = 0;
            while (num != 54) {
                i.setItem(num, Methods.getGlass());
                num++;
            }

            num = 10;
            int spot = 0;
            while (num != 26) {
                if (num == 17)
                    num = num + 2;

                if (spawnerData.getEntityDroppedItems().size() >= spot + 1 && editingMenu == EditingMenu.DROPS) {
                    i.setItem(num, spawnerData.getEntityDroppedItems().get(spot));
                } else if (spawnerData.getItems().size() >= spot + 1 && editingMenu == EditingMenu.ITEM) {
                    i.setItem(num, spawnerData.getItems().get(spot));
                } else if (spawnerData.getBlocks().size() >= spot + 1 && editingMenu == EditingMenu.BLOCK) {
                    i.setItem(num, new ItemStack(spawnerData.getBlocks().get(spot)));
                } else if (spawnerData.getEntities().size() >= spot + 1 && editingMenu == EditingMenu.ENTITY && spawnerData.getEntities().get(spot) != EntityType.GIANT) {
                    ItemStack it = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
                    ItemStack item = instance.getHeads().addTexture(it,
                            instance.getSpawnerManager().getSpawnerData(spawnerData.getEntities().get(spot)));
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(TextComponent.formatText("&e" + Methods.getTypeFromString(spawnerData.getEntities().get(spot).name())));
                    item.setItemMeta(meta);
                    i.setItem(num, item);

                } else if (spawnerData.getCommands().size() >= spot + 1 && editingMenu == EditingMenu.COMMAND) {
                    ItemStack parseStack = new ItemStack(Material.PAPER, 1);
                    ItemMeta meta = parseStack.getItemMeta();
                    meta.setDisplayName(TextComponent.formatText("&a/" + spawnerData.getCommands().get(spot)));
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

            i.setItem(0, Methods.createButton(Arconix.pl().getApi().getGUI().addTexture(new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3), "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"), instance.getLocale().getMessage("general.nametag.back")));

            if (editingMenu == EditingMenu.COMMAND) {
                i.setItem(49, Methods.createButton(Material.CLOCK, "&bSpawn Limit",
                        "",
                        "&7This is the spawn limit for entities you spawn",
                        "&7from this spawner. Set to &60 &7to disable this."));
                }

            if (editingMenu != EditingMenu.ITEM && editingMenu != EditingMenu.BLOCK && editingMenu != EditingMenu.DROPS) {
                ItemStack add;
                String addName;
                if (editingMenu == EditingMenu.COMMAND) {
                    add = new ItemStack(Material.PAPER);
                    addName = "&6Add Command";
                } else {
                    add = new ItemStack(Material.SHEEP_SPAWN_EGG);
                    addName = "&6Add entity";
                }

                i.setItem(39, Methods.createButton(add, addName));
            }

            i.setItem(editingMenu != EditingMenu.ITEM ? 41 : 49, Methods.createButton(Material.REDSTONE, "&aSave"));

            player.openInventory(i);
            editingData.setMenu(editingMenu);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void basicSettings(Player player) {
        try {
            EditingData editingData = userEditingData.get(player.getUniqueId());
            SpawnerData spawnerData = editingData.getSpawnerEditing();
            String name = Methods.compileName(spawnerData, 1, false);
            Inventory i = Bukkit.createInventory(null, 45, TextComponent.formatTitle(TextComponent.formatText(name + " &8Settings.")));
            int num = 0;
            while (num != 45) {
                i.setItem(num, Methods.getGlass());
                num++;
            }

            i.setItem(0, Methods.createButton(Arconix.pl().getApi().getGUI().addTexture(new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3), "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"), instance.getLocale().getMessage("general.nametag.back")));

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

            i.setItem(19, Methods.createButton(Material.SUNFLOWER, "&6&lShop Price",
                    "&7Currently: &a" + spawnerData.getShopPrice(),
                    "&7This is the price of the",
                    "&7spawner in the shop."));

            i.setItem(20, Methods.createButton(Material.DIAMOND, "&6&lIn Shop",
                    "&7Currently: &a" + spawnerData.isInShop(),
                    "&7If this is true this spawner",
                    "&7will show up in the shop GUI."));

            i.setItem(22, Methods.createButton(Material.FIRE_CHARGE, "&c&lSpawn On Fire",
                    "&7Currently: &a" + spawnerData.isSpawnOnFire(),
                    "&7If this is true this spawner",
                    "&7will spawn entities on fire."));

            i.setItem(13, Methods.createButton(Material.HOPPER, "&5&lUpgradable",
                    "&7Currently: &a" + spawnerData.isUpgradeable(),
                    "&7Setting this to true will define",
                    "&7upgradable."));

            i.setItem(24, Methods.createButton(Material.SUNFLOWER, "&6&lCustom Economy cost",
                    "&7Currently: &a" + spawnerData.getUpgradeCostEconomy(),
                    "&7This is the custom economy cost",
                    "&7to upgrade this spawner."));

            i.setItem(25, Methods.createButton(Material.EXPERIENCE_BOTTLE, "&5&lCustom Experience cost",
                    "&7Currently: &a" + spawnerData.getUpgradeCostExperience(),
                    "&7This is the custom XP cost",
                    "&7to upgrade this spawner."));

            i.setItem(30, Methods.createButton(Material.EXPERIENCE_BOTTLE, "&5&lCustom Goal",
                    "&7Currently: &a" + spawnerData.getKillGoal(),
                    "&7If this is set to anything",
                    "&7but 0 the default kill goal",
                    "&7will be adjusted for this spawner."));

            i.setItem(32, Methods.createButton(Material.DIAMOND, "&b&lPickup Cost",
                    "&7Currently: &a" + spawnerData.getPickupCost(),
                    "&7Setting this to anything but 0",
                    "&7will allow you to charge players",
                    "&7for breaking this type of spawner."));

            i.setItem(40, Methods.createButton(Material.CLOCK, "&6&lTick Rate",
                    "&7Currently: &a" + spawnerData.getTickRate(),
                    "&7This is the default tick rate",
                    "&7that your spawner will use",
                    "&7to create its delay with."));

            player.openInventory(i);
            editingData.setMenu(EditingMenu.GENERAL);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void alterSetting(Player player, ChatListeners.EditingType type) {
        try {
            EditingData editingData = userEditingData.get(player.getUniqueId());
            SpawnerData entity = editingData.getSpawnerEditing();
            player.sendMessage("");
            switch (type) {
                case SHOP_PRICE:
                    player.sendMessage(TextComponent.formatText("&7Enter a sale price for &6" + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    player.sendMessage(TextComponent.formatText("&7Example: &619.99&7."));
                    break;
                case CUSTOM_ECO_COST:
                    player.sendMessage(TextComponent.formatText("&7Enter a custom eco cost for " + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    player.sendMessage(TextComponent.formatText("&7Use &60 &7to use the default cost."));
                    player.sendMessage(TextComponent.formatText("&7Example: &619.99&7."));
                    break;
                case CUSTOM_XP_COST:
                    player.sendMessage(TextComponent.formatText("&7Enter a custom xp cost for " + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    player.sendMessage(TextComponent.formatText("&7Use &60 &7to use the default cost."));
                    player.sendMessage(TextComponent.formatText("&7Example: &625&7."));
                    break;
                case PICKUP_COST:
                    player.sendMessage(TextComponent.formatText("&7Enter a pickup cost for " + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    player.sendMessage(TextComponent.formatText("&7Use &60 &7to disable."));
                    player.sendMessage(TextComponent.formatText("&7Example: &719.99&6."));
                    player.sendMessage(TextComponent.formatText("&7Example: &625&7."));
                    break;
                case CUSTOM_GOAL:
                    player.sendMessage(TextComponent.formatText("&7Enter a custom goal for " + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    player.sendMessage(TextComponent.formatText("&7Use &60 &7to use the default price."));
                    player.sendMessage(TextComponent.formatText("&7Example: &35&6."));
                    break;
                case TICK_RATE:
                    player.sendMessage(TextComponent.formatText("&7Enter a tick rate min and max for " + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    player.sendMessage(TextComponent.formatText("&7Example: &3800:200&6."));
                    break;
            }
            player.sendMessage("");
            instance.getChatListeners().addToEditor(player, type);
            player.closeInventory();
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

    public void save(Player p, List<ItemStack> items) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            SpawnerData spawnerData = editingData.getSpawnerEditing();
            if (editingData.getMenu() == EditingMenu.ITEM) {
                spawnerData.setItems(items);
            } else if (editingData.getMenu() == EditingMenu.DROPS) {
                spawnerData.setEntityDroppedItems(items);
            } else if (editingData.getMenu() == EditingMenu.BLOCK) {
                List<Material> list = new ArrayList<>();
                for (ItemStack item : items) {
                    Material material = item.getType();
                    list.add(material);
                }

                spawnerData.setBlocks(list);
            } else if (editingData.getMenu() == EditingMenu.ENTITY) {
                List<EntityType> list = new ArrayList<>();
                for (ItemStack item : items) {
                    EntityType entityType = EntityType.valueOf(ChatColor.stripColor(item.getItemMeta().getDisplayName()).toUpperCase().replace(" ", "_"));
                    list.add(entityType);
                }
                spawnerData.setEntities(list);
            } else if (editingData.getMenu() == EditingMenu.COMMAND) {
                List<String> list = new ArrayList<>();
                for (ItemStack item : items) {
                    String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).substring(1);
                    list.add(name);
                }
                spawnerData.setCommands(list);
            }
            p.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&7Spawner Saved."));
            spawnerData.reloadSpawnMethods();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }


    public void addEntityInit(Player p) {
        try {
            p.sendMessage("");
            StringBuilder list = new StringBuilder();
            for (final EntityType value : EntityType.values()) {
                if (value.isSpawnable() && value.isAlive()) {
                    list.append(value.toString()).append("&7, &6");
                }
            }
            p.sendMessage(TextComponent.formatText("&6" + list));
            p.sendMessage("Enter an entity Type.");
            p.sendMessage("");
            instance.getChatListeners().addToEditor(p, ChatListeners.EditingType.ADD_ENTITY);
            p.closeInventory();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void destroy(Player p) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            p.sendMessage("");
            p.sendMessage(TextComponent.formatText("&cAre you sure you want to destroy &6" + editingData.getSpawnerEditing().getIdentifyingName() + "&7."));
            p.sendMessage(TextComponent.formatText("&7Type &l&6CONFIRM &7to continue. Otherwise Type anything else to cancel."));
            p.sendMessage("");
            instance.getChatListeners().addToEditor(p, ChatListeners.EditingType.DESTROY);
            p.closeInventory();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void destroyFinal(Player p, String msg) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());

            if (msg.toLowerCase().equals("confirm")) {
                p.sendMessage(TextComponent.formatText("&6" + editingData.getSpawnerEditing().getIdentifyingName() + " Spawner &7 has been destroyed successfully"));
                instance.getSpawnerManager().removeSpawnerData(editingData.getSpawnerEditing().getIdentifyingName());
                openSpawnerSelector(p, 1);
            } else {
                p.sendMessage(TextComponent.formatText("&7Action canceled..."));
                overview(p, editingData.getSpawnerEditing());
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void editSpawnLimit(Player p) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            if (editingData.getMenu() == EditingMenu.COMMAND) {
                SpawnerData spawnerData = editingData.getSpawnerEditing();

                p.sendMessage("");
                p.sendMessage(TextComponent.formatText("&7Enter a spawn limit for &6" + Methods.getTypeFromString(spawnerData.getIdentifyingName()) + "&7."));
                p.sendMessage("");
                instance.getChatListeners().addToEditor(p, ChatListeners.EditingType.SPAWN_LIMIT);
                p.closeInventory();
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void createCommand(Player p) {
        try {
            p.sendMessage("");
            p.sendMessage(TextComponent.formatText("&7Please Type a command. Example: &6eco give @p 1000&7."));
            p.sendMessage(TextComponent.formatText("&7You can use @X @Y and @Z for random X Y and Z coordinates around the spawner."));
            p.sendMessage(TextComponent.formatText("&7@n will execute the command for the person who originally placed the spawner."));
            p.sendMessage(TextComponent.formatText("&7If you're getting command output try &6/gamerule sendCommandFeedback false&7."));
            p.sendMessage(TextComponent.formatText("&7do not include a &a/"));
            p.sendMessage("");

            instance.getChatListeners().addToEditor(p, ChatListeners.EditingType.COMMAND);
            p.closeInventory();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void addCommand(Player p, String cmd) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            SpawnerData spawnerData = editingData.getSpawnerEditing();
            List<String> commands = new ArrayList<>(spawnerData.getCommands());
            commands.add(cmd);
            spawnerData.setCommands(commands);

            editor(p, EditingMenu.COMMAND);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void editSpawnerName(Player p) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            SpawnerData spawnerData = editingData.getSpawnerEditing();

            p.sendMessage("");
            p.sendMessage(TextComponent.formatText("&7Enter a display name for &6" + Methods.getTypeFromString(spawnerData.getIdentifyingName()) + "&7."));
            p.sendMessage("");
            instance.getChatListeners().addToEditor(p, ChatListeners.EditingType.NAME);
            p.closeInventory();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void saveSpawnerName(Player p, String name) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            SpawnerData spawnerData = editingData.getSpawnerEditing();
            spawnerData.setDisplayName(name);
            overview(p, spawnerData);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void addEntity(Player p, String ent) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            SpawnerData spawnerData = editingData.getSpawnerEditing();
            List<EntityType> entities = new ArrayList<>(spawnerData.getEntities());
            entities.add(EntityType.valueOf(ent));
            spawnerData.setEntities(entities);
            editor(p, EditingMenu.ENTITY);

        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public EditingData getEditingData(Player player) {
        return userEditingData.computeIfAbsent(player.getUniqueId(), uuid -> new EditingData());
    }
}
=======
package com.songoda.epicspawners.spawners.editor;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.listeners.ChatListeners;
import com.songoda.epicspawners.player.PlayerData;
import com.songoda.epicspawners.spawners.spawner.ESpawnerData;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SpawnerEditor {

    private final EpicSpawnersPlugin instance;

    private final Map<UUID, EditingData> userEditingData = new HashMap<>();

    public SpawnerEditor(EpicSpawnersPlugin instance) {
        this.instance = instance;
    }

    public void openSpawnerSelector(Player player, int page) {
        try {
            this.userEditingData.remove(player.getUniqueId());
            EditingData editingData = new EditingData();
            editingData.setMenu(EditingMenu.SPAWNER_SELECTOR);
            editingData.setNewSpawnerName(null);

            PlayerData playerData = instance.getPlayerActionManager().getPlayerAction(player);

            playerData.setCurrentPage(page);

            List<SpawnerData> entities = new ArrayList<>();

            int num = 0, show = 0;
            int start = (page - 1) * 32;

            for (SpawnerData spawnerData : instance.getSpawnerManager().getAllSpawnerData()) {
                if (num >= start && !spawnerData.getIdentifyingName().equalsIgnoreCase("omni")) {
                    if (show <= 32) {
                        entities.add(spawnerData);
                        show++;
                    }
                }
                num++;
            }

            int max = (int) Math.ceil((double) num / (double) 32);
            int amt = entities.size();
            if (amt == 24 || amt == 25) amt = 26;
            Inventory inventory = Bukkit.createInventory(null, 54, TextComponent.formatTitle("Spawner Editor"));
            int max2 = 54;
            if (amt <= 7) {
                inventory = Bukkit.createInventory(null, 27, TextComponent.formatTitle("Spawner Editor"));
                max2 = 27;
            } else if (amt <= 15) {
                inventory = Bukkit.createInventory(null, 36, TextComponent.formatTitle("Spawner Editor"));
                max2 = 36;
            } else if (amt <= 25) {
                inventory = Bukkit.createInventory(null, 45, TextComponent.formatTitle("Spawner Editor"));
                max2 = 45;
            }

            inventory.setItem(8, Methods.createButton(Material.WOOD_DOOR, instance.getLocale().getMessage("general.nametag.exit")));

            ItemStack next = Methods.createButton(Arconix.pl().getApi().getGUI().addTexture(new ItemStack(Material.SKULL_ITEM, 1, (byte) 3), "http://textures.minecraft.net/texture/1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b"), instance.getLocale().getMessage("general.nametag.next"));

            ItemStack back = Methods.createButton(Arconix.pl().getApi().getGUI().addTexture(new ItemStack(Material.SKULL_ITEM, 1, (byte) 3), "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"), instance.getLocale().getMessage("general.nametag.back"));


            int max22 = max2;
            int place = 10;

            for (SpawnerData spawnerData : entities) {
                if (place == 17 || place == (max22 - 18)) place++;

                ItemStack icon = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
                ItemStack item = instance.getHeads().addTexture(icon, spawnerData);
                if (spawnerData.getDisplayItem() != null && spawnerData.getDisplayItem() != Material.AIR) {
                    item.setType(spawnerData.getDisplayItem());
                }

                String name = Methods.compileName(spawnerData, 1, false);
                inventory.setItem(place, Methods.createButton(icon, TextComponent.convertToInvisibleString(spawnerData.getDisplayName() + ":") + name,
                        "&7Click to &a&lEdit&7."));

                place++;
            }

            ItemStack glass = Methods.getGlass();
            for (int i = 0; i < 8; i++) {
                inventory.setItem(i, glass);
            }

            for (int i = max22 - 9; i < max22; i++) {
                inventory.setItem(i, glass);
            }

            ItemStack glassType2 = Methods.getBackgroundGlass(true), glassType3 = Methods.getBackgroundGlass(false);
            inventory.setItem(0, glassType2);
            inventory.setItem(1, glassType2);
            inventory.setItem(9, glassType2);

            inventory.setItem(7, glassType2);
            inventory.setItem(17, glassType2);

            inventory.setItem(max22 - 18, glassType2);
            inventory.setItem(max22 - 9, glassType2);
            inventory.setItem(max22 - 8, glassType2);

            inventory.setItem(max22 - 10, glassType2);
            inventory.setItem(max22 - 2, glassType2);
            inventory.setItem(max22 - 1, glassType2);

            inventory.setItem(2, glassType3);
            inventory.setItem(6, glassType3);
            inventory.setItem(max22 - 7, glassType3);
            inventory.setItem(max22 - 3, glassType3);

            if (page != 1) {
                inventory.setItem(max22 - 8, back);
            }

            if (page != max) {
                inventory.setItem(max22 - 2, next);
            }


            inventory.setItem(max22 - 4, Methods.createButton(Material.PAPER, "&9&lNew Spawner"));

            player.openInventory(inventory);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void overview(Player player, SpawnerData spawnerData) {
        try {
            EditingData editingData = getEditingData(player);

            int csp = 1;
            for (SpawnerData spawnerData2 : instance.getSpawnerManager().getAllSpawnerData()) {
                if (spawnerData2.getIdentifyingName().toLowerCase().contains("custom"))
                    csp++;
            }
            String type = "Custom " + (editingData.getNewSpawnerName() != null ? editingData.getNewSpawnerName() : csp);

            if (spawnerData != null)
                type = spawnerData.getIdentifyingName();
            else
                editingData.setNewSpawnerName(type);

            String name;

            if (!instance.getSpawnerManager().isSpawnerData(type.toLowerCase())) {
                spawnerData = new ESpawnerData(0, type, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                ((ESpawnerData) spawnerData).addDefaultConditions();
                instance.getSpawnerManager().addSpawnerData(type, spawnerData);
            }
            spawnerData = instance.getSpawnerManager().getSpawnerData(type);

            name = Methods.compileName(spawnerData, 1, false);
            Inventory i = Bukkit.createInventory(null, 54, TextComponent.formatTitle(TextComponent.formatText("&8Editing: " + name + "&8.")));

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

            i.setItem(8, Methods.createButton(Arconix.pl().getApi().getGUI().addTexture(new ItemStack(Material.SKULL_ITEM, 1, (byte) 3), "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"), instance.getLocale().getMessage("general.nametag.back")));

            ItemStack it = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);

            ItemStack item = instance.getHeads().addTexture(it, spawnerData);
            if (spawnerData.getDisplayItem() != null && spawnerData.getDisplayItem() != Material.AIR) {
                item.setType(spawnerData.getDisplayItem());
            }

            ItemMeta itemmeta = item.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            lore.add(TextComponent.formatText("&7Left-Click to &9Change Spawner Name&7."));
            lore.add(TextComponent.formatText("&7Middle-Click to &bChange Spawner Display Item&7."));
            if (instance.getConfig().getBoolean("settings.beta-features"))
                lore.add(TextComponent.formatText("&7Right-Click to &9Change Spawner Head&7."));
            lore.add(TextComponent.formatText("&6-----------------------------"));

            lore.add(TextComponent.formatText("&6Display Name: &7" + spawnerData.getDisplayName() + "&7."));
            if (spawnerData.getDisplayItem() != null) {
                lore.add(TextComponent.formatText("&6Display Item: &7" + spawnerData.getDisplayItem().name() + "&7."));
            } else {
                if (!name.contains("Custom")) {
                    lore.add(TextComponent.formatText("&6Display Item: &7Unavailable&7."));
                } else {
                    lore.add(TextComponent.formatText("&6Display Item: &7Dirt&7."));
                }
            }
            lore.add(TextComponent.formatText("&6Config Name: &7" + type + "&7."));
            itemmeta.setLore(lore);
            itemmeta.setDisplayName(name);
            item.setItemMeta(itemmeta);
            i.setItem(11, item);

            boolean dont = false;
            for (final EntityType val : EntityType.values()) {
                if (val.isSpawnable() && val.isAlive()) {
                    if (val.name().equals(Methods.restoreType(type))) {
                        dont = true;
                    }
                }
            }

            lore = new ArrayList<>();
            if (!dont) lore.add(TextComponent.formatText("&7Right-Click to: &cDestroy Spawner"));
            lore.add(TextComponent.formatText("&6---------------------------"));
            lore.add(TextComponent.formatText(spawnerData.isActive() ? "&6Currently:&a Enabled." : "&6Currently:&c Disabled."));

            i.setItem(29, Methods.createButton(Material.TNT, "&7Left-Click to: &cDisable Spawner", lore));

            i.setItem(23, Methods.createButton(Material.LEVER, "&9&lGeneral Settings"));
            i.setItem(24, Methods.createButton(Material.BONE, "&e&lDrop Settings"));

            i.setItem(25, Methods.createButton(instance.getHeads().addTexture(new ItemStack(Material.SKULL_ITEM, 1, (byte) 3), instance.getSpawnerManager().getSpawnerData("omni")), "&a&lEntity Settings"));

            i.setItem(41, Methods.createButton(Material.CHEST, "&5&lItem Settings"));
            i.setItem(32, Methods.createButton(Material.GOLD_BLOCK, "&c&lBlock Settings"));
            i.setItem(34, Methods.createButton(Material.FIREWORK, "&b&lParticle Settings"));
            i.setItem(43, Methods.createButton(Material.PAPER, "&6&lCommand Settings"));

            player.openInventory(i);
            editingData.setMenu(EditingMenu.OVERVIEW);
            editingData.setSpawnerEditing(spawnerData);
            editingData.setNewId(-1);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public SpawnerData getType(String name) {
        SpawnerData type = instance.getSpawnerManager().getSpawnerData("pig");
        try {
            name = name.replace(String.valueOf(ChatColor.COLOR_CHAR), "").split(":")[0];
            return instance.getSpawnerManager().getSpawnerData(name);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return type;
    }

    public void particleEditor(Player player) {
        EditingData editingData = userEditingData.get(player.getUniqueId());
        SpawnerData spawnerData = editingData.getSpawnerEditing();

        String name = Methods.compileName(spawnerData, 1, false);
        Inventory i = Bukkit.createInventory(null, 45, TextComponent.formatTitle(TextComponent.formatText(name + " &8Particle &8Settings.")));

        int num = 0;
        while (num != 45) {
            i.setItem(num, Methods.getGlass());
            num++;
        }

        i.setItem(0, Methods.createButton(Arconix.pl().getApi().getGUI().addTexture(new ItemStack(Material.SKULL_ITEM, 1, (byte) 3), "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"), instance.getLocale().getMessage("general.nametag.back")));

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

        i.setItem(20, Methods.createButton(Material.ENDER_PEARL, "&5&lParticle Types",
                "&7Entity Spawn Particle: &a" + spawnerData.getEntitySpawnParticle().name(),
                "&cLeft-Click to change.",
                "&7Spawner Spawn Particle: &a" + spawnerData.getSpawnerSpawnParticle().name(),
                "&cMiddle-Click to change.",
                "&7Effect Particle: &a" + spawnerData.getSpawnEffectParticle().name(),
                "&cRight-Click to change."));

        i.setItem(22, Methods.createButton(Material.FIREWORK, "&6&lSpawner Effect",
                "&7Particle Effect: &a" + spawnerData.getParticleEffect().name(),
                "&cLeft-Click to change.",
                "&7Particle Effect For Boosted Only: &a" + spawnerData.isParticleEffectBoostedOnly(),
                "&cRight-Click to change."));

        i.setItem(24, Methods.createButton(Material.REDSTONE_COMPARATOR, "&6&lPerformance",
                "&7Currently: &a" + spawnerData.getParticleDensity().name() + " &cClick to change."));

        player.openInventory(i);
        editingData.setMenu(EditingMenu.PARTICLE);
    }

    public void editor(Player player, EditingMenu editingMenu) {
        try {
            EditingData editingData = userEditingData.get(player.getUniqueId());
            SpawnerData spawnerData = editingData.getSpawnerEditing();

            String name = Methods.compileName(spawnerData, 1, false);
            Inventory i = Bukkit.createInventory(null, 54, TextComponent.formatTitle(TextComponent.formatText(name + "&8 " + editingMenu + " &8Settings.")));

            int num = 0;
            while (num != 54) {
                i.setItem(num, Methods.getGlass());
                num++;
            }

            num = 10;
            int spot = 0;
            while (num != 26) {
                if (num == 17)
                    num = num + 2;

                if (spawnerData.getEntityDroppedItems().size() >= spot + 1 && editingMenu == EditingMenu.DROPS) {
                    i.setItem(num, spawnerData.getEntityDroppedItems().get(spot));
                } else if (spawnerData.getItems().size() >= spot + 1 && editingMenu == EditingMenu.ITEM) {
                    i.setItem(num, spawnerData.getItems().get(spot));
                } else if (spawnerData.getBlocks().size() >= spot + 1 && editingMenu == EditingMenu.BLOCK) {
                    i.setItem(num, new ItemStack(spawnerData.getBlocks().get(spot)));
                } else if (spawnerData.getEntities().size() >= spot + 1 && editingMenu == EditingMenu.ENTITY && spawnerData.getEntities().get(spot) != EntityType.GIANT) {
                    ItemStack it = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
                    ItemStack item = instance.getHeads().addTexture(it,
                            instance.getSpawnerManager().getSpawnerData(spawnerData.getEntities().get(spot)));
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(TextComponent.formatText("&e" + Methods.getTypeFromString(spawnerData.getEntities().get(spot).name())));
                    item.setItemMeta(meta);
                    i.setItem(num, item);

                } else if (spawnerData.getCommands().size() >= spot + 1 && editingMenu == EditingMenu.COMMAND) {
                    ItemStack parseStack = new ItemStack(Material.PAPER, 1);
                    ItemMeta meta = parseStack.getItemMeta();
                    meta.setDisplayName(TextComponent.formatText("&a/" + spawnerData.getCommands().get(spot)));
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

            i.setItem(0, Methods.createButton(Arconix.pl().getApi().getGUI().addTexture(new ItemStack(Material.SKULL_ITEM, 1, (byte) 3), "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"), instance.getLocale().getMessage("general.nametag.back")));

            if (editingMenu == EditingMenu.COMMAND) {
                i.setItem(49, Methods.createButton(Material.WATCH, "&bSpawn Limit",
                        "",
                        "&7This is the spawn limit for entities you spawn",
                        "&7from this spawner. Set to &60 &7to disable this."));
                }

            if (editingMenu != EditingMenu.ITEM && editingMenu != EditingMenu.BLOCK && editingMenu != EditingMenu.DROPS) {
                ItemStack add;
                String addName;
                if (editingMenu == EditingMenu.COMMAND) {
                    add = new ItemStack(Material.PAPER);
                    addName = "&6Add Command";
                } else {
                    add = new ItemStack(Material.MONSTER_EGG);
                    addName = "&6Add entity";
                }

                i.setItem(39, Methods.createButton(add, addName));
            }

            i.setItem(editingMenu != EditingMenu.ITEM ? 41 : 49, Methods.createButton(Material.REDSTONE, "&aSave"));

            player.openInventory(i);
            editingData.setMenu(editingMenu);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void basicSettings(Player player) {
        try {
            EditingData editingData = userEditingData.get(player.getUniqueId());
            SpawnerData spawnerData = editingData.getSpawnerEditing();
            String name = Methods.compileName(spawnerData, 1, false);
            Inventory i = Bukkit.createInventory(null, 45, TextComponent.formatTitle(TextComponent.formatText(name + " &8Settings.")));
            int num = 0;
            while (num != 45) {
                i.setItem(num, Methods.getGlass());
                num++;
            }

            i.setItem(0, Methods.createButton(Arconix.pl().getApi().getGUI().addTexture(new ItemStack(Material.SKULL_ITEM, 1, (byte) 3), "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"), instance.getLocale().getMessage("general.nametag.back")));

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

            i.setItem(19, Methods.createButton(Material.DOUBLE_PLANT, "&6&lShop Price",
                    "&7Currently: &a" + spawnerData.getShopPrice(),
                    "&7This is the price of the",
                    "&7spawner in the shop."));

            i.setItem(20, Methods.createButton(Material.DIAMOND, "&6&lIn Shop",
                    "&7Currently: &a" + spawnerData.isInShop(),
                    "&7If this is true this spawner",
                    "&7will show up in the shop GUI."));

            i.setItem(22, Methods.createButton(Material.FIREWORK_CHARGE, "&c&lSpawn On Fire",
                    "&7Currently: &a" + spawnerData.isSpawnOnFire(),
                    "&7If this is true this spawner",
                    "&7will spawn entities on fire."));

            i.setItem(13, Methods.createButton(Material.HOPPER, "&5&lUpgradable",
                    "&7Currently: &a" + spawnerData.isUpgradeable(),
                    "&7Setting this to true will define",
                    "&7upgradable."));

            i.setItem(24, Methods.createButton(Material.DOUBLE_PLANT, "&6&lCustom Economy cost",
                    "&7Currently: &a" + spawnerData.getUpgradeCostEconomy(),
                    "&7This is the custom economy cost",
                    "&7to upgrade this spawner."));

            i.setItem(25, Methods.createButton(Material.EXP_BOTTLE, "&5&lCustom Experience cost",
                    "&7Currently: &a" + spawnerData.getUpgradeCostExperience(),
                    "&7This is the custom XP cost",
                    "&7to upgrade this spawner."));

            i.setItem(30, Methods.createButton(Material.EXP_BOTTLE, "&5&lCustom Goal",
                    "&7Currently: &a" + spawnerData.getKillGoal(),
                    "&7If this is set to anything",
                    "&7but 0 the default kill goal",
                    "&7will be adjusted for this spawner."));

            i.setItem(32, Methods.createButton(Material.DIAMOND, "&b&lPickup Cost",
                    "&7Currently: &a" + spawnerData.getPickupCost(),
                    "&7Setting this to anything but 0",
                    "&7will allow you to charge players",
                    "&7for breaking this type of spawner."));

            i.setItem(40, Methods.createButton(Material.WATCH, "&6&lTick Rate",
                    "&7Currently: &a" + spawnerData.getTickRate(),
                    "&7This is the default tick rate",
                    "&7that your spawner will use",
                    "&7to create its delay with."));

            player.openInventory(i);
            editingData.setMenu(EditingMenu.GENERAL);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void alterSetting(Player player, ChatListeners.EditingType type) {
        try {
            EditingData editingData = userEditingData.get(player.getUniqueId());
            SpawnerData entity = editingData.getSpawnerEditing();
            player.sendMessage("");
            switch (type) {
                case SHOP_PRICE:
                    player.sendMessage(TextComponent.formatText("&7Enter a sale price for &6" + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    player.sendMessage(TextComponent.formatText("&7Example: &619.99&7."));
                    break;
                case CUSTOM_ECO_COST:
                    player.sendMessage(TextComponent.formatText("&7Enter a custom eco cost for " + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    player.sendMessage(TextComponent.formatText("&7Use &60 &7to use the default cost."));
                    player.sendMessage(TextComponent.formatText("&7Example: &619.99&7."));
                    break;
                case CUSTOM_XP_COST:
                    player.sendMessage(TextComponent.formatText("&7Enter a custom xp cost for " + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    player.sendMessage(TextComponent.formatText("&7Use &60 &7to use the default cost."));
                    player.sendMessage(TextComponent.formatText("&7Example: &625&7."));
                    break;
                case PICKUP_COST:
                    player.sendMessage(TextComponent.formatText("&7Enter a pickup cost for " + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    player.sendMessage(TextComponent.formatText("&7Use &60 &7to disable."));
                    player.sendMessage(TextComponent.formatText("&7Example: &719.99&6."));
                    player.sendMessage(TextComponent.formatText("&7Example: &625&7."));
                    break;
                case CUSTOM_GOAL:
                    player.sendMessage(TextComponent.formatText("&7Enter a custom goal for " + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    player.sendMessage(TextComponent.formatText("&7Use &60 &7to use the default price."));
                    player.sendMessage(TextComponent.formatText("&7Example: &35&6."));
                    break;
                case TICK_RATE:
                    player.sendMessage(TextComponent.formatText("&7Enter a tick rate min and max for " + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    player.sendMessage(TextComponent.formatText("&7Example: &3800:200&6."));
                    break;
            }
            player.sendMessage("");
            instance.getChatListeners().addToEditor(player, type);
            player.closeInventory();
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

    public void save(Player p, List<ItemStack> items) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            SpawnerData spawnerData = editingData.getSpawnerEditing();
            if (editingData.getMenu() == EditingMenu.ITEM) {
                spawnerData.setItems(items);
            } else if (editingData.getMenu() == EditingMenu.DROPS) {
                spawnerData.setEntityDroppedItems(items);
            } else if (editingData.getMenu() == EditingMenu.BLOCK) {
                List<Material> list = new ArrayList<>();
                for (ItemStack item : items) {
                    Material material = item.getType();
                    list.add(material);
                }

                spawnerData.setBlocks(list);
            } else if (editingData.getMenu() == EditingMenu.ENTITY) {
                List<EntityType> list = new ArrayList<>();
                for (ItemStack item : items) {
                    EntityType entityType = EntityType.valueOf(ChatColor.stripColor(item.getItemMeta().getDisplayName()).toUpperCase().replace(" ", "_"));
                    list.add(entityType);
                }
                spawnerData.setEntities(list);
            } else if (editingData.getMenu() == EditingMenu.COMMAND) {
                List<String> list = new ArrayList<>();
                for (ItemStack item : items) {
                    String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).substring(1);
                    list.add(name);
                }
                spawnerData.setCommands(list);
            }
            p.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&7Spawner Saved."));
            spawnerData.reloadSpawnMethods();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }


    public void addEntityInit(Player p) {
        try {
            p.sendMessage("");
            StringBuilder list = new StringBuilder();
            for (final EntityType value : EntityType.values()) {
                if (value.isSpawnable() && value.isAlive()) {
                    list.append(value.toString()).append("&7, &6");
                }
            }
            p.sendMessage(TextComponent.formatText("&6" + list));
            p.sendMessage("Enter an entity Type.");
            p.sendMessage("");
            instance.getChatListeners().addToEditor(p, ChatListeners.EditingType.ADD_ENTITY);
            p.closeInventory();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void destroy(Player p) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            p.sendMessage("");
            p.sendMessage(TextComponent.formatText("&cAre you sure you want to destroy &6" + editingData.getSpawnerEditing().getIdentifyingName() + "&7."));
            p.sendMessage(TextComponent.formatText("&7Type &l&6CONFIRM &7to continue. Otherwise Type anything else to cancel."));
            p.sendMessage("");
            instance.getChatListeners().addToEditor(p, ChatListeners.EditingType.DESTROY);
            p.closeInventory();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void destroyFinal(Player p, String msg) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());

            if (msg.toLowerCase().equals("confirm")) {
                p.sendMessage(TextComponent.formatText("&6" + editingData.getSpawnerEditing().getIdentifyingName() + " Spawner &7 has been destroyed successfully"));
                instance.getSpawnerManager().removeSpawnerData(editingData.getSpawnerEditing().getIdentifyingName());
                openSpawnerSelector(p, 1);
            } else {
                p.sendMessage(TextComponent.formatText("&7Action canceled..."));
                overview(p, editingData.getSpawnerEditing());
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void editSpawnLimit(Player p) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            if (editingData.getMenu() == EditingMenu.COMMAND) {
                SpawnerData spawnerData = editingData.getSpawnerEditing();

                p.sendMessage("");
                p.sendMessage(TextComponent.formatText("&7Enter a spawn limit for &6" + Methods.getTypeFromString(spawnerData.getIdentifyingName()) + "&7."));
                p.sendMessage("");
                instance.getChatListeners().addToEditor(p, ChatListeners.EditingType.SPAWN_LIMIT);
                p.closeInventory();
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void createCommand(Player p) {
        try {
            p.sendMessage("");
            p.sendMessage(TextComponent.formatText("&7Please Type a command. Example: &6eco give @p 1000&7."));
            p.sendMessage(TextComponent.formatText("&7You can use @X @Y and @Z for random X Y and Z coordinates around the spawner."));
            p.sendMessage(TextComponent.formatText("&7@n will execute the command for the person who originally placed the spawner."));
            p.sendMessage(TextComponent.formatText("&7If you're getting command output try &6/gamerule sendCommandFeedback false&7."));
            p.sendMessage(TextComponent.formatText("&7do not include a &a/"));
            p.sendMessage("");

            instance.getChatListeners().addToEditor(p, ChatListeners.EditingType.COMMAND);
            p.closeInventory();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void addCommand(Player p, String cmd) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            SpawnerData spawnerData = editingData.getSpawnerEditing();
            List<String> commands = new ArrayList<>(spawnerData.getCommands());
            commands.add(cmd);
            spawnerData.setCommands(commands);

            editor(p, EditingMenu.COMMAND);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void editSpawnerName(Player p) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            SpawnerData spawnerData = editingData.getSpawnerEditing();

            p.sendMessage("");
            p.sendMessage(TextComponent.formatText("&7Enter a display name for &6" + Methods.getTypeFromString(spawnerData.getIdentifyingName()) + "&7."));
            p.sendMessage("");
            instance.getChatListeners().addToEditor(p, ChatListeners.EditingType.NAME);
            p.closeInventory();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void saveSpawnerName(Player p, String name) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            SpawnerData spawnerData = editingData.getSpawnerEditing();
            spawnerData.setDisplayName(name);
            overview(p, spawnerData);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void addEntity(Player p, String ent) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            SpawnerData spawnerData = editingData.getSpawnerEditing();
            List<EntityType> entities = new ArrayList<>(spawnerData.getEntities());
            entities.add(EntityType.valueOf(ent));
            spawnerData.setEntities(entities);
            editor(p, EditingMenu.ENTITY);

        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public EditingData getEditingData(Player player) {
        return userEditingData.computeIfAbsent(player.getUniqueId(), uuid -> new EditingData());
    }
}
>>>>>>> c649bed... fixed issue in spawner editor regarding air displayitems

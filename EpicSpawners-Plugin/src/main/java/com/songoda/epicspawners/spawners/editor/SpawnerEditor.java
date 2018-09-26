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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * Created by songo on 9/3/2017.
 */
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
            int start = (page - 1) * 33;

            for (SpawnerData spawnerData : instance.getSpawnerManager().getAllSpawnerData()) {
                if (num >= start && !spawnerData.getIdentifyingName().equalsIgnoreCase("omni")) {
                    if (show <= 33) {
                        entities.add(spawnerData);
                        show++;
                    }
                }

                num++;
            }

            int max = (int) Math.ceil((double) num / (double) 32);
            int amt = entities.size();
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

            ItemStack exit = new ItemStack(Material.OAK_DOOR, 1);
            ItemMeta exitMeta = exit.getItemMeta();
            exitMeta.setDisplayName(instance.getLocale().getMessage("general.nametag.exit"));
            exit.setItemMeta(exitMeta);
            inventory.setItem(8, exit);

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

            int max22 = max2;
            int place = 10;
            int dis = start + 1;
            if (start != 0) dis--;

            for (SpawnerData spawnerData : entities) {
                if (place == 17 || place == (max22 - 18)) place++;

                ItemStack it = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
                ItemStack item = EpicSpawnersPlugin.getInstance().getHeads().addTexture(it, spawnerData);
                if (spawnerData.getDisplayItem() != null) {
                    item.setType(spawnerData.getDisplayItem());
                }

                ItemMeta meta = item.getItemMeta();


                StringBuilder hidden = new StringBuilder();
                for (char c : String.valueOf(dis).toCharArray()) hidden.append(";").append(c);
                String disStr = hidden.toString();


                String name = Methods.compileName(spawnerData, 1, false);
                List<String> lore = new ArrayList<>();
                lore.add(TextComponent.formatText("&7Click to &a&lEdit&7."));
                lore.add(TextComponent.convertToInvisibleString(disStr));
                meta.setLore(lore);
                meta.setDisplayName(name);
                item.setItemMeta(meta);

                inventory.setItem(place, item);

                place++;
                dis++;
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
                inventory.setItem(max22 - 8, skull2);
            }

            if (page != max) {
                inventory.setItem(max22 - 2, skull);
            }

            ItemStack newSpawner = new ItemStack(Material.PAPER, 1);
            ItemMeta newSpawnerMeta = newSpawner.getItemMeta();
            newSpawnerMeta.setDisplayName(TextComponent.formatText("&9&lNew Spawner"));
            newSpawner.setItemMeta(newSpawnerMeta);
            inventory.setItem(max22 - 4, newSpawner);

            player.openInventory(inventory);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void overview(Player player, int id) {
        try {
            EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();
            EditingData editingData = getEditingData(player);

            int csp = 1;
            for (SpawnerData spawnerData : instance.getSpawnerManager().getAllSpawnerData()) {
                if (spawnerData.getIdentifyingName().toLowerCase().contains("custom"))
                    csp++;
            }
            String type = "Custom " + (editingData.getNewSpawnerName() != null ? editingData.getNewSpawnerName() : csp);

            if (id != 0)
                type = getType(id).getIdentifyingName();
            else
                editingData.setNewSpawnerName(type);

            String name;

            SpawnerData spawnerData;
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


            ItemStack head2 = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
            ItemStack skull2 = Arconix.pl().getApi().getGUI().addTexture(head2, "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23");
            SkullMeta skull2Meta = (SkullMeta) skull2.getItemMeta();
            skull2.setDurability((short) 3);
            skull2Meta.setDisplayName(instance.getLocale().getMessage("general.nametag.back"));
            skull2.setItemMeta(skull2Meta);

            i.setItem(8, skull2);

            ItemStack it = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);

            ItemStack item = instance.getHeads().addTexture(it, spawnerData);
            if (spawnerData.getDisplayItem() != null) {
                item.setType(spawnerData.getDisplayItem());
            }

            ItemMeta itemmeta = item.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            lore.add(TextComponent.formatText("&7Left-Click to &9Change Spawner Name&7."));
            lore.add(TextComponent.formatText("&7Middle-Click to &bChange Spawner Display Item&7."));
            if (EpicSpawnersPlugin.getInstance().getConfig().getBoolean("settings.beta-features"))
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

            ItemStack destroy = new ItemStack(Material.TNT);
            ItemMeta destroymeta = destroy.getItemMeta();
            boolean dont = false;
            for (final EntityType val : EntityType.values()) {
                if (val.isSpawnable() && val.isAlive()) {
                    if (val.name().equals(Methods.restoreType(type))) {
                        dont = true;
                    }
                }
            }
            lore = new ArrayList<>();
            destroymeta.setDisplayName(TextComponent.formatText("&7Left-Click to: &cDisable Spawner"));
            if (!dont) lore.add(TextComponent.formatText("&7Right-Click to: &cDestroy Spawner"));
            lore.add(TextComponent.formatText("&6---------------------------"));
            if (!spawnerData.isActive())
                lore.add(TextComponent.formatText("&6Currently:&c Disabled."));
            else
                lore.add(TextComponent.formatText("&6Currently:&a Enabled."));
            destroymeta.setLore(lore);

            destroy.setItemMeta(destroymeta);
            i.setItem(29, destroy);

            ItemStack settings = new ItemStack(Material.LEVER);
            ItemMeta settingsmeta = settings.getItemMeta();
            settingsmeta.setDisplayName(TextComponent.formatText("&9&lGeneral Settings"));
            settings.setItemMeta(settingsmeta);
            i.setItem(23, settings);

            settings = new ItemStack(Material.BONE);
            settingsmeta = settings.getItemMeta();
            settingsmeta.setDisplayName(TextComponent.formatText("&e&lDrop Settings"));
            settings.setItemMeta(settingsmeta);
            i.setItem(24, settings);

            ItemStack it2 = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);

            ItemStack entity = EpicSpawnersPlugin.getInstance().getHeads().addTexture(it2, instance.getSpawnerManager().getSpawnerData("omni"));
            ItemMeta entitymeta = entity.getItemMeta();
            entitymeta.setDisplayName(TextComponent.formatText("&a&lEntity Settings"));
            entity.setItemMeta(entitymeta);
            i.setItem(25, entity);

            ItemStack item2 = new ItemStack(Material.CHEST);
            ItemMeta item2meta = item2.getItemMeta();
            item2meta.setDisplayName(TextComponent.formatText("&5&lItem Settings"));
            item2.setItemMeta(item2meta);
            i.setItem(41, item2);

            ItemStack item3 = new ItemStack(Material.GOLD_BLOCK);
            ItemMeta item3meta = item3.getItemMeta();
            item3meta.setDisplayName(TextComponent.formatText("&c&lBlock Settings"));
            item3.setItemMeta(item3meta);
            i.setItem(32, item3);

            ItemStack item4 = new ItemStack(Material.FIREWORK_ROCKET);
            ItemMeta item4meta = item4.getItemMeta();
            item4meta.setDisplayName(TextComponent.formatText("&b&lParticle Settings"));
            item4.setItemMeta(item4meta);
            i.setItem(34, item4);

            ItemStack command = new ItemStack(Material.PAPER);
            ItemMeta commandmeta = command.getItemMeta();
            commandmeta.setDisplayName(TextComponent.formatText("&6&lCommand Settings"));
            command.setItemMeta(commandmeta);
            i.setItem(43, command);

            player.openInventory(i);
            editingData.setMenu(EditingMenu.OVERVIEW);
            if (editingData.getNewId() != -1)
                id = editingData.getNewId();
            editingData.setSpawnerSlot(id);
            editingData.setNewId(-1);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public SpawnerData getType(int id) {
        SpawnerData type = EpicSpawnersPlugin.getInstance().getSpawnerManager().getSpawnerData("pig");
        try {
            if (id >= 33) id++;
            int num = 1;
            for (SpawnerData spawnerData : EpicSpawnersPlugin.getInstance().getSpawnerManager().getRegisteredSpawnerData().values()) {
                if (spawnerData.getIdentifyingName().toLowerCase().equals("omni")) continue;
                if (num == id) {
                    return spawnerData;
                }
                num++;
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return type;
    }

    public void particleEditor(Player player) {
        EditingData editingData = userEditingData.get(player.getUniqueId());
        SpawnerData spawnerData = getType(editingData.getSpawnerSlot());

        String name = Methods.compileName(spawnerData, 1, false);
        Inventory i = Bukkit.createInventory(null, 45, TextComponent.formatTitle(TextComponent.formatText(name + " &8Particle &8Settings.")));

        int num = 0;
        while (num != 45) {
            i.setItem(num, Methods.getGlass());
            num++;
        }

        ItemStack head2 = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
        ItemStack skull2 = Arconix.pl().getApi().getGUI().addTexture(head2, "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23");
        SkullMeta skull2Meta = (SkullMeta) skull2.getItemMeta();
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


        ItemStack item2 = new ItemStack(Material.ENDER_PEARL);
        ItemMeta item2meta = item2.getItemMeta();
        item2meta.setDisplayName(TextComponent.formatText("&5&lParticle Types"));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(TextComponent.formatText("&7Entity Spawn Particle: &a" + spawnerData.getEntitySpawnParticle().name()));
        lore.add(TextComponent.formatText("&cLeft-Click to change."));
        lore.add(TextComponent.formatText("&7Spawner Spawn Particle: &a" + spawnerData.getSpawnerSpawnParticle().name()));
        lore.add(TextComponent.formatText("&cMiddle-Click to change."));
        lore.add(TextComponent.formatText("&7Effect Particle: &a" + spawnerData.getSpawnEffectParticle().name()));
        lore.add(TextComponent.formatText("&cRight-Click to change."));
        item2meta.setLore(lore);
        item2.setItemMeta(item2meta);
        i.setItem(20, item2);

        item2 = new ItemStack(Material.FIREWORK_ROCKET);
        item2meta = item2.getItemMeta();
        item2meta.setDisplayName(TextComponent.formatText("&6&lSpawner Effect"));
        lore = new ArrayList<>();
        lore.add(TextComponent.formatText("&7Particle Effect: &a" + spawnerData.getParticleEffect().name()));
        lore.add(TextComponent.formatText("&cLeft-Click to change."));
        lore.add(TextComponent.formatText("&7Particle Effect For Boosted Only: &a" + spawnerData.isParticleEffectBoostedOnly()));
        lore.add(TextComponent.formatText("&cRight-Click to change."));
        item2meta.setLore(lore);
        item2.setItemMeta(item2meta);
        i.setItem(22, item2);

        item2 = new ItemStack(Material.COMPARATOR);
        item2meta = item2.getItemMeta();
        item2meta.setDisplayName(TextComponent.formatText("&6&lPerformance"));
        lore = new ArrayList<>();
        lore.add(TextComponent.formatText("&7Currently: &a" + spawnerData.getParticleDensity().name() + " &cClick to change."));
        item2meta.setLore(lore);
        item2.setItemMeta(item2meta);
        i.setItem(24, item2);

        player.openInventory(i);
        editingData.setMenu(EditingMenu.PARTICLE);
    }

    public void editor(Player player, EditingMenu editingMenu) {
        try {
            EditingData editingData = userEditingData.get(player.getUniqueId());
            SpawnerData spawnerData = getType(editingData.getSpawnerSlot());

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
                    ItemStack item = EpicSpawnersPlugin.getInstance().getHeads().addTexture(it,
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

            ItemStack head2 = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
            ItemStack skull2 = Arconix.pl().getApi().getGUI().addTexture(head2, "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23");
            SkullMeta skull2Meta = (SkullMeta) skull2.getItemMeta();
            skull2.setDurability((short) 3);
            skull2Meta.setDisplayName(instance.getLocale().getMessage("general.nametag.back"));
            skull2.setItemMeta(skull2Meta);

            i.setItem(0, skull2);

            if (editingMenu == EditingMenu.DROPS || editingMenu == EditingMenu.ITEM || editingMenu == EditingMenu.COMMAND || editingMenu == EditingMenu.BLOCK) {
                if (editingMenu == EditingMenu.COMMAND) {
                    ItemStack item = new ItemStack(Material.CLOCK);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(TextComponent.formatText("&bSpawn Limit"));
                    ArrayList<String> lore = new ArrayList<>();
                    // ToDo: This bit should be some sort of boolean to enable the built in spawn check.
                    //lore.add(Arconix.pl().format().formatText("&7Currently: &c" + EpicSpawners.getInstance().spawnerFile.getConfig().asInt("Entities." + Methods.getTypeFromString(spawnerData) + ".commandSpawnLimit")));
                    lore.add("");
                    lore.add(TextComponent.formatText("&7This is the spawn limit for entities you spawn"));
                    lore.add(TextComponent.formatText("&7from this spawner. Set to &60 &7to disable this."));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    i.setItem(49, item);
                }
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
                ItemMeta addmeta = add.getItemMeta();
                addmeta.setDisplayName(TextComponent.formatText(addName));

                add.setItemMeta(addmeta);
                i.setItem(39, add);
            }

            ItemStack save = new ItemStack(Material.REDSTONE);
            ItemMeta savemeta = save.getItemMeta();
            savemeta.setDisplayName(TextComponent.formatText("&aSave"));
            save.setItemMeta(savemeta);
            if (editingMenu != EditingMenu.ITEM)
                i.setItem(41, save);
            else
                i.setItem(49, save);

            player.openInventory(i);
            editingData.setMenu(editingMenu);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void basicSettings(Player p) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            SpawnerData spawnerData = getType(editingData.getSpawnerSlot());
            String name = Methods.compileName(spawnerData, 1, false);
            Inventory i = Bukkit.createInventory(null, 45, TextComponent.formatTitle(TextComponent.formatText(name + " &8Settings.")));
            int num = 0;
            while (num != 45) {
                i.setItem(num, Methods.getGlass());
                num++;
            }

            ItemStack head2 = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
            ItemStack skull2 = Arconix.pl().getApi().getGUI().addTexture(head2, "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23");
            SkullMeta skull2Meta = (SkullMeta) skull2.getItemMeta();
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


            ItemStack item2 = new ItemStack(Material.SUNFLOWER);
            ItemMeta item2meta = item2.getItemMeta();
            item2meta.setDisplayName(TextComponent.formatText("&6&lShop Price"));

            ArrayList<String> lore = new ArrayList<>();
            lore.add(TextComponent.formatText("&7Currently: &a" + spawnerData.getShopPrice()));

            lore.add(TextComponent.formatText("&7This is the price of the"));
            lore.add(TextComponent.formatText("&7spawner in the shop."));
            item2meta.setLore(lore);

            item2.setItemMeta(item2meta);
            i.setItem(19, item2);

            item2 = new ItemStack(Material.DIAMOND);
            item2meta = item2.getItemMeta();
            item2meta.setDisplayName(TextComponent.formatText("&6&lIn Shop"));
            lore = new ArrayList<>();
            lore.add(TextComponent.formatText("&7Currently: &a" + spawnerData.isInShop()));

            lore.add(TextComponent.formatText("&7If this is true this spawner"));
            lore.add(TextComponent.formatText("&7will show up in the shop GUI."));
            item2meta.setLore(lore);
            item2.setItemMeta(item2meta);
            i.setItem(20, item2);

            item2 = new ItemStack(Material.FIRE_CHARGE);
            item2meta = item2.getItemMeta();
            item2meta.setDisplayName(TextComponent.formatText("&c&lSpawn On Fire"));
            lore = new ArrayList<>();
            lore.add(TextComponent.formatText("&7Currently: &a" + spawnerData.isSpawnOnFire()));

            lore.add(TextComponent.formatText("&7If this is true this spawner"));
            lore.add(TextComponent.formatText("&7will spawn entities on fire."));
            item2meta.setLore(lore);
            item2.setItemMeta(item2meta);
            i.setItem(22, item2);

            item2 = new ItemStack(Material.HOPPER);
            item2meta = item2.getItemMeta();
            item2meta.setDisplayName(TextComponent.formatText("&5&lUpgradable"));
            lore = new ArrayList<>();
            lore.add(TextComponent.formatText("&7Currently: &a" + spawnerData.isUpgradeable()));

            lore.add(TextComponent.formatText("&7Setting this to true will define"));
            lore.add(TextComponent.formatText("&7whether or not this spawner is"));
            lore.add(TextComponent.formatText("&7upgradable."));
            item2meta.setLore(lore);
            item2.setItemMeta(item2meta);
            i.setItem(13, item2);

            item2 = new ItemStack(Material.SUNFLOWER);
            item2meta = item2.getItemMeta();
            item2meta.setDisplayName(TextComponent.formatText("&6&lCustom ECO cost"));
            lore = new ArrayList<>();
            lore.add(TextComponent.formatText("&7Currently: &a" + spawnerData.getUpgradeCostEconomy()));

            lore.add(TextComponent.formatText("&7This is the custom Economy cost"));
            lore.add(TextComponent.formatText("&7to upgrade this spawner."));
            item2meta.setLore(lore);
            item2.setItemMeta(item2meta);
            i.setItem(24, item2);

            item2 = new ItemStack(Material.EXPERIENCE_BOTTLE);
            item2meta = item2.getItemMeta();
            item2meta.setDisplayName(TextComponent.formatText("&5&lCustom XP cost"));
            lore = new ArrayList<>();
            lore.add(TextComponent.formatText("&7Currently: &a" + spawnerData.getUpgradeCostExperience()));

            lore.add(TextComponent.formatText("&7This is the custom XP cost"));
            lore.add(TextComponent.formatText("&7to upgrade this spawner."));
            item2meta.setLore(lore);
            item2.setItemMeta(item2meta);
            i.setItem(25, item2);

            item2 = new ItemStack(Material.SUNFLOWER);
            item2meta = item2.getItemMeta();
            item2meta.setDisplayName(TextComponent.formatText("&5&lCustom Goal"));
            lore = new ArrayList<>();
            lore.add(TextComponent.formatText("&7Currently: &a" + spawnerData.getKillGoal()));

            lore.add(TextComponent.formatText("&7If this is set to anything "));
            lore.add(TextComponent.formatText("&7but 0 the default kill goal "));
            lore.add(TextComponent.formatText("&7will be adjusted for this spawner."));
            item2meta.setLore(lore);
            item2.setItemMeta(item2meta);
            i.setItem(30, item2);

            item2 = new ItemStack(Material.DIAMOND);
            item2meta = item2.getItemMeta();
            item2meta.setDisplayName(TextComponent.formatText("&b&lPickup Cost"));
            lore = new ArrayList<>();
            lore.add(TextComponent.formatText("&7Currently: &a" + spawnerData.getPickupCost()));

            lore.add(TextComponent.formatText("&7Setting this to anything but 0"));
            lore.add(TextComponent.formatText("&7will allow you to charge players"));
            lore.add(TextComponent.formatText("&7for breaking this spawner."));
            item2meta.setLore(lore);
            item2.setItemMeta(item2meta);
            i.setItem(32, item2);

            item2 = new ItemStack(Material.CLOCK);
            item2meta = item2.getItemMeta();
            item2meta.setDisplayName(TextComponent.formatText("&6&lTick Rate"));
            lore = new ArrayList<>();
            lore.add(TextComponent.formatText("&7Currently: &a" + spawnerData.getTickRate()));

            lore.add(TextComponent.formatText("&7This is the default tick rate"));
            lore.add(TextComponent.formatText("&7that your spawner will use"));
            lore.add(TextComponent.formatText("&7to create its delay with."));
            item2meta.setLore(lore);
            item2.setItemMeta(item2meta);
            i.setItem(40, item2);

            p.openInventory(i);
            editingData.setMenu(EditingMenu.GENERAL);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void alterSetting(Player p, ChatListeners.EditingType type) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            SpawnerData entity = getType(editingData.getSpawnerSlot());
            p.sendMessage("");
            switch (type) {
                case SHOP_PRICE:
                    p.sendMessage(TextComponent.formatText("&7Enter a sale price for &6" + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    p.sendMessage(TextComponent.formatText("&7Example: &619.99&7."));
                    break;
                case CUSTOM_ECO_COST:
                    p.sendMessage(TextComponent.formatText("&7Enter a custom eco cost for " + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    p.sendMessage(TextComponent.formatText("&7Use &60 &7to use the default cost."));
                    p.sendMessage(TextComponent.formatText("&7Example: &619.99&7."));
                    break;
                case CUSTOM_XP_COST:
                    p.sendMessage(TextComponent.formatText("&7Enter a custom xp cost for " + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    p.sendMessage(TextComponent.formatText("&7Use &60 &7to use the default cost."));
                    p.sendMessage(TextComponent.formatText("&7Example: &625&7."));
                    break;
                case PICKUP_COST:
                    p.sendMessage(TextComponent.formatText("&7Enter a pickup cost for " + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    p.sendMessage(TextComponent.formatText("&7Use &60 &7to disable."));
                    p.sendMessage(TextComponent.formatText("&7Example: &719.99&6."));
                    p.sendMessage(TextComponent.formatText("&7Example: &625&7."));
                    break;
                case CUSTOM_GOAL:
                    p.sendMessage(TextComponent.formatText("&7Enter a custom goal for " + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    p.sendMessage(TextComponent.formatText("&7Use &60 &7to use the default price."));
                    p.sendMessage(TextComponent.formatText("&7Example: &35&6."));
                    break;
                case TICK_RATE:
                    p.sendMessage(TextComponent.formatText("&7Enter a tick rate min and max for " + Methods.getTypeFromString(entity.getIdentifyingName()) + "&7."));
                    p.sendMessage(TextComponent.formatText("&7Example: &3800:200&6."));
                    break;
            }
            p.sendMessage("");
            EpicSpawnersPlugin.getInstance().getChatListeners().addToEditor(p, type);
            p.closeInventory();
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
            SpawnerData spawnerData = getType(editingData.getSpawnerSlot());
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
            p.sendMessage(TextComponent.formatText(EpicSpawnersPlugin.getInstance().getReferences().getPrefix() + "&7Spawner Saved."));
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
            EpicSpawnersPlugin.getInstance().getChatListeners().addToEditor(p, ChatListeners.EditingType.ADD_ENTITY);
            p.closeInventory();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void destroy(Player p) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            p.sendMessage("");
            p.sendMessage(TextComponent.formatText("&cAre you sure you want to destroy &6" + getType(editingData.getSpawnerSlot()).getIdentifyingName() + "&7."));
            p.sendMessage(TextComponent.formatText("&7Type &l&6CONFIRM &7to continue. Otherwise Type anything else to cancel."));
            p.sendMessage("");
            EpicSpawnersPlugin.getInstance().getChatListeners().addToEditor(p, ChatListeners.EditingType.DESTROY);
            p.closeInventory();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void destroyFinal(Player p, String msg) {
        try {
            int type = userEditingData.get(p.getUniqueId()).getSpawnerSlot();

            if (msg.toLowerCase().equals("confirm")) {
                p.sendMessage(TextComponent.formatText("&6" + getType(type).getIdentifyingName() + " Spawner &7 has been destroyed successfully"));
                EpicSpawnersPlugin.getInstance().getSpawnerManager().removeSpawnerData(getType(type).getIdentifyingName());
                openSpawnerSelector(p, 1);
            } else {
                p.sendMessage(TextComponent.formatText("&7Action canceled..."));
                overview(p, type);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void editSpawnLimit(Player p) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            if (editingData.getMenu() == EditingMenu.COMMAND) {
                int type = editingData.getSpawnerSlot();
                SpawnerData spawnerData = getType(type);

                p.sendMessage("");
                p.sendMessage(TextComponent.formatText("&7Enter a spawn limit for &6" + Methods.getTypeFromString(spawnerData.getIdentifyingName()) + "&7."));
                p.sendMessage("");
                EpicSpawnersPlugin.getInstance().getChatListeners().addToEditor(p, ChatListeners.EditingType.SPAWN_LIMIT);
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

            EpicSpawnersPlugin.getInstance().getChatListeners().addToEditor(p, ChatListeners.EditingType.COMMAND);
            p.closeInventory();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void addCommand(Player p, String cmd) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            int type = editingData.getSpawnerSlot();
            SpawnerData spawnerData = getType(type);
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
            int type = editingData.getSpawnerSlot();
            SpawnerData spawnerData = getType(type);

            p.sendMessage("");
            p.sendMessage(TextComponent.formatText("&7Enter a display name for &6" + Methods.getTypeFromString(spawnerData.getIdentifyingName()) + "&7."));
            p.sendMessage("");
            EpicSpawnersPlugin.getInstance().getChatListeners().addToEditor(p, ChatListeners.EditingType.NAME);
            p.closeInventory();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void saveSpawnerName(Player p, String name) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            int type = editingData.getSpawnerSlot();
            SpawnerData spawnerData = getType(type);
            spawnerData.setDisplayName(name);
            overview(p, editingData.getSpawnerSlot());
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void addEntity(Player p, String ent) {
        try {
            EditingData editingData = userEditingData.get(p.getUniqueId());
            int type = editingData.getSpawnerSlot();
            SpawnerData spawnerData = getType(type);
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

package com.songoda.epicspawners.gui;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.References;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.utils.AbstractChatConfirm;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.gui.AbstractAnvilGUI;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import com.songoda.epicspawners.utils.gui.Range;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIEditorEdit extends AbstractGUI {

    private final EpicSpawnersPlugin plugin;
    private final EditType editType;

    private final AbstractGUI back;
    private SpawnerData spawnerData;

    public GUIEditorEdit(EpicSpawnersPlugin plugin, AbstractGUI abstractGUI, SpawnerData spawnerData, EditType editType, Player player) {
        super(player);
        this.plugin = plugin;
        this.back = abstractGUI;
        this.editType = editType;
        this.spawnerData = spawnerData;

        init(Methods.compileName(spawnerData, 1, false) + "&8 " + editType.getName() + " &8Settings.", 54);
    }

    @Override
    public void constructGUI() {
        inventory.clear();
        resetClickables();
        registerClickables();

        int num = 0;
        while (num != 54) {
            inventory.setItem(num, Methods.getGlass());
            num++;
        }

        num = 10;
        int spot = 0;
        while (num != 26) {
            if (num == 17)
                num = num + 2;

            if (spawnerData.getEntityDroppedItems().size() >= spot + 1 && editType == EditType.DROPS) {
                inventory.setItem(num, spawnerData.getEntityDroppedItems().get(spot));
            } else if (spawnerData.getItems().size() >= spot + 1 && editType == EditType.ITEM) {
                inventory.setItem(num, spawnerData.getItems().get(spot));
            } else if (spawnerData.getBlocks().size() >= spot + 1 && editType == EditType.BLOCK) {
                inventory.setItem(num, new ItemStack(spawnerData.getBlocks().get(spot)));
            } else if (spawnerData.getEntities().size() >= spot + 1 && editType == EditType.ENTITY && spawnerData.getEntities().get(spot) != EntityType.GIANT) {
                ItemStack it = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
                ItemStack item = plugin.getHeads().addTexture(it,
                        plugin.getSpawnerManager().getSpawnerData(spawnerData.getEntities().get(spot)));
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(Methods.formatText("&e" + Methods.getTypeFromString(spawnerData.getEntities().get(spot).name())));
                item.setItemMeta(meta);
                inventory.setItem(num, item);

            } else if (spawnerData.getCommands().size() >= spot + 1 && editType == EditType.COMMAND) {
                ItemStack parseStack = new ItemStack(Material.PAPER, 1);
                ItemMeta meta = parseStack.getItemMeta();
                meta.setDisplayName(Methods.formatText("&a/" + spawnerData.getCommands().get(spot)));
                parseStack.setItemMeta(meta);
                inventory.setItem(num, parseStack);
            } else {
                inventory.setItem(num, new ItemStack(Material.AIR));
            }
            spot++;
            num++;
        }

        inventory.setItem(1, Methods.getBackgroundGlass(true));
        inventory.setItem(7, Methods.getBackgroundGlass(true));
        inventory.setItem(8, Methods.getBackgroundGlass(true));

        inventory.setItem(9, Methods.getBackgroundGlass(true));
        inventory.setItem(17, Methods.getBackgroundGlass(true));

        inventory.setItem(36, Methods.getBackgroundGlass(false));
        inventory.setItem(37, Methods.getBackgroundGlass(false));
        inventory.setItem(38, Methods.getBackgroundGlass(false));
        inventory.setItem(42, Methods.getBackgroundGlass(false));
        inventory.setItem(43, Methods.getBackgroundGlass(false));
        inventory.setItem(44, Methods.getBackgroundGlass(false));

        inventory.setItem(45, Methods.getBackgroundGlass(true));
        inventory.setItem(46, Methods.getBackgroundGlass(true));
        inventory.setItem(47, Methods.getBackgroundGlass(false));
        inventory.setItem(51, Methods.getBackgroundGlass(false));
        inventory.setItem(52, Methods.getBackgroundGlass(true));
        inventory.setItem(53, Methods.getBackgroundGlass(true));

        createButton(0, Methods.addTexture(new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3),
                "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"),
                plugin.getLocale().getMessage("general.nametag.back"));


        if (editType != EditType.ITEM && editType != EditType.BLOCK && editType != EditType.DROPS) {
            ItemStack add;
            String addName;
            if (editType == EditType.COMMAND) {
                add = new ItemStack(Material.PAPER);
                addName = "&6Add Command";
            } else {
                add = new ItemStack(Material.SHEEP_SPAWN_EGG);
                addName = "&6Add entity";
            }

            createButton(39, add, addName);
        }

        createButton(editType != EditType.ITEM ? 41 : 49, Material.REDSTONE, "&aSave");

    }

    @Override
    protected void registerClickables() {
        addDraggable(new Range(10, 25, null, true), true);
        addDraggable(new Range(17, 17, null, true), false);
        addDraggable(new Range(27, 27, null, true), false);

        registerClickable(editType != EditType.ITEM ? 41 : 49, ((player1, inventory1, cursor, slot, type) ->
                save(player, getItems(player))));


        if (editType == EditType.COMMAND) {
            registerClickable(39, (player, inventory, cursor, slot, type) -> {
                player.sendMessage(Methods.formatText("&7Please Type a command. Example: &6eco give @p 1000&7."));
                player.sendMessage(Methods.formatText("&7You can use @X @Y and @Z for random X Y and Z coordinates around the spawner."));
                player.sendMessage(Methods.formatText("&7@n will execute the command for the person who originally placed the spawner."));
                player.sendMessage(Methods.formatText("&7If you're getting command output try &6/gamerule sendCommandFeedback false&7."));
                player.sendMessage(Methods.formatText("&7do not include a &a/"));
                AbstractChatConfirm abstractChatConfirm = new AbstractChatConfirm(player, event -> {
                    List<String> commands = new ArrayList<>(spawnerData.getCommands());
                    commands.add(event.getMessage());
                    spawnerData.setCommands(commands);
                    constructGUI();
                });

                abstractChatConfirm.setOnClose(() ->
                        init(setTitle, inventory.getSize()));
            });
        } else {
            registerClickable(39, (player, inventory, cursor, slot, type) -> {
                AbstractAnvilGUI gui = new AbstractAnvilGUI(player, event -> {
                    try {
                        EntityType eType = EntityType.valueOf(event.getName().toUpperCase());
                        List<EntityType> entities = new ArrayList<>(spawnerData.getEntities());
                        entities.add(eType);
                        spawnerData.setEntities(entities);
                        constructGUI();
                    } catch (Exception ex) {
                        player.sendMessage("That is not a correct EntityType. Please try again..");
                    }
                });

                gui.setOnClose((player1, inventory1) -> init(setTitle, inventory.getSize()));

                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("Entity: Ex. IRON_GOLEM");
                item.setItemMeta(meta);

                gui.setSlot(AbstractAnvilGUI.AnvilSlot.INPUT_LEFT, item);
                gui.open();
            });
        }

        registerClickable(0, (player, inventory, cursor, slot, type) -> {
            back.init(back.getSetTitle(), back.getInventory().getSize());
            back.constructGUI();
        });
    }

    @Override
    protected void registerOnCloses() {

    }

    private List<ItemStack> getItems(Player p) {
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

    private void save(Player p, List<ItemStack> items) {
        try {
            if (editType == EditType.ITEM) {
                spawnerData.setItems(items);
            } else if (editType == EditType.DROPS) {
                spawnerData.setEntityDroppedItems(items);
            } else if (editType == EditType.BLOCK) {
                List<Material> list = new ArrayList<>();
                for (ItemStack item : items) {
                    Material material = item.getType();
                    list.add(material);
                }
                spawnerData.setBlocks(list);
            } else if (editType == EditType.ENTITY) {
                List<EntityType> list = new ArrayList<>();
                for (ItemStack item : items) {
                    EntityType entityType = EntityType.valueOf(ChatColor.stripColor(item.getItemMeta().getDisplayName()).toUpperCase().replace(" ", "_"));
                    list.add(entityType);
                }
                spawnerData.setEntities(list);
            } else if (editType == EditType.COMMAND) {
                List<String> list = new ArrayList<>();
                for (ItemStack item : items) {
                    String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).substring(1);
                    list.add(name);
                }
                spawnerData.setCommands(list);
            }
            p.sendMessage(Methods.formatText(References.getPrefix() + "&7Spawner Saved."));
            spawnerData.reloadSpawnMethods();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public enum EditType {
        ENTITY("Entity"),
        ITEM("Item"),
        COMMAND("Command"),
        BLOCK("Block"),
        DROPS("Drops");

        private final String name;

        EditType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}

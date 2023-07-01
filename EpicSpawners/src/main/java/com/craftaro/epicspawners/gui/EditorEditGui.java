package com.craftaro.epicspawners.gui;

import com.craftaro.core.compatibility.CompatibleMaterial;
import com.craftaro.core.gui.AnvilGui;
import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.input.ChatPrompt;
import com.craftaro.core.utils.PlayerUtils;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.epicspawners.api.utils.HeadUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class EditorEditGui extends Gui {

    private final EpicSpawners plugin;
    private final EditType editType;

    private final Gui back;
    private SpawnerTier spawnerTier;

    public EditorEditGui(EpicSpawners plugin, Gui back, SpawnerTier spawnerTier, EditType editType) {
        super(6);
        this.plugin = plugin;
        this.back = back;
        this.editType = editType;
        this.spawnerTier = spawnerTier;

        setTitle(spawnerTier.getGuiTitle());
        setOnClose(event -> plugin.getSpawnerManager().saveSpawnerDataToFile());
        setAcceptsItems(true);

        paint();
    }

    public void paint() {
        reset();
        if (editType == EditType.ITEM || editType == EditType.BLOCK)
            setUnlockedRange(10, 25);
        unlockedCells.remove(17);
        unlockedCells.remove(27);

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(CompatibleMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(CompatibleMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        setItem(1, glass2);
        setItem(7, glass2);
        setItem(8, glass2);

        setItem(9, glass2);
        setItem(17, glass2);

        setItem(36, glass3);
        setItem(37, glass3);
        setItem(38, glass3);
        setItem(42, glass3);
        setItem(43, glass3);
        setItem(44, glass3);

        setItem(45, glass2);
        setItem(46, glass2);
        setItem(47, glass3);
        setItem(51, glass3);
        setItem(52, glass2);
        setItem(53, glass2);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int num = 9;
            for (int i = 0; i < 14; i++) {
                num++;
                if (num == 17)
                    num = num + 2;

                switch (editType) {
                    case ITEM:
                        if (i >= spawnerTier.getItems().size()) {
                            setItem(num, null);
                            continue;
                        }
                        setItem(num, spawnerTier.getItems().get(i));
                        break;
                    case BLOCK:
                        if (i >= spawnerTier.getBlocks().size()) {
                            setItem(num, null);
                            continue;
                        }
                        setItem(num, spawnerTier.getBlocks().get(i).getItem());
                        break;
                    case ENTITY: {
                        if (i >= spawnerTier.getEntities().size()) {
                            setItem(num, null);
                            continue;
                        }
                        ItemStack item = HeadUtils.getTexturedSkull(
                                plugin.getSpawnerManager().getSpawnerData(spawnerTier.getEntities().get(i)));
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(TextUtils.formatText("&e" + spawnerTier.getEntities().get(i).name()));
                        item.setItemMeta(meta);
                        int numFinal = num;
                        setButton(num, item, event -> setItem(numFinal, null));
                    }
                    break;
                    case COMMAND: {
                        if (i >= spawnerTier.getCommands().size()) {
                            setItem(num, null);
                            continue;
                        }
                        ItemStack parseStack = new ItemStack(Material.PAPER, 1);
                        ItemMeta meta = parseStack.getItemMeta();
                        meta.setDisplayName(TextUtils.formatText("&a/" + spawnerTier.getCommands().get(i)));
                        parseStack.setItemMeta(meta);
                        int numFinal = num;
                        setButton(num, parseStack, event -> setItem(numFinal, null));
                    }
                    break;
                    default:
                        setItem(num, null);
                        break;
                }
            }
        }, 1L);

        setButton(0, GuiUtils.createButtonItem(CompatibleMaterial.OAK_DOOR,
                TextUtils.formatText(plugin.getLocale().getMessage("general.nametag.back").getMessage())),
                (event) -> guiManager.showGUI(event.player, back));

        if (editType != EditType.ITEM && editType != EditType.BLOCK) {
            ItemStack add;
            String addName;
            if (editType == EditType.COMMAND) {
                add = new ItemStack(Material.PAPER);
                addName = "&6Add Command";
            } else {
                add = CompatibleMaterial.SHEEP_SPAWN_EGG.getItem();
                addName = "&6Add entity";
            }

            setButton(39, GuiUtils.createButtonItem(add, TextUtils.formatText(addName)), event -> {
                Player player = event.player;
                if (editType == EditType.COMMAND) {
                    PlayerUtils.sendMessages(player, TextUtils.formatText("&7Please Type a command. Example: &6eco give @p 1000&7.",
                            "&7You can use @X @Y and @Z for random X Y and Z coordinates around the spawner.",
                            "&7If you need the world name, you can use @W for the current world.",
                            "&7@n will execute the command for the person who originally placed the spawner.",
                            "&7If you're getting command output try &6/gamerule sendCommandFeedback false&7.",
                            "&7do not include a &a/"));
                    ChatPrompt.showPrompt(plugin, player, evnt -> {
                        List<String> commands = new ArrayList<>(spawnerTier.getCommands());
                        commands.add(evnt.getMessage());
                        spawnerTier.setCommands(commands);
                        paint();
                    }).setOnClose(() -> guiManager.showGUI(player, this))
                            .setTimeOut(player, 20L * 15L);
                } else {
                    AnvilGui gui = new AnvilGui(player, this);
                    gui.setTitle("Entity: Ex. IRON_GOLEM");
                    gui.setAction(evnt -> {
                        try {
                            EntityType eType = EntityType.valueOf(gui.getInputText().trim().toUpperCase());
                            List<EntityType> entities = new ArrayList<>(spawnerTier.getEntities());
                            entities.add(eType);
                            spawnerTier.setEntities(entities);
                            player.closeInventory();
                        } catch (Exception ex) {
                            player.sendMessage("That is not a correct EntityType. Please try again..");
                        }
                    }).setOnClose(e -> paint());
                    plugin.getGuiManager().showGUI(player, gui);
                }
            });
        }

        setButton(editType != EditType.ITEM ? 41 : 49,
                GuiUtils.createButtonItem(CompatibleMaterial.REDSTONE, TextUtils.formatText("&aSave")),
                event -> save(event.player, getItems(event.player)));

    }

    private List<ItemStack> getItems(Player player) {
        ItemStack[] items2 = player.getOpenInventory().getTopInventory().getContents();
        //items2 = Arrays.copyOf(items2, items2.length - 9);

        List<ItemStack> items = new ArrayList<>();

        int num = 9;
        for (int i = 0; i < 14; i++) {
            num++;
            if (num == 17)
                num = num + 2;
            ItemStack item = getItem(num);
            if (item != null)
                items.add(getItem(num));
        }
        return items;
    }

    private void save(Player player, List<ItemStack> items) {
        if (editType == EditType.ITEM) {
            spawnerTier.setItems(items);
        } else if (editType == EditType.BLOCK) {
            List<CompatibleMaterial> list = new ArrayList<>();
            for (ItemStack item : items) {
                CompatibleMaterial material = CompatibleMaterial.getMaterial(item);
                list.add(material);
            }
            spawnerTier.setBlocks(list);
        } else if (editType == EditType.ENTITY) {
            List<EntityType> list = new ArrayList<>();
            for (ItemStack item : items) {
                EntityType entityType = EntityType.valueOf(ChatColor.stripColor(item.getItemMeta().getDisplayName()).toUpperCase().replace(" ", "_"));
                list.add(entityType);
            }
            spawnerTier.setEntities(list);
        } else if (editType == EditType.COMMAND) {
            List<String> list = new ArrayList<>();
            for (ItemStack item : items) {
                String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).substring(1);
                list.add(name);
            }
            spawnerTier.setCommands(list);
        }
        plugin.getLocale().newMessage("&7Spawner Saved.").sendPrefixedMessage(player);
        spawnerTier.reloadSpawnMethods();
    }

    public enum EditType {
        ENTITY("Entity"),
        ITEM("Item"),
        COMMAND("Command"),
        BLOCK("Block");

        private final String name;

        EditType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}

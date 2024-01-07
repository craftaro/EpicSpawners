package com.craftaro.epicspawners.gui;

import com.craftaro.core.gui.AnvilGui;
import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.input.ChatPrompt;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.utils.PlayerUtils;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.api.utils.HeadUtils;
import com.craftaro.epicspawners.settings.Settings;
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
    private final SpawnerTier spawnerTier;

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
        if (this.editType == EditType.ITEM || this.editType == EditType.BLOCK) {
            setUnlockedRange(10, 25);
        }
        this.unlockedCells.remove(17);
        this.unlockedCells.remove(27);

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(XMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

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

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            int num = 9;
            for (int i = 0; i < 14; i++) {
                num++;
                if (num == 17) {
                    num = num + 2;
                }

                switch (this.editType) {
                    case ITEM:
                        if (i >= this.spawnerTier.getItems().size()) {
                            setItem(num, null);
                            continue;
                        }
                        setItem(num, this.spawnerTier.getItems().get(i));
                        break;
                    case BLOCK:
                        if (i >= this.spawnerTier.getBlocks().size()) {
                            setItem(num, null);
                            continue;
                        }
                        setItem(num, this.spawnerTier.getBlocks().get(i).parseItem());
                        break;
                    case ENTITY: {
                        if (i >= this.spawnerTier.getEntities().size()) {
                            setItem(num, null);
                            continue;
                        }
                        ItemStack item = HeadUtils.getTexturedSkull(
                                this.plugin.getSpawnerManager().getSpawnerData(this.spawnerTier.getEntities().get(i)));
                        ItemMeta meta = item.getItemMeta();
                        meta.setDisplayName(TextUtils.formatText("&e" + this.spawnerTier.getEntities().get(i).name()));
                        item.setItemMeta(meta);
                        int numFinal = num;
                        setButton(num, item, event -> setItem(numFinal, null));
                    }
                    break;
                    case COMMAND: {
                        if (i >= this.spawnerTier.getCommands().size()) {
                            setItem(num, null);
                            continue;
                        }
                        ItemStack parseStack = new ItemStack(Material.PAPER, 1);
                        ItemMeta meta = parseStack.getItemMeta();
                        meta.setDisplayName(TextUtils.formatText("&a/" + this.spawnerTier.getCommands().get(i)));
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

        setButton(0, GuiUtils.createButtonItem(XMaterial.OAK_DOOR,
                        TextUtils.formatText(this.plugin.getLocale().getMessage("general.nametag.back").getMessage())),
                (event) -> this.guiManager.showGUI(event.player, this.back));

        if (this.editType != EditType.ITEM && this.editType != EditType.BLOCK) {
            ItemStack add;
            String addName;
            if (this.editType == EditType.COMMAND) {
                add = new ItemStack(Material.PAPER);
                addName = "&6Add Command";
            } else {
                add = XMaterial.SHEEP_SPAWN_EGG.parseItem();
                addName = "&6Add entity";
            }

            setButton(39, GuiUtils.createButtonItem(add, TextUtils.formatText(addName)), event -> {
                Player player = event.player;
                if (this.editType == EditType.COMMAND) {
                    PlayerUtils.sendMessages(player, TextUtils.formatText("&7Please Type a command. Example: &6eco give @p 1000&7.",
                            "&7You can use @X @Y and @Z for random X Y and Z coordinates around the spawner.",
                            "&7If you need the world name, you can use @W for the current world.",
                            "&7@n will execute the command for the person who originally placed the spawner.",
                            "&7If you're getting command output try &6/gamerule sendCommandFeedback false&7.",
                            "&7do not include a &a/"));
                    ChatPrompt.showPrompt(this.plugin, player, evnt -> {
                                List<String> commands = new ArrayList<>(this.spawnerTier.getCommands());
                                commands.add(evnt.getMessage());
                                this.spawnerTier.setCommands(commands);
                                paint();
                            }).setOnClose(() -> this.guiManager.showGUI(player, this))
                            .setTimeOut(player, 20L * 15L);
                } else {
                    AnvilGui gui = new AnvilGui(player, this);
                    gui.setTitle("Entity: Ex. IRON_GOLEM");
                    gui.setAction(evnt -> {
                        try {
                            EntityType eType = EntityType.valueOf(gui.getInputText().trim().toUpperCase());
                            List<EntityType> entities = new ArrayList<>(this.spawnerTier.getEntities());
                            entities.add(eType);
                            this.spawnerTier.setEntities(entities);
                            player.closeInventory();
                        } catch (Exception ex) {
                            player.sendMessage("That is not a correct EntityType. Please try again..");
                        }
                    }).setOnClose(e -> paint());
                    this.plugin.getGuiManager().showGUI(player, gui);
                }
            });
        }

        setButton(this.editType != EditType.ITEM ? 41 : 49,
                GuiUtils.createButtonItem(XMaterial.REDSTONE, TextUtils.formatText("&aSave")),
                event -> save(event.player, getItems(event.player)));

    }

    private List<ItemStack> getItems(Player player) {
        List<ItemStack> items = new ArrayList<>();

        int num = 9;
        for (int i = 0; i < 14; i++) {
            num++;
            if (num == 17) {
                num = num + 2;
            }
            ItemStack item = getItem(num);
            if (item != null) {
                items.add(getItem(num));
            }
        }
        return items;
    }

    private void save(Player player, List<ItemStack> items) {
        if (this.editType == EditType.ITEM) {
            this.spawnerTier.setItems(items);
        } else if (this.editType == EditType.BLOCK) {
            List<XMaterial> list = new ArrayList<>();
            for (ItemStack item : items) {
                XMaterial material = XMaterial.matchXMaterial(item);
                list.add(material);
            }
            this.spawnerTier.setBlocks(list);
        } else if (this.editType == EditType.ENTITY) {
            List<EntityType> list = new ArrayList<>();
            for (ItemStack item : items) {
                EntityType entityType = EntityType.valueOf(ChatColor.stripColor(item.getItemMeta().getDisplayName()).toUpperCase().replace(" ", "_"));
                list.add(entityType);
            }
            this.spawnerTier.setEntities(list);
        } else if (this.editType == EditType.COMMAND) {
            List<String> list = new ArrayList<>();
            for (ItemStack item : items) {
                String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).substring(1);
                list.add(name);
            }
            this.spawnerTier.setCommands(list);
        }
        this.plugin.getLocale().newMessage("&7Spawner Saved.").sendPrefixedMessage(player);
        this.spawnerTier.reloadSpawnMethods();
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
            return this.name;
        }
    }
}

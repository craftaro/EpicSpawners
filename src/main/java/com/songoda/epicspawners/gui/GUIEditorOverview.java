package com.songoda.epicspawners.gui;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.gui.AnvilGui;
import com.songoda.core.input.ChatPrompt;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class GUIEditorOverview extends AbstractGUI {

    private final EpicSpawners plugin;
    private final AbstractGUI back;
    private SpawnerData spawnerData;

    public GUIEditorOverview(EpicSpawners plugin, AbstractGUI abstractGUI, SpawnerData spawnerData, Player player) {
        super(player);
        this.plugin = plugin;
        this.back = abstractGUI;
        this.spawnerData = spawnerData;

        if (spawnerData == null) {
            String type;
            for (int i = 1; true; i++) {
                String temp = "Custom " + i;
                if (!plugin.getSpawnerManager().isSpawnerData(temp)) {
                    type = temp;
                    break;
                }
            }

            this.spawnerData = plugin.getSpawnerManager().addSpawnerData(type, new SpawnerData(type));
            this.spawnerData.addDefaultConditions();
            this.spawnerData.setCustom(true);
        }

        init("&8Editing: " + this.spawnerData.getCompiledDisplayName() + "&8.", 54);
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

        inventory.setItem(0, Methods.getBackgroundGlass(false));
        inventory.setItem(1, Methods.getBackgroundGlass(false));
        inventory.setItem(2, Methods.getBackgroundGlass(false));
        inventory.setItem(3, Methods.getBackgroundGlass(true));
        inventory.setItem(4, Methods.getBackgroundGlass(true));

        inventory.setItem(9, Methods.getBackgroundGlass(false));
        inventory.setItem(13, Methods.getBackgroundGlass(true));
        inventory.setItem(14, Methods.getBackgroundGlass(false));
        inventory.setItem(15, Methods.getBackgroundGlass(true));
        inventory.setItem(16, Methods.getBackgroundGlass(true));
        inventory.setItem(17, Methods.getBackgroundGlass(true));

        inventory.setItem(18, Methods.getBackgroundGlass(false));
        inventory.setItem(22, Methods.getBackgroundGlass(false));
        inventory.setItem(26, Methods.getBackgroundGlass(true));

        inventory.setItem(27, Methods.getBackgroundGlass(true));
        inventory.setItem(31, Methods.getBackgroundGlass(false));
        inventory.setItem(35, Methods.getBackgroundGlass(false));

        inventory.setItem(36, Methods.getBackgroundGlass(true));
        inventory.setItem(37, Methods.getBackgroundGlass(true));
        inventory.setItem(38, Methods.getBackgroundGlass(false));
        inventory.setItem(39, Methods.getBackgroundGlass(true));
        inventory.setItem(40, Methods.getBackgroundGlass(true));
        inventory.setItem(44, Methods.getBackgroundGlass(false));

        inventory.setItem(49, Methods.getBackgroundGlass(true));
        inventory.setItem(50, Methods.getBackgroundGlass(true));
        inventory.setItem(51, Methods.getBackgroundGlass(false));
        inventory.setItem(52, Methods.getBackgroundGlass(false));
        inventory.setItem(53, Methods.getBackgroundGlass(false));

        createButton(8, Methods.addTexture(new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3),
                "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"),
                plugin.getLocale().getMessage("general.nametag.back").getMessage());

        ItemStack it = new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3);

        ItemStack item = plugin.getHeads().addTexture(it, spawnerData);
        if (spawnerData.getDisplayItem() != null && spawnerData.getDisplayItem() != Material.AIR) {
            item.setType(spawnerData.getDisplayItem());
        }

        ItemMeta itemmeta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(TextUtils.formatText("&7Left-Click to &9Change Spawner Name&7."));
        lore.add(TextUtils.formatText("&7Right-Click to &bChange Spawner Display Item&7."));
        lore.add(TextUtils.formatText("&6-----------------------------"));

        lore.add(TextUtils.formatText("&6Display Name: &7" + spawnerData.getDisplayName() + "&7."));
        if (spawnerData.getDisplayItem() != null) {
            lore.add(TextUtils.formatText("&6Display Item: &7" + spawnerData.getDisplayItem().name() + "&7."));
        } else {
            if (!spawnerData.isCustom()) {
                lore.add(TextUtils.formatText("&6Display Item: &7Unavailable&7."));
            } else {
                lore.add(TextUtils.formatText("&6Display Item: &7Dirt&7."));
            }
        }
        lore.add(TextUtils.formatText("&6Config Name: &7" + spawnerData.getIdentifyingName() + "&7."));
        itemmeta.setLore(lore);
        itemmeta.setDisplayName(spawnerData.getCompiledDisplayName());
        item.setItemMeta(itemmeta);
        inventory.setItem(11, item);

        lore = new ArrayList<>();
        if (spawnerData.isCustom()) lore.add(TextUtils.formatText("&7Right-Click to: &cDestroy Spawner"));
        lore.add(TextUtils.formatText("&6---------------------------"));
        lore.add(TextUtils.formatText(spawnerData.isActive() ? "&6Currently:&a Enabled." : "&6Currently:&c Disabled."));

        createButton(29, Material.TNT, "&7Left-Click to: " + (!spawnerData.isActive() ? "&cDisable" : "&aEnable") + " Spawner", lore);

        createButton(23, Material.LEVER, "&9&lGeneral Settings");
        createButton(24, Material.BONE, "&e&lDrop Settings");

        createButton(25, plugin.getHeads().addTexture(new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3), plugin.getSpawnerManager().getSpawnerData("omni")), "&a&lEntity Settings");

        createButton(41, Material.CHEST, "&5&lItem Settings");
        createButton(32, Material.GOLD_BLOCK, "&c&lBlock Settings");
        createButton(34, ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.FIREWORK_ROCKET : Material.valueOf("FIREWORK"), "&b&lParticle Settings");
        createButton(43, Material.PAPER, "&6&lCommand Settings");
    }

    @Override
    protected void registerClickables() {

        registerClickable(29, ((player1, inventory1, cursor, slot, type) -> {
            if (type == ClickType.LEFT || !spawnerData.isCustom()) {
                if (spawnerData.isActive())
                    spawnerData.setActive(false);
                else
                    spawnerData.setActive(true);
                constructGUI();
            } else if (type == ClickType.RIGHT) {
                player.sendMessage("Type \"yes\" to confirm this action.");
                ChatPrompt chatPrompt = ChatPrompt.showPrompt(plugin, player, event -> {
                    if (event.getMessage().equalsIgnoreCase("yes")) {
                        player.sendMessage(TextUtils.formatText("&6" + spawnerData.getIdentifyingName() + " Spawner &7 has been destroyed successfully"));
                        plugin.getSpawnerManager().removeSpawnerData(spawnerData.getIdentifyingName());
                    }
                });

                chatPrompt.setOnClose(() -> {
                    back.init(back.getSetTitle(), back.getInventory().getSize());
                    back.constructGUI();
                });
            }
        }));

        registerClickable(23, ((player1, inventory1, cursor, slot, type) ->
                new GUIEditorGeneral(plugin, this, spawnerData, player)));

        registerClickable(41, ((player1, inventory1, cursor, slot, type) ->
                new GUIEditorEdit(plugin, this, spawnerData, GUIEditorEdit.EditType.ITEM, player)));

        registerClickable(43, ((player1, inventory1, cursor, slot, type) ->
                new GUIEditorEdit(plugin, this, spawnerData, GUIEditorEdit.EditType.COMMAND, player)));

        registerClickable(24, ((player1, inventory1, cursor, slot, type) ->
                new GUIEditorEdit(plugin, this, spawnerData, GUIEditorEdit.EditType.DROPS, player)));

        registerClickable(25, ((player1, inventory1, cursor, slot, type) ->
                new GUIEditorEdit(plugin, this, spawnerData, GUIEditorEdit.EditType.ENTITY, player)));

        registerClickable(32, ((player1, inventory1, cursor, slot, type) ->
                new GUIEditorEdit(plugin, this, spawnerData, GUIEditorEdit.EditType.BLOCK, player)));

        registerClickable(34, ((player1, inventory1, cursor, slot, type) ->
                new GUIEditorParticle(plugin, this, spawnerData, player)));

        registerClickable(8, (player, inventory, cursor, slot, type) -> {
            back.init(back.getSetTitle(), back.getInventory().getSize());
            back.constructGUI();
        });

        registerClickable(11, (player, inventory, cursor, slot, type) -> {
            if (type == ClickType.RIGHT) {
                spawnerData.setDisplayItem(Material.valueOf(player.getInventory().getItemInHand().getType().toString()));
                plugin.getLocale().newMessage("&7Display Item for &6" + spawnerData.getIdentifyingName() + " &7set to &6" + player.getInventory().getItemInHand().getType().toString() + "&7.")
                        .sendPrefixedMessage(player);
                constructGUI();
            } else if (type == ClickType.LEFT) {
                AnvilGui gui = new AnvilGui(player);
                gui.setTitle("Enter a display name.");
                gui.setAction(event -> {
                    spawnerData.setDisplayName(gui.getInputText().trim());

                    player.closeInventory();
                    player.openInventory(inventory);
                    init("&8Editing: " + spawnerData.getCompiledDisplayName() + "&8.", inventory.getSize());

                });

                plugin.getGuiManager().showGUI(player, gui);
            }
        });
    }

    @Override
    protected void registerOnCloses() {

        registerOnClose(((player1, inventory1) -> {
            plugin.getSpawnerManager().saveSpawnerDataToFile();
        }));
    }
}

package com.songoda.epicspawners.gui;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.SpawnerData;
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

public class GUIEditorSelector extends AbstractGUI {

    private final EpicSpawnersPlugin plugin;
    private List<SpawnerData> entities;
    private int page = 1;
    private int max = 0;
    private int totalAmount = 0;
    private int slots = 0;

    public GUIEditorSelector(EpicSpawnersPlugin plugin, Player player) {
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
            if (totalAmount >= start && !spawnerData.getIdentifyingName().equalsIgnoreCase("omni")) {
                if (show <= 31) {
                    entities.add(spawnerData);
                    show++;
                }
            }
            totalAmount++;
        }

        int size = entities.size();
        if (size == 24 || size == 25) size = 26;
        slots = 54;
        if (size <= 7) {
            slots = 27;
        } else if (size <= 14) {
            slots = 36;
        } else if (size <= 25) {
            slots = 45;
        }

        init("Spawner Editor", slots);
    }

    @Override
    public void constructGUI() {
        inventory.clear();
        resetClickables();
        registerClickables();

        max = (int) Math.ceil((double) totalAmount / (double) 32);

        createButton(8, Material.OAK_DOOR, plugin.getLocale().getMessage("general.nametag.exit"));

        int place = 10;

        for (SpawnerData spawnerData : entities) {
            if (place == 17 || place == (slots - 18)) place++;
            if (place == 18 && slots == 36) place++;

            ItemStack icon = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
            ItemStack item = plugin.getHeads().addTexture(icon, spawnerData);
            if (spawnerData.getDisplayItem() != null && spawnerData.getDisplayItem() != Material.AIR) {
                item.setType(spawnerData.getDisplayItem());
            }

            String name = Methods.compileName(spawnerData, 1, false);

            createButton(place, icon, Methods.convertToInvisibleString(spawnerData.getIdentifyingName() + ":") + name,
                    "&7Click to &a&lEdit&7.");

            registerClickable(place, ((player1, inventory1, cursor, slot, type) -> {
                new GUIEditorOverview(plugin, this, spawnerData, player);

            }));

            place++;
        }

        ItemStack glass = Methods.getGlass();
        for (int i = 0; i < 8; i++) {
            inventory.setItem(i, glass);
        }

        for (int i = slots - 9; i < slots; i++) {
            inventory.setItem(i, glass);
        }

        ItemStack glassType2 = Methods.getBackgroundGlass(true), glassType3 = Methods.getBackgroundGlass(false);
        inventory.setItem(0, glassType2);
        inventory.setItem(1, glassType2);
        inventory.setItem(9, glassType2);

        inventory.setItem(7, glassType2);
        inventory.setItem(17, glassType2);

        inventory.setItem(slots - 18, glassType2);
        inventory.setItem(slots - 9, glassType2);
        inventory.setItem(slots - 8, glassType2);

        inventory.setItem(slots - 10, glassType2);
        inventory.setItem(slots - 2, glassType2);
        inventory.setItem(slots - 1, glassType2);

        inventory.setItem(2, glassType3);
        inventory.setItem(6, glassType3);
        inventory.setItem(slots - 7, glassType3);
        inventory.setItem(slots - 3, glassType3);

        if (page != 1) {
            createButton(slots - 8, Methods.addTexture(new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3),
                    "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"),
                    plugin.getLocale().getMessage("general.nametag.back"));
        }

        if (page != max) {
            createButton(slots - 2, Methods.addTexture(new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3),
                    "http://textures.minecraft.net/texture/1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b"),
                    plugin.getLocale().getMessage("general.nametag.next"));
        }


        createButton(slots - 4, Material.PAPER, "&9&lNew Spawner");
    }

    @Override
    protected void registerClickables() {
        registerClickable(8, (player, inventory, cursor, slot, type) -> player.closeInventory());

        registerClickable(slots - 4, ((player1, inventory1, cursor, slot, type) ->
                new GUIEditorOverview(plugin, this, null, player)));

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

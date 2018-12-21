package com.songoda.epicspawners.gui;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.ESpawner;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.SettingsManager;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Map;

public class GUISpawnerBoost extends AbstractGUI {

    private final EpicSpawnersPlugin plugin;
    private final Spawner spawner;
    private int amount = 1;

    public GUISpawnerBoost(EpicSpawnersPlugin plugin, Spawner spawner, Player player) {
        super(player);
        this.plugin = plugin;
        this.spawner = spawner;
        setUp();
    }

    private void setUp() {
        if (amount > SettingsManager.Setting.MAX_PLAYER_BOOST.getInt()) {
            amount = SettingsManager.Setting.MAX_PLAYER_BOOST.getInt();
            return;
        } else if (amount < 1) {
            amount = 1;
        }
        init(plugin.getLocale().getMessage("interface.boost.title", Methods.compileName(spawner.getIdentifyingData(), spawner.getSpawnerDataCount(), false), amount), 27);
    }

    @Override
    protected void constructGUI() {
        if (!player.hasPermission("epicspawners.canboost")) return;

        int num = 0;
        while (num != 27) {
            inventory.setItem(num, Methods.getGlass());
            num++;
        }

        ItemStack coal = new ItemStack(Material.COAL);
        ItemMeta coalMeta = coal.getItemMeta();
        coalMeta.setDisplayName(EpicSpawnersPlugin.getInstance().getLocale().getMessage("interface.boost.boostfor", "5"));
        ArrayList<String> coalLore = new ArrayList<>();
        coalLore.add(TextComponent.formatText("&7Costs &6&l" + Methods.getBoostCost(5, amount) + "."));
        coalMeta.setLore(coalLore);
        coal.setItemMeta(coalMeta);

        ItemStack iron = new ItemStack(Material.IRON_INGOT);
        ItemMeta ironMeta = iron.getItemMeta();
        ironMeta.setDisplayName(EpicSpawnersPlugin.getInstance().getLocale().getMessage("interface.boost.boostfor", "15"));
        ArrayList<String> ironLore = new ArrayList<>();
        ironLore.add(TextComponent.formatText("&7Costs &6&l" + Methods.getBoostCost(15, amount) + "."));
        ironMeta.setLore(ironLore);
        iron.setItemMeta(ironMeta);

        ItemStack diamond = new ItemStack(Material.DIAMOND);
        ItemMeta diamondMeta = diamond.getItemMeta();
        diamondMeta.setDisplayName(EpicSpawnersPlugin.getInstance().getLocale().getMessage("interface.boost.boostfor", "30"));
        ArrayList<String> diamondLore = new ArrayList<>();
        diamondLore.add(TextComponent.formatText("&7Costs &6&l" + Methods.getBoostCost(30, amount) + "."));
        diamondMeta.setLore(diamondLore);
        diamond.setItemMeta(diamondMeta);

        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta emeraldMeta = emerald.getItemMeta();
        emeraldMeta.setDisplayName(EpicSpawnersPlugin.getInstance().getLocale().getMessage("interface.boost.boostfor", "60"));
        ArrayList<String> emeraldLore = new ArrayList<>();
        emeraldLore.add(TextComponent.formatText("&7Costs &6&l" + Methods.getBoostCost(60, amount) + "."));
        emeraldMeta.setLore(emeraldLore);
        emerald.setItemMeta(emeraldMeta);

        inventory.setItem(10, coal);
        inventory.setItem(12, iron);
        inventory.setItem(14, diamond);
        inventory.setItem(16, emerald);

        inventory.setItem(0, Methods.getBackgroundGlass(true));
        inventory.setItem(1, Methods.getBackgroundGlass(true));
        inventory.setItem(2, Methods.getBackgroundGlass(false));
        inventory.setItem(6, Methods.getBackgroundGlass(false));
        inventory.setItem(7, Methods.getBackgroundGlass(true));
        inventory.setItem(8, Methods.getBackgroundGlass(true));
        inventory.setItem(9, Methods.getBackgroundGlass(true));
        inventory.setItem(17, Methods.getBackgroundGlass(true));
        inventory.setItem(18, Methods.getBackgroundGlass(true));
        inventory.setItem(19, Methods.getBackgroundGlass(true));
        inventory.setItem(20, Methods.getBackgroundGlass(false));
        inventory.setItem(24, Methods.getBackgroundGlass(false));
        inventory.setItem(25, Methods.getBackgroundGlass(true));
        inventory.setItem(26, Methods.getBackgroundGlass(true));

        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
        ItemStack skull = Arconix.pl().getApi().getGUI().addTexture(head, "http://textures.minecraft.net/texture/1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b");
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skull.setDurability((short) 3);
        skullMeta.setDisplayName(TextComponent.formatText("&6&l+1"));
        skull.setItemMeta(skullMeta);

        ItemStack head2 = new ItemStack(Material.PLAYER_HEAD, 1, (byte) 3);
        ItemStack skull2 = Arconix.pl().getApi().getGUI().addTexture(head2, "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23");
        SkullMeta skull2Meta = (SkullMeta) skull2.getItemMeta();
        skull2.setDurability((short) 3);
        skull2Meta.setDisplayName(TextComponent.formatText("&6&l-1"));
        skull2.setItemMeta(skull2Meta);

        if (amount != 1) {
            inventory.setItem(0, skull2);
        }
        if (amount < EpicSpawnersPlugin.getInstance().getConfig().getInt("Spawner Boosting.Max Multiplier For A Spawner Boost")) {
            inventory.setItem(8, skull);
        }
    }

    @Override
    protected void registerClickables() {
        resetClickables();
        registerClickable(0, (player, inventory, cursor, slot, type) -> {
            amount--;
            setUp();
            constructGUI();
        });

        registerClickable(8, (player, inventory, cursor, slot, type) -> {
            amount++;
            setUp();
            constructGUI();
        });

        registerClickable(10, (player, inventory, cursor, slot, type) ->
                ((ESpawner)spawner).purchaseBoost(player, 5, amount));

        registerClickable(12, (player, inventory, cursor, slot, type) ->
                ((ESpawner)spawner).purchaseBoost(player, 15, amount));

        registerClickable(14, (player, inventory, cursor, slot, type) ->
                ((ESpawner)spawner).purchaseBoost(player, 30, amount));

        registerClickable(16, (player, inventory, cursor, slot, type) ->
                ((ESpawner)spawner).purchaseBoost(player, 60, amount));
    }

    @Override
    protected void registerOnCloses() {

    }
}

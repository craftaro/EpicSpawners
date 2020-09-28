package com.songoda.epicspawners.gui;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Map;

public class GUIShopItem extends AbstractGUI {

    private final EpicSpawners plugin;
    private final AbstractGUI back;
    private final SpawnerData spawnerData;
    private int amount = 1;

    public GUIShopItem(EpicSpawners plugin, AbstractGUI abstractGUI, SpawnerData spawnerData, Player player) {
        super(player);
        this.plugin = plugin;
        this.back = abstractGUI;
        this.spawnerData = spawnerData;

        init(plugin.getLocale().getMessage("interface.shop.spawnershoptitle")
                .processPlaceholder("type", spawnerData.getCompiledDisplayName())
                .getMessage(), 45);
    }

    @Override
    public void constructGUI() {
        inventory.clear();
        resetClickables();
        registerClickables();

        int num = 0;
        while (num != 9) {
            inventory.setItem(num, Methods.getGlass());
            num++;
        }

        num = 36;
        while (num != 45) {
            inventory.setItem(num, Methods.getGlass());
            num++;
        }

        inventory.setItem(1, Methods.getBackgroundGlass(true));
        inventory.setItem(9, Methods.getBackgroundGlass(true));

        inventory.setItem(7, Methods.getBackgroundGlass(true));
        inventory.setItem(17, Methods.getBackgroundGlass(true));

        inventory.setItem(27, Methods.getBackgroundGlass(true));
        inventory.setItem(36, Methods.getBackgroundGlass(true));
        inventory.setItem(37, Methods.getBackgroundGlass(true));

        inventory.setItem(35, Methods.getBackgroundGlass(true));
        inventory.setItem(43, Methods.getBackgroundGlass(true));
        inventory.setItem(44, Methods.getBackgroundGlass(true));

        inventory.setItem(2, Methods.getBackgroundGlass(false));
        inventory.setItem(6, Methods.getBackgroundGlass(false));
        inventory.setItem(38, Methods.getBackgroundGlass(false));
        inventory.setItem(42, Methods.getBackgroundGlass(false));

        double price = spawnerData.getShopPrice() * amount;

        ItemStack it = new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), amount, (byte) 3);

        ItemStack item = EpicSpawners.getInstance().getHeads().addTexture(it, spawnerData);

        if (spawnerData.getDisplayItem() != null) {
            Material mat = spawnerData.getDisplayItem();
            if (!mat.equals(Material.AIR))
                item = new ItemStack(mat, 1);
        }

        item.setAmount(amount);
        ItemMeta itemmeta = item.getItemMeta();
        String name = spawnerData.getCompiledDisplayName();
        itemmeta.setDisplayName(name);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(plugin.getLocale().getMessage("interface.shop.buyprice")
                .processPlaceholder("cost", EconomyManager.formatEconomy(price)).getMessage());
        itemmeta.setLore(lore);
        item.setItemMeta(itemmeta);
        inventory.setItem(22, item);

        ItemStack plus = new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.LIME_STAINED_GLASS_PANE : Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 5);
        ItemMeta plusmeta = plus.getItemMeta();
        plusmeta.setDisplayName(plugin.getLocale().getMessage("interface.shop.add1").getMessage());
        plus.setItemMeta(plusmeta);
        if (item.getAmount() + 1 <= 64) {
            inventory.setItem(15, plus);

            registerClickable(15, (player, inventory2, cursor, slot, type) -> {
                this.amount = amount + 1;
                constructGUI();
            });
        }

        plus = new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.LIME_STAINED_GLASS_PANE : Material.valueOf("STAINED_GLASS_PANE"), 10, (short) 5);
        plusmeta.setDisplayName(plugin.getLocale().getMessage("interface.shop.add10").getMessage());
        plus.setItemMeta(plusmeta);
        if (item.getAmount() + 10 <= 64) {
            inventory.setItem(33, plus);

            registerClickable(33, (player, inventory2, cursor, slot, type) -> {
                this.amount = amount + 10;
                constructGUI();
            });
        }

        plus = new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.LIME_STAINED_GLASS_PANE : Material.valueOf("STAINED_GLASS_PANE"), 64, (short) 5);
        plusmeta.setDisplayName(plugin.getLocale().getMessage("interface.shop.set64").getMessage());
        plus.setItemMeta(plusmeta);
        if (item.getAmount() != 64) {
            inventory.setItem(25, plus);

            registerClickable(25, (player, inventory2, cursor, slot, type) -> {
                this.amount = 64;
                constructGUI();
            });
        }

        ItemStack minus = new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.RED_STAINED_GLASS_PANE : Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 14);
        ItemMeta minusmeta = minus.getItemMeta();
        minusmeta.setDisplayName(plugin.getLocale().getMessage("interface.shop.remove1").getMessage());
        minus.setItemMeta(minusmeta);
        if (item.getAmount() != 1) {
            inventory.setItem(11, minus);

            registerClickable(11, (player, inventory2, cursor, slot, type) -> {
                this.amount = amount - 1;
                constructGUI();
            });
        }

        minus = new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.RED_STAINED_GLASS_PANE : Material.valueOf("STAINED_GLASS_PANE"), 10, (short) 14);
        minusmeta.setDisplayName(plugin.getLocale().getMessage("interface.shop.remove10").getMessage());
        minus.setItemMeta(minusmeta);
        if (item.getAmount() - 10 >= 0) {
            inventory.setItem(29, minus);

            registerClickable(29, (player, inventory2, cursor, slot, type) -> {
                this.amount = amount - 10;
                constructGUI();
            });
        }

        minus = new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.RED_STAINED_GLASS_PANE : Material.valueOf("STAINED_GLASS_PANE"), 1, (short) 14);
        minusmeta.setDisplayName(plugin.getLocale().getMessage("interface.shop.set1").getMessage());
        minus.setItemMeta(minusmeta);
        if (item.getAmount() != 1) {
            inventory.setItem(19, minus);

            registerClickable(19, (player, inventory2, cursor, slot, type) -> {
                this.amount = 1;
                constructGUI();
            });
        }

        createButton(8, Settings.EXIT_ICON.getMaterial().getMaterial(), plugin.getLocale().getMessage("general.nametag.exit").getMessage());

        ItemStack skull = Methods.addTexture(new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                        ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3),
                "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23");

        createButton(0, skull, plugin.getLocale().getMessage("general.nametag.back").getMessage());

        createButton(40, Material.valueOf(Settings.BUY_ICON.getString()), plugin.getLocale().getMessage("general.nametag.confirm").getMessage());
    }

    @Override
    protected void registerClickables() {
        registerClickable(0, (player, inventory, cursor, slot, type) ->
                back.init(back.getSetTitle(), back.getInventory().getSize()));

        registerClickable(8, (player, inventory, cursor, slot, type) ->
                player.closeInventory());

        registerClickable(40, (player, inventory, cursor, slot, type) -> {
            confirm(player, amount);
            player.closeInventory();
        });
    }

    private void confirm(Player player, int amount) {
        if (!EconomyManager.isEnabled()) {
            player.sendMessage("Economy not enabled.");
            return;
        }

        double price = spawnerData.getShopPrice() * amount;
        if (!EconomyManager.hasBalance(player, price)) {
            plugin.getLocale().getMessage("event.shop.cannotafford").sendPrefixedMessage(player);
            return;
        }

        ItemStack item = spawnerData.toItemStack(amount);
        Map<Integer, ItemStack> overfilled = player.getInventory().addItem(item);
        for (ItemStack item2 : overfilled.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item2);
        }
        plugin.getLocale().getMessage("event.shop.purchasesuccess").sendPrefixedMessage(player);
        EconomyManager.withdrawBalance(player, price);
    }

    @Override
    protected void registerOnCloses() {

    }
}

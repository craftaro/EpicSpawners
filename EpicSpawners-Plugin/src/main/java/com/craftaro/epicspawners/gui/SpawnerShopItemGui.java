package com.craftaro.epicspawners.gui;

import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.gui.CustomizableGui;
import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.hooks.EconomyManager;
import com.craftaro.core.utils.ItemUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.epicspawners.spawners.spawner.SpawnerDataImpl;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.api.utils.HeadUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Map;

public class SpawnerShopItemGui extends CustomizableGui {

    private final EpicSpawners plugin;
    private final SpawnerTier spawnerTier;
    private final SpawnerData spawnerData;
    private final Gui back;
    private int amount = 1;

    public SpawnerShopItemGui(EpicSpawners plugin, SpawnerTier spawnerTier, Gui back) {
        super(plugin, "shopitem");
        setRows(9);
        this.plugin = plugin;
        this.spawnerTier = spawnerTier;
        this.spawnerData = spawnerTier.getSpawnerData();
        this.back = back;

        setTitle(plugin.getLocale().getMessage("interface.shop.spawnershoptitle")
                .processPlaceholder("type", spawnerTier.getCompiledDisplayName())
                .getMessage());

        paint();
    }

    public void paint() {
        reset();

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(XMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        // edges will be type 3
        mirrorFill("mirrorfill_1", 0, 2, true, true, glass3);
        mirrorFill("mirrorfill_2", 1, 1, true, true, glass3);

        // decorate corners with type 2
        mirrorFill("mirrorfill_3", 0, 0, true, true, glass2);
        mirrorFill("mirrorfill_4", 1, 0, true, true, glass2);
        mirrorFill("mirrorfill_5", 0, 1, true, true, glass2);

        double price = spawnerData.getShopPrice() * amount;

        ItemStack item = HeadUtils.getTexturedSkull(spawnerData);

        if (spawnerData.getDisplayItem() != null) {
            XMaterial mat = spawnerData.getDisplayItem();
            if (!mat.equals(XMaterial.AIR))
                item = mat.parseItem();
        }

        item.setAmount(amount);
        ItemMeta itemmeta = item.getItemMeta();
        String name = spawnerData.getFirstTier().getCompiledDisplayName();
        itemmeta.setDisplayName(name);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(plugin.getLocale().getMessage("interface.shop.buyprice")
                .processPlaceholder("cost", EconomyManager.formatEconomy(price)).getMessage());
        itemmeta.setLore(lore);
        item.setItemMeta(itemmeta);
        setItem("spawner",22, item);

        ItemStack plus = XMaterial.LIME_STAINED_GLASS_PANE.parseItem();
        ItemMeta plusmeta = plus.getItemMeta();
        plusmeta.setDisplayName(plugin.getLocale().getMessage("interface.shop.add1").getMessage());
        plus.setItemMeta(plusmeta);
        if (item.getAmount() + 1 <= 64) {
            setButton("add1", 15, plus, event -> {
                this.amount = amount + 1;
                paint();
            });
        }

        plus = XMaterial.LIME_STAINED_GLASS_PANE.parseItem();
        plus.setAmount(10);
        plusmeta.setDisplayName(plugin.getLocale().getMessage("interface.shop.add10").getMessage());
        plus.setItemMeta(plusmeta);
        if (item.getAmount() + 10 <= 64) {
            setButton("add10", 33, plus, event -> {
                this.amount = amount + 10;
                paint();
            });
        }

        plus = XMaterial.LIME_STAINED_GLASS_PANE.parseItem();
        plus.setAmount(64);
        plusmeta.setDisplayName(plugin.getLocale().getMessage("interface.shop.set64").getMessage());
        plus.setItemMeta(plusmeta);
        if (item.getAmount() != 64) {
            setButton("set64", 25, plus, event -> {
                this.amount = 64;
                paint();
            });
        }

        ItemStack minus = XMaterial.RED_STAINED_GLASS_PANE.parseItem();
        minus.setAmount(1);
        ItemMeta minusmeta = minus.getItemMeta();
        minusmeta.setDisplayName(plugin.getLocale().getMessage("interface.shop.remove1").getMessage());
        minus.setItemMeta(minusmeta);
        if (item.getAmount() != 1) {
            setButton("remove1", 11, minus, event -> {
                this.amount = amount - 1;
                paint();
            });
        }

        minus = XMaterial.RED_STAINED_GLASS_PANE.parseItem();
        minus.setAmount(10);
        minusmeta.setDisplayName(plugin.getLocale().getMessage("interface.shop.remove10").getMessage());
        minus.setItemMeta(minusmeta);
        if (item.getAmount() - 10 >= 0) {
            setButton("remove10", 29, minus, event -> {
                this.amount = amount - 10;
                paint();
            });
        }

        minus = XMaterial.RED_STAINED_GLASS_PANE.parseItem();
        minus.setAmount(1);
        minusmeta.setDisplayName(plugin.getLocale().getMessage("interface.shop.set1").getMessage());
        minus.setItemMeta(minusmeta);
        if (item.getAmount() != 1) {
            setButton("set1", 19, minus, event -> {
                this.amount = 1;
                paint();
            });
        }

        setButton("exit", 8, GuiUtils.createButtonItem(Settings.EXIT_ICON.getMaterial(),
                plugin.getLocale().getMessage("general.nametag.exit").getMessage()), event -> event.player.closeInventory());

        setButton("back", 0, GuiUtils.createButtonItem(ItemUtils.getCustomHead("3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"),
                plugin.getLocale().getMessage("general.nametag.back").getMessage()),
                event -> guiManager.showGUI(event.player, back));

        setButton("buy", 40, GuiUtils.createButtonItem(Settings.BUY_ICON.getMaterial(),
                plugin.getLocale().getMessage("general.nametag.confirm").getMessage()), event -> {
                    Player player = event.player;
                    confirm(player, amount);
                    player.closeInventory();
                }
        );
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

        ItemStack item = spawnerTier.toItemStack(amount);
        Map<Integer, ItemStack> overfilled = player.getInventory().addItem(item);
        for (ItemStack item2 : overfilled.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item2);
        }
        plugin.getLocale().getMessage("event.shop.purchasesuccess").sendPrefixedMessage(player);
        EconomyManager.withdrawBalance(player, price);
    }
}

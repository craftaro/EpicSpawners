package com.songoda.epicspawners.gui;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.ServerVersion;
import com.songoda.epicspawners.utils.gui.AbstractAnvilGUI;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIEditorGeneral extends AbstractGUI {

    private final EpicSpawners plugin;
    private final AbstractGUI back;
    private SpawnerData spawnerData;

    public GUIEditorGeneral(EpicSpawners plugin, AbstractGUI abstractGUI, SpawnerData spawnerData, Player player) {
        super(player);
        this.plugin = plugin;
        this.back = abstractGUI;
        this.spawnerData = spawnerData;

        init(Methods.compileName(spawnerData, 1, false) + " &8Settings.", 45);
    }

    @Override
    public void constructGUI() {
        inventory.clear();
        resetClickables();
        registerClickables();

        int num = 0;
        while (num != 45) {
            inventory.setItem(num, Methods.getGlass());
            num++;
        }

        createButton(0, Methods.addTexture(new ItemStack(plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3),
                "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"),
                plugin.getLocale().getMessage("general.nametag.back").getMessage());

        inventory.setItem(1, Methods.getBackgroundGlass(true));
        inventory.setItem(2, Methods.getBackgroundGlass(false));
        inventory.setItem(9, Methods.getBackgroundGlass(true));

        inventory.setItem(6, Methods.getBackgroundGlass(false));
        inventory.setItem(7, Methods.getBackgroundGlass(true));
        inventory.setItem(8, Methods.getBackgroundGlass(true));
        inventory.setItem(17, Methods.getBackgroundGlass(true));

        inventory.setItem(27, Methods.getBackgroundGlass(true));

        inventory.setItem(35, Methods.getBackgroundGlass(true));
        inventory.setItem(36, Methods.getBackgroundGlass(true));
        inventory.setItem(37, Methods.getBackgroundGlass(true));
        inventory.setItem(38, Methods.getBackgroundGlass(false));

        inventory.setItem(42, Methods.getBackgroundGlass(false));
        inventory.setItem(43, Methods.getBackgroundGlass(true));
        inventory.setItem(44, Methods.getBackgroundGlass(true));

        createButton(19, plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SUNFLOWER : Material.valueOf("DOUBLE_PLANT"), "&6&lShop Price",
                "&7Currently: &a" + spawnerData.getShopPrice(),
                "&7This is the price of the",
                "&7spawner in the shop.");

        createButton(20, Material.DIAMOND, "&6&lIn Shop",
                "&7Currently: &a" + spawnerData.isInShop(),
                "&7If this is true this spawner",
                "&7will show up in the shop GUI.");

        createButton(22, plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.FIRE_CHARGE : Material.valueOf("FIREWORK_CHARGE"), "&c&lSpawn On Fire",
                "&7Currently: &a" + spawnerData.isSpawnOnFire(),
                "&7If this is true this spawner",
                "&7will spawn entities on fire.");

        createButton(13, Material.HOPPER, "&5&lUpgradable",
                "&7Currently: &a" + spawnerData.isUpgradeable(),
                "&7Setting this to true will define",
                "&7upgradable.");

        createButton(24, plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SUNFLOWER : Material.valueOf("DOUBLE_PLANT"), "&6&lCustom Economy cost",
                "&7Currently: &a" + spawnerData.getUpgradeCostEconomy(),
                "&7This is the custom economy cost",
                "&7to upgrade this spawner.");

        createButton(25, plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.EXPERIENCE_BOTTLE : Material.valueOf("EXP_BOTTLE"), "&5&lCustom Experience cost",
                "&7Currently: &a" + spawnerData.getUpgradeCostExperience(),
                "&7This is the custom XP cost",
                "&7to upgrade this spawner.");

        createButton(30, plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.EXPERIENCE_BOTTLE : Material.valueOf("EXP_BOTTLE"), "&5&lCustom Goal",
                "&7Currently: &a" + spawnerData.getKillGoal(),
                "&7If this is set to anything",
                "&7but 0 the default kill goal",
                "&7will be adjusted for this spawner.");

        createButton(32, Material.DIAMOND, "&b&lPickup Cost",
                "&7Currently: &a" + spawnerData.getPickupCost(),
                "&7Setting this to anything but 0",
                "&7will allow you to charge players",
                "&7for breaking this type of spawner.");

        createButton(40, plugin.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.CLOCK : Material.valueOf("WATCH"), "&6&lTick Rate",
                "&7Currently: &a" + spawnerData.getTickRate(),
                "&7This is the default tick rate",
                "&7that your spawner will use",
                "&7to create its delay with.");
    }

    @Override
    protected void registerClickables() {
        registerClickable(13, (player, inventory, cursor, slot, type) -> {
            if (spawnerData.isUpgradeable())
                spawnerData.setUpgradeable(false);
            else
                spawnerData.setUpgradeable(true);
            constructGUI();
        });

        registerClickable(20, (player, inventory, cursor, slot, type) -> {
            if (spawnerData.isInShop())
                spawnerData.setInShop(false);
            else
                spawnerData.setInShop(true);
            constructGUI();
        });

        registerClickable(22, (player, inventory, cursor, slot, type) -> {
            if (spawnerData.isSpawnOnFire())
                spawnerData.setSpawnOnFire(false);
            else
                spawnerData.setSpawnOnFire(true);
            constructGUI();
        });

        registerClickable(19, (player, inventory, cursor, slot, type) -> {
            AbstractAnvilGUI gui = new AbstractAnvilGUI(player, event -> {
                if (Methods.isNumeric(event.getName())) {
                    spawnerData.setShopPrice(Double.parseDouble(event.getName()));
                } else {
                    player.sendMessage(Methods.formatText("&CYou must enter a number."));
                }
            });

            gui.setOnClose((player1, inventory1) -> init(setTitle, inventory.getSize()));

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Goal: Ex. 19.99");
            item.setItemMeta(meta);

            gui.setSlot(AbstractAnvilGUI.AnvilSlot.INPUT_LEFT, item);
            gui.open();

            player.sendMessage(Methods.formatText("&7Enter a sale price for &6" + Methods.getTypeFromString(spawnerData.getIdentifyingName()) + "&7."));
            player.sendMessage(Methods.formatText("&7Example: &619.99&7."));
        });

        registerClickable(24, (player, inventory, cursor, slot, type) -> {
            AbstractAnvilGUI gui = new AbstractAnvilGUI(player, event -> {
                if (Methods.isNumeric(event.getName())) {
                    spawnerData.setUpgradeCostEconomy(Double.parseDouble(event.getName()));
                } else {
                    player.sendMessage(Methods.formatText("&CYou must enter a number."));
                }
            });

            gui.setOnClose((player1, inventory1) -> init(setTitle, inventory.getSize()));

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Goal: Ex. 19.99");
            item.setItemMeta(meta);

            gui.setSlot(AbstractAnvilGUI.AnvilSlot.INPUT_LEFT, item);
            gui.open();

            player.sendMessage(Methods.formatText("&7Enter a custom eco cost for " + Methods.getTypeFromString(spawnerData.getIdentifyingName()) + "&7."));
            player.sendMessage(Methods.formatText("&7Use &60 &7to use the default cost."));
            player.sendMessage(Methods.formatText("&7Example: &619.99&7."));
        });

        registerClickable(25, (player, inventory, cursor, slot, type) -> {
            AbstractAnvilGUI gui = new AbstractAnvilGUI(player, event -> {
                if (Methods.isInt(event.getName())) {
                    spawnerData.setUpgradeCostExperience(Integer.parseInt(event.getName()));
                } else {
                    player.sendMessage(Methods.formatText("&CYou must enter a number."));
                }
            });

            gui.setOnClose((player1, inventory1) -> init(setTitle, inventory.getSize()));

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Goal: Ex. 25");
            item.setItemMeta(meta);

            gui.setSlot(AbstractAnvilGUI.AnvilSlot.INPUT_LEFT, item);
            gui.open();

            player.sendMessage(Methods.formatText("&7Enter a custom xp cost for " + Methods.getTypeFromString(spawnerData.getIdentifyingName()) + "&7."));
            player.sendMessage(Methods.formatText("&7Use &60 &7to use the default cost."));
            player.sendMessage(Methods.formatText("&7Example: &625&7."));
        });

        registerClickable(30, (player, inventory, cursor, slot, type) -> {
            AbstractAnvilGUI gui = new AbstractAnvilGUI(player, event -> {
                if (Methods.isInt(event.getName())) {
                    spawnerData.setKillGoal(Integer.parseInt(event.getName()));
                } else {
                    player.sendMessage(Methods.formatText("&CYou must enter a number."));
                }
            });

            gui.setOnClose((player1, inventory1) -> init(setTitle, inventory.getSize()));

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Goal: Ex. 5");
            item.setItemMeta(meta);

            gui.setSlot(AbstractAnvilGUI.AnvilSlot.INPUT_LEFT, item);
            gui.open();

            player.sendMessage(Methods.formatText("&7Enter a custom goal for " + Methods.getTypeFromString(spawnerData.getIdentifyingName()) + "&7."));
            player.sendMessage(Methods.formatText("&7Use &60 &7to use the default price."));
            player.sendMessage(Methods.formatText("&7Example: &35&6."));
        });

        registerClickable(32, (player, inventory, cursor, slot, type) -> {
            AbstractAnvilGUI gui = new AbstractAnvilGUI(player, event -> {
                if (Methods.isNumeric(event.getName())) {
                    spawnerData.setPickupCost(Double.parseDouble(event.getName()));
                } else {
                    player.sendMessage(Methods.formatText("&CYou must enter a number."));
                }
            });

            gui.setOnClose((player1, inventory1) -> init(setTitle, inventory.getSize()));

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Cost: Ex. 719.99");
            item.setItemMeta(meta);

            gui.setSlot(AbstractAnvilGUI.AnvilSlot.INPUT_LEFT, item);
            gui.open();

            player.sendMessage(Methods.formatText("&7Enter a pickup cost for " + Methods.getTypeFromString(spawnerData.getIdentifyingName()) + "&7."));
            player.sendMessage(Methods.formatText("&7Use &60 &7to disable."));
            player.sendMessage(Methods.formatText("&7Example: &719.99&6."));
            player.sendMessage(Methods.formatText("&7Example: &625&7."));
        });

        registerClickable(40, (player, inventory, cursor, slot, type) -> {
            AbstractAnvilGUI gui = new AbstractAnvilGUI(player, event ->
                    spawnerData.setTickRate(event.getName().trim()));

            gui.setOnClose((player1, inventory1) -> init(setTitle, inventory.getSize()));

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("Rate: Ex. 800:200");
            item.setItemMeta(meta);

            gui.setSlot(AbstractAnvilGUI.AnvilSlot.INPUT_LEFT, item);
            gui.open();

            player.sendMessage(Methods.formatText("&7Enter a tick rate min and max for " + Methods.getTypeFromString(spawnerData.getIdentifyingName()) + "&7."));
            player.sendMessage(Methods.formatText("&7Example: &3800:200&6."));
        });

        registerClickable(0, (player, inventory, cursor, slot, type) -> {
            back.init(back.getSetTitle(), back.getInventory().getSize());
            back.constructGUI();
        });
    }

    @Override
    protected void registerOnCloses() {

    }
}

package com.songoda.epicspawners.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.gui.AnvilGui;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.core.utils.NumberUtils;
import com.songoda.core.utils.PlayerUtils;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.spawner.SpawnerTier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EditorGeneralGui extends Gui {

    private final EpicSpawners plugin;
    private final Gui back;
    private final SpawnerTier spawnerTier;

    public EditorGeneralGui(EpicSpawners plugin, Gui back, SpawnerTier spawnerTier) {
        super(5);
        this.plugin = plugin;
        this.back = back;
        this.spawnerTier = spawnerTier;

        setDefaultItem(GuiUtils.getBorderItem(Settings.GLASS_TYPE_1.getMaterial().getItem()));
        setTitle(spawnerTier.getGuiTitle());
        setOnClose(event -> plugin.getSpawnerManager().saveSpawnerDataToFile());

        paint();
    }

    public void paint() {
        reset();

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(CompatibleMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(CompatibleMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        // edges will be type 3
        mirrorFill(0, 2, true, true, glass3);
        mirrorFill(1, 1, true, true, glass3);

        // decorate corners with type 2
        mirrorFill(0, 0, true, true, glass2);
        mirrorFill(1, 0, true, true, glass2);
        mirrorFill(0, 1, true, true, glass2);

        setButton(0, GuiUtils.createButtonItem(CompatibleMaterial.OAK_DOOR,
                plugin.getLocale().getMessage("general.nametag.back").getMessage()),
                (event) -> guiManager.showGUI(event.player, back));

        setButton(20, GuiUtils.createButtonItem(CompatibleMaterial.SUNFLOWER, TextUtils.formatText("&6&lIn Shop",
                "&7Currently: &a" + spawnerTier.getSpawnerData().isInShop(),
                "&7If this is true this spawner",
                "&7will show up in the shop GUI.")),
                event -> {
                    spawnerTier.getSpawnerData().setInShop(!spawnerTier.getSpawnerData().isInShop());
                    paint();
                });

        setButton(22, GuiUtils.createButtonItem(CompatibleMaterial.FIRE_CHARGE, TextUtils.formatText("&c&lSpawn On Fire",
                "&7Currently: &a" + spawnerTier.isSpawnOnFire(),
                "&7If this is true this spawner",
                "&7will spawn entities on fire.")),
                event -> {
                    spawnerTier.setSpawnOnFire(!spawnerTier.isSpawnOnFire());
                    paint();
                });

        setButton(13, GuiUtils.createButtonItem(CompatibleMaterial.HOPPER, TextUtils.formatText("&5&lUpgradable",
                "&7Currently: &a" + spawnerTier.getSpawnerData().isUpgradeable(),
                "&7Setting this to true will define",
                "&7upgradable.")),
                event -> {
                    spawnerTier.getSpawnerData().setUpgradeable(!spawnerTier.getSpawnerData().isUpgradeable());
                    paint();
                });

        setButton(24, GuiUtils.createButtonItem(CompatibleMaterial.SUNFLOWER, TextUtils.formatText("&6&lEconomy cost",
                "&7Currently: &a" + spawnerTier.getCostEconomy(),
                "&7This is the economy cost",
                "&7to upgrade or sell this spawner.")),
                event -> {
                    Player player = event.player;
                    AnvilGui gui = new AnvilGui(player, this);
                    gui.setTitle("Goal: Ex. 19.99");
                    gui.setAction(evnt -> {
                        String msg = gui.getInputText().trim();
                        if (NumberUtils.isNumeric(msg)) {
                            spawnerTier.setCostEconomy(Double.parseDouble(msg));
                            player.closeInventory();
                        } else {
                            player.sendMessage(TextUtils.formatText("&CYou must enter a number."));
                        }
                    }).setOnClose(e -> paint());
                    guiManager.showGUI(player, gui);

                    PlayerUtils.sendMessages(player, TextUtils.formatText("&7Enter a custom eco cost for " + spawnerTier.getIdentifyingName() + "&7.",
                            "&7Use &60 &7to use the default cost.",
                            "&7Example: &619.99&7."));
                });

        setButton(25, GuiUtils.createButtonItem(CompatibleMaterial.EXPERIENCE_BOTTLE, TextUtils.formatText("&5&lLevels cost",
                "&7Currently: &a" + spawnerTier.getCostLevels(),
                "&7This is the custom levels cost",
                "&7to upgrade this spawner.")),
                event -> {
                    Player player = event.player;
                    AnvilGui gui = new AnvilGui(player, this);
                    gui.setTitle("Goal: Ex. 25");
                    gui.setAction(evnt -> {
                        String msg = gui.getInputText().trim();
                        if (NumberUtils.isInt(msg)) {
                            spawnerTier.setCostLevels(Integer.parseInt(msg));
                            player.closeInventory();
                        } else {
                            player.sendMessage(TextUtils.formatText("&CYou must enter a number."));
                        }
                    }).setOnClose(e -> paint());
                    plugin.getGuiManager().showGUI(player, gui);

                    PlayerUtils.sendMessages(player, TextUtils.formatText("&7Enter a custom levels cost for " + spawnerTier.getIdentifyingName() + "&7.",
                            "&7Use &60 &7to use the default cost.",
                            "&7Example: &625&7."));
                });

        setButton(30, GuiUtils.createButtonItem(CompatibleMaterial.EXPERIENCE_BOTTLE, TextUtils.formatText("&5&lCustom Goal",
                "&7Currently: &a" + spawnerTier.getSpawnerData().getKillGoal(),
                "&7If this is set to anything",
                "&7but 0 the default kill goal",
                "&7will be adjusted for this spawner.")),
                event -> {
                    Player player = event.player;
                    AnvilGui gui = new AnvilGui(player, this);
                    gui.setTitle("Goal: Ex. 5");
                    gui.setAction(evnt -> {
                        String msg = gui.getInputText().trim();
                        if (NumberUtils.isInt(msg)) {
                            spawnerTier.getSpawnerData().setKillGoal(Integer.parseInt(msg));
                            player.closeInventory();
                        } else {
                            player.sendMessage(TextUtils.formatText("&CYou must enter a number."));
                        }
                    }).setOnClose(e -> paint());
                    plugin.getGuiManager().showGUI(player, gui);

                    PlayerUtils.sendMessages(player, TextUtils.formatText("&7Enter a custom goal for " + spawnerTier.getIdentifyingName() + "&7.",
                            "&7Use &60 &7to use the default price.",
                            "&7Example: &35&6."));
                });

        setButton(32, GuiUtils.createButtonItem(CompatibleMaterial.DIAMOND, TextUtils.formatText("&b&lPickup Cost",
                "&7Currently: &a" + spawnerTier.getPickupCost(),
                "&7Setting this to anything but 0",
                "&7will allow you to charge players",
                "&7for breaking this type of spawner.")),
                event -> {
                    Player player = event.player;
                    AnvilGui gui = new AnvilGui(player, this);
                    gui.setTitle("Goal: Ex. 719.99");
                    gui.setAction(evnt -> {
                        String msg = gui.getInputText().trim();
                        if (NumberUtils.isNumeric(msg)) {
                            spawnerTier.setPickupCost(Double.parseDouble(msg));
                            player.closeInventory();
                        } else {
                            player.sendMessage(TextUtils.formatText("&CYou must enter a number."));
                        }
                    }).setOnClose(e -> paint());
                    plugin.getGuiManager().showGUI(player, gui);

                    PlayerUtils.sendMessages(player, TextUtils.formatText("&7Enter a pickup cost for " + spawnerTier.getIdentifyingName() + "&7.",
                            "&7Use &60 &7to disable.",
                            "&7Example: &719.99&6.",
                            "&7Example: &625&7."));
                });

        setButton(40, GuiUtils.createButtonItem(CompatibleMaterial.CLOCK, TextUtils.formatText("&6&lTick Rate",
                "&7Currently: &a" + spawnerTier.getTickRate(),
                "&7This is the default tick rate",
                "&7that your spawner will use",
                "&7to create its delay with.")),
                event -> {
                    Player player = event.player;
                    AnvilGui gui = new AnvilGui(player, this);
                    gui.setTitle("Goal: Ex. 800:200");
                    gui.setAction(evnt -> {
                        spawnerTier.setTickRate(gui.getInputText().trim());
                        player.closeInventory();
                    }).setOnClose(e -> paint());
                    plugin.getGuiManager().showGUI(player, gui);

                    PlayerUtils.sendMessages(player, TextUtils.formatText("&7Enter a tick rate min and max for " + spawnerTier.getIdentifyingName() + "&7.",
                            "&7Example: &3800:200&6."));
                });
    }
}

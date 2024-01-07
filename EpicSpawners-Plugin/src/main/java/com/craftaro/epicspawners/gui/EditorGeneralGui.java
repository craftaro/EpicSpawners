package com.craftaro.epicspawners.gui;

import com.craftaro.core.gui.AnvilGui;
import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.utils.NumberUtils;
import com.craftaro.core.utils.PlayerUtils;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.settings.Settings;
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

        setDefaultItem(GuiUtils.getBorderItem(Settings.GLASS_TYPE_1.getMaterial().parseItem()));
        setTitle(spawnerTier.getGuiTitle());
        setOnClose(event -> plugin.getSpawnerManager().saveSpawnerDataToFile());

        paint();
    }

    public void paint() {
        reset();

        // decorate the edges
        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial(XMaterial.BLUE_STAINED_GLASS_PANE));
        ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial(XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE));

        // edges will be type 3
        mirrorFill(0, 2, true, true, glass3);
        mirrorFill(1, 1, true, true, glass3);

        // decorate corners with type 2
        mirrorFill(0, 0, true, true, glass2);
        mirrorFill(1, 0, true, true, glass2);
        mirrorFill(0, 1, true, true, glass2);

        setButton(0, GuiUtils.createButtonItem(XMaterial.OAK_DOOR,
                        this.plugin.getLocale().getMessage("general.nametag.back").getMessage()),
                (event) -> this.guiManager.showGUI(event.player, this.back));

        setButton(22, GuiUtils.createButtonItem(XMaterial.FIRE_CHARGE, TextUtils.formatText("&c&lSpawn On Fire",
                        "&7Currently: &a" + this.spawnerTier.isSpawnOnFire(),
                        "&7If this is true this spawner",
                        "&7will spawn entities on fire.")),
                event -> {
                    this.spawnerTier.setSpawnOnFire(!this.spawnerTier.isSpawnOnFire());
                    paint();
                });

        setButton(20, GuiUtils.createButtonItem(XMaterial.SUNFLOWER, TextUtils.formatText("&6&lEconomy cost",
                        "&7Currently: &a" + this.spawnerTier.getCostEconomy(),
                        "&7This is the economy cost",
                        "&7to upgrade or sell this spawner.")),
                event -> {
                    Player player = event.player;
                    AnvilGui gui = new AnvilGui(player, this);
                    gui.setTitle("Goal: Ex. 19.99");
                    gui.setAction(evnt -> {
                        String msg = gui.getInputText().trim();
                        if (NumberUtils.isNumeric(msg)) {
                            this.spawnerTier.setCostEconomy(Double.parseDouble(msg));
                            player.closeInventory();
                        } else {
                            player.sendMessage(TextUtils.formatText("&CYou must enter a number."));
                        }
                    }).setOnClose(e -> paint());
                    this.guiManager.showGUI(player, gui);

                    PlayerUtils.sendMessages(player, TextUtils.formatText("&7Enter a custom eco cost for " + this.spawnerTier.getIdentifyingName() + "&7.",
                            "&7Use &60 &7to use the default cost.",
                            "&7Example: &619.99&7."));
                });

        setButton(24, GuiUtils.createButtonItem(XMaterial.EXPERIENCE_BOTTLE, TextUtils.formatText("&5&lLevels cost",
                        "&7Currently: &a" + this.spawnerTier.getCostLevels(),
                        "&7This is the custom levels cost",
                        "&7to upgrade this spawner.")),
                event -> {
                    Player player = event.player;
                    AnvilGui gui = new AnvilGui(player, this);
                    gui.setTitle("Goal: Ex. 25");
                    gui.setAction(evnt -> {
                        String msg = gui.getInputText().trim();
                        if (NumberUtils.isInt(msg)) {
                            this.spawnerTier.setCostLevels(Integer.parseInt(msg));
                            player.closeInventory();
                        } else {
                            player.sendMessage(TextUtils.formatText("&CYou must enter a number."));
                        }
                    }).setOnClose(e -> paint());
                    this.plugin.getGuiManager().showGUI(player, gui);

                    PlayerUtils.sendMessages(player, TextUtils.formatText("&7Enter a custom levels cost for " + this.spawnerTier.getIdentifyingName() + "&7.",
                            "&7Use &60 &7to use the default cost.",
                            "&7Example: &625&7."));
                });

        setButton(30, GuiUtils.createButtonItem(XMaterial.EXPERIENCE_BOTTLE, TextUtils.formatText("&5&lKill Drop Goal",
                        "&7Currently: &a" + this.spawnerTier.getSpawnerData().getKillDropGoal(),
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
                            this.spawnerTier.getSpawnerData().setKillDropGoal(Integer.parseInt(msg));
                            player.closeInventory();
                        } else {
                            player.sendMessage(TextUtils.formatText("&CYou must enter a number."));
                        }
                    }).setOnClose(e -> paint());
                    this.plugin.getGuiManager().showGUI(player, gui);

                    PlayerUtils.sendMessages(player, TextUtils.formatText("&7Enter a custom goal for " + this.spawnerTier.getIdentifyingName() + "&7.",
                            "&7Use &60 &7to use the default price.",
                            "&7Example: &35&6."));
                });

        setButton(31, GuiUtils.createButtonItem(XMaterial.EXPERIENCE_BOTTLE, TextUtils.formatText("&5&lKill Drop Chance",
                        "&7Currently: &a" + this.spawnerTier.getSpawnerData().getKillDropGoal(),
                        "&7If this is set to anything",
                        "&7but 0 the default kill chance",
                        "&7will be adjusted for this spawner.")),
                event -> {
                    Player player = event.player;
                    AnvilGui gui = new AnvilGui(player, this);
                    gui.setTitle("Goal: Ex. 5");
                    gui.setAction(evnt -> {
                        String msg = gui.getInputText().trim();
                        if (NumberUtils.isInt(msg)) {
                            this.spawnerTier.getSpawnerData().setKillDropGoal(Integer.parseInt(msg));
                            player.closeInventory();
                        } else {
                            player.sendMessage(TextUtils.formatText("&CYou must enter a number."));
                        }
                    }).setOnClose(e -> paint());
                    this.plugin.getGuiManager().showGUI(player, gui);

                    PlayerUtils.sendMessages(player, TextUtils.formatText("&7Enter a custom goal for " + this.spawnerTier.getIdentifyingName() + "&7.",
                            "&7Use &60 &7to use the default price.",
                            "&7Example: &35&6."));
                });

        setButton(32, GuiUtils.createButtonItem(XMaterial.DIAMOND, TextUtils.formatText("&b&lPickup Cost",
                        "&7Currently: &a" + this.spawnerTier.getPickupCost(),
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
                            this.spawnerTier.setPickupCost(Double.parseDouble(msg));
                            player.closeInventory();
                        } else {
                            player.sendMessage(TextUtils.formatText("&CYou must enter a number."));
                        }
                    }).setOnClose(e -> paint());
                    this.plugin.getGuiManager().showGUI(player, gui);

                    PlayerUtils.sendMessages(player, TextUtils.formatText("&7Enter a pickup cost for " + this.spawnerTier.getIdentifyingName() + "&7.",
                            "&7Use &60 &7to disable.",
                            "&7Example: &719.99&6.",
                            "&7Example: &625&7."));
                });

        setButton(40, GuiUtils.createButtonItem(XMaterial.CLOCK, TextUtils.formatText("&6&lTick Rate",
                        "&7Currently: &a" + this.spawnerTier.getTickRate(),
                        "&7This is the default tick rate",
                        "&7that your spawner will use",
                        "&7to create its delay with.")),
                event -> {
                    Player player = event.player;
                    AnvilGui gui = new AnvilGui(player, this);
                    gui.setTitle("Goal: Ex. 800:200");
                    gui.setAction(evnt -> {
                        this.spawnerTier.setTickRate(gui.getInputText().trim());
                        player.closeInventory();
                    }).setOnClose(e -> paint());
                    this.plugin.getGuiManager().showGUI(player, gui);

                    PlayerUtils.sendMessages(player, TextUtils.formatText("&7Enter a tick rate min and max for " + this.spawnerTier.getIdentifyingName() + "&7.",
                            "&7Example: &3800:200&6."));
                });
    }
}

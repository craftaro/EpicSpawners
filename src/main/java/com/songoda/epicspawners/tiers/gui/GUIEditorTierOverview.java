package com.songoda.epicspawners.tiers.gui;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.gui.AnvilGui;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.tiers.models.TierData;
import com.songoda.epicspawners.tiers.models.TierType;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * This GUI is the menu that opens
 * when a player clicks on a egg tier type
 *
 * Made by CodePunisher with <3
 */
public class GUIEditorTierOverview extends AbstractGUI
{
    private final EpicSpawners plugin;                          // Instance of plugin
    private final GUIEditorTierSelector tierSelector;           // Tier selector class instance
    private final Player player;                                // Player instance
    private final TierType tierType;                            // Tiertype object
    private final ServerVersion version;                        // Server version
    private int page;                                           // Page counter
    private final int maxAmount;                                // Max items per page
    private int maxPages;                                       // Max pages allowed

    /** Setting instance variables and opening gui for player */
    public GUIEditorTierOverview(EpicSpawners plugin, GUIEditorTierSelector tierSelector, Player player, TierType tierType) {
        super(player);
        this.plugin = plugin;
        this.tierSelector = tierSelector;
        this.player = player;
        this.tierType = tierType;
        this.version = ServerVersion.getServerVersion();
        this.page = 1;
        this.maxAmount = 32;
        updateMaxPages();

        // Creating gui, adding items to it, and then opening it
        openMenu();
    }

    /** Getters for instance variables */
    public EpicSpawners getPlugin() { return this.plugin; }
    public GUIEditorTierSelector getTierSelector() { return this.tierSelector; }
    public Player getPlayer() { return this.player; }
    public TierType getTierType() { return this.tierType; }
    public ServerVersion getVersion() { return this.version; }
    public int getPage() { return this.page; }
    public int getMaxAmount() { return this.maxAmount; }
    public int getMaxPages() { return this.maxPages; }
    public void updateMaxPages() {
        double itemCount = getTierType().getTierData().size() <= 0 ? 1 : getTierType().getTierData().size();
        double maxPerPage = getMaxAmount();
        double maxTotal = itemCount / maxPerPage;
        this.maxPages = maxTotal > (int) maxTotal ? (int) maxTotal + 1 : (int) maxTotal;

        // Updating page counter
        if (getPage() > this.maxPages)
            this.page--;
    }

    // Opens menu yaheet
    public void openMenu() { init("Editor: " + getTierType().getPrettyName() + " (Page " + getPage() + ")", 54); }

    /**
     * This methods creates buttons
     * and adds items to the gui
     */
    @Override
    public void constructGUI() {
        getInventory().clear();
        resetClickables();
        registerClickables();

        // List to loop through
        List<TierData> data = plugin.getTierDataManager().listFromPageCount(getPage(), getMaxAmount(), getTierType().getTierData());

        if (!data.isEmpty()) {
            // Adding tier types to the GUI
            int place = 9;
            for (TierData tierData : data) {
                place += place == 16 || place == 35 ? 2 : 1; // Making sure items get placed in GUI correctly

                // Creating button for item stack
                ItemStack item = getVersion().isAtLeast(ServerVersion.V1_13) ? new ItemStack(Material.SPAWNER) : new ItemStack(Material.valueOf("MOB_SPAWNER"));
                createButton(place, item, "&aSpawner Level: " + tierData.getLevel(), "&7Left-Click to &e&lAdd Items", "&7Right-Click to &c&lRemove");

                // Registering clickable (because spawner egg is clickable)
                registerClickable(place, ((player1, inventory1, cursor, slot, type) -> {
                    if (type.name().contains("LEFT")) {
                        new GUIEditorTierData(getPlugin(), this, getPlayer(), tierData);
                    } else if (type.name().contains("RIGHT")) {
                        // Just in case they accidentally remove it
                        AnvilGui gui = new AnvilGui(getPlayer());
                        gui.setTitle("Are you sure?");
                        gui.setAction(event -> {
                            String input = gui.getInputText().trim();

                            if (input.equalsIgnoreCase("yes")) {
                                // Removing that bitch yo
                                getTierType().getTierData().remove(tierData);
                                plugin.getLocale().newMessage("&7Level &e" + tierData.getLevel() + " &7has been removed!").sendPrefixedMessage(getPlayer());
                                updateMaxPages();
                            } else {
                                plugin.getLocale().newMessage("&7Level &e" + tierData.getLevel() + " &7was not removed!").sendPrefixedMessage(getPlayer());
                            }

                            openMenu();
                        });

                        // Showing anvil gui to player
                        plugin.getGuiManager().showGUI(getPlayer(), gui);
                    }
                }));
            }
        }

        // Item stacks and shit you know
        ItemStack glassType2 = Methods.getBackgroundGlass(true), glassType3 = Methods.getBackgroundGlass(false), glassType4 = Methods.getGlass();

        // Blue glass
        inventory.setItem(0, glassType2);
        inventory.setItem(1, glassType2);
        inventory.setItem(7, glassType2);
        inventory.setItem(9, glassType2);
        inventory.setItem(17, glassType2);
        inventory.setItem(36, glassType2);
        inventory.setItem(44, glassType2);
        inventory.setItem(45, glassType2);
        inventory.setItem(46, glassType2);
        inventory.setItem(52, glassType2);
        inventory.setItem(53, glassType2);

        // Light blue glass
        inventory.setItem(2, glassType3);
        inventory.setItem(6, glassType3);
        inventory.setItem(47, glassType3);
        inventory.setItem(51, glassType3);

        // Black glass
        inventory.setItem(3, glassType4);
        inventory.setItem(4, glassType4);
        inventory.setItem(5, glassType4);
        inventory.setItem(48, glassType4);
        inventory.setItem(49, glassType4);
        inventory.setItem(50, glassType4);

        // Took code from your other GUI here
        createButton(8, Methods.addTexture(new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3),
                "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"),
                plugin.getLocale().getMessage("general.nametag.back").getMessage());

        if (getPage() != 1) {
            createButton(46, Methods.addTexture(new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3),
                    "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"),
                    plugin.getLocale().getMessage("general.nametag.back").getMessage());
        }

        if (getPage() != getMaxPages()) {
            createButton(52, Methods.addTexture(new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3),
                    "http://textures.minecraft.net/texture/1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b"),
                    plugin.getLocale().getMessage("general.nametag.next").getMessage());
        }

        createButton(48, Material.PAPER, "&7Type: &6" + getTierType().getType(), "&7Click to &e&lUpdate");

        // If they're inside the global menu (option for enabling/disabling
        if (getTierType().getEntityType().equalsIgnoreCase("global"))
            createButton(49, Material.PAPER, "&7Enabled: &6" + plugin.getTierYMLManager().isGlobalEnabled(), "&7Click to &e&lUpdate");

        createButton(50, Material.PAPER, "&9&lNew Level");
    }

    /** Registering the clickable buttons */
    @Override
    protected void registerClickables() {
        // Back button (for the top)
        registerClickable(8, (player, inventory, cursor, slot, type) -> getTierSelector().openMenu());

        // Type editor button (Switching between all and random)
        registerClickable(48, ((player1, inventory1, cursor, slot, type) -> {
            String stringType = getTierType().getType();
            getTierType().setType(stringType.equalsIgnoreCase("all") ? "RANDOM" : "ALL");
            createButton(48, Material.PAPER, "&7Type: &6" + getTierType().getType(), "&7Click to &e&lUpdate");
        }));

        // Global enabled option
        registerClickable(49, ((player1, inventory1, cursor, slot, type) -> {
            boolean enabled = plugin.getTierYMLManager().isGlobalEnabled();
            plugin.getTierYMLManager().setGlobalEnabled(!enabled);
            createButton(49, Material.PAPER, "&7Enabled: &6" + !enabled, "&7Click to &e&lUpdate");
        }));

        // New level option (Doing this via an anvil + player input)
        registerClickable(50, ((player1, inventory1, cursor, slot, type) -> {
            AnvilGui gui = new AnvilGui(getPlayer());
            gui.setTitle("Enter a level:");
            gui.setAction(event -> {
                String input = gui.getInputText().trim();

                try
                {
                    // Level of input
                    int level = Integer.parseInt(input);

                    // Tier type object
                    TierType tierType = getTierType();

                    // Making sure it doesn't already exist
                    if (!tierType.tierDataHasValue(level)) {
                        // Making sure user doesn't try to enter 0 or less
                        if (level > 0) {
                            List<ItemStack> items = new ArrayList<>();
                            TierData tierData = new TierData(level, items);

                            // Updating tier data
                            tierType.getTierData().add(tierData);
                            plugin.getLocale().newMessage("&e" + level + " &7has been added!").sendPrefixedMessage(getPlayer());
                            updateMaxPages();
                            openMenu();
                            return;
                        } else {
                            plugin.getLocale().newMessage("&7Common! Are you really trying to set up a level &e" + level + " &7spawner?!").sendPrefixedMessage(getPlayer());
                        }
                    } else {
                        plugin.getLocale().newMessage("&7That level already exists for &e" + tierType.getPrettyName() + "&7!").sendPrefixedMessage(getPlayer());
                    }
                }
                catch (Exception e) { // If the user doesn't enter a number
                    plugin.getLocale().newMessage("&e" + input + " &7is not a valid number!").sendPrefixedMessage(getPlayer());
                }

                // Closing menu if they don't get it right
                getPlayer().closeInventory();
            });

            // Showing anvil gui to player
            plugin.getGuiManager().showGUI(getPlayer(), gui);
        }));


        // Back button (for bottom aka the pages)
        registerClickable(46, (player, inventory, cursor, slot, type) -> {
            if (getPage() == 1) return;
            this.page--;
            openMenu();
        });

        // Next page button
        registerClickable(52, (player, inventory, cursor, slot, type) -> {
            if (getPage() == getMaxPages()) return;
            this.page++;
            openMenu();
        });
    }

    /** Saving file */
    @Override
    protected void registerOnCloses() {
        registerOnClose(((player1, inventory1) -> {
            // Updating file
            plugin.getTierYMLManager().updateTierTypeInFile(tierType);
        }));
    }
}

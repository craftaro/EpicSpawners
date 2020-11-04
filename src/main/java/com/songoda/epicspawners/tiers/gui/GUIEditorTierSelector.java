package com.songoda.epicspawners.tiers.gui;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.gui.AnvilGui;
import com.songoda.epicspawners.EpicSpawners;

import com.songoda.epicspawners.tiers.models.TierType;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.gui.AbstractGUI;;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

/**
 * This is my first time attempting to
 * use your GUI API, so bare with me
 *
 * This is the GUI that opens when
 * the player types /Es tierseditor
 *
 * Made by CodePunisher with <3
 */
public class GUIEditorTierSelector extends AbstractGUI
{
    private final EpicSpawners plugin;                                  // Instance of plugin
    private final List<TierType> tierTypes;                             // List of all tier types
    private final Player player;                                        // Player instance
    private final ServerVersion version;                                // Version of the server
    private int page;                                                   // The current page the user is on
    private final int maxAmount;                                        // The max amount of items per page
    private int maxPages;                                               // The max amount of pages allowed

    /**
     * This sets all values and opens the GUI for the player
     *
     * @param plugin epicspawners plugin
     * @param player the player who is viewing the gui
     */
    public GUIEditorTierSelector(EpicSpawners plugin, Player player) {
        super(player);
        this.plugin = plugin;
        this.tierTypes = plugin.getTierDataManager().getTierTypes();
        this.player = player;
        this.version = ServerVersion.getServerVersion();
        this.page = 1;
        this.maxAmount = 32;
        updateMaxPages();

        // Setting up and opening menu for player
        openMenu();
    }

    /** Getters for instance variables **/
    public EpicSpawners getPlugin() { return this.plugin; }
    public List<TierType> getTierTypes() { return this.tierTypes; }
    public Player getPlayer() { return this.player; }
    public ServerVersion getVersion() { return this.version; }
    public int getPage() { return this.page; }
    public int getMaxAmount() { return this.maxAmount; }
    public int getMaxPages() { return this.maxPages; }
    public void updateMaxPages() {
        // I'm subtracing one here to take "global" into consideration
        double itemCount = getTierTypes().size() <= 0 ? 1 : getTierTypes().size();
        double maxPerPage = getMaxAmount();
        double maxTotal = itemCount / maxPerPage;
        this.maxPages = maxTotal > (int) maxTotal ? (int) maxTotal + 1 : (int) maxTotal;

        // Updating page counter
        if (getPage() > this.maxPages)
            this.page--;
    }

    // Got tired of running this over and over again
    // So I made a method to just do it for me
    public void openMenu() { init("Tiers Editor (Page " + getPage() + ")", 54); }

    /**
     * This method basically just
     * creates buttons and adds the
     * item stacks to the gui
     */
    @Override
    public void constructGUI() {
        getInventory().clear();
        resetClickables();
        registerClickables();

        // Help button (just basically tells them what the fuck this is)
        createButton(7, Material.BOOK, "&f&lInfo",
                    "&7&oHere you can add Tier", "&7&oTypes for certain mobs!",
                         "", "&7&oEach Tier Type allowes you", "&7&oto specify mob drops",
                         "&7&obased on spawner level!", "", "&7&oEditing specific tier types",
                         "&7&ooverrides the global setting!", "", "&7&o\"All\" drops every item", "&7&o\"Random\" drops a random item");

        // Exit button
        createButton(8, getVersion().isAtLeast(ServerVersion.V1_13) ? Material.OAK_DOOR : Material.valueOf("WOOD_DOOR"), plugin.getLocale().getMessage("general.nametag.exit").getMessage());

        // List to loop through
        List<TierType> types = plugin.getTierDataManager().listFromPageCount(getPage(), getMaxAmount(), getTierTypes());

        if (!types.isEmpty()) {
            // Adding tier types to the GUI
            int place = 9;
            for (TierType tierType : types) {
                place += place == 16 || place == 35 ? 2 : 1; // Making sure items get placed in GUI correctly

                // Creating button for item stack
                String typeName = tierType.getEntityType();
                ItemStack icon = new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3);

                /** IMPORTANT: I had to edit your heads class to allow me to do this */
                icon = plugin.getHeads().addTexture(icon, typeName);

                // Beautiful button
                createButton(place, icon, "&e" + tierType.getPrettyName(), "&7Left-Click to &a&lEdit", "&7Right-Click to &c&lRemove");

                // Registering clickable (because each egg is clickable)
                registerClickable(place, ((player1, inventory1, cursor, slot, type) -> {
                    if (type.name().contains("LEFT")) {
                        new GUIEditorTierOverview(getPlugin(), this, getPlayer(), tierType);
                    } else if (type.name().contains("RIGHT")) {
                        // Just in case they accidentally remove it
                        AnvilGui gui = new AnvilGui(getPlayer());
                        gui.setTitle("Are you sure?");
                        gui.setAction(event -> {
                            String input = gui.getInputText().trim();

                            if (input.equalsIgnoreCase("yes")) {
                                getTierTypes().remove(tierType);
                                plugin.getTierDataManager().removeTierType(typeName);
                                plugin.getTierYMLManager().removeTypeFromFile(typeName);
                                plugin.getLocale().newMessage("&e" + tierType.getPrettyName() + " &7has been removed!").sendPrefixedMessage(getPlayer());
                                updateMaxPages();
                            } else{
                                plugin.getLocale().newMessage("&e" + tierType.getPrettyName() + " &7was not removed!").sendPrefixedMessage(getPlayer());
                            }

                            // Re-opening menu for player
                            openMenu();
                        });

                        // Showing anvil gui to player
                        plugin.getGuiManager().showGUI(getPlayer(), gui);
                    }
                }));
            }
        }

        // I'd do a loop here, but honestly, it doesn't really make sense to
        ItemStack glassType2 = Methods.getBackgroundGlass(true), glassType3 = Methods.getBackgroundGlass(false), glassType4 = Methods.getGlass();

        // Blue glass
        inventory.setItem(0, glassType2);
        inventory.setItem(1, glassType2);
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

        // Took code from your other GUI here
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

        createButton(48, Material.PAPER, "&9&lGlobal Settings");
        createButton(49, Material.PAPER, "&7Feature: &e" + plugin.getTierYMLManager().isEnabled(), "&7Click to &e&lUpdate");
        createButton(50, Material.PAPER, "&9&lNew Tier");
    }

    /**
     * This method registers the gui
     * clickables for the user
     */
    @Override
    protected void registerClickables() {
        // Exit button
        registerClickable(8, (player, inventory, cursor, slot, type) -> player.closeInventory());

        // Global settings button
        registerClickable(48, ((player1, inventory1, cursor, slot, type) -> {
            new GUIEditorTierOverview(getPlugin(), this, getPlayer(), plugin.getTierDataManager().getTierType("GLOBAL"));
        }));

        // Editing enabled option
        registerClickable(49, ((player1, inventory1, cursor, slot, type) -> {
            boolean enabled = plugin.getTierYMLManager().isEnabled();
            plugin.getTierYMLManager().setEnabled(!enabled);
            createButton(49, Material.PAPER, "&7Feature: &e" + !enabled, "&7Click to &e&lUpdate");
        }));

        // New tier button (Doing this via an anvil + player input)
        registerClickable(50, ((player1, inventory1, cursor, slot, type) -> {
            AnvilGui gui = new AnvilGui(getPlayer());
            gui.setTitle("Enter Mob-Type:");
            gui.setAction(event -> {
                String input = gui.getInputText().toUpperCase().trim();
                EntityType entityType = EntityType.fromName(input);

                if (entityType != null) {
                    // Making sure it doesn't already exist
                    if (!plugin.getTierDataManager().containsEntity(input)) {
                        HashMap<Integer, List<ItemStack>> map = new HashMap<>();
                        TierType tierType = new TierType(input, "random", map);

                        if (input.contains(" "))
                            input = input.replaceAll(" ", "_");

                        getTierTypes().add(tierType);
                        plugin.getTierDataManager().addTierType(input, tierType);
                        plugin.getTierYMLManager().addTypeToFile(input, tierType);
                        plugin.getLocale().newMessage("&e" + tierType.getPrettyName() + " &7has been added as a Tier type!").sendPrefixedMessage(getPlayer());
                        updateMaxPages();
                        openMenu();
                        return;
                    } else {
                        plugin.getLocale().newMessage("&7That Mob-Type already exists as a Tier type!").sendPrefixedMessage(getPlayer());
                    }
                } else {
                    plugin.getLocale().newMessage("&7That was not a valid Mob-Type! Example Mob-Type: &eCow").sendPrefixedMessage(getPlayer());
                }

                // Closing menu if they don't get it right
                getPlayer().closeInventory();
            });

            // Showing anvil gui to player
            plugin.getGuiManager().showGUI(getPlayer(), gui);
        }));

        // Back button
        registerClickable(46, (player, inventory, cursor, slot, type) -> {
            if (getPage() == 1) return;
            this.page--;
            openMenu();
        });

        // Next button
        registerClickable(52, (player, inventory, cursor, slot, type) -> {
            if (getPage() == getMaxPages()) return;
            this.page++;
            openMenu();
        });
    }

    /**
     * Saving enabled feature on close
     * Running async
     */
    @Override
    protected void registerOnCloses() {
        // If user edited the enabled feature
        registerOnClose(((player1, inventory1) -> {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, ()-> {
                // Updating enabled feature in the file
                plugin.getTierYMLManager().getTierConfig().set("enabled", plugin.getTierYMLManager().isEnabled());
                plugin.getTierYMLManager().getTierConfig().save();

                if (!plugin.getTierYMLManager().isEnabled()) {
                    plugin.getTierSQLManager().removeTableValues();
                }
            });
        }));
    }
}

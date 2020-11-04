package com.songoda.epicspawners.tiers.gui;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.tiers.models.TierData;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.gui.AbstractGUI;
import com.songoda.epicspawners.utils.gui.Range;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * This GUI is simply for directly
 * adding items to a specific tier type
 *
 * Made by CodePunisher with <3
 */
public class GUIEditorTierData extends AbstractGUI
{
    private final EpicSpawners plugin;                                      // Plugin instance
    private final GUIEditorTierOverview editorOverView;                     // GUIEditorOverview instance
    private final Player player;                                            // Player instance
    private final TierData tierData;                                        // Tier data object
    private final ServerVersion version;                                    // Server version

    /** Setting instance variables and opening gui for player */
    public GUIEditorTierData(EpicSpawners plugin, GUIEditorTierOverview editorOverView, Player player, TierData tierData) {
        super(player);
        this.plugin = plugin;
        this.editorOverView = editorOverView;
        this.player = player;
        this.tierData = tierData;
        this.version = ServerVersion.getServerVersion();

        // Opening menu
        openMenu();
    }

    /** Getters + a couple methods */
    public EpicSpawners getPlugin() { return this.plugin; }
    public GUIEditorTierOverview getEditorOverView() { return this.editorOverView; }
    public Player getPlayer() { return this.player; }
    public TierData getTierData() { return this.tierData; }
    public ServerVersion getVersion() { return this.version; }

    // Opens menu yaheet
    public void openMenu() { init("Spawner Level " + getTierData().getLevel(), 54); }

    /** Setting items inside GUI */
    @Override
    public void constructGUI() {
        getInventory().clear();
        resetClickables();
        registerClickables();

        if (!tierData.getItems().isEmpty()) {
            // Adding item stacks to the GUI
            int place = 9;
            for (ItemStack itemStack : tierData.getItems()) {
                place += place == 16 || place == 35 ? 2 : 1; // Making sure items get placed in GUI correctly
                inventory.setItem(place, itemStack);
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

        // Back button
        createButton(8, Methods.addTexture(new ItemStack(ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.PLAYER_HEAD : Material.valueOf("SKULL_ITEM"), 1, (byte) 3),
                "http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"),
                plugin.getLocale().getMessage("general.nametag.back").getMessage());
    }

    /** Registering the clickable buttons */
    @Override
    protected void registerClickables() {
        // Setting draggable option
        addDraggable(new Range(10, 16, null, true), true);
        addDraggable(new Range(18, 26, null, true), true);
        addDraggable(new Range(27, 35, null, true), true);
        addDraggable(new Range(37, 43, null, true), true);

        // Back button (for the top)
        registerClickable(8, (player, inventory, cursor, slot, type) -> getEditorOverView().openMenu());
    }

    /** Updating stack list and saving to file */
    @Override
    protected void registerOnCloses() {
        registerOnClose(((player1, inventory1) -> {
            // Gonna update the stack list with this one
            List<ItemStack> newList = new ArrayList<>();

            for (int i = 0; i < inventory.getSize(); i++) {
                // Looping through draggable section
                if ((i >= 10 && i <= 16) || (i >= 18 && i <= 26) ||
                    (i >= 27 && i <= 35) || (i >= 37 && i <= 43)) {
                    ItemStack item = inventory.getItem(i);

                    if (item != null && item.getType() != Material.AIR)
                        newList.add(item);
                }
            }

            // Only updating file if I have to
            if (!newList.isEmpty()) {
                getTierData().setItems(newList);
                plugin.getTierYMLManager().updateTierTypeInFile(getEditorOverView().getTierType());
            }
        }));
    }
}

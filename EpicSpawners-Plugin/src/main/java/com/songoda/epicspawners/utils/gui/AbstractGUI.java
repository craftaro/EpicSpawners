package com.songoda.epicspawners.utils.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.songoda.epicspawners.utils.Range;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractGUI implements GUI {

    private static boolean listenersInitialized = false;

    private boolean initialized = false;

    private final Inventory inventory;
    private final Clickable[] clickActions;
    private final Map<Range, Clickable> rangedClickActions = new HashMap<>();

    /**
     * Construct a new GUI based on an {@link InventoryType}'s default size, as well
     * as a provided name
     *
     * @param type the type of inventory to create
     * @param inventoryName the name of the GUI
     */
    protected AbstractGUI(InventoryType type, String inventoryName) {
        this.inventory = Bukkit.createInventory(new GUIHolder(), type, inventoryName);
        this.clickActions = new Clickable[inventory.getSize()];
    }

    /**
     * Construct a new Chest GUI with a particular size and a provided name
     *
     * @param size the size of the GUI to create (must be a multiple of 9)
     * @param inventoryName the name of the inventory
     */
    protected AbstractGUI(int size, String inventoryName) {
        this.inventory = Bukkit.createInventory(new GUIHolder(), size, inventoryName);
        this.clickActions = new Clickable[inventory.getSize()];
    }

    /**
     * Construct a new GUI based on an {@link InventoryType}'s default size
     *
     * @param type the type of inventory to create
     */
    protected AbstractGUI(InventoryType type) {
        this.inventory = Bukkit.createInventory(new GUIHolder(), type);
        this.clickActions = new Clickable[inventory.getSize()];
    }

    /**
     * Construct a new Chest GUI with a particular size
     *
     * @param size the size of the GUI to create (must be a multiple of 9)
     */
    protected AbstractGUI(int size) {
        this.inventory = Bukkit.createInventory(new GUIHolder(), size);
        this.clickActions = new Clickable[inventory.getSize()];
    }

    @Override
    public final Inventory getInventory() {
        if (!initialized) {
            this.init();
        }

        return inventory;
    }

    @Override
    public final void openFor(Player player) {
        Preconditions.checkNotNull(player, "Cannot open inventory for null player");

        if (!initialized) {
            this.init();
        }

        player.openInventory(inventory);
    }

    @Override
    public final boolean hasClickAction(int slot) {
        if (!initialized) {
            this.init();
        }

        return slot >= 0 && slot < inventory.getSize() && clickActions[slot] != null;
    }

    @Override
    public void dispose() {
        Arrays.setAll(clickActions, null);
        this.rangedClickActions.clear();
        this.inventory.getViewers().forEach(HumanEntity::closeInventory);
    }

    /**
     * Register a new {@link Clickable} action for the provided slot
     *
     * @param slot the slot at which a clickable action should be registered
     * @param action the action to register
     */
    protected final void registerClickableObject(int slot, Clickable action) {
        Preconditions.checkArgument(slot >= 0 || slot < inventory.getSize(), "Action slots must be between 0 and %s", inventory.getSize());
        Preconditions.checkNotNull(action, "Cannot reigster a null action");

        this.clickActions[slot] = action;
    }

    /**
     * Register a new {@link Clickable} action for the provided range. If the
     * range overlaps that of an existing range or either bounds of the range exceed
     * the size of the inventory, an exception will be thrown. Additionally, for any
     * actions registered for an individual slot within the registered range, they
     * will be removed and replaced with the action registered in this range.
     *
     * @param from the beginning of the range (inclusive). Must be less than "to"
     * @param to the end of the range (inclusive). Must be greater than "from"
     * @param action the action to register
     */
    protected final void registerClickableRange(int from, int to, Clickable action) {
        Preconditions.checkArgument(from >= 0 && from < inventory.getSize(), "Action slots must be between 0 and %s", inventory.getSize());
        Preconditions.checkArgument(to >= 0 && to < inventory.getSize(), "Action slots must be between 0 and %s", inventory.getSize());
        Preconditions.checkArgument(from < to, "From must be less than to");
        Preconditions.checkNotNull(action, "Cannot register a null action");

        for (Range range : rangedClickActions.keySet()) {
            if (range.isWithin(from) || range.isWithin(to)) {
                throw new IllegalStateException("Range overlaps that of another range (Existing range: " + range.getLow() + " - " + range.getHigh() + ")");
            }
        }

        for (int i = from; i < to; i++) {
            this.clickActions[i] = null;
        }

        this.rangedClickActions.put(Range.from(from, to), action);
    }

    /**
     * Remove an action from the specified slot. This does not affect ranged actions
     *
     * @param slot the slot to remove the action from
     */
    protected final void removeActionFrom(int slot) {
        Preconditions.checkArgument(slot >= 0 || slot < inventory.getSize(), "Cannot remove action from invalid slot. Must be between 0 and %s", inventory.getSize());
        this.clickActions[slot] = null;
    }

    /**
     * Remove a ranged action such that it includes the specified slot
     *
     * @param slot the slot within the range for which to remove the action from
     * @return true if successful, false if no changes were made
     */
    protected final boolean removeRangedActionForSlot(int slot) {
        Preconditions.checkArgument(slot >= 0 || slot < inventory.getSize(), "Cannot remove action from invalid slot. Must be between 0 and %s", inventory.getSize());

        Iterator<Range> ranges = rangedClickActions.keySet().iterator();
        while (ranges.hasNext()) {
            Range range = ranges.next();
            if (range.isWithin(slot)) {
                ranges.remove();
                return true;
            }
        }

        return false;
    }

    /**
     * Initialize the inventory's items. It is from here that the GUI's inventory should have ItemStacks added
     *
     * @param inventory the GUI's underlying inventory
     */
    protected abstract void initInventoryItems(Inventory inventory);

    /**
     * Initialize the inventory's clickable objects. It is from here that {@link #registerClickableObject(int, Clickable)}
     * should be called in GUI implementations
     */
    protected abstract void initClickableObjects();

    /**
     * Called the moment before the GUI has been opened by a player
     *
     * @param player the player opening the GUI
     */
    protected void onGUIOpen(Player player) { }

    /**
     * Called the moment before the GUI has been closed by the player
     *
     * @param player the player closing the GUI
     */
    protected void onGUIClose(Player player) { }

    private void init() {
        Preconditions.checkArgument(listenersInitialized, "The GUI class has not yet been initialized. Invoke GUI#initializeListeners(JavaPlugin)");
        Preconditions.checkArgument(!initialized, "This GUI has already been initialized");

        this.initInventoryItems(inventory);
        this.initClickableObjects();
        this.initialized = true;
    }

    private boolean invokeClickAction(Player player, Inventory inventory, ItemStack cursor, int slot, ClickType type) {
        Preconditions.checkNotNull(player, "A null player cannot click the inventory");
        Preconditions.checkNotNull(inventory, "A null inventory cannot be clicked");
        Preconditions.checkArgument(slot >= 0 && slot < inventory.getSize(), "The slot exceeds the size limitations of the inventory (0 - %s)", inventory.getSize());
        Preconditions.checkNotNull(type, "A null click type cannot be performed");

        Clickable action = clickActions[slot];
        if (action == null) {
            for (Entry<Range, Clickable> rangedAction : rangedClickActions.entrySet()) {
                if (!rangedAction.getKey().isWithin(slot)) continue;
                action = rangedAction.getValue();
            }
        }

        if (action == null) return false; // If still no action, just give up

        action.click(player, inventory, cursor, slot, type);
        return true;
    }

    /**
     * Initialize the GUI class and register its internal event listeners. This MUST be called before any
     * GUI functionality / implementations may be used. If this method has already been called and the
     * GUI API has been initialized, the call will fail silently
     *
     * @param plugin the plugin initializing the GUI listeners
     */
    public static void initializeListeners(JavaPlugin plugin) {
        if (listenersInitialized) return;

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onClickGUI(InventoryClickEvent event) {
                Inventory inventory = event.getClickedInventory();
                AbstractGUI gui = (AbstractGUI) getGUIFromInventory(inventory);
                if (gui == null) return;

                event.setCancelled(true);
                gui.invokeClickAction((Player) event.getWhoClicked(), inventory, event.getCursor(), event.getRawSlot(), event.getClick());
            }

            @EventHandler
            public void onOpenGUI(InventoryOpenEvent event) {
                Inventory inventory = event.getInventory();
                AbstractGUI gui = (AbstractGUI) getGUIFromInventory(inventory);
                if (gui == null) return;

                gui.onGUIOpen((Player) event.getPlayer());
            }

            @EventHandler
            public void onCloseGUI(InventoryCloseEvent event) {
                Inventory inventory = event.getInventory();
                AbstractGUI gui = (AbstractGUI) getGUIFromInventory(inventory);
                if (gui == null) return;

                gui.onGUIClose((Player) event.getPlayer());
            }

            private GUI getGUIFromInventory(Inventory inventory) {
                if (inventory == null) return null;

                InventoryHolder holder = inventory.getHolder();
                if (!(holder instanceof GUIHolder)) return null;

                return ((GUIHolder) holder).getGUI();
            }
        }, plugin);

        listenersInitialized = true;
    }


    /**
     * Represents an InventoryHolder implementation for GUIs
     */
    private class GUIHolder implements InventoryHolder {

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        /**
         * Get the GUI instance associated with this holder
         *
         * @return the associated GUI
         */
        public GUI getGUI() {
            return AbstractGUI.this;
        }

    }

}
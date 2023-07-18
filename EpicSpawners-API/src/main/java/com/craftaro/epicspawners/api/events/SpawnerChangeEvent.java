package com.craftaro.epicspawners.api.events;

import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a spawner has been changed. This includes changes such as stack size as
 * well as a change in {@link SpawnerTier}
 */
public class SpawnerChangeEvent extends SpawnerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    public enum ChangeType {
        STACK_SIZE, SPAWNER_DATA;
    }

    private boolean canceled = false;

    private final int stackSize, oldStackSize;
    private final SpawnerTier spawnerTier, oldSpawnerTier;
    private final ChangeType type;

    public SpawnerChangeEvent(Player player, PlacedSpawner spawner, int stackSize, int oldStackSize) {
        super(player, spawner);

        this.stackSize = stackSize;
        this.oldStackSize = oldStackSize;
        this.spawnerTier = oldSpawnerTier = null;
        this.type = ChangeType.STACK_SIZE;
    }

    public SpawnerChangeEvent(Player player, PlacedSpawner spawner, SpawnerTier data, SpawnerTier oldSpawnerTier) {
        super(player, spawner);

        this.spawnerTier = data;
        this.oldSpawnerTier = oldSpawnerTier;
        this.stackSize = oldStackSize = spawner.getStackSize();
        this.type = ChangeType.SPAWNER_DATA;
    }

    /**
     * Get the new stack size of the spawner after this event completes. If this
     * event is not to do with stack size changing, this method simply returns
     * the spawner's current stack size
     *
     * @return the new stack size
     */
    public int getStackSize() {
        return stackSize;
    }

    /**
     * Get the old stack size of the spawner from before this event was called. If
     * this event is not to do with stack size changing, this method simply returns
     * the spawner's current stack size
     *
     * @return the old stack size
     */
    public int getOldStackSize() {
        return oldStackSize;
    }

    /**
     * Get the new spawner data after this event completes. If this event is not
     * to do with the spawner data changing, this method simply returns null
     *
     * @return the new spawner data
     */
    public SpawnerTier getSpawnerTier() {
        return spawnerTier;
    }

    /**
     * Get the old spawner data from before this event was called. If this event
     * is not to do with the spawner data changing, this method simply returns null
     *
     * @return the old spawner data
     */
    public SpawnerTier getOldSpawnerTier() {
        return oldSpawnerTier;
    }

    /**
     * Get the type of change performed in this event
     *
     * @return the change type
     */
    public ChangeType getChange() {
        return type;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public void setCancelled(boolean canceled) {
        this.canceled = canceled;
    }

    /**
     * The spawner's multiplier (stack size)
     *
     * @return the stack size
     * @deprecated see {@link #getStackSize()}
     */
    @Deprecated
    public int getCurrentMulti() {
        return stackSize;
    }

    /**
     * Get the old stack size
     *
     * @return the old stack size
     * @deprecated see {@link #getOldStackSize()}
     */
    @Deprecated
    public int getOldMulti() {
        return oldStackSize;
    }

    /**
     * Get the new spawner data type
     *
     * @return the spawner type
     * @deprecated see {@link #getSpawnerTier()}
     */
    @Deprecated
    public String getType() {
        return spawnerTier.getIdentifyingName();
    }

    /**
     * Get the old spawner data type
     *
     * @return the spawner type
     * @deprecated see {@link #getOldSpawnerTier()}
     */
    @Deprecated
    public String getOldType() {
        return oldSpawnerTier.getIdentifyingName();
    }

}

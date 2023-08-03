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
        STACK_SIZE, SPAWNER_DATA
    }

    private boolean canceled = false;

    private final int stackSize, oldStackSize;
    private final SpawnerTier spawnerTier, oldSpawnerTier;
    private final ChangeType type;

    public SpawnerChangeEvent(Player who, PlacedSpawner spawner, int stackSize, int oldStackSize) {
        super(who, spawner);

        this.stackSize = stackSize;
        this.oldStackSize = oldStackSize;
        this.spawnerTier = this.oldSpawnerTier = null;
        this.type = ChangeType.STACK_SIZE;
    }

    public SpawnerChangeEvent(Player who, PlacedSpawner spawner, SpawnerTier data, SpawnerTier oldSpawnerTier) {
        super(who, spawner);

        this.spawnerTier = data;
        this.oldSpawnerTier = oldSpawnerTier;
        this.stackSize = this.oldStackSize = spawner.getStackSize();
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
        return this.stackSize;
    }

    /**
     * Get the old stack size of the spawner from before this event was called. If
     * this event is not to do with stack size changing, this method simply returns
     * the spawner's current stack size
     *
     * @return the old stack size
     */
    public int getOldStackSize() {
        return this.oldStackSize;
    }

    /**
     * Get the new spawner data after this event completes. If this event is not
     * to do with the spawner data changing, this method simply returns null
     *
     * @return the new spawner data
     */
    public SpawnerTier getSpawnerTier() {
        return this.spawnerTier;
    }

    /**
     * Get the old spawner data from before this event was called. If this event
     * is not to do with the spawner data changing, this method simply returns null
     *
     * @return the old spawner data
     */
    public SpawnerTier getOldSpawnerTier() {
        return this.oldSpawnerTier;
    }

    /**
     * Get the type of change performed in this event
     *
     * @return the change type
     */
    public ChangeType getChange() {
        return this.type;
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
        return this.canceled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.canceled = cancel;
    }

    /**
     * The spawner's multiplier (stack size)
     *
     * @return the stack size
     * @deprecated see {@link #getStackSize()}
     */
    @Deprecated
    public int getCurrentMulti() {
        return this.stackSize;
    }

    /**
     * Get the old stack size
     *
     * @return the old stack size
     * @deprecated see {@link #getOldStackSize()}
     */
    @Deprecated
    public int getOldMulti() {
        return this.oldStackSize;
    }

    /**
     * Get the new spawner data type
     *
     * @return the spawner type
     * @deprecated see {@link #getSpawnerTier()}
     */
    @Deprecated
    public String getType() {
        return this.spawnerTier.getIdentifyingName();
    }

    /**
     * Get the old spawner data type
     *
     * @return the spawner type
     * @deprecated see {@link #getOldSpawnerTier()}
     */
    @Deprecated
    public String getOldType() {
        return this.oldSpawnerTier.getIdentifyingName();
    }
}

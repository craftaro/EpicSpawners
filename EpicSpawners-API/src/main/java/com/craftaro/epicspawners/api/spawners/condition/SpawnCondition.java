package com.craftaro.epicspawners.api.spawners.condition;


import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;

import java.util.function.Predicate;

/**
 * A predicate on which to check whether a {@link PlacedSpawner} should
 * be permitted to perform a spawn or not.
 */
public interface SpawnCondition {
    /**
     * Get the name of this spawn condition.
     *
     * @return the name of this condition
     */
    String getName();

    /**
     * Get a short description of what this condition imposes on
     * a spawner. This should be brief enough to display to players.
     *
     * @return the description for this condition
     */
    String getDescription();

    /**
     * Check whether the provided spawner meets this condition or not.
     *
     * @param spawner the spawner to check
     * @return true if condition is met, false otherwise
     */
    boolean isMet(PlacedSpawner spawner);

    /**
     * Get this SpawnCondition instance as a {@link Predicate}
     *
     * @return this condition as a predicate
     */
    default Predicate<PlacedSpawner> asPredicate() {
        return this::isMet;
    }
}

package com.craftaro.epicspawners.api.particles;

/**
 * All possible types of particles supported by EpicSpawners
 */
public enum ParticleType {
    /**
     * A small explosion effect. Not to be confused with the smoke particle
     * effect. This is a poorly named constant. This constant's true particle
     * represents that of "EXPLOSION_NORMAL"
     */
    SMOKE("EXPLOSION_NORMAL"),

    /**
     * A spell effect used frequently by potions
     */
    SPELL("SPELL"),

    /**
     * A redstone dust effect
     */
    REDSTONE("REDSTONE"),

    /**
     * A little flame effect
     */
    FIRE("FLAME"),

    NONE(null);

    private final String effect;

    ParticleType(String effect) {
        this.effect = effect;
    }

    /**
     * Get the name of the particle effect displayed by this type
     *
     * @return the particle name
     */
    public String getEffect() {
        return this.effect;
    }
}

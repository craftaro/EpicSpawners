package com.songoda.epicspawners.api.particles;

import org.bukkit.Particle;

/**
 * All possible types of particles supported by EpicSpawners
 */
public enum ParticleType {

    /**
     * A small explosion effect. Not to be confused with the smoke particle
     * effect. This is a poorly named constant. This constant's true particle
     * represents that of "EXPLOSION_NORMAL"
     */
    SMOKE(Particle.EXPLOSION_NORMAL),

    /**
     * A spell effect used frequently by potions
     */
    SPELL(Particle.SPELL),

    /**
     * A redstone dust effect
     */
    REDSTONE(Particle.REDSTONE),

    /**
     * A little flame effect
     */
    FIRE(Particle.FLAME),

    NONE(null);

    private final Particle effect;

    private ParticleType(Particle effect) {
        this.effect = effect;
    }

    /**
     * Get the name of the particle effect displayed by this type
     * 
     * @return the particle name
     */
    public Particle getEffect() {
        return effect;
    }

}

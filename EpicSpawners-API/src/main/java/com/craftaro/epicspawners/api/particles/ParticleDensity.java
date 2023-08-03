package com.craftaro.epicspawners.api.particles;

/**
 * The density of a particle effect displayed by EpicSpawners
 */
public enum ParticleDensity {
    /**
     * Very light particle effects. The lowest possible density
     */
    LIGHT(8, 2, 1),

    /**
     * A medium density. The third-highest density with average performance
     */
    NORMAL(15, 5, 3),

    /**
     * An excessive amount of particles.
     * The second-highest density with questionable performance
     */
    EXCESSIVE(21, 9, 7),

    /**
     * The highest density with a potential to greatly impact server performance
     * relative to other densities
     */
    MAD(30, 13, 15);

    private final int spawnerSpawn;
    private final int entitySpawn;
    private final int effect;

    ParticleDensity(int spawnerSpawn, int entitySpawn, int effect) {
        this.spawnerSpawn = spawnerSpawn;
        this.entitySpawn = entitySpawn;
        this.effect = effect;
    }

    /**
     * Get the amount of particles to be displayed when a spawner spawns
     * an objects
     *
     * @return the amount of particles
     */
    public int getSpawnerSpawn() {
        return this.spawnerSpawn;
    }

    /**
     * Get the amount of particles to be displayed when a spawner spawns
     * entities
     *
     * @return the amount of particles
     */
    public int getEntitySpawn() {
        return this.entitySpawn;
    }

    /**
     * Get the amount of particles to be displayed when a spawner has a special
     * effect playing around it
     *
     * @return the amount of particles
     * @see ParticleEffect
     */
    public int getEffect() {
        return this.effect;
    }
}

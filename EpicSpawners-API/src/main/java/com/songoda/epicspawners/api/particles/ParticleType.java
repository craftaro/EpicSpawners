package com.songoda.epicspawners.api.particles;

public enum ParticleType {

    SMOKE("EXPLOSION_NORMAL"),
    SPELL("SPELL"),
    REDSTONE("REDSTONE"),
    FIRE("FLAME");

    String effect;

    ParticleType(String effect) {
        this.effect = effect;
    }

    public String getEffect() {
        return effect;
    }
}

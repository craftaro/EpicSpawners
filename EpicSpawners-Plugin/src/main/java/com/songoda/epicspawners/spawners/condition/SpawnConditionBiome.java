package com.songoda.epicspawners.spawners.condition;

import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.condition.SpawnCondition;

import org.apache.commons.lang.WordUtils;
import org.bukkit.block.Biome;

public class SpawnConditionBiome implements SpawnCondition {

    private final Set<Biome> biomes;

    public SpawnConditionBiome(Biome... biomes) {
        this.biomes = (biomes.length > 1) ? EnumSet.of(biomes[0], biomes) : EnumSet.noneOf(Biome.class);
    }

    public SpawnConditionBiome(Set<Biome> biomes) {
        this.biomes = biomes;
    }

    @Override
    public String getName() {
        return "biome";
    }

    @Override
    public String getDescription() {
        return (biomes.size() == 1)
                ? "Spawner must be in a " + getFriendlyBiomeName() + " biome."
                : "Spawner must be in one of (" + biomes.size() + ") biomes.";
    }

    @Override
    public boolean isMet(Spawner spawner) {
        return biomes.contains(spawner.getLocation().getBlock().getBiome());
    }

    private String getFriendlyBiomeName() {
        return WordUtils.capitalizeFully(Iterables.get(biomes, 0).name().replace("_", " "));
    }

    public Set<Biome> getBiomes() {
        return biomes;
    }
}
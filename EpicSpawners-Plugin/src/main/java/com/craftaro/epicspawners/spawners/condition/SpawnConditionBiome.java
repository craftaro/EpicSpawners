package com.craftaro.epicspawners.spawners.condition;

import com.craftaro.third_party.org.apache.commons.text.WordUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.condition.SpawnCondition;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.google.common.collect.Iterables;
import org.bukkit.block.Biome;

import java.util.EnumSet;
import java.util.Set;

public class SpawnConditionBiome implements SpawnCondition {
    private final Set<Biome> biomes;

    public SpawnConditionBiome(Biome... biomes) {
        this.biomes = (biomes.length >= 1) ? EnumSet.of(biomes[0], biomes) : EnumSet.noneOf(Biome.class);
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
        return (this.biomes.size() == 1)
                ? EpicSpawners.getInstance().getLocale().getMessage("interface.spawner.conditionBiome1")
                .processPlaceholder("biome", getFriendlyBiomeName()).getMessage()
                : EpicSpawners.getInstance().getLocale().getMessage("interface.spawner.conditionBiome2")
                .getMessage();
    }

    @Override
    public boolean isMet(PlacedSpawner spawner) {
        return this.biomes.contains(spawner.getLocation().getBlock().getBiome());
    }

    private String getFriendlyBiomeName() {
        return WordUtils.capitalizeFully(Iterables.get(this.biomes, 0).name().replace("_", " "));
    }

    public Set<Biome> getBiomes() {
        return this.biomes;
    }
}

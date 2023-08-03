package com.craftaro.epicspawners.spawners.spawner.option;

import com.craftaro.core.math.MathUtils;
import com.craftaro.core.utils.EntityUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.boosts.types.Boosted;
import com.craftaro.epicspawners.api.events.SpawnerSpawnEvent;
import com.craftaro.epicspawners.api.particles.ParticleType;
import com.craftaro.epicspawners.api.spawners.condition.SpawnCondition;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerStack;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.api.spawners.spawner.option.SpawnOption;
import com.craftaro.epicspawners.api.spawners.spawner.option.SpawnOptionType;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.epicspawners.spawners.condition.SpawnConditionNearbyEntities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class SpawnOptionEntity implements SpawnOption {
    private final EntityType[] types;

    private final EpicSpawners plugin = EpicSpawners.getInstance();

    private static final boolean mcmmo = Bukkit.getPluginManager().isPluginEnabled("mcMMO");

    public SpawnOptionEntity(EntityType... types) {
        this.types = types;
    }

    public SpawnOptionEntity(Collection<EntityType> entities) {
        this(entities.toArray(new EntityType[0]));
    }

    @Override
    public void spawn(SpawnerTier data, SpawnerStack stack, PlacedSpawner spawner) {
        Location location = spawner.getLocation();
        location.add(.5, .5, .5);
        if (location.getWorld() == null) {
            return;
        }

        String highLow = Settings.RANDOM_LOW_HIGH.getString();
        String[] randomLowHigh = highLow.split(":");
        boolean isSame = randomLowHigh.length == 1 || randomLowHigh[0].equals(randomLowHigh[1]);

        // Get the amount of entities to spawn per spawner in the stack.
        int spawnCount = 0;
        for (int i = 0; i < stack.getStackSize(); i++) {
            int randomAmt = isSame ? Integer.parseInt(randomLowHigh[0]) :
                    ThreadLocalRandom.current().nextInt(Integer.parseInt(randomLowHigh[0]),
                            Integer.parseInt(randomLowHigh[1]));

            String equation = Settings.SPAWNER_SPAWN_EQUATION.getString();
            equation = equation.replace("{RAND}", Integer.toString(randomAmt));
            equation = equation.replace("{STACK_SIZE}", Integer.toString(stack.getStackSize()));

            spawnCount += MathUtils.eval(equation, "EpicSpawners (Mobs Spawned Per Single Spawn) Equation");
        }

        // Get the max entities allowed around a spawner.
        int maxEntitiesAllowed = 0;
        for (SpawnCondition spawnCondition : data.getConditions()) {
            if (spawnCondition instanceof SpawnConditionNearbyEntities) {
                maxEntitiesAllowed = ((SpawnConditionNearbyEntities) spawnCondition).getMax();
            }
        }

        // Should we skip the max entity amount on first spawn?
        if (spawner.getSpawnCount() == 0 && Settings.IGNORE_MAX_ON_FIRST_SPAWN.getBoolean()) {
            maxEntitiesAllowed = Integer.MAX_VALUE;
        }

        // Get the amount of entities around the spawner.
        int size = SpawnConditionNearbyEntities.getEntitiesAroundSpawner(location, true);

        // Calculate the amount of entities to spawn.
        spawnCount = Math.min(maxEntitiesAllowed - size, spawnCount) + spawner.getBoosts().stream().mapToInt(Boosted::getAmountBoosted).sum();

        ParticleType particleType = data.getEntitySpawnParticle();

        int amountSpawned = spawner.spawn(spawnCount,
                particleType != ParticleType.NONE ? null : particleType.getEffect(),
                new HashSet<>(Arrays.asList(data.getSpawnBlocks())), entity -> {
                    SpawnerSpawnEvent event = new SpawnerSpawnEvent(entity, spawner);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        entity.remove();
                        return false;
                    }
                    if (data.isSpawnOnFire()) {
                        entity.setFireTicks(160);
                    }

                    entity.setMetadata("ESData", new FixedMetadataValue(this.plugin, data.getSpawnerData().getIdentifyingName()));
                    entity.setMetadata("ESTier", new FixedMetadataValue(this.plugin, data.getIdentifyingName()));

                    if (mcmmo) {
                        entity.setMetadata("mcMMO: Spawned Entity", new FixedMetadataValue(this.plugin, true));
                    }

                    if (Settings.NO_AI.getBoolean()) {
                        EntityUtils.setUnaware(entity);
                    }

                    this.plugin.getSpawnManager().addUnnaturalSpawn(entity.getUniqueId());
                    return true;
                }, this.types);

        spawner.setSpawnCount(spawner.getSpawnCount() + amountSpawned);
        EpicSpawners.getInstance().getDataManager().save(spawner);
    }

    @Override
    public SpawnOptionType getType() {
        return SpawnOptionType.ENTITY;
    }

    @Override
    public int hashCode() {
        return 31 * (this.types != null ? Arrays.hashCode(this.types) : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SpawnOptionEntity)) {
            return false;
        }

        return Arrays.equals(this.types, ((SpawnOptionEntity) obj).types);
    }
}

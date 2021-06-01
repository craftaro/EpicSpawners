package com.songoda.epicspawners.spawners.spawner.option;

import com.songoda.core.math.MathUtils;
import com.songoda.core.utils.EntityUtils;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.api.events.SpawnerSpawnEvent;
import com.songoda.epicspawners.boost.types.Boosted;
import com.songoda.epicspawners.particles.ParticleType;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.condition.SpawnCondition;
import com.songoda.epicspawners.spawners.condition.SpawnConditionNearbyEntities;
import com.songoda.epicspawners.spawners.spawner.PlacedSpawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.spawners.spawner.SpawnerTier;
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
        if (location.getWorld() == null) return;

        String[] randomLowHigh = Settings.RANDOM_LOW_HIGH.getString().split(":");

        // Get the amount of entities to spawn per spawner in the stack.
        int spawnCount = 0;
        for (int i = 0; i < stack.getStackSize(); i++) {
            int randomAmt = ThreadLocalRandom.current().nextInt(Integer.parseInt(randomLowHigh[0]), Integer.parseInt(randomLowHigh[1]));

            String equation = Settings.SPAWNER_SPAWN_EQUATION.getString();
            equation = equation.replace("{RAND}", Integer.toString(randomAmt));
            equation = equation.replace("{STACK_SIZE}", Integer.toString(stack.getStackSize()));

            spawnCount += MathUtils.eval(equation,
                    "EpicSpawners (Mobs Spawned Per Single Spawn) Equation");
        }

        // Get the max entities allowed around a spawner.
        int maxEntitiesAllowed = 0;
        for (SpawnCondition spawnCondition : data.getConditions()) {
            if (spawnCondition instanceof SpawnConditionNearbyEntities)
                maxEntitiesAllowed = ((SpawnConditionNearbyEntities) spawnCondition).getMax();
        }

        // Should we skip the max entity amount on first spawn?
        if (spawner.getSpawnCount() == 0 && Settings.IGNORE_MAX_ON_FIRST_SPAWN.getBoolean())
            maxEntitiesAllowed = Integer.MAX_VALUE;

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
                    if (data.isSpawnOnFire()) entity.setFireTicks(160);

                    entity.setMetadata("ESData", new FixedMetadataValue(plugin, data.getSpawnerData().getIdentifyingName()));
                    entity.setMetadata("ESTier", new FixedMetadataValue(plugin, data.getIdentifyingName()));

                    if (mcmmo)
                        entity.setMetadata("mcMMO: Spawned Entity", new FixedMetadataValue(plugin, true));

                    if (Settings.NO_AI.getBoolean())
                        EntityUtils.setUnaware(entity);

                    plugin.getSpawnManager().addUnnaturalSpawn(entity.getUniqueId());
                    return true;
                }, types);

        spawner.setSpawnCount(spawner.getSpawnCount() + amountSpawned);
        EpicSpawners.getInstance().getDataManager().updateSpawner(spawner);
    }

    @Override
    public SpawnOptionType getType() {
        return SpawnOptionType.ENTITY;
    }

    @Override
    public int hashCode() {
        return 31 * (types != null ? Arrays.hashCode(types) : 0);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof SpawnOptionEntity)) return false;

        SpawnOptionEntity other = (SpawnOptionEntity) object;
        return Arrays.equals(types, other.types);
    }
}

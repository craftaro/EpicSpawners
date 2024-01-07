package com.craftaro.epicspawners.api.spawners.spawner;

import com.craftaro.core.compatibility.CompatibleHand;
import com.craftaro.core.database.Data;
import com.craftaro.core.nms.world.SpawnedEntity;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.epicspawners.api.boosts.types.Boosted;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PlacedSpawner extends Data {
    int spawn(int amountToSpawn, String particle, Set<XMaterial> canSpawnOn, SpawnedEntity spawned, EntityType... types);

    SpawnerStack addSpawnerStack(SpawnerStack spawnerStack);

    boolean spawn();

    void displaySpawnParticles();

    Location getLocation();

    int getX();

    int getY();

    int getZ();

    World getWorld();

    CreatureSpawner getCreatureSpawner();

    SpawnerStack getFirstStack();

    int getStackSize();

    boolean checkConditions();

    boolean isRedstonePowered();

    void overview(Player player);

    boolean unstack(Player player, CompatibleHand hand);

    boolean stack(Player player, SpawnerTier tier, int amount, CompatibleHand hand);

    SpawnerTier getFirstTier();

    void upgradeEffects(Player player, SpawnerTier tier, boolean stacked);

    List<Boosted> getBoosts();

    int updateDelay();

    String getIdentifyingName();

    List<SpawnerStack> getSpawnerStacks();

    void replaceStacks(List<SpawnerStack> stacks);

    OfflinePlayer getPlacedBy();

    int getId();

    void setId(int id);

    void setPlacedBy(Player placedBy);

    void setPlacedBy(UUID placedBy);

    int getSpawnCount();

    void setSpawnCount(int spawnCount);

    String getOmniState();

    void setOmniState(String omniState);

    void destroy();

    boolean isValid();

    boolean merge(SpawnerStack toMerge, SpawnerTier oldTier);

    String getHologramId();
}

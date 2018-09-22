package com.songoda.epicspawners.spawners.spawner.option;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.events.SpawnerChangeEvent;
import com.songoda.epicspawners.api.events.SpawnerSpawnEvent;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.api.spawner.condition.SpawnCondition;
import com.songoda.epicspawners.spawners.condition.SpawnConditionNearbyEntities;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Consumer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SpawnOptionEntity implements SpawnOption {

    private static Method nmsSpawnMethod;

    private final EntityType[] types;

    private final ScriptEngineManager mgr;

    private final ScriptEngine engine;

    private EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();

    private Map<String, Integer> cache = new HashMap<>();

    public SpawnOptionEntity(EntityType... types) {
        this.types = types;
        this.mgr = new ScriptEngineManager();
        this.engine = mgr.getEngineByName("JavaScript");
    }

    public SpawnOptionEntity(Collection<EntityType> entities) {
        this(entities.toArray(new EntityType[entities.size()]));
    }

    @Override
    public void spawn(SpawnerData data, SpawnerStack stack, Spawner spawner) {
        Location location = spawner.getLocation();
        location.add(.5,.5,.5);
        if (location == null || location.getWorld() == null) return;

        World world = location.getWorld();

        // Instantiate nmsSpawnMethod if not done so already

        if (nmsSpawnMethod == null) {
            try {
                nmsSpawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, Consumer.class, CreatureSpawnEvent.SpawnReason.class);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                return;
            }
        }

        String[] randomLowHigh = instance.getConfig().getString("Main.Random Amount Added To Each Spawn").split(":");

        int randomAmt = ThreadLocalRandom.current().nextInt(Integer.valueOf(randomLowHigh[0]), Integer.valueOf(randomLowHigh[1]));

        String equation = instance.getConfig().getString("Main.Equations.Mobs Spawned Per Spawn");
        equation = equation.replace("{RAND}", Integer.toString(randomAmt));
        equation = equation.replace("{MULTI}", Integer.toString(stack.getStackSize()));


        int spawnCount = 0;
        try {
            if (!cache.containsKey(equation)) {
                spawnCount = (int) Math.round(Double.parseDouble(engine.eval(equation).toString()));
                cache.put(equation, spawnCount);
            } else {
                spawnCount = cache.get(equation);
            }
        } catch (ScriptException e) {
            System.out.println("Your spawner equation is broken, fix it.");
        }

        int limit = 0;
        for (SpawnCondition spawnCondition : data.getConditions()) {
            if (spawnCondition instanceof SpawnConditionNearbyEntities)
                limit = ((SpawnConditionNearbyEntities) spawnCondition).getMax();
        }

        int spawnerBoost = spawner.getBoost();

        String[] arr = EpicSpawnersPlugin.getInstance().getConfig().getString("Main.Radius To Search Around Spawners").split("x");
        Collection<Entity> amt = location.getWorld().getNearbyEntities(location, Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
        amt.removeIf(e -> e instanceof Player || !(e instanceof LivingEntity) || e instanceof ArmorStand);

        if (amt.size() == limit && spawnerBoost == 0) return;
        spawnCount = Math.min(limit - amt.size(), spawnCount) + spawner.getBoost();

        while (spawnCount-- > 0) {
            EntityType type = types[ThreadLocalRandom.current().nextInt(types.length)];
            spawnEntity(location, type, spawner, data);
            spawner.setSpawnCount(spawner.getSpawnCount() + 1);
            // TODO: Talk to the author of StackMob to get his ass in gear. lolk (I dropped support, try and add it in later)
        }
    }

    private void spawnEntity(Location location, EntityType type, Spawner spawner, SpawnerData data) {
        try {

            Location spot = null;
            boolean in = false;

            int amt = 0;
            while (!in && amt <= 25) {
                double testX = ThreadLocalRandom.current().nextDouble(-1, 1);
                double testY = ThreadLocalRandom.current().nextDouble(-1, 2);
                double testZ = ThreadLocalRandom.current().nextDouble(-1, 1);

                double x = location.getX() + testX * 3;
                double y = location.getY() + testY;
                double z = location.getZ() + testZ * 3;

                spot = new Location(location.getWorld(), x, y, z);

                if (canSpawn(data, spot))
                    in = true;

                amt++;
            }

            if (in) {
                float x = (float) (0 + (Math.random() * 1));
                float y = (float) (0 + (Math.random() * 2));
                float z = (float) (0 + (Math.random() * 1));

                //ToDo: Make this work for all spawn types
                Arconix.pl().getApi().packetLibrary.getParticleManager().broadcastParticle(spot, x, y, z, 0, data.getEntitySpawnParticle().getEffect(), data.getParticleDensity().getEntitySpawn());

                Location loc = spot.clone();
                loc.subtract(0, 1, 0);
                spot = spot.clone().getBlock().getLocation();

                double spawnX = ThreadLocalRandom.current().nextDouble(0.4, 0.6);
                double spawnZ = ThreadLocalRandom.current().nextDouble(0.4, 0.6);

                spot.add(spawnX, .5, spawnZ);


                spawnFinal(spot, data, spawner, type);
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    private boolean canSpawn(SpawnerData data, Location location) {
        try {
            List<Material> spawnBlocks = Arrays.asList(data.getSpawnBlocks());

            if (!Methods.isAir(location.getBlock().getType()) && (!isWater(location.getBlock().getType()) && !spawnBlocks.contains("WATER"))) {
                return false;
            }

                for (Material material : spawnBlocks) {
                    Location loc = location.clone().subtract(0, 1, 0);
                    if (loc.getBlock().getType().toString().equalsIgnoreCase(material.name())
                            || (material.toString().equals("GRASS") && loc.getBlock().getType() == Material.GRASS_BLOCK)
                            || isWater(loc.getBlock().getType()) && spawnBlocks.contains("WATER")) {
                        return true;
                    }
                }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return false;
    }

    private boolean isWater(Material type) {
        try {
            if (type == Material.WATER) {
                return true;
            }

        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return false;
    }

    private boolean spawnFinal(Location location, SpawnerData data, Spawner spawner, EntityType type) {
        World world = location.getWorld();

        try {
            Entity entity = (Entity) nmsSpawnMethod.invoke(world, location, type.getEntityClass(), null, CreatureSpawnEvent.SpawnReason.SPAWNER); //ToDo: account for all mobs in the spawner.

            SpawnerSpawnEvent event = new SpawnerSpawnEvent(entity, spawner);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                entity.remove();
                return true;
            }

            if (data.isSpawnOnFire())
                entity.setFireTicks(160);

            entity.setMetadata("ES", new FixedMetadataValue(instance, data.getIdentifyingName()));

            EpicSpawnersPlugin.getInstance().getSpawnManager().addUnnaturalSpawn(entity.getUniqueId());
            return true;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    @Override
    public SpawnOptionType getType() {
        return SpawnOptionType.ENTITY;
    }

    @Override
    public int hashCode() {
        return 31 * (types != null ? types.hashCode() : 0);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof SpawnOptionEntity)) return false;

        SpawnOptionEntity other = (SpawnOptionEntity) object;
        return Arrays.equals(types, other.types);
    }

}

package com.songoda.epicspawners.spawners.spawner.option;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.api.events.SpawnerSpawnEvent;
import com.songoda.epicspawners.particles.ParticleType;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.condition.SpawnCondition;
import com.songoda.epicspawners.spawners.condition.SpawnConditionNearbyEntities;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SpawnOptionEntity_1_13 implements SpawnOption {

    private final EntityType[] types;

    private final ScriptEngineManager mgr;

    private final ScriptEngine engine;

    private EpicSpawners plugin = EpicSpawners.getInstance();

    private boolean useUltimateStacker;

    private Enum<?> SpawnerEnum;

    private boolean mcmmo;

    private Map<String, Integer> cache = new HashMap<>();
    private Class<?> clazzMobSpawnerData, clazzEnumMobSpawn, clazzWorldServer, clazzGeneratorAccess, clazzEntityTypes, clazzNBTTagCompound, clazzCraftWorld, clazzWorld, clazzChunkRegionLoader, clazzEntity, clazzCraftEntity, clazzEntityInsentient, clazzGroupDataEntity, clazzDifficultyDamageScaler, clazzBlockPosition, clazzIWorldReader, clazzICollisionAccess, clazzAxisAlignedBB;
    private Method methodGetEntity, methodSetString, methodSetPosition, methodA, methodAddEntity, methodGetHandle, methodChunkRegionLoaderA, methodEntityGetBukkitEntity, methodCraftEntityTeleport, methodEntityInsentientPrepare, methodChunkRegionLoaderA2, methodGetDamageScaler, methodGetCubes, methodGetBoundingBox;
    private Field fieldWorldRandom;

    public SpawnOptionEntity_1_13(EntityType... types) {
        this.types = types;
        this.mgr = new ScriptEngineManager();
        this.engine = mgr.getEngineByName("JavaScript");
        if (Bukkit.getPluginManager().isPluginEnabled("UltimateStacker")) {
            this.useUltimateStacker = ((Plugin) com.songoda.ultimatestacker.UltimateStacker.getInstance()).getConfig().getBoolean("Entities.Enabled");
        }
        init();
    }

    public SpawnOptionEntity_1_13(Collection<EntityType> entities) {
        this(entities.toArray(new EntityType[entities.size()]));
    }

    private void init() {
        try {
            String ver = Bukkit.getServer().getClass().getPackage().getName().substring(23);
            clazzMobSpawnerData = Class.forName("net.minecraft.server." + ver + ".MobSpawnerData");
            clazzNBTTagCompound = Class.forName("net.minecraft.server." + ver + ".NBTTagCompound");
            clazzCraftWorld = Class.forName("org.bukkit.craftbukkit." + ver + ".CraftWorld");
            clazzWorld = Class.forName("net.minecraft.server." + ver + ".World");
            clazzChunkRegionLoader = Class.forName("net.minecraft.server." + ver + ".ChunkRegionLoader");
            clazzEntity = Class.forName("net.minecraft.server." + ver + ".Entity");
            clazzCraftEntity = Class.forName("org.bukkit.craftbukkit." + ver + ".entity.CraftEntity");
            clazzEntityInsentient = Class.forName("net.minecraft.server." + ver + ".EntityInsentient");
            clazzGroupDataEntity = Class.forName("net.minecraft.server." + ver + ".GroupDataEntity");
            clazzDifficultyDamageScaler = Class.forName("net.minecraft.server." + ver + ".DifficultyDamageScaler");
            clazzBlockPosition = Class.forName("net.minecraft.server." + ver + ".BlockPosition");
            clazzIWorldReader = Class.forName("net.minecraft.server." + ver + ".IWorldReader");
            clazzAxisAlignedBB = Class.forName("net.minecraft.server." + ver + ".AxisAlignedBB");
            clazzEntityTypes = Class.forName("net.minecraft.server." + ver + ".EntityTypes");

            try {
                clazzICollisionAccess = Class.forName("net.minecraft.server." + ver + ".ICollisionAccess");
                methodGetCubes = clazzICollisionAccess.getDeclaredMethod("getCubes", clazzEntity, clazzAxisAlignedBB);
            } catch (ClassNotFoundException e) {
                clazzIWorldReader = Class.forName("net.minecraft.server." + ver + ".IWorldReader");
                methodGetCubes = clazzIWorldReader.getDeclaredMethod("getCubes", clazzEntity, clazzAxisAlignedBB);
            }

            clazzGeneratorAccess = Class.forName("net.minecraft.server." + ver + ".GeneratorAccess");

            try {
                methodGetEntity = clazzMobSpawnerData.getDeclaredMethod("getEntity");
            } catch (NoSuchMethodException e) {
                methodGetEntity = clazzMobSpawnerData.getDeclaredMethod("b");
            }
            methodSetString = clazzNBTTagCompound.getDeclaredMethod("setString", String.class, String.class);

            methodGetBoundingBox = clazzEntity.getDeclaredMethod("getBoundingBox");
            methodSetPosition = clazzEntity.getDeclaredMethod("setPosition", double.class, double.class, double.class);
            methodGetHandle = clazzCraftWorld.getDeclaredMethod("getHandle");
            try {
                methodChunkRegionLoaderA = clazzChunkRegionLoader.getDeclaredMethod("a", clazzNBTTagCompound, clazzWorld, double.class, double.class, double.class, boolean.class);
                methodEntityInsentientPrepare = clazzEntityInsentient.getDeclaredMethod("prepare", clazzDifficultyDamageScaler, clazzGroupDataEntity, clazzNBTTagCompound);
                methodChunkRegionLoaderA2 = clazzChunkRegionLoader.getDeclaredMethod("a", clazzEntity, clazzGeneratorAccess, Class.forName("org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason"));
            } catch (NoSuchMethodException e) {
                methodA = clazzEntityTypes.getDeclaredMethod("a", clazzNBTTagCompound, clazzWorld);

                clazzEnumMobSpawn = Class.forName("net.minecraft.server." + ver + ".EnumMobSpawn");
                for (Object enumValue : clazzEnumMobSpawn.getEnumConstants()) {
                    Enum<?> mobSpawnEnum = (Enum<?>) enumValue;
                    if (mobSpawnEnum.name().equals("SPAWNER")) {
                        this.SpawnerEnum = mobSpawnEnum;
                        break;
                    }
                }

                clazzWorldServer = Class.forName("net.minecraft.server." + ver + ".WorldServer");

                methodEntityInsentientPrepare = clazzEntityInsentient.getDeclaredMethod("prepare", clazzGeneratorAccess, clazzDifficultyDamageScaler, clazzEnumMobSpawn, clazzGroupDataEntity, clazzNBTTagCompound);
                methodAddEntity = clazzWorldServer.getDeclaredMethod("addEntity", clazzEntity, Class.forName("org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason"));
            }

            methodEntityGetBukkitEntity = clazzEntity.getDeclaredMethod("getBukkitEntity");
            methodCraftEntityTeleport = clazzCraftEntity.getDeclaredMethod("teleport", Location.class);
            methodGetDamageScaler = clazzWorld.getDeclaredMethod("getDamageScaler", clazzBlockPosition);

            fieldWorldRandom = clazzWorld.getDeclaredField("random");
            fieldWorldRandom.setAccessible(true);

        } catch (NoSuchFieldException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        this.mcmmo = Bukkit.getPluginManager().isPluginEnabled("mcMMO");
    }

    @Override
    public void spawn(SpawnerData data, SpawnerStack stack, Spawner spawner) {
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
            try {
                if (!cache.containsKey(equation)) {
                    spawnCount += (int) Math.round(Double.parseDouble(engine.eval(equation).toString()));
                    cache.put(equation, spawnCount);
                } else {
                    spawnCount += cache.get(equation);
                }
            } catch (ScriptException e) {
                System.out.println("Your spawner equation is broken, fix it.");
            }
        }

        // Get the max entities allowed around a spawner.
        int maxEntitiesAllowed = 0;
        for (SpawnCondition spawnCondition : data.getConditions()) {
            if (spawnCondition instanceof SpawnConditionNearbyEntities)
                maxEntitiesAllowed = ((SpawnConditionNearbyEntities) spawnCondition).getMax();
        }

        // Get the amount of entities around the spawner.
        int size = SpawnConditionNearbyEntities.getEntitiesAroundSpawner(location, true);

        // Calculate the amount of entities to spawn.
        spawnCount = Math.min(maxEntitiesAllowed - size, spawnCount) + spawner.getBoost();

        // Check to make sure we're not spawning a stack smaller than the minimum stack size.
        boolean useUltimateStacker = this.useUltimateStacker
                && spawnCount >= com.songoda.ultimatestacker.settings.Settings.MIN_STACK_ENTITIES.getInt();

        int spawnCountUsed = useUltimateStacker ? 1 : spawnCount;

        while (spawnCountUsed-- > 0) {
            EntityType type = types[ThreadLocalRandom.current().nextInt(types.length)];
            Entity entity = spawnEntity(type, spawner, data);
            if (entity != null) {
                // If we're using UltimateStacker and this entity is indeed stackable then spawn a single stack with the desired stack size.
                if (useUltimateStacker && com.songoda.ultimatestacker.UltimateStacker.getInstance().getMobFile().getBoolean("Mobs." + entity.getType().name() + ".Enabled"))
                    com.songoda.ultimatestacker.UltimateStacker.getInstance().getEntityStackManager().addStack(entity.getUniqueId(), spawnCount);
                spawner.setSpawnCount(spawner.getSpawnCount() + (useUltimateStacker ? spawnCount : 1));
            }
        }
    }

    private Entity spawnEntity(EntityType type, Spawner spawner, SpawnerData data) {
        try {
            Object objMobSpawnerData = clazzMobSpawnerData.newInstance();
            Object objNTBTagCompound = methodGetEntity.invoke(objMobSpawnerData);

            String name = type.name().toLowerCase().replace("pig_zombie", "zombie_pigman").replace("snowman", "snow_golem").replace("mushroom_cow", "mooshroom");
            methodSetString.invoke(objNTBTagCompound, "id", "minecraft:" + name);

            short spawnRange = 4;
            for (int i = 0; i < 50; i++) {
                Object objNBTTagCompound = methodGetEntity.invoke(objMobSpawnerData);
                Object objCraftWorld = clazzCraftWorld.cast(spawner.getWorld());
                objCraftWorld = methodGetHandle.invoke(objCraftWorld);
                Object objWorld = clazzWorld.cast(objCraftWorld);

                Random random = (Random) fieldWorldRandom.get(objWorld);
                double x = (double) spawner.getX() + (random.nextDouble() - random.nextDouble()) * (double) spawnRange + 0.5D;
                double y = (double) (spawner.getY() + random.nextInt(3) - 1);
                double z = (double) spawner.getZ() + (random.nextDouble() - random.nextDouble()) * (double) spawnRange + 0.5D;

                Object objEntity;
                if (methodChunkRegionLoaderA != null) {
                    objEntity = methodChunkRegionLoaderA.invoke(null, objNBTTagCompound, objWorld, x, y, z, false);
                } else {
                    Optional optional = (Optional) methodA.invoke(null, objNBTTagCompound, objWorld);

                    if (!optional.isPresent()) continue;

                    objEntity = optional.get();

                    methodSetPosition.invoke(objEntity, x, y, z);
                }

                Object objBlockPosition = clazzBlockPosition.getConstructor(clazzEntity).newInstance(objEntity);
                Object objDamageScaler = methodGetDamageScaler.invoke(objWorld, objBlockPosition);

                Object objEntityInsentient = clazzEntityInsentient.isInstance(objEntity) ? clazzEntityInsentient.cast(objEntity) : null;

                Location spot = new Location(spawner.getWorld(), x, y, z);
                if (!canSpawn(objWorld, objEntityInsentient, data, spot))
                    continue;

                if (methodChunkRegionLoaderA != null) {
                    methodEntityInsentientPrepare.invoke(objEntity, objDamageScaler, null, null);
                } else {
                    methodEntityInsentientPrepare.invoke(objEntity, objWorld, objDamageScaler, SpawnerEnum, null, null);
                }

                Entity craftEntity = (Entity) methodEntityGetBukkitEntity.invoke(objEntity);

                SpawnerSpawnEvent event = new SpawnerSpawnEvent(craftEntity, spawner);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    craftEntity.remove();
                    return null;
                }

                ParticleType particleType = data.getEntitySpawnParticle();

                if (particleType != ParticleType.NONE) {
                    float xx = (float) (0 + (Math.random() * 1));
                    float yy = (float) (0 + (Math.random() * 2));
                    float zz = (float) (0 + (Math.random() * 1));
                    spot.getWorld().spawnParticle(Particle.valueOf(particleType.getEffect()), spot, 5, xx, yy, zz, 0);
                }

                if (methodChunkRegionLoaderA != null) {
                    methodChunkRegionLoaderA2.invoke(null, objEntity, objWorld, CreatureSpawnEvent.SpawnReason.SPAWNER);
                } else {
                    methodAddEntity.invoke(clazzWorldServer.cast(objWorld), objEntity, CreatureSpawnEvent.SpawnReason.SPAWNER);
                }

                if (data.isSpawnOnFire()) craftEntity.setFireTicks(160);

                craftEntity.setMetadata("ES", new FixedMetadataValue(plugin, data.getIdentifyingName()));

                if (mcmmo)
                    craftEntity.setMetadata("mcMMO: Spawned Entity", new FixedMetadataValue(plugin, true));

                if (Settings.NO_AI.getBoolean())
                    ((LivingEntity) craftEntity).setAI(false);

                Object objBukkitEntity = methodEntityGetBukkitEntity.invoke(objEntity);
                spot.setYaw(random.nextFloat() * 360.0F);
                methodCraftEntityTeleport.invoke(objBukkitEntity, spot);

                plugin.getSpawnManager().addUnnaturalSpawn(craftEntity.getUniqueId());

                return craftEntity;

            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean canSpawn(Object objWorld, Object objEntityInsentient, SpawnerData data, Location location) {
        try {
            Object objIWR = clazzIWorldReader == null ? clazzICollisionAccess.cast(objWorld) : clazzIWorldReader.cast(objWorld);

            if (!(boolean) methodGetCubes.invoke(objIWR, objEntityInsentient, methodGetBoundingBox.invoke(objEntityInsentient)))
                return false;

            Material[] spawnBlocks = data.getSpawnBlocks();

            Material spawnedIn = location.getBlock().getType();
            Material spawnedOn = location.getBlock().getRelative(BlockFace.DOWN).getType();

            if (!Methods.isAir(spawnedIn)
                    && !isWater(spawnedIn)
                    && !spawnedIn.name().contains("PRESSURE")
                    && !spawnedIn.name().contains("SLAB")) {
                return false;
            }

            for (Material material : spawnBlocks) {
                if (material == null) continue;
                if (spawnedOn == material)
                    return true;
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }


    private boolean isWater(Material type) {
        return type == Material.WATER;
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
        if (!(object instanceof SpawnOptionEntity_1_13)) return false;

        SpawnOptionEntity_1_13 other = (SpawnOptionEntity_1_13) object;
        return Arrays.equals(types, other.types);
    }

}

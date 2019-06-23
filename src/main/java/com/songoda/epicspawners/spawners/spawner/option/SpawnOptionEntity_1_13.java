package com.songoda.epicspawners.spawners.spawner.option;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.api.events.SpawnerSpawnEvent;
import com.songoda.epicspawners.particles.ParticleType;
import com.songoda.epicspawners.spawners.condition.SpawnCondition;
import com.songoda.epicspawners.spawners.condition.SpawnConditionNearbyEntities;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.settings.Setting;
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

    private EpicSpawners instance = EpicSpawners.getInstance();

    private Enum<?> SpawnerEnum;

    private boolean stackPlugin;

    private Map<String, Integer> cache = new HashMap<>();
    private Class<?> clazzMobSpawnerData, clazzEnumMobSpawn, clazzWorldServer, clazzGeneratorAccess, clazzEntityTypes, clazzNBTTagCompound, clazzCraftWorld, clazzWorld, clazzChunkRegionLoader, clazzEntity, clazzCraftEntity, clazzEntityInsentient, clazzGroupDataEntity, clazzDifficultyDamageScaler, clazzBlockPosition, clazzIWorldReader, clazzAxisAlignedBB;
    private Method methodGetEntity, methodSetString, methodSetPosition, methodA, methodAddEntity, methodGetHandle, methodChunkRegionLoaderA, methodEntityGetBukkitEntity, methodCraftEntityTeleport, methodEntityInsentientPrepare, methodChunkRegionLoaderA2, methodGetDamageScaler, methodGetCubes, methodGetBoundingBox;
    private Field fieldWorldRandom;

    public SpawnOptionEntity_1_13(EntityType... types) {
        this.types = types;
        this.mgr = new ScriptEngineManager();
        this.engine = mgr.getEngineByName("JavaScript");
        this.stackPlugin = Bukkit.getPluginManager().isPluginEnabled("UltimateStacker") || Bukkit.getPluginManager().isPluginEnabled("StackMob");
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
            clazzIWorldReader = Class.forName("net.minecraft.server." + ver + ".IWorldReader");
            clazzGeneratorAccess = Class.forName("net.minecraft.server." + ver + ".GeneratorAccess");

            try {
                methodGetEntity = clazzMobSpawnerData.getDeclaredMethod("getEntity");
            } catch (NoSuchMethodException e) {
                methodGetEntity = clazzMobSpawnerData.getDeclaredMethod("b");
            }
            methodSetString = clazzNBTTagCompound.getDeclaredMethod("setString", String.class, String.class);

            methodGetBoundingBox = clazzEntity.getDeclaredMethod("getBoundingBox");
            methodSetPosition = clazzEntity.getDeclaredMethod("setPosition", double.class, double.class, double.class);
            methodGetCubes = clazzIWorldReader.getDeclaredMethod("getCubes", clazzEntity, clazzAxisAlignedBB);
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
    }

    @Override
    public void spawn(SpawnerData data, SpawnerStack stack, Spawner spawner) {
        Location location = spawner.getLocation();
        location.add(.5, .5, .5);
        if (location.getWorld() == null) return;

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

        String[] arr = EpicSpawners.getInstance().getConfig().getString("Main.Radius To Search Around Spawners").split("x");
        Collection<Entity> amt = location.getWorld().getNearbyEntities(location, Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
        amt.removeIf(entity -> !(entity instanceof LivingEntity) || entity.getType() == EntityType.PLAYER || entity.getType() == EntityType.ARMOR_STAND);


        if (amt.size() == limit && spawnerBoost == 0) return;
        spawnCount = (stackPlugin ? spawnCount : Math.min(limit - amt.size(), spawnCount)) + spawner.getBoost();

        while (spawnCount-- > 0) {
            EntityType type = types[ThreadLocalRandom.current().nextInt(types.length)];
            if (spawnEntity(type, spawner, data))
                spawner.setSpawnCount(spawner.getSpawnCount() + 1);
        }
    }

    private boolean spawnEntity(EntityType type, Spawner spawner, SpawnerData data) {
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

                ParticleType particleType = data.getEntitySpawnParticle();

                if (particleType != ParticleType.NONE) {
                    float xx = (float) (0 + (Math.random() * 1));
                    float yy = (float) (0 + (Math.random() * 2));
                    float zz = (float) (0 + (Math.random() * 1));
                    spot.getWorld().spawnParticle(Particle.valueOf(particleType.getEffect()), spot, 5, xx, yy, zz, 0);
                }

                Entity craftEntity = (Entity) methodEntityGetBukkitEntity.invoke(objEntity);

                SpawnerSpawnEvent event = new SpawnerSpawnEvent(craftEntity, spawner);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) return false;

                if (methodChunkRegionLoaderA != null) {
                    methodChunkRegionLoaderA2.invoke(null, objEntity, objWorld, CreatureSpawnEvent.SpawnReason.SPAWNER);
                } else {
                    methodAddEntity.invoke(clazzWorldServer.cast(objWorld), objEntity, CreatureSpawnEvent.SpawnReason.SPAWNER);
                }

                if (data.isSpawnOnFire()) craftEntity.setFireTicks(160);

                craftEntity.setMetadata("ES", new FixedMetadataValue(instance, data.getIdentifyingName()));

                if (Setting.NO_AI.getBoolean())
                    ((LivingEntity) craftEntity).setAI(false);

                Object objBukkitEntity = methodEntityGetBukkitEntity.invoke(objEntity);
                spot.setYaw(random.nextFloat() * 360.0F);
                methodCraftEntityTeleport.invoke(objBukkitEntity, spot);

                EpicSpawners.getInstance().getSpawnManager().addUnnaturalSpawn(craftEntity.getUniqueId());
                return true;
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean canSpawn(Object objWorld, Object objEntityInsentient, SpawnerData data, Location location) {
        try {
            Object objIWR = clazzIWorldReader.cast(objWorld);

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

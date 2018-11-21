package com.songoda.epicspawners.spawners.spawner.option;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.events.SpawnerSpawnEvent;
import com.songoda.epicspawners.api.particles.ParticleType;
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
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowman;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SpawnOptionEntity implements SpawnOption {

    private final EntityType[] types;

    private final ScriptEngineManager mgr;

    private final ScriptEngine engine;

    private EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();

    private Map<String, Integer> cache = new HashMap<>();
    private Class<?> clazzMobSpawnerData, clazzNBTTagCompound, clazzNBTTagList, clazzCraftWorld, clazzWorld, clazzChunkRegionLoader, clazzEntity, clazzCraftEntity, clazzEntityInsentient, clazzGroupDataEntity, clazzDifficultyDamageScaler, clazzBlockPosition, clazzIWorldReader, clazzAxisAlignedBB;
    private Method methodB, methodSetString, methodGetHandle, methodChunkRegionLoaderA, methodEntityGetBukkitEntity, methodCraftEntityTeleport, methodEntityInsentientCanSpawn, methodEntityInsentientPrepare, methodChunkRegionLoaderA2, methodGetDamageScaler, methodGetCubes, methodGetBoundingBox;
    private Field fieldWorldRandom;
    public SpawnOptionEntity(EntityType... types) {
        this.types = types;
        this.mgr = new ScriptEngineManager();
        this.engine = mgr.getEngineByName("JavaScript");
        init();
    }
    public SpawnOptionEntity(Collection<EntityType> entities) {
        this(entities.toArray(new EntityType[entities.size()]));
    }

    private void init() {
        try {
            String ver = Bukkit.getServer().getClass().getPackage().getName().substring(23);
            clazzMobSpawnerData = Class.forName("net.minecraft.server." + ver + ".MobSpawnerData");
            clazzNBTTagCompound = Class.forName("net.minecraft.server." + ver + ".NBTTagCompound");
            clazzNBTTagList = Class.forName("net.minecraft.server." + ver + ".NBTTagList");
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

            methodB = clazzMobSpawnerData.getDeclaredMethod("b");
            methodSetString = clazzNBTTagCompound.getDeclaredMethod("setString", String.class, String.class);

            methodGetBoundingBox = clazzEntity.getDeclaredMethod("getBoundingBox");
            methodGetCubes = clazzIWorldReader.getDeclaredMethod("getCubes", clazzEntity, clazzAxisAlignedBB);
            methodGetHandle = clazzCraftWorld.getDeclaredMethod("getHandle");
            methodChunkRegionLoaderA = clazzChunkRegionLoader.getDeclaredMethod("a", clazzNBTTagCompound, clazzWorld, double.class, double.class, double.class, boolean.class);
            methodEntityGetBukkitEntity = clazzEntity.getDeclaredMethod("getBukkitEntity");
            methodCraftEntityTeleport = clazzCraftEntity.getDeclaredMethod("teleport", Location.class);
            methodEntityInsentientCanSpawn = clazzEntityInsentient.getDeclaredMethod("canSpawn");
            methodEntityInsentientPrepare = clazzEntityInsentient.getDeclaredMethod("prepare", clazzDifficultyDamageScaler, clazzGroupDataEntity, clazzNBTTagCompound);
            methodChunkRegionLoaderA2 = clazzChunkRegionLoader.getDeclaredMethod("a", clazzEntity, Class.forName("net.minecraft.server." + ver + ".GeneratorAccess"), Class.forName("org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason"));
            methodGetDamageScaler = clazzWorld.getDeclaredMethod("getDamageScaler", clazzBlockPosition);

            fieldWorldRandom = clazzWorld.getDeclaredField("random");
            fieldWorldRandom.setAccessible(true);

        } catch (Exception t) {
            Debugger.runReport(t);
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

        String[] arr = EpicSpawnersPlugin.getInstance().getConfig().getString("Main.Radius To Search Around Spawners").split("x");
        Collection<Entity> amt = location.getWorld().getNearbyEntities(location, Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
        amt.removeIf(entity -> !(entity instanceof LivingEntity) || entity.getType() == EntityType.PLAYER || entity.getType() == EntityType.ARMOR_STAND);

        if (amt.size() == limit && spawnerBoost == 0) return;
        spawnCount = Math.min(limit - amt.size(), spawnCount) + spawner.getBoost();

        while (spawnCount-- > 0) {
            EntityType type = types[ThreadLocalRandom.current().nextInt(types.length)];
            spawnEntity(type, spawner, data);
            spawner.setSpawnCount(spawner.getSpawnCount() + 1);
            // TODO: Talk to the author of StackMob to get his ass in gear. lolk (I dropped support, try and add it in later)
        }
    }

    private void spawnEntity(EntityType type, Spawner spawner, SpawnerData data) {
        try {
            Snowman;
            type.getEntityClass()
            Object objMobSpawnerData = clazzMobSpawnerData.newInstance();
            Object objNTBTagCompound = methodB.invoke(objMobSpawnerData);

            String name = type.name().toLowerCase().replace("pig_zombie", "zombie_pigman").replace("snowman", "snow_golem").replace("mushroom_cow", "mooshroom");
            methodSetString.invoke(objNTBTagCompound, "id", "minecraft:" + name);

            short spawnRange = 4;
            for (int i = 0; i < 25; i++) {
                Object objNBTTagCompound = methodB.invoke(objMobSpawnerData);
                Object objCraftWorld = clazzCraftWorld.cast(spawner.getWorld());
                objCraftWorld = methodGetHandle.invoke(objCraftWorld);
                Object objWorld = clazzWorld.cast(objCraftWorld);

                Random random = (Random) fieldWorldRandom.get(objWorld);
                double x = (double) spawner.getX() + (random.nextDouble() - random.nextDouble()) * (double) spawnRange + 0.5D;
                double y = (double) (spawner.getY() + random.nextInt(3) - 1);
                double z = (double) spawner.getZ() + (random.nextDouble() - random.nextDouble()) * (double) spawnRange + 0.5D;

                Object objEntity = methodChunkRegionLoaderA.invoke(null, objNBTTagCompound, objWorld, x, y, z, false);
                Object objBlockPosition = clazzBlockPosition.getConstructor(clazzEntity).newInstance(objEntity);
                Object objDamageScaler = methodGetDamageScaler.invoke(objWorld, objBlockPosition);

                methodEntityInsentientPrepare.invoke(objEntity, objDamageScaler, null, null);

                Object objEntityInsentient = clazzEntityInsentient.isInstance(objEntity) ? clazzEntityInsentient.cast(objEntity) : null;

                Location spot = new Location(spawner.getWorld(), x, y, z);
                if (!canSpawn(objWorld, objEntityInsentient, data, spot))
                    continue;

                float px = (float) (0 + (Math.random() * 1));
                float py = (float) (0 + (Math.random() * 2));
                float pz = (float) (0 + (Math.random() * 1));

                ParticleType particleType = data.getEntitySpawnParticle();

                if (particleType != ParticleType.NONE)
                    Arconix.pl().getApi().packetLibrary.getParticleManager().broadcastParticle(spot, px, py, pz, 0, particleType.getEffect(), data.getParticleDensity().getEntitySpawn());

                Entity craftEntity = (Entity) methodEntityGetBukkitEntity.invoke(objEntity);

                SpawnerSpawnEvent event = new SpawnerSpawnEvent(craftEntity, spawner);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) return;

                methodChunkRegionLoaderA2.invoke(null, objEntity, objWorld, CreatureSpawnEvent.SpawnReason.SPAWNER);

                if (data.isSpawnOnFire()) craftEntity.setFireTicks(160);

                craftEntity.setMetadata("ES", new FixedMetadataValue(instance, data.getIdentifyingName()));

                Object objBukkitEntity = methodEntityGetBukkitEntity.invoke(objEntity);
                spot.setYaw(random.nextFloat() * 360.0F);
                methodCraftEntityTeleport.invoke(objBukkitEntity, spot);

                EpicSpawnersPlugin.getInstance().getSpawnManager().addUnnaturalSpawn(craftEntity.getUniqueId());
                return;
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    private boolean canSpawn(Object objWorld, Object objEntityInsentient, SpawnerData data, Location location) {
        try {
            Object objIWR = clazzIWorldReader.cast(objWorld);

            if (!(Boolean) methodGetCubes.invoke(objIWR, objEntityInsentient, methodGetBoundingBox.invoke(objEntityInsentient)))
                return false;


            List<Material> spawnBlocks = Arrays.asList(data.getSpawnBlocks());

            if (!Methods.isAir(location.getBlock().getType()) && (!isWater(location.getBlock().getType()) && !spawnBlocks.contains("WATER"))) {
                return false;
            }

            for (Material material : spawnBlocks) {
                Material down = location.getBlock().getRelative(BlockFace.DOWN).getType();
                if ((location.getBlock().getType() == Material.AIR || location.getBlock().getType().name().toLowerCase().contains("pressure")) && material == Material.AIR)
                    return true;
                if (down.toString().equalsIgnoreCase(material.name())
                        || (material.toString().equals("GRASS") && down == Material.GRASS)
                        || isWater(down) && spawnBlocks.contains("WATER")) {
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

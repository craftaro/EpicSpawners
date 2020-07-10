package com.songoda.epicspawners.spawners.spawner.option;

import com.songoda.core.compatibility.CompatibleParticleHandler;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.utils.EntityUtils;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.api.events.SpawnerSpawnEvent;
import com.songoda.epicspawners.boost.types.Boosted;
import com.songoda.epicspawners.particles.ParticleType;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.condition.SpawnCondition;
import com.songoda.epicspawners.spawners.condition.SpawnConditionNearbyEntities;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import com.songoda.epicspawners.utils.Methods;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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

public class SpawnOptionEntity_1_12 implements SpawnOption {

    private final EntityType[] types;

    private final ScriptEngineManager mgr;

    private final ScriptEngine engine;

    private EpicSpawners plugin = EpicSpawners.getInstance();

    private boolean useUltimateStacker;

    private boolean mcmmo;

    private Map<String, Integer> cache = new HashMap<>();
    private Class<?> clazzEntityTypes, clazzMobSpawnerData, clazzNBTTagCompound, clazzNBTTagList, clazzCraftWorld, clazzWorld, clazzChunkRegionLoader, clazzEntity, clazzCraftEntity, clazzEntityInsentient, clazzGroupDataEntity, clazzDifficultyDamageScaler, clazzBlockPosition, clazzAxisAlignedBB;
    private Method methodAddEntity, methodCreateEntityByName, methodSetPositionRotation, methodB, methodSetString, methodNTBTagListSize, methodGetHandle, methodTBNTagListK, methodEntityInsentientPrepare, methodChunkRegionLoaderA, methodEntityGetBukkitEntity, methodCraftEntityTeleport, methodEntityInsentientCanSpawn, methodChunkRegionLoaderA2, methodGetDamageScaler, methodGetCubes, methodGetBoundingBox;
    private Field fieldWorldRandom;

    public SpawnOptionEntity_1_12(EntityType... types) {
        this.types = types;
        this.mgr = new ScriptEngineManager();
        this.engine = mgr.getEngineByName("JavaScript");
        if (Bukkit.getPluginManager().isPluginEnabled("UltimateStacker")) {
            this.useUltimateStacker = ((Plugin) com.songoda.ultimatestacker.UltimateStacker.getInstance()).getConfig().getBoolean("Entities.Enabled");
        }
        init();
    }

    public SpawnOptionEntity_1_12(Collection<EntityType> entities) {
        this(entities.toArray(new EntityType[entities.size()]));
    }

    private void init() {
        try {
            String ver = Bukkit.getServer().getClass().getPackage().getName().substring(23);
            clazzWorld = Class.forName("net.minecraft.server." + ver + ".World");
            clazzEntity = Class.forName("net.minecraft.server." + ver + ".Entity");
            clazzNBTTagList = Class.forName("net.minecraft.server." + ver + ".NBTTagList");
            clazzCraftWorld = Class.forName("org.bukkit.craftbukkit." + ver + ".CraftWorld");
            clazzChunkRegionLoader = Class.forName("net.minecraft.server." + ver + ".ChunkRegionLoader");
            clazzCraftEntity = Class.forName("org.bukkit.craftbukkit." + ver + ".entity.CraftEntity");
            clazzEntityInsentient = Class.forName("net.minecraft.server." + ver + ".EntityInsentient");
            clazzBlockPosition = Class.forName("net.minecraft.server." + ver + ".BlockPosition");
            Class<?> clazzSpawnReason = Class.forName("org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason");
            clazzGroupDataEntity = Class.forName("net.minecraft.server." + ver + ".GroupDataEntity");
            clazzDifficultyDamageScaler = Class.forName("net.minecraft.server." + ver + ".DifficultyDamageScaler");
            clazzAxisAlignedBB = Class.forName("net.minecraft.server." + ver + ".AxisAlignedBB");

            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) {
                clazzMobSpawnerData = Class.forName("net.minecraft.server." + ver + ".MobSpawnerData");
                clazzNBTTagCompound = Class.forName("net.minecraft.server." + ver + ".NBTTagCompound");

                methodB = clazzMobSpawnerData.getDeclaredMethod("b");
                methodSetString = clazzNBTTagCompound.getDeclaredMethod("setString", String.class, String.class);

                methodTBNTagListK = clazzNBTTagList.getDeclaredMethod("f", int.class);
                methodGetDamageScaler = clazzWorld.getDeclaredMethod("D", clazzBlockPosition);

                methodChunkRegionLoaderA = clazzChunkRegionLoader.getDeclaredMethod("a", clazzNBTTagCompound, clazzWorld, double.class, double.class, double.class, boolean.class);
                methodChunkRegionLoaderA2 = clazzChunkRegionLoader.getDeclaredMethod("a", clazzEntity, clazzWorld, clazzSpawnReason);
            } else {
                clazzEntityTypes = Class.forName("net.minecraft.server." + ver + ".EntityTypes");
                methodCreateEntityByName = clazzEntityTypes.getDeclaredMethod("createEntityByName", String.class, clazzWorld);
                methodSetPositionRotation = clazzEntity.getDeclaredMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class);
                methodAddEntity = clazzWorld.getDeclaredMethod("addEntity", clazzEntity, clazzSpawnReason);
                methodGetDamageScaler = clazzWorld.getDeclaredMethod("E", clazzBlockPosition);
            }


            methodGetBoundingBox = clazzEntity.getDeclaredMethod("getBoundingBox");
            methodGetCubes = clazzWorld.getDeclaredMethod("getCubes", clazzEntity, clazzAxisAlignedBB);

            methodGetHandle = clazzCraftWorld.getDeclaredMethod("getHandle");

            methodEntityGetBukkitEntity = clazzEntity.getDeclaredMethod("getBukkitEntity");
            methodCraftEntityTeleport = clazzCraftEntity.getDeclaredMethod("teleport", Location.class);
            methodEntityInsentientCanSpawn = clazzEntityInsentient.getDeclaredMethod("canSpawn");
            methodEntityInsentientPrepare = clazzEntityInsentient.getDeclaredMethod("prepare", clazzDifficultyDamageScaler, clazzGroupDataEntity);

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
        spawnCount = Math.min(maxEntitiesAllowed - size, spawnCount) + spawner.getBoosts().stream().mapToInt(Boosted::getAmountBoosted).sum();

        // Check to make sure we're not spawning a stack smaller than the minimum stack size.
        boolean useUltimateStacker = this.useUltimateStacker && com.songoda.ultimatestacker.settings
                .Settings.DISABLED_WORLDS.getStringList().stream()
                .noneMatch(worldStr -> location.getWorld().getName().equalsIgnoreCase(worldStr))
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
                EpicSpawners.getInstance().getDataManager().updateSpawner(spawner);
            }
        }
    }

    private Entity spawnEntity(EntityType type, Spawner spawner, SpawnerData data) {
        try {
            Object objMobSpawnerData = null;
            Object objNBTTagCompound;


            Methods.Tuple<String, String> typeTranslation = TypeTranslations.fromType(type);

            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) {
                objMobSpawnerData = clazzMobSpawnerData.newInstance();
                objNBTTagCompound = methodB.invoke(objMobSpawnerData);

                methodSetString.invoke(objNBTTagCompound, "id", ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11) ? "minecraft:" + typeTranslation.getKey().replace(" ", "_") : typeTranslation.getValue());
            }

            int spawnRange = 4;
            for (int i = 0; i < 25; i++) {
                Object objCraftWorld = clazzCraftWorld.cast(spawner.getWorld());
                objCraftWorld = methodGetHandle.invoke(objCraftWorld);
                Object objWorld = clazzWorld.cast(objCraftWorld);


                Random random = (Random) fieldWorldRandom.get(objWorld);
                double x = (double) spawner.getX() + (random.nextDouble() - random.nextDouble()) * (double) spawnRange + 0.5D;
                double y = (double) (spawner.getY() + random.nextInt(3) - 1);
                double z = (double) spawner.getZ() + (random.nextDouble() - random.nextDouble()) * (double) spawnRange + 0.5D;

                Object objEntity;
                if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) {
                    objNBTTagCompound = methodB.invoke(objMobSpawnerData);
                    objEntity = methodChunkRegionLoaderA.invoke(null, objNBTTagCompound, objWorld, x, y, z, false);
                } else {
                    objEntity = methodCreateEntityByName.invoke(null, typeTranslation.getValue(), objWorld);
                    methodSetPositionRotation.invoke(
                            objEntity, x, y, z, 360.0F, 0.0F);
                }
                Object objEntityInsentient = null;
                if (clazzEntityInsentient.isInstance(objEntity))
                    objEntityInsentient = clazzEntityInsentient.cast(objEntity);

                Location spot = new Location(spawner.getWorld(), x, y, z);
                if (!canSpawn(objEntityInsentient, data, spot))
                    continue;

                Object objBlockPosition = clazzBlockPosition.getConstructor(clazzEntity).newInstance(objEntity);
                Object objDamageScaler = methodGetDamageScaler.invoke(objWorld, objBlockPosition);

                methodEntityInsentientPrepare.invoke(objEntity, objDamageScaler, null);

                ParticleType particleType = data.getEntitySpawnParticle();

                if (particleType != ParticleType.NONE) {
                    float xx = (float) (0 + (Math.random() * 1));
                    float yy = (float) (0 + (Math.random() * 2));
                    float zz = (float) (0 + (Math.random() * 1));
                    CompatibleParticleHandler.spawnParticles(CompatibleParticleHandler.ParticleType.getParticle(particleType.getEffect()),
                            spot, 5, xx, yy, zz, 0);
                }

                Entity craftEntity = (Entity) methodEntityGetBukkitEntity.invoke(objEntity);

                SpawnerSpawnEvent event = new SpawnerSpawnEvent(craftEntity, spawner);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    craftEntity.remove();
                    return null;
                }

                if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9)) {
                    methodChunkRegionLoaderA2.invoke(null, objEntity, objWorld, CreatureSpawnEvent.SpawnReason.SPAWNER);
                } else {
                    methodAddEntity.invoke(objWorld, objEntity, CreatureSpawnEvent.SpawnReason.SPAWNER);
                }

                if (data.isSpawnOnFire()) craftEntity.setFireTicks(160);

                craftEntity.setMetadata("ES", new FixedMetadataValue(plugin, data.getIdentifyingName()));

                if (mcmmo)
                    craftEntity.setMetadata("mcMMO: Spawned Entity", new FixedMetadataValue(plugin, true));

                if (Settings.NO_AI.getBoolean())
                    EntityUtils.setUnaware(objEntity);

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

    private boolean canSpawn(Object objEntityInsentient, SpawnerData data, Location location) {
        try {
            if (!(boolean) methodEntityInsentientCanSpawn.invoke(objEntityInsentient))
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
        return type == Material.WATER || type == Material.valueOf("STATIONARY_WATER");
    }

    public enum TypeTranslations {
        VINDICATOR("vindication illager", "VindicationIllager"),
        SNOWMAN("snowman", "SnowMan"),
        PIG_ZOMBIE("zombie_pigman", "PigZombie"),
        EVOKER("evocation_illager", "EvocationIllager"),
        ILLUSIONER("illusion_illager", "IllusionIllager"),
        IRON_GOLEM("villager_golem", "VillagerGolem"),
        MUSHROOM_COW("mooshroom", "MushroomCow"),
        MAGMA_CUBE("magma_cube", "LavaSlime");

        private String lower;
        private String upper;

        TypeTranslations(String lower, String upper) {
            this.lower = lower;
            this.upper = upper;
        }

        public static Methods.Tuple<String, String> fromType(EntityType type) {
            try {
                TypeTranslations typeTranslation = valueOf(type.name());
                return new Methods.Tuple<>(typeTranslation.lower, typeTranslation.upper);
            } catch (Exception e) {
                String lower = type.name().toLowerCase();
                String upper = StringUtils.capitaliseAllWords(lower.replace("_", " ")).replace(" ", "");
                return new Methods.Tuple<>(lower, upper);
            }
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
        if (!(object instanceof SpawnOptionEntity_1_12)) return false;

        SpawnOptionEntity_1_12 other = (SpawnOptionEntity_1_12) object;
        return Arrays.equals(types, other.types);
    }

}

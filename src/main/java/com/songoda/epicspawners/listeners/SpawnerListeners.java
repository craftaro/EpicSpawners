package com.songoda.epicspawners.listeners;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.Spawner;
import com.songoda.epicspawners.spawners.SpawnerItem;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * Created by songoda on 2/25/2017.
 */
public class SpawnerListeners implements Listener {

    @SuppressWarnings("unchecked")
    @EventHandler
    public void onSpawn(SpawnerSpawnEvent e) {
        try {
            String type = e.getEntityType().name();
            if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(e.getSpawner().getBlock()) + ".type")) {
                if (!EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(e.getSpawner().getBlock()) + ".type").equals("OMNI"))
                    type = EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(e.getSpawner().getBlock()) + ".type");
            }

            if (e.getEntityType() == EntityType.FALLING_BLOCK)
                return;
            String sloc = Arconix.pl().getApi().serialize().serializeLocation(e.getSpawner().getBlock());
            Spawner eSpawner = new Spawner(e.getSpawner().getBlock());

            if (EpicSpawners.getInstance().dataFile.getConfig().getInt("data.spawner." + sloc) != 0) {
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(EpicSpawners.getInstance(), eSpawner::updateDelay, 10L);
            }

            if ((!e.getEntityType().equals(EntityType.IRON_GOLEM) && !e.getEntityType().equals(EntityType.GHAST) || !EpicSpawners.getInstance().getConfig().getBoolean("Entity.Use Default Minecraft Spawn Method For Large Entities")) && !e.isCancelled() && e.getLocation() != null) {
                e.getEntity().remove();
                if (EpicSpawners.getInstance().getConfig().getBoolean("Spawner Drops.Allow Killing Mobs To Drop Spawners")) {
                    EpicSpawners.getInstance().dataFile.getConfig().set("data.Entities." + e.getEntity().getUniqueId(), true);
                }


                if (Methods.countEntitiesAroundLoation(e.getSpawner().getLocation()) < EpicSpawners.getInstance().getConfig().getInt("Main.Max Entities Around Single Spawner")) {
                    long lastSpawn = 1001;
                    if (EpicSpawners.getInstance().lastSpawn.containsKey(e.getSpawner().getLocation())) {
                        lastSpawn = (new Date()).getTime() - EpicSpawners.getInstance().lastSpawn.get(e.getSpawner().getLocation()).getTime();
                    }
                    if (lastSpawn >= 1000) {
                        if (EpicSpawners.getInstance().dataFile.getConfig().contains("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(e.getSpawner().getLocation()) + ".type")
                                && EpicSpawners.getInstance().dataFile.getConfig().getString("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(e.getSpawner().getLocation()) + ".type").equals("OMNI")) {
                            List<SpawnerItem> list = EpicSpawners.getInstance().getApi().convertFromList(EpicSpawners.getInstance().dataFile.getConfig().getStringList("data.spawnerstats." + Arconix.pl().getApi().serialize().serializeLocation(e.getSpawner().getLocation()) + ".entities"));
                            for (SpawnerItem ent : list) {
                                if (ent.getType().equals(e.getSpawner().getSpawnedType())) continue;

                                int high = eSpawner.getMulti();
                                int low = eSpawner.getMulti();

                                if (ent.getMulti() > eSpawner.getMulti()) {
                                    high = ent.getMulti();
                                } else {
                                    low = ent.getMulti();
                                }

                                String[] randomLowHigh = EpicSpawners.getInstance().getConfig().getString("Main.Random Amount Added To Each Spawn").split(":");

                                int rand = ThreadLocalRandom.current().nextInt(Integer.valueOf(randomLowHigh[0]), Integer.valueOf(randomLowHigh[1]));
                                int times = high - low + rand + eSpawner.getBoost();

                                for (String entt : (ArrayList<String>) EpicSpawners.getInstance().spawnerFile.getConfig().getList("Entities." + Methods.getTypeFromString(ent.getType()) + ".entities")) {
                                    spawnEntity(e.getSpawner().getLocation(), EntityType.valueOf(entt), times);
                                }
                            }
                        }


                        String[] randomLowHigh = EpicSpawners.getInstance().getConfig().getString("Main.Random Amount Added To Each Spawn").split(":");

                        int rand = ThreadLocalRandom.current().nextInt(Integer.valueOf(randomLowHigh[0]), Integer.valueOf(randomLowHigh[1]));

                        String equation = EpicSpawners.getInstance().getConfig().getString("Main.Equations.Mobs Spawned Per Spawn");
                        equation = equation.replace("{RAND}", rand + "");
                        equation = equation.replace("{MULTI}", eSpawner.getMulti() + "");

                        int times;
                        if (!EpicSpawners.getInstance().cache.containsKey(equation)) {
                            ScriptEngineManager mgr = new ScriptEngineManager();
                            ScriptEngine engine = mgr.getEngineByName("JavaScript");
                            times = (int) Math.round(Double.parseDouble(engine.eval(equation).toString()));
                            EpicSpawners.getInstance().cache.put(equation, times);
                        } else {
                            times = EpicSpawners.getInstance().cache.get(equation);
                        }
                        int size = EpicSpawners.getInstance().spawnerFile.getConfig().getList("Entities." + Methods.getTypeFromString(type) + ".entities", new ArrayList<>()).size();
                        if (size == 0)
                            size = 1;
                        times = (int) Math.ceil((times + eSpawner.getBoost()) / size);

                        if (EpicSpawners.getInstance().spawnerFile.getConfig().contains("Entities." + Methods.getTypeFromString(type) + ".entities")) {
                            for (String ent : (ArrayList<String>) EpicSpawners.getInstance().spawnerFile.getConfig().getList("Entities." + Methods.getTypeFromString(type) + ".entities")) {
                                spawnEntity(e.getSpawner().getLocation(), EntityType.valueOf(ent), times);
                            }
                        }
                        EpicSpawners.getInstance().lastSpawn.put(e.getSpawner().getLocation(), new Date());
                    }

                }
                return;
            }

            int spawnAmt = 1;
            if (EpicSpawners.getInstance().dataFile.getConfig().getInt("data.spawnerstats." + sloc + ".spawns") != 0) {
                spawnAmt = EpicSpawners.getInstance().dataFile.getConfig().getInt("data.spawnerstats." + sloc + ".spawns") + 1;
            }
            EpicSpawners.getInstance().dataFile.getConfig().set("data.spawnerstats." + sloc + ".spawns", spawnAmt);

        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    public void spawnEntity(Location location, EntityType type, int times) {
        try {
            Block b = location.getBlock();
            if (b.isBlockPowered() && EpicSpawners.getInstance().getConfig().getBoolean("Main.Redstone Power Deactivates Spawners"))
                return;

            String sloc = Arconix.pl().getApi().serialize().serializeLocation(location);

            int spawnAmt = 1;
            if (EpicSpawners.getInstance().dataFile.getConfig().getInt("data.spawnerstats." + sloc + ".spawns") != 0) {
                spawnAmt = EpicSpawners.getInstance().dataFile.getConfig().getInt("data.spawnerstats." + sloc + ".spawns") + 1;
            }
            int stack = 0;
            if (EpicSpawners.getInstance().getServer().getPluginManager().getPlugin("StackMob") != null) {
                stack = times;
                times = 1;
            }
            int num = 0;
            while (num != times) {
                Location spot = null;
                boolean in = false;

                int amt = 0;
                while (!in && amt <= 25) {
                    double testX = ThreadLocalRandom.current().nextDouble(-1, 1);
                    double testY = ThreadLocalRandom.current().nextDouble(-1, 2);
                    double testZ = ThreadLocalRandom.current().nextDouble(-1, 1);

                    double x = location.getX() + testX * (double) 3;
                    double y = location.getY() + testY;
                    double z = location.getZ() + testZ * (double) 3;

                    spot = new Location(location.getWorld(), x, y, z);
                    if (canSpawn(type, spot))
                        in = true;

                    amt++;
                }
                if (in) {

                    if (spot != null) {
                        float x = (float) (0 + (Math.random() * 1));
                        float y = (float) (0 + (Math.random() * 2));
                        float z = (float) (0 + (Math.random() * 1));
                        Arconix.pl().getApi().packetLibrary.getParticleManager().broadcastParticle(spot, x, y, z, 0, EpicSpawners.getInstance().getConfig().getString("Entity.Spawn Particle Effect"), 5);

                        Location loc = spot.clone();
                        loc.subtract(0, 1, 0);
                        if (type.equals(EntityType.IRON_GOLEM)) {

                            spot.add(.5, .5, .5);

                        } else {
                            spot = spot.clone().getBlock().getLocation();

                            double spawnX = ThreadLocalRandom.current().nextDouble(0.4, 0.6);
                            double spawnZ = ThreadLocalRandom.current().nextDouble(0.4, 0.6);

                            spot.add(spawnX, .5, spawnZ);
                        }

                        spawnMob(location, spot, type, CreatureSpawnEvent.SpawnReason.SPAWNER, stack);
                    }
                }
                spawnAmt++;
                num++;
            }
            EpicSpawners.getInstance().dataFile.getConfig().set("data.spawnerstats." + sloc + ".spawns", spawnAmt);
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    private static Method spawnMethod = null;
    private static Method methodGetHandle = null;
    private static Method methodK = null;


    public Entity spawnMob(Location spawnerLoc, Location loc, EntityType type, CreatureSpawnEvent.SpawnReason reason, int stack) {
        try {
            World world = loc.getWorld();
            Class<? extends Entity> clazz = type.getEntityClass();
            Entity e;
            if (EpicSpawners.getInstance().v1_12 || EpicSpawners.getInstance().v1_11) {
                if (spawnMethod == null)
                    spawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, Consumer.class, CreatureSpawnEvent.SpawnReason.class);
                e = (Entity) spawnMethod.invoke(world, loc, clazz, null, reason);
            } else {
                if (spawnMethod == null)
                    spawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, CreatureSpawnEvent.SpawnReason.class);
                e = (Entity) spawnMethod.invoke(world, loc, clazz, reason);
            }

            Spawner spawner = new Spawner(spawnerLoc);

            if (spawner.isSpawningOnFire())
                e.setFireTicks(160);

            Bukkit.getScheduler().runTaskLater(EpicSpawners.getInstance(), () -> {
                try {
                    if (!spawner.isSpawningWithAI() && !EpicSpawners.getInstance().v1_7) {
                        if (EpicSpawners.getInstance().v1_8) {
                            Class<?> claz = e.getClass();

                            if (methodGetHandle == null)
                                methodGetHandle = claz.getDeclaredMethod("getHandle");
                            Object nmsEntity = methodGetHandle.invoke(e);

                            if (methodK == null)
                                methodK = nmsEntity.getClass().getMethod("k", Boolean.TYPE);

                            methodK.invoke(nmsEntity, true);
                        } else {
                            ((LivingEntity) e).setAI(false);
                        }
                    }
                } catch (Exception ex) {
                    Debugger.runReport(ex);
                }
            }, EpicSpawners.getInstance().getConfig().getLong("Main.Ticks Until AI Disabled"));

            if (EpicSpawners.getInstance().getServer().getPluginManager().getPlugin("StackMob") != null && stack != 0) {
                uk.antiperson.stackmob.StackMob sm = ((uk.antiperson.stackmob.StackMob) Bukkit.getPluginManager().getPlugin("StackMob"));
                if (!sm.config.getCustomConfig().getStringList("no-stack-types").contains(e.getType().toString())) {
                    e.setMetadata(uk.antiperson.stackmob.tools.extras.GlobalValues.METATAG, new FixedMetadataValue(sm, stack));
                    e.setMetadata(uk.antiperson.stackmob.tools.extras.GlobalValues.NO_SPAWN_STACK, new FixedMetadataValue(sm, true));
                }
            }

            if (EpicSpawners.getInstance().getConfig().getBoolean("Spawner Drops.Allow Killing Mobs To Drop Spawners")) {
                EpicSpawners.getInstance().dataFile.getConfig().set("data.Entities." + e.getUniqueId(), true);
            }
            return e;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }


    public boolean canSpawn(EntityType type, Location location) {
        boolean canSpawn = true;
        try {

            String spawnBlocks = EpicSpawners.getInstance().spawnerFile.getConfig().getString("Entities." + Methods.getTypeFromString(Methods.getType(type)) + ".Spawn-Block");

            List<String> blocks = Arrays.asList(spawnBlocks.split("\\s*,\\s*"));

            if (!Methods.isAir(location.getBlock().getType()) && (!isWater(location.getBlock().getType()) && !blocks.contains("WATER"))) {
                canSpawn = false;
            }

            boolean canSpawnUnder = false;
            if (canSpawn != false) {
                for (String block : blocks) {
                    Location loc = location.clone().subtract(0, 1, 0);
                    if (loc.getBlock().getType().toString().equalsIgnoreCase(block) || isWater(loc.getBlock().getType()) && blocks.contains("WATER")) {
                        canSpawnUnder = true;
                    }
                }
                canSpawn = canSpawnUnder;
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return canSpawn;
    }

    public boolean isWater(Material type) {
        try {
            if (type == Material.WATER || type == Material.STATIONARY_WATER) {
                return true;
            }

        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        try {
            if (EpicSpawners.getInstance().getConfig().getBoolean("Entity.Hostile Mobs Attack Second")) {
                if (event.getEntity().getLastDamageCause() != null) {
                    if (event.getEntity().getLastDamageCause().getCause().name().equals("ENTITY_ATTACK")) {
                        return;
                    }
                }
                event.setCancelled(true);
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}

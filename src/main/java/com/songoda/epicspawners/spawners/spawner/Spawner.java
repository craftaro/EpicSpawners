package com.songoda.epicspawners.spawners.spawner;

import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.api.events.SpawnerChangeEvent;
import com.songoda.epicspawners.boost.BoostData;
import com.songoda.epicspawners.gui.GUISpawnerOverview;
import com.songoda.epicspawners.particles.ParticleType;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.condition.SpawnCondition;
import com.songoda.epicspawners.utils.CostType;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.*;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.time.Instant;
import java.util.*;

public class Spawner {

    private static ScriptEngine engine = null;
    //Holds the different types of spawners contained by this creatureSpawner.
    private final Deque<SpawnerStack> spawnerStacks = new ArrayDeque<>();
    private Location location;
    private int spawnCount;
    private String omniState = null;
    private UUID placedBy = null;
    private CreatureSpawner creatureSpawner = null;

    public Spawner(Location location) {
        this.location = location;
        if (engine == null) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            engine = mgr.getEngineByName("JavaScript");
        }
    }


    public boolean spawn() {
        EpicSpawners instance = EpicSpawners.getInstance();

        if (getFirstStack().getSpawnerData() == null) return false;

        Location particleLocation = location.clone();
        particleLocation.add(.5, .5, .5);

        SpawnerData spawnerData = getFirstStack().getSpawnerData();

        if (!isRedstonePowered()) return false;

        ParticleType particleType = spawnerData.getSpawnerSpawnParticle();
        if (particleType != ParticleType.NONE && ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12)) {
            float x = (float) (0 + (Math.random() * .8));
            float y = (float) (0 + (Math.random() * .8));
            float z = (float) (0 + (Math.random() * .8));
            particleLocation.getWorld().spawnParticle(Particle.valueOf(particleType.getEffect()), particleLocation, 0, x, y, z, 0);
        }

        for (SpawnerStack stack : getSpawnerStacks()) {
            stack.getSpawnerData().spawn(this, stack);
        }

        if (spawnerData.getSpawnLimit() != -1 && spawnCount * spawnerStacks.size() > spawnerData.getSpawnLimit()) {
            this.location.getBlock().setType(Material.AIR);

            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_9))
                location.getWorld().spawnParticle(Particle.LAVA, location.clone().add(.5, .5, .5), 5, 0, 0, 0, 5);
            location.getWorld().playSound(location, ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)
                    ? Sound.ENTITY_GENERIC_EXPLODE : Sound.valueOf("EXPLODE"), 10, 10);

            instance.getSpawnerManager().removeSpawnerFromWorld(location);
            instance.clearHologram(this);
            return true;
        }


        updateDelay();

        return true;
    }


    public void addSpawnerStack(SpawnerStack spawnerStack) {
        this.spawnerStacks.addFirst(spawnerStack);
    }


    public Location getLocation() {
        return location.clone();
    }


    public int getX() {
        return location.getBlockX();
    }


    public int getY() {
        return location.getBlockY();
    }


    public int getZ() {
        return location.getBlockZ();
    }


    public World getWorld() {
        return location.getWorld();
    }


    public CreatureSpawner getCreatureSpawner() {
        if (!getWorld().isChunkLoaded(getX() >> 4, getZ() >> 4))
            return null;
        if (creatureSpawner == null) {
            if (location.getBlock().getType() != (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13) ? Material.SPAWNER : Material.valueOf("MOB_SPAWNER"))) {
                EpicSpawners.getInstance().getSpawnerManager().removeSpawnerFromWorld(location);
                return null;
            }
            this.creatureSpawner = (CreatureSpawner) location.getBlock().getState();
        }
        return creatureSpawner;
    }

    public SpawnerStack getFirstStack() {
        if (spawnerStacks.size() == 0) return null;
        return spawnerStacks.getFirst();
    }

    public int getSpawnerDataCount() {
        int multi = 0;
        for (SpawnerStack stack : spawnerStacks) {
            multi += stack.getStackSize();
        }
        return multi;
    }

    public boolean checkConditions() {
        for (SpawnerStack stack : spawnerStacks) {
            if (stack.getSpawnerData() == null) continue;
            for (SpawnCondition spawnCondition : stack.getSpawnerData().getConditions()) {
                if (!spawnCondition.isMet(this)) return false;
            }
        }
        return true;
    }

    public boolean isRedstonePowered() {
        return (!location.getBlock().isBlockPowered()
                && !location.getBlock().isBlockIndirectlyPowered())
                || !Settings.REDSTONE_ACTIVATE.getBoolean();
    }

    public void overview(Player player) {
        EpicSpawners instance = EpicSpawners.getInstance();
        if (!player.hasPermission("epicspawners.overview")
                || (getPlacedBy() == null && Settings.DISABLE_NATURAL_SPAWNERS.getBoolean())) return;
        new GUISpawnerOverview(instance, this, player);
    }

    public void convert(SpawnerData type, Player player, boolean forced) {
        EpicSpawners instance = EpicSpawners.getInstance();
        if (!EconomyManager.isEnabled()) {
            player.sendMessage("Economy not enabled.");
            return;
        }
        double price = type.getConvertPrice() * getSpawnerDataCount();

        if (!forced && !EconomyManager.hasBalance(player, price)) {
            EpicSpawners.getInstance().getLocale().getMessage("event.upgrade.cannotafford").sendPrefixedMessage(player);
            return;
        }

        SpawnerChangeEvent event = new SpawnerChangeEvent(player, this, getFirstStack().getSpawnerData(), type);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        this.spawnerStacks.getFirst().setSpawnerData(type);
        try {
            this.creatureSpawner.setSpawnedType(EntityType.valueOf(type.getIdentifyingName().toUpperCase()));
        } catch (Exception e) {
            this.creatureSpawner.setSpawnedType(EntityType.DROPPED_ITEM);
        }
        this.creatureSpawner.update();

        EpicSpawners.getInstance().getLocale().getMessage("event.convert.success").sendPrefixedMessage(player);

        instance.updateHologram(this);
        player.closeInventory();
        if (!forced)
            EconomyManager.withdrawBalance(player, price);
    }

    public int getUpgradeCost(CostType type) {
        int cost = 0;
        try {
            if (type == CostType.ECONOMY) {
                if (getFirstStack().getSpawnerData().getUpgradeCostEconomy() != 0)
                    cost = (int) getFirstStack().getSpawnerData().getUpgradeCostEconomy();
                else
                    cost = Settings.UPGRADE_COST_ECONOMY.getInt();
                if (Settings.USE_CUSTOM_UPGRADE_EQUATION.getBoolean()) {
                    String math = Settings.COST_EQUATION_ECONOMY.getString().replace("{ECOCost}", Integer.toString(cost)).replace("{Level}", Integer.toString(getSpawnerDataCount()));
                    cost = (int) Math.round(Double.parseDouble(engine.eval(math).toString()));
                }
            } else if (type == CostType.EXPERIENCE) {
                if (getFirstStack().getSpawnerData().getUpgradeCostExperience() != 0) {
                    cost = getFirstStack().getSpawnerData().getUpgradeCostExperience();
                } else
                    cost = Settings.UPGRADE_COST_EXPERIANCE.getInt();
                if (Settings.USE_CUSTOM_UPGRADE_EQUATION.getBoolean()) {
                    String math = Settings.COST_EQUATION_EXPERIANCE.getString().replace("{XPCost}", Integer.toString(cost)).replace("{Level}", Integer.toString(getSpawnerDataCount()));
                    cost = (int) Math.round(Double.parseDouble(engine.eval(math).toString()));
                }
            }
        } catch (ScriptException e) {
            cost = Integer.MAX_VALUE;
        }
        return cost;
    }


    public boolean unstack(Player player) {
        EpicSpawners instance = EpicSpawners.getInstance();
        SpawnerStack stack = spawnerStacks.getFirst();

        int stackSize = 1;

        if (player.isSneaking() && Settings.SNEAK_FOR_STACK.getBoolean()
                || Settings.ONLY_DROP_STACKED.getBoolean()) {
            stackSize = stack.getStackSize();
        }

        if (Settings.SOUNDS_ENABLED.getBoolean() && ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12)) {
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.6F, 15.0F);
        }
        ItemStack item = stack.getSpawnerData().toItemStack(1, stackSize);


        ItemStack inHand = player.getInventory().getItemInHand();
        if (Settings.SILKTOUCH_SPAWNERS.getBoolean()
                && inHand.hasItemMeta()
                && inHand.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)
                && inHand.getEnchantmentLevel(Enchantment.SILK_TOUCH) >= Settings.SILKTOUCH_MIN_LEVEL.getInt()
                && player.hasPermission("epicspawners.silkdrop." + stack.getSpawnerData().getIdentifyingName().replace(' ', '_'))
                || player.hasPermission("epicspawners.no-silk-drop")) {
            if (Settings.SPAWNERS_TO_INVENTORY.getBoolean()) {
                Collection<ItemStack> leftOver = player.getInventory().addItem(item).values();
                for (ItemStack itemStack : leftOver) {
                    player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                }
            } else if (!Settings.ONLY_DROP_PLACED.getBoolean() || placedBy != null) {
                int ch = Integer.parseInt((placedBy != null
                        ? Settings.SILKTOUCH_PLACED_SPAWNER_DROP_CHANCE.getString() : Settings.SILKTOUCH_NATURAL_SPAWNER_DROP_CHANCE.getString()).replace("%", ""));

                double rand = Math.random() * 100;

                if (rand - ch < 0 || ch == 100) {
                    if (Settings.SPAWNERS_TO_INVENTORY.getBoolean() && player.getInventory().firstEmpty() != -1)
                        player.getInventory().addItem(item);
                    else
                        location.getWorld().dropItemNaturally(location.clone().add(.5, 0, .5), item);
                }
            }
        }

        if (stack.getStackSize() > 1 && stackSize == 1) {
            stack.setStackSize(stack.getStackSize() - 1);
            return true;
        }

        spawnerStacks.removeFirst();

        if (spawnerStacks.size() != 0) return true;

        location.getBlock().setType(Material.AIR);
        EpicSpawners.getInstance().getSpawnerManager().removeSpawnerFromWorld(location);
        instance.clearHologram(this);
        return true;
    }

    public boolean preStack(Player player, ItemStack itemStack) {
        return stack(player, EpicSpawners.getInstance().getSpawnerManager().getSpawnerData(itemStack), Methods.getStackSizeFromItem(itemStack));
    }

    public boolean stack(Player player, SpawnerData data, int amount) {
        EpicSpawners instance = EpicSpawners.getInstance();

        int max = Settings.SPAWNERS_MAX.getInt();
        int currentStackSize = getSpawnerDataCount();

        if (max <= 0 || getSpawnerDataCount() == max) {
            instance.getLocale().getMessage("event.upgrade.maxed").sendPrefixedMessage(player);
            return false;
        }

        if (data != getIdentifyingData()
                && (!Settings.OMNI_SPAWNERS.getBoolean() || !player.hasPermission("epicspawners.omni")))
            return false;

        if ((getSpawnerDataCount() + amount) > max) {
            ItemStack item = data.toItemStack(1, (getSpawnerDataCount() + amount) - max);
            if (player.getInventory().firstEmpty() == -1)
                location.getWorld().dropItemNaturally(location.clone().add(.5, 0, .5), item);
            else
                player.getInventory().addItem(item);

            amount = max - currentStackSize;
        }


        for (SpawnerStack stack : spawnerStacks) {
            if (!stack.getSpawnerData().equals(data)) continue;
            stack.setStackSize(stack.getStackSize() + amount);
            upgradeFinal(player, currentStackSize);

            if (player.getGameMode() != GameMode.CREATIVE)
                Methods.takeItem(player, 1);

            return true;
        }

        SpawnerStack stack = new SpawnerStack(data, amount);
        spawnerStacks.push(stack);

        if (player.getGameMode() != GameMode.CREATIVE)
            Methods.takeItem(player, 1);

        return true;
    }

    private void upgradeFinal(Player player, int oldStackSize) {
        EpicSpawners plugin = EpicSpawners.getInstance();
        int currentStackSize = getSpawnerDataCount();

        SpawnerChangeEvent event = new SpawnerChangeEvent(player, this, currentStackSize, oldStackSize);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        if (getSpawnerDataCount() != Settings.SPAWNERS_MAX.getInt())
            plugin.getLocale().getMessage("event.upgrade.success")
                    .processPlaceholder("level", currentStackSize).sendPrefixedMessage(player);
        else
            plugin.getLocale().getMessage("event.upgrade.successmaxed")
                    .processPlaceholder("level", currentStackSize).sendPrefixedMessage(player);

        Location loc = location.clone();
        loc.setX(loc.getX() + .5);
        loc.setY(loc.getY() + .5);
        loc.setZ(loc.getZ() + .5);
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) {
            player.getWorld().spawnParticle(org.bukkit.Particle.valueOf(Settings.UPGRADE_PARTICLE_TYPE.getString()), loc, 100, .5, .5, .5);
        }

        plugin.updateHologram(this);

        if (!Settings.SOUNDS_ENABLED.getBoolean()
                || !ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) {
            return;
        }
        if (currentStackSize != Settings.SPAWNERS_MAX.getInt()) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6F, 15.0F);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2F, 25.0F);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 2F, 25.0F);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.2F, 35.0F), 5L);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.8F, 35.0F), 10L);
        }
    }

    public void upgrade(Player player, CostType type) {
        EpicSpawners plugin = EpicSpawners.getInstance();
        int cost = getUpgradeCost(type);

        boolean maxed = getSpawnerDataCount() == Settings.SPAWNERS_MAX.getInt();

        if (maxed) {
            plugin.getLocale().getMessage("event.upgrade.maxed").sendPrefixedMessage(player);
            return;
        }
        if (type == CostType.ECONOMY) {
            if (!EconomyManager.isEnabled()) {
                player.sendMessage("Economy not enabled.");
                return;
            }
            if (!EconomyManager.hasBalance(player, cost)) {
                plugin.getLocale().getMessage("event.upgrade.cannotafford").sendPrefixedMessage(player);
                return;
            }
            EconomyManager.withdrawBalance(player, cost);
            int oldMultiplier = getSpawnerDataCount();
            spawnerStacks.getFirst().setStackSize(spawnerStacks.getFirst().getStackSize() + 1);
            upgradeFinal(player, oldMultiplier);
        } else if (type == CostType.EXPERIENCE) {
            if (player.getLevel() >= cost || player.getGameMode() == GameMode.CREATIVE) {
                if (player.getGameMode() != GameMode.CREATIVE) {
                    player.setLevel(player.getLevel() - cost);
                }
                int oldMultiplier = getSpawnerDataCount();
                spawnerStacks.getFirst().setStackSize(spawnerStacks.getFirst().getStackSize() + 1);
                upgradeFinal(player, oldMultiplier);
            } else {
                plugin.getLocale().getMessage("event.upgrade.cannotafford").sendPrefixedMessage(player);
            }
        }
    }


    public int getBoost() {
        EpicSpawners instance = EpicSpawners.getInstance();
        if (placedBy == null) return 0;

        Set<BoostData> boosts = instance.getBoostManager().getBoosts();

        if (boosts.size() == 0) return 0;

        int amountToBoost = 0;

        for (BoostData boostData : boosts) {
            if (System.currentTimeMillis() >= boostData.getEndTime()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> instance.getBoostManager().removeBoostFromSpawner(boostData), 1);
                continue;
            }

            switch (boostData.getBoostType()) {
                case LOCATION:
                    if (!location.equals(boostData.getData())) continue;
                    break;
                case PLAYER:
                    if (!placedBy.toString().equals(boostData.getData().toString())) continue;
                    break;
            }
            amountToBoost += boostData.getAmtBoosted();
        }
        return amountToBoost;
    }


    public Instant getBoostEnd() { //ToDo: Wrong.
        EpicSpawners instance = EpicSpawners.getInstance();

        Set<BoostData> boosts = instance.getBoostManager().getBoosts();

        if (boosts.size() == 0) return null;

        for (BoostData boostData : boosts) {
            if (System.currentTimeMillis() >= boostData.getEndTime()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> instance.getBoostManager().removeBoostFromSpawner(boostData), 1);
                continue;
            }

            switch (boostData.getBoostType()) {
                case LOCATION:
                    if (!location.equals(boostData.getData())) continue;
                    break;
                case PLAYER:
                    if (!placedBy.toString().equals(boostData.getData().toString())) continue;
                    break;
            }

            return Instant.ofEpochMilli(boostData.getEndTime());
        }
        return null;
    }


    public int updateDelay() {
        int max = 0;
        int min = 0;
        for (SpawnerStack stack : spawnerStacks) {
            String tickRate = stack.getSpawnerData().getTickRate();

            String[] tick = tickRate.contains(":") ? tickRate.split(":") : new String[]{tickRate, tickRate};

            int tickMin = Integer.parseInt(tick[1]);
            int tickMax = Integer.parseInt(tick[0]);
            if (max == 0 && min == 0) {
                max = tickMax;
                min = tickMin;
                continue;
            }
            if ((max + min) < (tickMax + min)) {
                max = tickMax;
                min = tickMin;
            }
        }
        int extraTicks = Settings.EXTRA_SPAWN_TICKS.getInt();

        if (getSpawnerDataCount() == 0) return 0;

        int delay = (int) (Math.random() * max + min) + extraTicks;

        getCreatureSpawner().setDelay(delay);
        getCreatureSpawner().update();

        return delay;
    }


    public String getIdentifyingName() {
        String name = spawnerStacks.getFirst().getSpawnerData().getIdentifyingName();

        if (spawnerStacks.size() > 1)
            name = EpicSpawners.getInstance().getSpawnerManager().getSpawnerData("omni").getIdentifyingName();

        return name;
    }


    public SpawnerData getIdentifyingData() {
        SpawnerData name = spawnerStacks.getFirst().getSpawnerData();

        if (spawnerStacks.size() > 1)
            name = EpicSpawners.getInstance().getSpawnerManager().getSpawnerData("omni");

        return name;
    }


    public String getDisplayName() {
        if (spawnerStacks.size() == 0) {
            return Methods.getTypeFromString(creatureSpawner.getSpawnedType().name());
        } else if (spawnerStacks.size() > 1) {
            return EpicSpawners.getInstance().getSpawnerManager().getSpawnerData("omni").getDisplayName();
        }

        return spawnerStacks.getFirst().getSpawnerData().getDisplayName();
    }


    public Collection<SpawnerStack> getSpawnerStacks() {
        return Collections.unmodifiableCollection(spawnerStacks);
    }

    public void clearSpawnerStacks() {
        spawnerStacks.clear();
    }

    public OfflinePlayer getPlacedBy() {
        if (placedBy == null) return null;
        return Bukkit.getOfflinePlayer(placedBy);
    }

    public void setPlacedBy(Player placedBy) {
        this.placedBy = placedBy.getUniqueId();
    }

    public void setPlacedBy(UUID placedBy) {
        this.placedBy = placedBy;
    }

    public int getSpawnCount() {
        return spawnCount;
    }

    public void setSpawnCount(int spawnCount) {
        this.spawnCount = spawnCount;
    }

    public void addSpawn() {
        this.spawnCount++;
    }

    public String getOmniState() {
        return omniState;
    }

    public void setOmniState(String omniState) {
        this.omniState = omniState;
    }

    public void destroy(EpicSpawners plugin) {
        plugin.getAppearanceTask().removeDisplayItem(this);
        plugin.getSpawnerManager().removeSpawnerFromWorld(location);
        plugin.clearHologram(this);
    }

    @Override
    public int hashCode() {
        int result = 31 * (location == null ? 0 : location.hashCode());
        result = 31 * result + (placedBy == null ? 0 : placedBy.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Spawner)) return false;

        Spawner other = (Spawner) obj;
        return Objects.equals(location, other.location) && Objects.equals(placedBy, other.placedBy);
    }

    @Override
    public String toString() {
        return "Spawner:{"
                + "Owner:\"" + placedBy + "\","
                + "Location:{"
                + "World:\"" + location.getWorld().getName() + "\","
                + "X:" + location.getBlockX() + ","
                + "Y:" + location.getBlockY() + ","
                + "Z:" + location.getBlockZ()
                + "},"
                + "StackCount:" + spawnerStacks.size()
                + "}";
    }
}

package com.songoda.epicspawners.spawners.spawner;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.References;
import com.songoda.epicspawners.api.CostType;
import com.songoda.epicspawners.api.EpicSpawners;
import com.songoda.epicspawners.api.EpicSpawnersAPI;
import com.songoda.epicspawners.api.events.SpawnerChangeEvent;
import com.songoda.epicspawners.api.particles.ParticleType;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.api.spawner.condition.SpawnCondition;
import com.songoda.epicspawners.boost.BoostData;
import com.songoda.epicspawners.gui.GUISpawnerOverview;
import com.songoda.epicspawners.hook.HookType;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.SettingsManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ESpawner implements Spawner {

    private static final ThreadLocalRandom rand = ThreadLocalRandom.current();
    private static ScriptEngine engine = null;
    //Holds the different types of spawners contained by this creatureSpawner.
    private final Deque<SpawnerStack> spawnerStacks = new ArrayDeque<>();
    private Location location;
    private int spawnCount;
    private String omniState = null;
    private UUID placedBy = null;
    private CreatureSpawner creatureSpawner = null;
    //ToDo: Use this for all spawner things (Like items, commands and what not) instead of the old shit
    //ToDO: There is a weird error that is triggered when a spawner is not found in the config.
    private Map<Location, Date> lastSpawns = new HashMap<>();

    public ESpawner(Location location) {
        this.location = location;
        if (engine == null) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            engine = mgr.getEngineByName("JavaScript");
        }
    }

    @Override
    public boolean spawn() {
        EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();
        long lastSpawn = lastSpawns.containsKey(location) ? new Date().getTime() - lastSpawns.get(location).getTime() : 1001;

        if (lastSpawn >= 1000) {
            lastSpawns.put(location, new Date());
        } else return false;

        if (getFirstStack().getSpawnerData() == null) return false;

        Location particleLocation = location.clone();
        particleLocation.add(.5, .5, .5);

        SpawnerData spawnerData = getFirstStack().getSpawnerData();

        if (!isRedstonePowered()) return false;

        ParticleType particleType = spawnerData.getSpawnerSpawnParticle();
        if (particleType != ParticleType.NONE) {
            float x = (float) (0 + (Math.random() * .8));
            float y = (float) (0 + (Math.random() * .8));
            float z = (float) (0 + (Math.random() * .8));
            particleLocation.getWorld().spawnParticle(particleType.getEffect(), particleLocation, 0, x, y, z, 0);
        }

        for (SpawnerStack stack : getSpawnerStacks()) {
            ((ESpawnerData) stack.getSpawnerData()).spawn(this, stack);
        }
        Bukkit.getScheduler().runTaskLater(instance, this::updateDelay, 10);
        return true;
    }

    @Override
    public void addSpawnerStack(SpawnerStack spawnerStack) {
        this.spawnerStacks.addFirst(spawnerStack);
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public int getX() {
        return location.getBlockX();
    }

    @Override
    public int getY() {
        return location.getBlockY();
    }

    @Override
    public int getZ() {
        return location.getBlockZ();
    }

    @Override
    public World getWorld() {
        return location.getWorld();
    }

    @Override
    public CreatureSpawner getCreatureSpawner() {
        if (creatureSpawner == null) {
            if (location.getBlock().getType() != Material.SPAWNER) {
                EpicSpawnersPlugin.getInstance().getSpawnerManager().removeSpawnerFromWorld(location);
                return null;
            }
            this.creatureSpawner = (CreatureSpawner) location.getBlock().getState();
        }
        return creatureSpawner;
    }

    @Override
    public SpawnerStack getFirstStack() {
        if (spawnerStacks.size() == 0) return null;
        return spawnerStacks.getFirst();
    }

    @Override
    public int getSpawnerDataCount() {
        int multi = 0;
        for (SpawnerStack stack : spawnerStacks) {
            multi += stack.getStackSize();
        }
        return multi;
    }

    @Override
    public boolean checkConditions() {
        for (SpawnerStack stack : spawnerStacks) {
            if (stack.getSpawnerData() == null) continue;
            for (SpawnCondition spawnCondition : stack.getSpawnerData().getConditions()) {
                if (!spawnCondition.isMet(this)) return false;
            }
        }
        return true;
    }

    @Override
    public boolean isRedstonePowered() {
        return (!location.getBlock().isBlockPowered()
                && !location.getBlock().isBlockIndirectlyPowered())
                || !SettingsManager.Setting.REDSTONE_ACTIVATE.getBoolean();
    }

    public void overview(Player player) {
        try {
            EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();
            if (!player.hasPermission("epicspawners.overview")) return;
            new GUISpawnerOverview(instance, this, player);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void convert(SpawnerData type, Player player) {
        try {
            EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();
            if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
                player.sendMessage("Vault is not installed.");
                return;
            }

            RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = EpicSpawnersPlugin.getInstance().getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            net.milkbowl.vault.economy.Economy econ = rsp.getProvider();

            double price = type.getConvertPrice() * getSpawnerDataCount();

            if (!(econ.has(player, price) || player.isOp())) {
                player.sendMessage(References.getPrefix() + EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
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

            player.sendMessage(References.getPrefix() + EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.convert.success"));

            if (instance.getHologram() != null)
                instance.getHologram().update(this);
            player.closeInventory();
            if (!player.isOp()) {
                econ.withdrawPlayer(player, price);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public int getUpgradeCost(CostType type) {
        try {
            int cost = 0;
            if (type == CostType.ECONOMY) {
                if (getFirstStack().getSpawnerData().getUpgradeCostEconomy() != 0)
                    cost = (int) getFirstStack().getSpawnerData().getUpgradeCostEconomy();
                else
                    cost = SettingsManager.Setting.UPGRADE_COST_ECONOMY.getInt();
                if (SettingsManager.Setting.USE_CUSTOM_UPGRADE_EQUATION.getBoolean()) {
                    String math = SettingsManager.Setting.COST_EQUATION_ECONOMY.getString().replace("{ECOCost}", Integer.toString(cost)).replace("{Level}", Integer.toString(getSpawnerDataCount()));
                    cost = (int) Math.round(Double.parseDouble(engine.eval(math).toString()));
                }
            } else if (type == CostType.EXPERIENCE) {
                if (getFirstStack().getSpawnerData().getUpgradeCostExperience() != 0) {
                    cost = getFirstStack().getSpawnerData().getUpgradeCostExperience();
                } else
                    cost = SettingsManager.Setting.UPGRADE_COST_EXPERIANCE.getInt();
                if (SettingsManager.Setting.USE_CUSTOM_UPGRADE_EQUATION.getBoolean()) {
                    String math = SettingsManager.Setting.COST_EQUATION_EXPERIANCE.getString().replace("{XPCost}", Integer.toString(cost)).replace("{Level}", Integer.toString(getSpawnerDataCount()));
                    cost = (int) Math.round(Double.parseDouble(engine.eval(math).toString()));
                }
            }
            return cost;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean unstack(Player player) {
        EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();
        SpawnerStack stack = spawnerStacks.getFirst();

        int stackSize = 1;

        if (player.isSneaking() && SettingsManager.Setting.SNEAK_FOR_STACK.getBoolean()
                || SettingsManager.Setting.ONLY_DROP_STACKED.getBoolean()) {
            stackSize = stack.getStackSize();
        }

        if (SettingsManager.Setting.SOUNDS_ENABLED.getBoolean()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.6F, 15.0F);
        }
        ItemStack item = stack.getSpawnerData().toItemStack(1, stackSize);


        ItemStack inHand = player.getInventory().getItemInHand();
        if (SettingsManager.Setting.SILKTOUCH_SPAWNERS.getBoolean()
                && inHand != null
                && inHand.hasItemMeta()
                && inHand.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)
                && inHand.getEnchantmentLevel(Enchantment.SILK_TOUCH) >= SettingsManager.Setting.SILKTOUCH_MIN_LEVEL.getInt()
                && player.hasPermission("epicspawners.silkdrop." + stack.getSpawnerData().getIdentifyingName().replace(' ', '_'))
                || player.hasPermission("epicspawners.no-silk-drop")) {
            if (SettingsManager.Setting.SPAWNERS_TO_INVENTORY.getBoolean()) {
                Collection<ItemStack> leftOver = player.getInventory().addItem(item).values();
                for (ItemStack itemStack : leftOver) {
                    player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                }
            } else if (!SettingsManager.Setting.ONLY_DROP_PLACED.getBoolean() || placedBy != null) {
                int ch = Integer.parseInt((placedBy != null
                        ? SettingsManager.Setting.SILKTOUCH_PLACED_SPAWNER_DROP_CHANCE.getString() : SettingsManager.Setting.SILKTOUCH_NATURAL_SPAWNER_DROP_CHANCE.getString()).replace("%", ""));

                double rand = Math.random() * 100;

                if (rand - ch < 0 || ch == 100) {
                    if (SettingsManager.Setting.SPAWNERS_TO_INVENTORY.getBoolean() && player.getInventory().firstEmpty() != -1)
                        player.getInventory().addItem(item);
                    else
                        location.getWorld().dropItemNaturally(location.clone().add(.5, 0, .5), item);
                }
            }
        }

        if (stack.getStackSize() != stackSize) {
            stack.setStackSize(stack.getStackSize() - 1);
            return true;
        }

        spawnerStacks.removeFirst();

        if (spawnerStacks.size() != 0) return true;

        location.getBlock().setType(Material.AIR);
        EpicSpawnersPlugin.getInstance().getSpawnerManager().removeSpawnerFromWorld(location);
        if (instance.getHologram() != null)
            instance.getHologram().remove(this);
        return true;
    }

    @Override
    public boolean preStack(Player player, ItemStack itemStack) {
        return stack(player, EpicSpawnersAPI.getSpawnerDataFromItem(itemStack), EpicSpawnersAPI.getStackSizeFromItem(itemStack));
    }

    @Override
    @Deprecated
    public boolean stack(Player player, String type, int amt) {
        return stack(player, EpicSpawnersAPI.getSpawnerManager().getSpawnerData(type), amt);
    }

    @Override
    public boolean stack(Player player, SpawnerData data, int amount) {
        EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();

        int max = SettingsManager.Setting.SPAWNERS_MAX.getInt();
        int currentStackSize = getSpawnerDataCount();

        if (getSpawnerDataCount() == max) {
            player.sendMessage(References.getPrefix() + instance.getLocale().getMessage("event.upgrade.maxed", max));
            return false;
        }

        if (data != getIdentifyingData()
                && (!SettingsManager.Setting.OMNI_SPAWNERS.getBoolean() || !player.hasPermission("epicspawners.omni")))
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

        ESpawnerStack stack = new ESpawnerStack(data, amount);
        spawnerStacks.push(stack);

        if (player.getGameMode() != GameMode.CREATIVE)
            Methods.takeItem(player, 1);

        return true;
    }

    private void upgradeFinal(Player player, int oldStackSize) {
        try {
            int currentStackSize = getSpawnerDataCount();

            SpawnerChangeEvent event = new SpawnerChangeEvent(player, this, currentStackSize, oldStackSize);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;

            if (getSpawnerDataCount() != SettingsManager.Setting.SPAWNERS_MAX.getInt())
                player.sendMessage(References.getPrefix() + EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.upgrade.success", currentStackSize));
            else
                player.sendMessage(References.getPrefix() + EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.upgrade.successmaxed", currentStackSize));

            Location loc = location.clone();
            loc.setX(loc.getX() + .5);
            loc.setY(loc.getY() + .5);
            loc.setZ(loc.getZ() + .5);
            player.getWorld().spawnParticle(org.bukkit.Particle.valueOf(SettingsManager.Setting.UPGRADE_PARTICLE_TYPE.getString()), loc, 100, .5, .5, .5);

            if (EpicSpawnersPlugin.getInstance().getHologram() != null)
                EpicSpawnersPlugin.getInstance().getHologram().update(this);

            if (!SettingsManager.Setting.SOUNDS_ENABLED.getBoolean()) {
                return;
            }
            if (currentStackSize != SettingsManager.Setting.SPAWNERS_MAX.getInt()) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6F, 15.0F);
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2F, 25.0F);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 2F, 25.0F);
                Bukkit.getScheduler().scheduleSyncDelayedTask(EpicSpawnersPlugin.getInstance(), () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.2F, 35.0F), 5L);
                Bukkit.getScheduler().scheduleSyncDelayedTask(EpicSpawnersPlugin.getInstance(), () -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.8F, 35.0F), 10L);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    public void upgrade(Player player, CostType type) {
        try {
            int cost = getUpgradeCost(type);

            boolean maxed = getSpawnerDataCount() == SettingsManager.Setting.SPAWNERS_MAX.getInt();

            if (maxed) {
                player.sendMessage(References.getPrefix() + EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.upgrade.maxed"));
                return;
            }
            if (type == CostType.ECONOMY) {
                if (EpicSpawnersPlugin.getInstance().getServer().getPluginManager().getPlugin("Vault") != null) {
                    RegisteredServiceProvider<Economy> rsp = EpicSpawnersPlugin.getInstance().getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                    net.milkbowl.vault.economy.Economy econ = rsp.getProvider();
                    if (econ.has(player, cost)) {
                        econ.withdrawPlayer(player, cost);
                        int oldMultiplier = getSpawnerDataCount();
                        spawnerStacks.getFirst().setStackSize(spawnerStacks.getFirst().getStackSize() + 1);
                        upgradeFinal(player, oldMultiplier);
                    } else {
                        player.sendMessage(References.getPrefix() + EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
                    }
                } else {
                    player.sendMessage("Vault is not installed.");
                }
            } else if (type == CostType.EXPERIENCE) {
                if (player.getLevel() >= cost || player.getGameMode() == GameMode.CREATIVE) {
                    if (player.getGameMode() != GameMode.CREATIVE) {
                        player.setLevel(player.getLevel() - cost);
                    }
                    int oldMultiplier = getSpawnerDataCount();
                    spawnerStacks.getFirst().setStackSize(spawnerStacks.getFirst().getStackSize() + 1);
                    upgradeFinal(player, oldMultiplier);
                } else {
                    player.sendMessage(References.getPrefix() + EpicSpawnersPlugin.getInstance().getLocale().getMessage("event.upgrade.cannotafford"));
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    @Override
    public int getBoost() {
        EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();
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
                    if (!placedBy.toString().equals(boostData.getData())) continue;
                    break;
                case FACTION:
                    if (!instance.getHookManager().isInClaim(HookType.FACTION, (String) boostData.getData(), location))
                        continue;
                    break;
                case ISLAND:
                    if (!instance.getHookManager().isInClaim(HookType.ISLAND, (String) boostData.getData(), location))
                        continue;
                    break;
                case TOWN:
                    if (!instance.getHookManager().isInClaim(HookType.TOWN, (String) boostData.getData(), location))
                        continue;
                    break;
            }
            amountToBoost += boostData.getAmtBoosted();
        }
        return amountToBoost;
    }

    @Override
    public Instant getBoostEnd() { //ToDo: Wrong.
        EpicSpawnersPlugin instance = EpicSpawnersPlugin.getInstance();

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
                    if (!placedBy.toString().equals(boostData.getData())) continue;
                    break;
                case FACTION:
                    if (!instance.getHookManager().isInClaim(HookType.FACTION, (String) boostData.getData(), location))
                        continue;
                    break;
                case ISLAND:
                    if (!instance.getHookManager().isInClaim(HookType.ISLAND, (String) boostData.getData(), location))
                        continue;
                    break;
                case TOWN:
                    if (!instance.getHookManager().isInClaim(HookType.TOWN, (String) boostData.getData(), location))
                        continue;
                    break;
            }

            return Instant.ofEpochMilli(boostData.getEndTime());
        }
        return null;
    }

    @Override
    public int updateDelay() { //ToDO: Should be redesigned to work with spawner.setmaxdelay
        try {
            if (!SettingsManager.Setting.ALTER_DELAY.getBoolean())
                return 0;

            int max = 0;
            int min = 0;
            for (SpawnerStack stack : spawnerStacks) { //ToDo: You can probably do this only on spawner stack or upgrade.
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
            int extraTicks = SettingsManager.Setting.EXTRA_SPAWN_TICKS.getInt();

            if (getSpawnerDataCount() == 0) return 0;
            int delay = (rand.nextInt(min, max + 1) / getSpawnerDataCount()) + extraTicks;

            getCreatureSpawner().setDelay(delay);
            getCreatureSpawner().update();

            return delay;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return 999999;
    }

    @Override
    public String getIdentifyingName() {
        String name = spawnerStacks.getFirst().getSpawnerData().getIdentifyingName();

        if (spawnerStacks.size() > 1)
            name = EpicSpawnersPlugin.getInstance().getSpawnerManager().getSpawnerData("omni").getIdentifyingName();

        return name;
    }

    @Override
    public SpawnerData getIdentifyingData() {
        SpawnerData name = spawnerStacks.getFirst().getSpawnerData();

        if (spawnerStacks.size() > 1)
            name = EpicSpawnersPlugin.getInstance().getSpawnerManager().getSpawnerData("omni");

        return name;
    }

    @Override
    public String getDisplayName() {
        if (spawnerStacks.size() == 0) {
            return Methods.getTypeFromString(creatureSpawner.getSpawnedType().name());
        } else if (spawnerStacks.size() > 1) {
            return EpicSpawnersPlugin.getInstance().getSpawnerManager().getSpawnerData("omni").getDisplayName();
        }

        return spawnerStacks.getFirst().getSpawnerData().getDisplayName();
    }

    @Override
    public Collection<SpawnerStack> getSpawnerStacks() {
        return Collections.unmodifiableCollection(spawnerStacks);
    }

    @Override
    public void clearSpawnerStacks() {
        spawnerStacks.clear();
    }

    @Override
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

    @Override
    public int getSpawnCount() {
        return spawnCount;
    }

    @Override
    public void setSpawnCount(int spawnCount) {
        this.spawnCount = spawnCount;
    }

    public String getOmniState() {
        return omniState;
    }

    public void setOmniState(String omniState) {
        this.omniState = omniState;
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
        if (!(obj instanceof ESpawner)) return false;

        ESpawner other = (ESpawner) obj;
        return Objects.equals(location, other.location) && Objects.equals(placedBy, other.placedBy);
    }

    @Override
    public String toString() {
        return "ESpawner:{"
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

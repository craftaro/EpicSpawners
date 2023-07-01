package com.craftaro.epicspawners.spawners.spawner;

import com.craftaro.core.database.Data;
import com.craftaro.core.hooks.EconomyManager;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.events.SpawnerChangeEvent;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerStack;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.epicspawners.api.utils.CostType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Map;

public class SpawnerStackImpl implements SpawnerStack {

    private final PlacedSpawnerImpl spawner;
    private int stackSize;

    private SpawnerTier currentTier;

    public SpawnerStackImpl(PlacedSpawnerImpl spawner) {
        this(spawner, null, 1);
    }

    public SpawnerStackImpl(PlacedSpawnerImpl spawner, int stackSize) {
        this(spawner, null, stackSize);
    }

    public SpawnerStackImpl(PlacedSpawnerImpl spawner, SpawnerTier tier) {
        this(spawner, tier, 1);
    }

    public SpawnerStackImpl(PlacedSpawnerImpl spawner, SpawnerTier tier, int stackSize) {
        this.spawner = spawner;
        if (tier != null)
            this.currentTier = tier;
        this.stackSize = stackSize;
    }

    @Override
    public PlacedSpawnerImpl getSpawner() {
        return spawner;
    }

    @Override
    public int getStackSize() {
        return stackSize;
    }

    @Override
    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
    }

    @Override
    public SpawnerTier getCurrentTier() {
        return currentTier;
    }

    @Override
    public SpawnerData getSpawnerData() {
        return currentTier.getSpawnerData();
    }

    @Override
    public SpawnerStack setTier(SpawnerTier tier) {
        this.currentTier = tier;
        return this;
    }

    @Override
    public void upgrade(Player player, CostType type) {
        EpicSpawners plugin = EpicSpawners.getInstance();

        if (getSpawnerData().getNextTier(currentTier) == null) {
            plugin.getLocale().getMessage("event.upgrade.maxed").sendPrefixedMessage(player);
            return;
        }

        SpawnerTier tier = getSpawnerData().getNextTier(currentTier);
        SpawnerChangeEvent event = new SpawnerChangeEvent(player, spawner, tier, currentTier);

        double cost = tier.getUpgradeCost(type);
        SpawnerTier oldTier = currentTier;

        if (type == CostType.ECONOMY) {
            if (!EconomyManager.isEnabled()) {
                player.sendMessage("Economy not enabled.");
                return;
            }
            if (!player.isOp() && !EconomyManager.hasBalance(player, cost)) {
                plugin.getLocale().getMessage("event.upgrade.cannotafford").sendPrefixedMessage(player);
                return;
            }

            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;

            if (!player.isOp())
                EconomyManager.withdrawBalance(player, cost);

            currentTier = tier;
        } else if (type == CostType.LEVELS) {
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;

            if (player.getLevel() >= cost || player.getGameMode() == GameMode.CREATIVE && !Settings.CHARGE_FOR_CREATIVE.getBoolean()) {
                if (player.getGameMode() != GameMode.CREATIVE || Settings.CHARGE_FOR_CREATIVE.getBoolean())
                    player.setLevel(player.getLevel() - Math.toIntExact(Math.round(cost)));

                currentTier = tier;
                spawner.upgradeEffects(player, tier, false);
            } else {
                plugin.getLocale().getMessage("event.upgrade.cannotafford").sendPrefixedMessage(player);
            }
        }
        if (!spawner.merge(this, oldTier))
            plugin.getDataManager().save(this);
    }

    @Override
    public void convert(SpawnerData data, Player player, boolean forced) {
        EpicSpawners plugin = EpicSpawners.getInstance();
        SpawnerTier oldTier = currentTier;

        if (!EconomyManager.isEnabled()) {
            player.sendMessage("Economy not enabled.");
            return;
        }
        double price = data.getConvertPrice() * getStackSize();

        if (!forced && !EconomyManager.hasBalance(player, price)) {
            plugin.getLocale().getMessage("event.upgrade.cannotafford").sendPrefixedMessage(player);
            return;
        }

        SpawnerChangeEvent event = new SpawnerChangeEvent(player, spawner, currentTier, data.getFirstTier());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        currentTier = data.getFirstTier();
        if (!spawner.merge(this, oldTier))
            plugin.getDataManager().save(this);
        try {
            spawner.getCreatureSpawner().setSpawnedType(EntityType.valueOf(data.getIdentifyingName().toUpperCase()));
        } catch (Exception e) {
            spawner.getCreatureSpawner().setSpawnedType(EntityType.DROPPED_ITEM);
        }
        spawner.getCreatureSpawner().update();

        plugin.getLocale().getMessage("event.convert.success").sendPrefixedMessage(player);

        plugin.updateHologram(spawner);
        plugin.getAppearanceTask().updateDisplayItem(spawner, currentTier);
        player.closeInventory();
        if (!forced)
            EconomyManager.withdrawBalance(player, price);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = spawner.serialize();
        map.put("spawner_id", getSpawner().getId());
        map.put("data_type", getSpawnerData().getIdentifyingName());
        map.put("tier", getCurrentTier().getIdentifyingName());
        map.put("amount", getStackSize());
        return map;
    }

    @Override
    public Data deserialize(Map<String, Object> map) {
        //TODO: Deserialize
        return this;
    }

    @Override
    public String getTableName() {
        return "spawner_stacks";
    }
}
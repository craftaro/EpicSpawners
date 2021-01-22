package com.songoda.epicspawners.spawners.spawner;

import com.songoda.core.hooks.EconomyManager;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.api.events.SpawnerChangeEvent;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.utils.CostType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SpawnerStack {

    private final PlacedSpawner spawner;
    private int stackSize;

    private SpawnerTier currentTier;

    public SpawnerStack(PlacedSpawner spawner) {
        this(spawner, null, 1);
    }

    public SpawnerStack(PlacedSpawner spawner, int stackSize) {
        this(spawner, null, stackSize);
    }

    public SpawnerStack(PlacedSpawner spawner, SpawnerTier tier) {
        this(spawner, tier, 1);
    }

    public SpawnerStack(PlacedSpawner spawner, SpawnerTier tier, int stackSize) {
        this.spawner = spawner;
        if (tier != null)
            this.currentTier = tier;
        this.stackSize = stackSize;
    }

    public PlacedSpawner getSpawner() {
        return spawner;
    }

    public int getStackSize() {
        return stackSize;
    }

    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
    }

    public SpawnerTier getCurrentTier() {
        return currentTier;
    }

    public SpawnerData getSpawnerData() {
        return currentTier.getSpawnerData();
    }

    public SpawnerStack setTier(SpawnerTier tier) {
        this.currentTier = tier;
        return this;
    }

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
            plugin.getDataManager().updateSpawnerStack(this, oldTier.getIdentifyingName());
    }

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
            plugin.getDataManager().updateSpawnerStack(this, oldTier.getIdentifyingName());
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
}
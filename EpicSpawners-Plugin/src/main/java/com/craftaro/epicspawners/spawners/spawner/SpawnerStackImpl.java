package com.craftaro.epicspawners.spawners.spawner;

import com.craftaro.core.database.Data;
import com.craftaro.core.hooks.EconomyManager;
import com.craftaro.third_party.org.jooq.impl.DSL;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.events.SpawnerChangeEvent;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerData;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerStack;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.api.utils.CostType;
import com.craftaro.epicspawners.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public class SpawnerStackImpl implements SpawnerStack {
    private PlacedSpawnerImpl spawner;
    private int stackSize;

    private SpawnerTier currentTier;

    /**
     * Default constructor used for database loading.
     */
    public SpawnerStackImpl() {
        this.spawner = null;
        this.currentTier = null;
        this.stackSize = 0;
    }

    /**
     * Constructor used for database loading.
     */
    public SpawnerStackImpl(int spawnerId, String dataType, String tier, int amount) {
        this.spawner = (PlacedSpawnerImpl) EpicSpawners.getInstance().getSpawnerManager().getSpawner(spawnerId);
        this.currentTier = EpicSpawners.getInstance().getSpawnerManager().getSpawnerData(dataType).getTier(tier);
        this.stackSize = amount;
    }

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
        if (tier != null) {
            this.currentTier = tier;
        }
        this.stackSize = stackSize;
    }

    @Override
    public PlacedSpawnerImpl getSpawner() {
        return this.spawner;
    }

    public void setSpawner(PlacedSpawnerImpl spawner) {
        this.spawner = spawner;
    }

    @Override
    public int getStackSize() {
        return this.stackSize;
    }

    @Override
    public void setStackSize(int stackSize) {
        this.stackSize = stackSize;
    }

    @Override
    public SpawnerTier getCurrentTier() {
        return this.currentTier;
    }

    @Override
    public SpawnerData getSpawnerData() {
        return this.currentTier.getSpawnerData();
    }

    @Override
    public SpawnerStack setTier(SpawnerTier tier) {
        this.currentTier = tier;
        return this;
    }

    @Override
    public void upgrade(Player player, CostType type) {
        EpicSpawners plugin = EpicSpawners.getInstance();

        if (getSpawnerData().getNextTier(this.currentTier) == null) {
            plugin.getLocale().getMessage("event.upgrade.maxed").sendPrefixedMessage(player);
            return;
        }

        SpawnerTier tier = getSpawnerData().getNextTier(this.currentTier);
        SpawnerChangeEvent event = new SpawnerChangeEvent(player, this.spawner, tier, this.currentTier);

        double cost = tier.getUpgradeCost(type);
        SpawnerTier oldTier = this.currentTier;

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
            if (event.isCancelled()) {
                return;
            }

            if (!player.isOp()) {
                EconomyManager.withdrawBalance(player, cost);
            }

            this.currentTier = tier;
        } else if (type == CostType.LEVELS) {
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            if (player.getLevel() >= cost || player.getGameMode() == GameMode.CREATIVE && !Settings.CHARGE_FOR_CREATIVE.getBoolean()) {
                if (player.getGameMode() != GameMode.CREATIVE || Settings.CHARGE_FOR_CREATIVE.getBoolean()) {
                    player.setLevel(player.getLevel() - Math.toIntExact(Math.round(cost)));
                }

                this.currentTier = tier;
                this.spawner.upgradeEffects(player, tier, false);
            } else {
                plugin.getLocale().getMessage("event.upgrade.cannotafford").sendPrefixedMessage(player);
            }
        }
        if (!this.spawner.merge(this, oldTier)) {
            plugin.getDataManager().getDatabaseConnector().connectDSL(dslContext -> {
                //Update tier
                dslContext.update(DSL.table(plugin.getDataManager().getTablePrefix() + "spawner_stacks"))
                        .set(DSL.field("tier"), tier.getIdentifyingName())
                        .where(DSL.field("spawner_id").eq(this.getSpawner().getId()))
                        .and(DSL.field("data_type").eq(oldTier.getSpawnerData().getIdentifyingName()))
                        .and(DSL.field("amount").eq(this.getStackSize()))
                        .execute();
            });
        }
    }

    @Override
    public void convert(SpawnerData data, Player player, boolean forced) {
        EpicSpawners plugin = EpicSpawners.getInstance();
        SpawnerTier oldTier = this.currentTier;

        if (!EconomyManager.isEnabled()) {
            player.sendMessage("Economy not enabled.");
            return;
        }
        double price = data.getConvertPrice() * getStackSize();

        if (!forced && !EconomyManager.hasBalance(player, price)) {
            plugin.getLocale().getMessage("event.upgrade.cannotafford").sendPrefixedMessage(player);
            return;
        }

        SpawnerChangeEvent event = new SpawnerChangeEvent(player, this.spawner, this.currentTier, data.getFirstTier());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        this.currentTier = data.getFirstTier();
        if (!this.spawner.merge(this, oldTier)) {
            plugin.getDataManager().save(this, "spawner_id", this.spawner.getId());
        }
        try {
            this.spawner.getCreatureSpawner().setSpawnedType(EntityType.valueOf(data.getIdentifyingName().toUpperCase()));
        } catch (Exception e) {
            this.spawner.getCreatureSpawner().setSpawnedType(EntityType.DROPPED_ITEM);
        }
        this.spawner.getCreatureSpawner().update();

        plugin.getLocale().getMessage("event.convert.success").sendPrefixedMessage(player);

        plugin.updateHologram(this.spawner);
        plugin.getAppearanceTask().updateDisplayItem(this.spawner, this.currentTier);
        player.closeInventory();
        if (!forced) {
            EconomyManager.withdrawBalance(player, price);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("spawner_id", getSpawner().getId());
        map.put("data_type", getSpawnerData().getIdentifyingName());
        map.put("amount", getStackSize());
        map.put("tier", getCurrentTier().getIdentifyingName());
        return map;
    }

    @Override
    public Data deserialize(Map<String, Object> map) {
        int spawnerId = (int) map.get("spawner_id");
        String dataType = (String) map.get("data_type");
        int amount = (int) map.get("amount");
        String tier = (String) map.get("tier");
        return new SpawnerStackImpl(spawnerId, dataType, tier, amount);
    }

    @Override
    public String getTableName() {
        return "spawner_stacks";
    }
}

package com.craftaro.epicspawners.spawners.condition;

import com.craftaro.epicanchors.EpicAnchorsApi;
import com.craftaro.epicanchors.api.AnchorManager;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.condition.SpawnCondition;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class SpawnConditionNearbyPlayers implements SpawnCondition {
    private final int distance;
    private final int amount;
    private final boolean epicAnchorsAvailable;

    public SpawnConditionNearbyPlayers(int distance, int amount) {
        this.amount = amount;
        this.distance = distance;
        this.epicAnchorsAvailable = Bukkit.getPluginManager().isPluginEnabled("EpicAnchors");
    }

    @Override
    public String getName() {
        return "nearby_player";
    }

    @Override
    public String getDescription() {
        return EpicSpawners.getInstance().getLocale().getMessage("interface.spawner.conditionNearbyPlayers")
                .processPlaceholder("amount", this.amount)
                .processPlaceholder("distance", this.distance)
                .getMessage();
    }

    @Override
    public boolean isMet(PlacedSpawner spawner) {
        Location location = spawner.getLocation().add(0.5, 0.5, 0.5);
        assert location.getWorld() != null;

        long count = 0;

        if (this.epicAnchorsAvailable) {
            long anchorWeight = Settings.EPIC_ANCHORS_PLAYER_WEIGHT.getLong();


            if (anchorWeight < 0 && getAnchorManager().hasAnchor(location.getChunk())) {
                return true;
            }

            if (anchorWeight > 0) {
                count += getAnchorManager().searchAnchors(location, this.distance).size() * anchorWeight;
            }
        }

        if (count >= this.amount) {
            return true;
        }

        count += location.getWorld()
                .getNearbyEntities(location, this.distance, this.distance, this.distance)
                .stream()
                .filter(e -> e.getType() == EntityType.PLAYER)
                .count();

        return count >= this.amount;
    }

    public int getDistance() {
        return this.distance;
    }

    public int getAmount() {
        return this.amount;
    }

    private AnchorManager getAnchorManager() {
        return EpicAnchorsApi.getApi().getAnchorManager();
    }
}

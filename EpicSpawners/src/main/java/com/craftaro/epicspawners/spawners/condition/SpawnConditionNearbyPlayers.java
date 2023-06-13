package com.craftaro.epicspawners.spawners.condition;

import com.craftaro.epicanchors.EpicAnchors;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.condition.SpawnCondition;
import com.craftaro.epicspawners.settings.Settings;
import com.craftaro.epicspawners.spawners.spawner.PlacedSpawnerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnConditionNearbyPlayers implements SpawnCondition {
    private final int distance;
    private final int amount;

    private EpicAnchors epicAnchors;

    /** -1 = unknown, 0 = unsupported version, 1 = supported version */
    static int epicAnchorsState = -1;

    public SpawnConditionNearbyPlayers(int distance, int amount) {
        this.amount = amount;
        this.distance = distance;

        if (getEpicAnchorsState() == 1) {
            this.epicAnchors = JavaPlugin.getPlugin(EpicAnchors.class);
        }
    }

    @Override
    public String getName() {
        return "nearby_player";
    }

    @Override
    public String getDescription() {
        return EpicSpawners.getInstance().getLocale().getMessage("interface.spawner.conditionNearbyPlayers")
                .processPlaceholder("amount", amount)
                .processPlaceholder("distance", distance)
                .getMessage();
    }

    @Override
    public boolean isMet(PlacedSpawnerImpl spawner) {
        Location location = spawner.getLocation().add(0.5, 0.5, 0.5);
        assert location.getWorld() != null;

        long count = 0;

        if (epicAnchors != null) {
            long anchorWeight = Settings.EPIC_ANCHORS_PLAYER_WEIGHT.getLong();

            if (anchorWeight < 0 && epicAnchors.getAnchorManager().hasAnchor(location.getChunk())) {
                return true;
            }

            if (anchorWeight > 0) {
                count += epicAnchors.getAnchorManager().searchAnchors(location, distance).size() * anchorWeight;
            }
        }

        if (count >= amount) {
            return true;
        }

        count += location.getWorld().getNearbyEntities(location, distance, distance, distance)
                .stream().filter(e -> e.getType() == EntityType.PLAYER).count();

        return count >= amount;
    }

    public int getDistance() {
        return distance;
    }

    public int getAmount() {
        return amount;
    }

    private static int getEpicAnchorsState() {
        if (epicAnchorsState == -1) {
            epicAnchorsState = 0;

            if (Bukkit.getPluginManager().isPluginEnabled("EpicAnchors")) {
                EpicAnchors epicAnchors = JavaPlugin.getPlugin(EpicAnchors.class);

                char majorVersionChar = epicAnchors.getDescription().getVersion().charAt(0);

                if (Character.isDigit(majorVersionChar) && majorVersionChar - '0' >= 2) {
                    epicAnchorsState = 1;
                }
            }
        }

        return epicAnchorsState;
    }
}

package com.craftaro.epicspawners.spawners.condition;

import com.craftaro.core.hooks.EntityStackerManager;
import com.craftaro.core.utils.NumberUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.condition.SpawnCondition;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.settings.Settings;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public class SpawnConditionNearbyEntities implements SpawnCondition {
    private final int max;

    private static Map<Location, Integer> cachedAmounts = new HashMap<>();

    public SpawnConditionNearbyEntities(int max) {
        this.max = max;
    }

    @Override
    public String getName() {
        return "nearby_entities";
    }

    @Override
    public String getDescription() {
        return EpicSpawners.getInstance().getLocale().getMessage("interface.spawner.conditionNearbyEntities")
                .processPlaceholder("max", this.max).getMessage();
    }

    @Override
    public boolean isMet(PlacedSpawner spawner) {

        // Should we skip the max entity amount on first spawn?
        if (spawner.getSpawnCount() == 0 && Settings.IGNORE_MAX_ON_FIRST_SPAWN.getBoolean()) {
            return true;
        }

        return getEntitiesAroundSpawner(spawner.getLocation().add(0.5, 0.5, 0.5), false) < this.max;
    }

    public static int getEntitiesAroundSpawner(Location location, boolean cached) {
        if (cached) {
            if (cachedAmounts.containsKey(location)) {
                return cachedAmounts.get(location);
            }

            return 0;
        }
        String[] arr = Settings.SEARCH_RADIUS.getString().split("x");

        if (arr.length == 1) {
            if (NumberUtils.isNumeric(arr[0])) {
                arr = new String[]{arr[0], arr[0], arr[0]};
            }
        }

        if (arr.length != 3) {
            EpicSpawners.getInstance().getLogger().warning("Search radius is invalid:" + Settings.SEARCH_RADIUS.getString());
            return 0;
        }

        int amt = location.getWorld().getNearbyEntities(location, Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]))
                .stream()
                .filter(e -> e.isValid() &&
                        e instanceof LivingEntity &&
                        e.getType() != EntityType.PLAYER &&
                        e.getType() != EntityType.ARMOR_STAND)
                .mapToInt(e -> {
                    if (EntityStackerManager.getStacker() == null) {
                        return 1;
                    }
                    return EntityStackerManager.getStacker().getSize((LivingEntity) e);
                }).sum();

        cachedAmounts.put(location, amt);
        return amt;
    }

    public int getMax() {
        return this.max;
    }
}

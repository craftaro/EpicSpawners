package com.craftaro.epicspawners.spawners.condition;

import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.spawners.condition.SpawnCondition;
import com.craftaro.epicspawners.spawners.spawner.PlacedSpawnerImpl;
import org.bukkit.Location;

public class SpawnConditionLightDark implements SpawnCondition {

    private final Type lightDark;

    public SpawnConditionLightDark(Type lightDark) {
        this.lightDark = lightDark;
    }

    @Override
    public String getName() {
        return "lightdark";
    }

    @Override
    public String getDescription() {
        switch (lightDark) {
            case LIGHT:
                return EpicSpawners.getInstance().getLocale().getMessage("interface.spawner.conditionLight").getMessage();
            case DARK:
                return EpicSpawners.getInstance().getLocale().getMessage("interface.spawner.conditionDark").getMessage();
            default:
                return null;
        }
    }

    @Override
    public boolean isMet(PlacedSpawnerImpl spawner) {
        Location location = spawner.getLocation();
        switch (lightDark) {
            case LIGHT:
                return !isDark(location);
            case DARK:
                return isDark(location);
        }
        return true;
    }

    public boolean isDark(Location location) {
        return location.getBlock().getLightLevel() <= 7;
    }

    public Type getType() {
        return lightDark;
    }

    public enum Type {LIGHT, DARK, BOTH}
}

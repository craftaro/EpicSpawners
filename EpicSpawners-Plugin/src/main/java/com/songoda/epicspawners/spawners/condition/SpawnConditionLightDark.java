package com.songoda.epicspawners.spawners.condition;

import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.condition.SpawnCondition;
import org.bukkit.Location;
import org.bukkit.World;

public class SpawnConditionLightDark implements SpawnCondition {

    public enum Type { LIGHT, DARK, BOTH }

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
        switch(lightDark) {
            case BOTH:
                return "Spawns in all lighting";
            case LIGHT:
                return "Spawns in light";
            case DARK:
                return "Spawns in darkness";
        }
        return null;
    }

    @Override
    public boolean isMet(Spawner spawner) {
        Location location = spawner.getLocation();
        switch(lightDark) {
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
}

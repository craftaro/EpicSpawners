package com.craftaro.epicspawners.spawners.spawner.option;

import com.craftaro.third_party.org.apache.commons.lang3.math.NumberUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.boosts.types.Boosted;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerStack;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.api.spawners.spawner.option.SpawnOption;
import com.craftaro.epicspawners.api.spawners.spawner.option.SpawnOptionType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public class SpawnOptionCommand implements SpawnOption {
    private static final int MAX_SEARCH_COUNT = 150;
    private static final int SPAWN_RADIUS = 3;

    private final Random random;
    private final String[] commands;

    public SpawnOptionCommand(String... commands) {
        this.commands = commands;
        this.random = new Random();
    }

    public SpawnOptionCommand(Collection<String> commands) {
        this(commands.toArray(new String[0]));
    }

    @Override
    public void spawn(SpawnerTier data, SpawnerStack stack, PlacedSpawner spawner) {
        Location location = spawner.getLocation();
        if (location == null || location.getWorld() == null) {
            return;
        }

        int spawnerBoost = spawner.getBoosts().stream().mapToInt(Boosted::getAmountBoosted).sum();

        for (int i = 0; i < stack.getStackSize() + spawnerBoost; i++) {
            for (String command : this.commands) {
                String finalCommand = command;
                String lowercaseCommand = finalCommand.toLowerCase();

                // Search for random location if x, y or z is present in command
                if (lowercaseCommand.contains("@x") || lowercaseCommand.contains("@y") || lowercaseCommand.contains("@z")) {
                    int searchIndex = 0;
                    while (searchIndex++ <= MAX_SEARCH_COUNT) {
                        spawner.setSpawnCount(spawner.getSpawnCount() + 1);

                        double xOffset = this.random.nextInt((SPAWN_RADIUS * 2) + 1) - SPAWN_RADIUS;
                        double yOffset = this.random.nextInt((SPAWN_RADIUS * 2) + 1) - SPAWN_RADIUS;
                        double zOffset = this.random.nextInt((SPAWN_RADIUS * 2) + 1) - SPAWN_RADIUS;

                        location.add(xOffset, yOffset, zOffset);
                        finalCommand = finalCommand.replaceAll("@[xX]", String.valueOf(location.getX()))
                                .replaceAll("@[yY]", String.valueOf(location.getY()))
                                .replaceAll("@[zZ]", String.valueOf(location.getZ()));
                        location.subtract(xOffset, yOffset, zOffset);
                    }
                }

                // Add the world name if @w present in command
                if (lowercaseCommand.contains("@w")) {
                    finalCommand = finalCommand.replaceAll("@[wW]", location.getWorld().getName());
                }

                // Get nearest player if @p is present in command
                if (lowercaseCommand.contains("@p")) {
                    Player nearbyPlayer = getNearestPlayer(location);
                    if (nearbyPlayer == null) {
                        continue;
                    }

                    finalCommand = finalCommand.replaceAll("@[pP]", nearbyPlayer.getName());
                }

                if (lowercaseCommand.contains("@n")) {
                    OfflinePlayer whoPlaced = spawner.getPlacedBy();

                    finalCommand = finalCommand.replaceAll("@[nN]", whoPlaced.getName());
                }

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            }
        }

        EpicSpawners.getInstance().getDataManager().save(spawner);
    }

    @Override
    public SpawnOptionType getType() {
        return SpawnOptionType.COMMAND;
    }

    private Player getNearestPlayer(Location location) {
        String[] playerRadius = EpicSpawners.getInstance().getConfig().getString("Main.Radius To Search Around Spawners").split("x");
        if (playerRadius.length != 3) {
            return null;
        }

        double xRadius = NumberUtils.toDouble(playerRadius[0], 8);
        double yRadius = NumberUtils.toDouble(playerRadius[1], 4);
        double zRadius = NumberUtils.toDouble(playerRadius[2], 8);

        Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, xRadius, yRadius, zRadius);
        return (Player) nearbyEntities.stream().filter(e -> e instanceof Player).findFirst().orElse(null);
    }

    @Override
    public int hashCode() {
        return 31 * (this.commands != null ? Arrays.hashCode(this.commands) : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SpawnOptionCommand)) {
            return false;
        }

        SpawnOptionCommand other = (SpawnOptionCommand) obj;
        return Arrays.equals(this.commands, other.commands);
    }
}

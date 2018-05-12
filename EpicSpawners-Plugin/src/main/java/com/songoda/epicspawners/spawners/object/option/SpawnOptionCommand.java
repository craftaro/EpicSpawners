package com.songoda.epicspawners.spawners.object.option;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.Spawner;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.api.spawner.SpawnerStack;
import com.songoda.epicspawners.spawners.object.SpawnOptionType;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
        this(commands.toArray(new String[commands.size()]));
    }

    @Override
    public void spawn(SpawnerData data, SpawnerStack stack, Spawner spawner) {
        Location location = spawner.getLocation();
        if (location == null || location.getWorld() == null) return;

        for (int i = 0; i < spawner.getSpawnerDataCount(); i++) {
            for (String command : commands) {
                String finalCommand = command;
                String lowercaseCommand = finalCommand.toLowerCase();

                // Search for random location if x, y or z is present in command
                if (lowercaseCommand.contains("@x") || lowercaseCommand.contains("@y") || lowercaseCommand.contains("@z")) {
                    int searchIndex = 0;
                    while (searchIndex++ <= MAX_SEARCH_COUNT) {
                        spawner.setSpawnCount(spawner.getSpawnCount() + 1);
                        double xOffset = random.nextInt((SPAWN_RADIUS * 2) + 1) - SPAWN_RADIUS;
                        double yOffset = random.nextInt((SPAWN_RADIUS * 2) + 1) - SPAWN_RADIUS;
                        double zOffset = random.nextInt((SPAWN_RADIUS * 2) + 1) - SPAWN_RADIUS;

                        location.add(xOffset, yOffset, zOffset);
                        finalCommand = finalCommand.replaceAll("@[xX]", String.valueOf(location.getX()))
                                .replaceAll("@[yY]", String.valueOf(location.getY()))
                                .replaceAll("@[zZ]", String.valueOf(location.getZ()));
                        location.subtract(xOffset, yOffset, zOffset);
                    }
                }

                // Get nearest player if @p is present in command
                if (lowercaseCommand.contains("@p")) {
                    Player nearbyPlayer = getNearestPlayer(location);
                    if (nearbyPlayer == null) continue;

                    finalCommand = finalCommand.replaceAll("@[pP]", nearbyPlayer.getName());
                }

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            }
        }
    }

    @Override
    public SpawnOptionType getType() {
        return SpawnOptionType.COMMAND;
    }

    private Player getNearestPlayer(Location location) {
        if (EpicSpawnersPlugin.getInstance().v1_7) return null;

        String[] playerRadius = EpicSpawnersPlugin.getInstance().getConfig().getString("Main.Radius To Search Around Spawners").split("x");
        if (playerRadius.length != 3) return null;

        double xRadius = NumberUtils.toDouble(playerRadius[0], 8);
        double yRadius = NumberUtils.toDouble(playerRadius[1], 4);
        double zRadius = NumberUtils.toDouble(playerRadius[2], 8);

        Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, xRadius, yRadius, zRadius);
        return (Player) nearbyEntities.stream().filter(e -> e instanceof Player).findFirst().orElse(null);
    }

    @Override
    public int hashCode() {
        return 31 * (commands != null ? commands.hashCode() : 0);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof SpawnOptionCommand)) return false;

        SpawnOptionCommand other = (SpawnOptionCommand) object;
        return Arrays.equals(commands, other.commands);
    }

}
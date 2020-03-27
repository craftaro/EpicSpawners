package com.songoda.epicspawners.database;

import com.songoda.core.database.DataManagerAbstract;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.boost.types.Boosted;
import com.songoda.epicspawners.boost.types.BoostedPlayer;
import com.songoda.epicspawners.boost.types.BoostedSpawner;
import com.songoda.epicspawners.spawners.spawner.Spawner;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.function.Consumer;

public class DataManager extends DataManagerAbstract {

    public DataManager(DatabaseConnector databaseConnector, Plugin plugin) {
        super(databaseConnector, plugin);
    }

    public void updateSpawner(Spawner spawner) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String updateSpawner = "UPDATE " + this.getTablePrefix() + "placed_spawners SET spawn_count = ?, placed_by = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateSpawner)) {
                statement.setInt(1, spawner.getSpawnCount());
                statement.setString(2,
                        spawner.getPlacedBy() == null ? null : spawner.getPlacedBy().getUniqueId().toString());
                statement.setInt(3, spawner.getId());
                statement.executeUpdate();
            }
        }));
    }

    public void updateSpawnerStack(SpawnerStack stack) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String updateSpawnerStack = "UPDATE " + this.getTablePrefix() + "spawner_stacks SET data_type = ?, amount = ? WHERE spawner_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateSpawnerStack)) {
                statement.setString(1, stack.getSpawnerData().getIdentifyingName());
                statement.setInt(2, stack.getStackSize());
                statement.setInt(3, stack.getSpawner().getId());
                statement.executeUpdate();
            }
        }));
    }

    public void deleteSpawnerStack(SpawnerStack stack) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String deleteSpawnerStacks = "DELETE FROM " + this.getTablePrefix() + "spawner_stacks WHERE spawner_id = ? AND data_type = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteSpawnerStacks)) {
                statement.setInt(1, stack.getSpawner().getId());
                statement.setString(2, stack.getSpawnerData().getIdentifyingName());
                statement.executeUpdate();
            }
        }));
    }


    public void createSpawners(List<Spawner> spawners) {
        for (Spawner spawner : spawners)
            createSpawner(spawner);
    }

    public void createSpawner(Spawner spawner) {
        this.sync(() -> this.databaseConnector.connect(connection -> {
            String createSpawner = "INSERT INTO " + this.getTablePrefix() + "placed_spawners (spawn_count, placed_by, world, x, y, z) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createSpawner)) {
                statement.setInt(1, spawner.getSpawnCount());
                statement.setString(2,
                        spawner.getPlacedBy() == null ? null : spawner.getPlacedBy().getUniqueId().toString());

                statement.setString(3, spawner.getWorld().getName());
                statement.setInt(4, spawner.getX());
                statement.setInt(5, spawner.getY());
                statement.setInt(6, spawner.getZ());
                statement.executeUpdate();
            }

            int spawnerId = this.lastInsertedId(connection, "placed_spawners");
            spawner.setId(spawnerId);

            String createSpawnerStack = "INSERT INTO " + this.getTablePrefix() + "spawner_stacks (spawner_id, data_type, amount) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createSpawnerStack)) {
                for (SpawnerStack stack : spawner.getSpawnerStacks()) {
                    statement.setInt(1, spawnerId);
                    statement.setString(2, stack.getSpawnerData().getIdentifyingName());
                    statement.setInt(3, stack.getStackSize());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        }), "create");
    }

    public void createSpawnerStack(SpawnerStack stack) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String createSpawnerStack = "INSERT INTO " + this.getTablePrefix() + "spawner_stacks (spawner_id, data_type, amount) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createSpawnerStack)) {
                statement.setInt(1, stack.getSpawner().getId());
                statement.setString(2, stack.getSpawnerData().getIdentifyingName());
                statement.setInt(3, stack.getStackSize());
                statement.executeUpdate();
            }
        }));
    }

    public void createBoost(Boosted boosted) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            if (boosted instanceof BoostedPlayer) {
                BoostedPlayer boostedPlayer = (BoostedPlayer) boosted;
                String createBoostedPlayer = "INSERT INTO " + this.getTablePrefix() + "boosted_players (player, amount, end_time) VALUES (?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(createBoostedPlayer)) {
                    statement.setString(1, boostedPlayer.getPlayer().getUniqueId().toString());
                    statement.setInt(2, boostedPlayer.getAmountBoosted());
                    statement.setLong(3, boostedPlayer.getEndTime());
                    statement.executeUpdate();
                }
            } else if (boosted instanceof BoostedSpawner) {
                BoostedSpawner boostedSpawner = (BoostedSpawner) boosted;
                String createBoostedSpawner = "INSERT INTO " + this.getTablePrefix() + "boosted_spawners (world, x, y, z, amount, end_time) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(createBoostedSpawner)) {
                    Location location = ((BoostedSpawner) boosted).getLocation();
                    statement.setString(1, location.getWorld().getName());
                    statement.setInt(2, Math.toIntExact(Math.round(location.getX())));
                    statement.setInt(3, Math.toIntExact(Math.round(location.getY())));
                    statement.setInt(4, Math.toIntExact(Math.round(location.getZ())));
                    statement.setInt(5, boostedSpawner.getAmountBoosted());
                    statement.setLong(6, boostedSpawner.getEndTime());
                    statement.executeUpdate();
                }
            }
        }));
    }

    public void createEntityKill(OfflinePlayer player, EntityType entityType, int count) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String createEntityKill = "INSERT INTO " + this.getTablePrefix() + "entity_kills (player, entity_type, count) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(createEntityKill)) {
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, entityType.name());
                statement.setInt(3, count);
                statement.executeUpdate();
            }
        }));
    }


    public void updateEntityKill(OfflinePlayer player, EntityType entityType, int count) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String updateEntityKill = "UPDATE " + this.getTablePrefix() + "entity_kills SET count = ? WHERE player = ? AND entity_type = ?";
            try (PreparedStatement statement = connection.prepareStatement(updateEntityKill)) {
                statement.setInt(1, count);
                statement.setString(2, player.getUniqueId().toString());
                statement.setString(3, entityType.name());
                statement.executeUpdate();
            }
        }));
    }

    public void deleteEntityKills(OfflinePlayer player, EntityType entityType) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String deleteEntityKills = "DELETE FROM " + this.getTablePrefix() + "entity_kills WHERE player = ? AND entity_type = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteEntityKills)) {
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, entityType.name());
                statement.executeUpdate();
            }
        }));
    }

    public void getBoosts(Consumer<List<Boosted>> callback) {
        List<Boosted> boosts = new ArrayList<>();
        this.async(() -> this.databaseConnector.connect(connection -> {
            try (Statement statement = connection.createStatement()) {
                String selectBoostedPlayers = "SELECT * FROM " + this.getTablePrefix() + "boosted_players";
                ResultSet result = statement.executeQuery(selectBoostedPlayers);
                while (result.next()) {
                    UUID player = UUID.fromString(result.getString("player"));
                    int amount = result.getInt("amount");
                    long endTime = result.getLong("end_time");
                    boosts.add(new BoostedPlayer(player, amount, endTime));
                }
            }

            try (Statement statement = connection.createStatement()) {
                String selectBoostedSpawners = "SELECT * FROM " + this.getTablePrefix() + "boosted_spawners";
                ResultSet result = statement.executeQuery(selectBoostedSpawners);
                while (result.next()) {
                    World world = Bukkit.getWorld(result.getString("world"));

                    if (world == null)
                        continue;

                    int x = result.getInt("x");
                    int y = result.getInt("y");
                    int z = result.getInt("z");
                    Location location = new Location(world, x, y, z);
                    int amount = result.getInt("amount");
                    long endTime = result.getLong("end_time");
                    boosts.add(new BoostedSpawner(location, amount, endTime));
                }
            }
            this.sync(() -> callback.accept(boosts));
        }));
    }

    public void getEntityKills(Consumer<Map<UUID, Map<EntityType, Integer>>> callback) {
        Map<UUID, Map<EntityType, Integer>> entityKills = new HashMap<>();
        this.async(() -> this.databaseConnector.connect(connection -> {
            try (Statement statement = connection.createStatement()) {
                String selectEntityKills = "SELECT * FROM " + this.getTablePrefix() + "entity_kills";
                ResultSet result = statement.executeQuery(selectEntityKills);
                while (result.next()) {
                    UUID player = UUID.fromString(result.getString("player"));
                    EntityType type = EntityType.valueOf(result.getString("entity_type"));
                    int count = result.getInt("count");
                    if (!entityKills.containsKey(player))
                        entityKills.put(player, new HashMap<>());
                    entityKills.get(player).put(type, count);
                }
            }
            this.sync(() -> callback.accept(entityKills));
        }));
    }

    public void deleteBoost(Boosted boosted) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            if (boosted instanceof BoostedPlayer) {
                String deleteBoost = "DELETE FROM " + this.getTablePrefix() + "boosted_players WHERE end_time = ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteBoost)) {
                    statement.setLong(1, boosted.getEndTime());
                    statement.executeUpdate();
                }
            } else if (boosted instanceof BoostedSpawner) {
                String deleteBoost = "DELETE FROM " + this.getTablePrefix() + "boosted_spawners WHERE end_time = ?";
                try (PreparedStatement statement = connection.prepareStatement(deleteBoost)) {
                    statement.setLong(1, boosted.getEndTime());
                    statement.executeUpdate();
                }
            }
        }));
    }

    public void deleteSpawner(Spawner spawner) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String deleteSpawner = "DELETE FROM " + this.getTablePrefix() + "placed_spawners WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteSpawner)) {
                statement.setInt(1, spawner.getId());
                statement.executeUpdate();
            }

            String deleteSpawnerStack = "DELETE FROM " + this.getTablePrefix() + "spawner_stacks WHERE spawner_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteSpawnerStack)) {
                statement.setInt(1, spawner.getId());
                statement.executeUpdate();
            }
        }));
    }

    public void getSpawners(Consumer<Map<Location, Spawner>> callback) {
        this.async(() -> this.databaseConnector.connect(connection -> {

            Map<Integer, Spawner> spawners = new HashMap<>();

            try (Statement statement = connection.createStatement()) {
                String selectSpawners = "SELECT * FROM " + this.getTablePrefix() + "placed_spawners";
                ResultSet result = statement.executeQuery(selectSpawners);
                while (result.next()) {
                    World world = Bukkit.getWorld(result.getString("world"));

                    if (world == null)
                        continue;

                    int id = result.getInt("id");
                    int spawns = result.getInt("spawn_count");

                    int x = result.getInt("x");
                    int y = result.getInt("y");
                    int z = result.getInt("z");
                    Location location = new Location(world, x, y, z);

                    Spawner spawner = new Spawner(location);
                    spawner.setId(id);
                    spawner.setSpawnCount(spawns);
                    spawners.put(id, spawner);
                }
            }

            try (Statement statement = connection.createStatement()) {
                String selectSpawnerStacks = "SELECT * FROM " + this.getTablePrefix() + "spawner_stacks";
                ResultSet result = statement.executeQuery(selectSpawnerStacks);
                while (result.next()) {
                    Spawner spawner = spawners.get(result.getInt("spawner_id"));
                    if (spawner == null)
                        continue;

                    String type = result.getString("data_type");
                    int amount = result.getInt("amount");
                    SpawnerData spawnerData = EpicSpawners.getInstance().getSpawnerManager().getSpawnerData(type);
                    SpawnerStack stack = new SpawnerStack(spawnerData);
                    stack.setStackSize(amount);
                    spawner.addSpawnerStack(stack);
                }
            }

            Map<Location, Spawner> returnableSpawners = new HashMap<>();
            for (Spawner spawner : spawners.values())
                returnableSpawners.put(spawner.getLocation(), spawner);

            this.sync(() -> callback.accept(returnableSpawners));
        }));
    }
}

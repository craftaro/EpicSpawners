package com.craftaro.epicspawners.database;

import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.core.database.DataManagerAbstract;
import com.craftaro.core.database.DatabaseConnector;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.api.boosts.types.Boosted;
import com.craftaro.epicspawners.api.spawners.spawner.PlacedSpawner;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerStack;
import com.craftaro.epicspawners.api.spawners.spawner.SpawnerTier;
import com.craftaro.epicspawners.boost.types.BoostedImpl;
import com.craftaro.epicspawners.boost.types.BoostedPlayerImpl;
import com.craftaro.epicspawners.boost.types.BoostedSpawnerImpl;
import com.craftaro.epicspawners.spawners.spawner.PlacedSpawnerImpl;
import com.craftaro.epicspawners.spawners.spawner.SpawnerDataImpl;
import com.craftaro.epicspawners.spawners.spawner.SpawnerStackImpl;
import com.craftaro.epicspawners.spawners.spawner.SpawnerTier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class DataManager extends DataManagerAbstract {

    public DataManager(DatabaseConnector databaseConnector, Plugin plugin) {
        super(databaseConnector, plugin);
    }

    public void updateSpawner(PlacedSpawner spawner) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()){
                String updateSpawner = "UPDATE " + this.getTablePrefix() + "placed_spawners SET spawn_count = ?, placed_by = ? WHERE id = ?";
                PreparedStatement statement = connection.prepareStatement(updateSpawner);
                statement.setInt(1, spawner.getSpawnCount());
                statement.setString(2,
                        spawner.getPlacedBy() == null ? null : spawner.getPlacedBy().getUniqueId().toString());
                statement.setInt(3, spawner.getId());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void updateSpawnerStack(SpawnerStack spawnerStack) {
        updateSpawnerStack(spawnerStack, spawnerStack.getCurrentTier().getIdentifyingName());
    }

    public void updateSpawnerStack(SpawnerStack spawnerStack, String tierBeforeUpdate) {
        updateSpawnerStack(spawnerStack, spawnerStack.getSpawnerData().getIdentifyingName(), tierBeforeUpdate);
    }

    public void updateSpawnerStack(SpawnerStack stack, String dataBeforeUpdate, String tierBeforeUpdate) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()){
                String updateSpawnerStack = "UPDATE " + this.getTablePrefix() + "spawner_stacks SET amount = ?, data_type = ?, tier = ? WHERE spawner_id = ? AND data_type = ? AND tier = ?";
                PreparedStatement statement = connection.prepareStatement(updateSpawnerStack);
                statement.setInt(1, stack.getStackSize());
                statement.setString(2, stack.getSpawnerData().getIdentifyingName());
                statement.setString(3, stack.getCurrentTier().getIdentifyingName());
                statement.setInt(4, stack.getSpawner().getId());
                statement.setString(5, dataBeforeUpdate);
                statement.setString(6, tierBeforeUpdate);
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void deleteSpawnerStack(SpawnerStack stack) {
        deleteSpawnerStack(stack, stack.getCurrentTier());
    }

    public void deleteSpawnerStack(SpawnerStack stack, SpawnerTier tier) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()){
                String deleteSpawnerStacks = "DELETE FROM " + this.getTablePrefix() + "spawner_stacks WHERE spawner_id = ? AND data_type = ? AND tier = ?";
                PreparedStatement statement = connection.prepareStatement(deleteSpawnerStacks);
                statement.setInt(1, stack.getSpawner().getId());
                statement.setString(2, stack.getSpawnerData().getIdentifyingName());
                statement.setString(3, tier.getIdentifyingName());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void createSpawner(PlacedSpawnerImpl spawner) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()){
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

                String createSpawnerStack = "INSERT INTO " + this.getTablePrefix() + "spawner_stacks (spawner_id, data_type, tier, amount) VALUES (?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(createSpawnerStack)) {
                    for (SpawnerStackImpl stack : spawner.getSpawnerStacks()) {
                        statement.setInt(1, spawnerId);
                        statement.setString(2, stack.getSpawnerData().getIdentifyingName());
                        statement.setString(3, stack.getCurrentTier().getIdentifyingName());
                        statement.setInt(4, stack.getStackSize());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void createSpawnerStack(SpawnerStack stack) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()){
                String createSpawnerStack = "INSERT INTO " + this.getTablePrefix() + "spawner_stacks (spawner_id, data_type, tier, amount) VALUES (?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(createSpawnerStack);
                statement.setInt(1, stack.getSpawner().getId());
                statement.setString(2, stack.getSpawnerData().getIdentifyingName());
                statement.setString(3, stack.getCurrentTier().getIdentifyingName());
                statement.setInt(4, stack.getStackSize());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void createBoost(BoostedImpl boosted) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()){
                if (boosted instanceof BoostedPlayerImpl) {
                    BoostedPlayerImpl boostedPlayer = (BoostedPlayerImpl) boosted;
                    String createBoostedPlayer = "INSERT INTO " + this.getTablePrefix() + "boosted_players (player, amount, end_time) VALUES (?, ?, ?)";
                    PreparedStatement statement = connection.prepareStatement(createBoostedPlayer);
                    statement.setString(1, boostedPlayer.getPlayer().getUniqueId().toString());
                    statement.setInt(2, boostedPlayer.getAmountBoosted());
                    statement.setLong(3, boostedPlayer.getEndTime());
                    statement.executeUpdate();
                } else if (boosted instanceof BoostedSpawnerImpl) {
                    BoostedSpawnerImpl boostedSpawner = (BoostedSpawnerImpl) boosted;
                    String createBoostedSpawner = "INSERT INTO " + this.getTablePrefix() + "boosted_spawners (world, x, y, z, amount, end_time) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement statement = connection.prepareStatement(createBoostedSpawner);
                    Location location = ((BoostedSpawnerImpl) boosted).getLocation();
                    statement.setString(1, location.getWorld().getName());
                    statement.setInt(2, Math.toIntExact(Math.round(location.getX())));
                    statement.setInt(3, Math.toIntExact(Math.round(location.getY())));
                    statement.setInt(4, Math.toIntExact(Math.round(location.getZ())));
                    statement.setInt(5, boostedSpawner.getAmountBoosted());
                    statement.setLong(6, boostedSpawner.getEndTime());
                    statement.executeUpdate();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void createEntityKill(OfflinePlayer player, EntityType entityType, int count) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()){
                String createEntityKill = "INSERT INTO " + this.getTablePrefix() + "entity_kills (player, entity_type, count) VALUES (?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(createEntityKill);
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, entityType.name());
                statement.setInt(3, count);
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }


    public void updateEntityKill(OfflinePlayer player, EntityType entityType, int count) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()){
                String updateEntityKill = "UPDATE " + this.getTablePrefix() + "entity_kills SET count = ? WHERE player = ? AND entity_type = ?";
                PreparedStatement statement = connection.prepareStatement(updateEntityKill);
                statement.setInt(1, count);
                statement.setString(2, player.getUniqueId().toString());
                statement.setString(3, entityType.name());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void deleteEntityKills(OfflinePlayer player, EntityType entityType) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()){
                String deleteEntityKills = "DELETE FROM " + this.getTablePrefix() + "entity_kills WHERE player = ? AND entity_type = ?";
                PreparedStatement statement = connection.prepareStatement(deleteEntityKills);
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, entityType.name());
                statement.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void getBoosts(Consumer<List<Boosted>> callback) {
        List<Boosted> boosts = new ArrayList<>();
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()){
                try (Statement statement = connection.createStatement()) {
                    String selectBoostedPlayers = "SELECT * FROM " + this.getTablePrefix() + "boosted_players";
                    ResultSet result = statement.executeQuery(selectBoostedPlayers);
                    while (result.next()) {
                        UUID player = UUID.fromString(result.getString("player"));
                        int amount = result.getInt("amount");
                        long endTime = result.getLong("end_time");
                        boosts.add(new BoostedPlayerImpl(player, amount, endTime));
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
                        boosts.add(new BoostedSpawnerImpl(location, amount, endTime));
                    }
                }
                this.sync(() -> callback.accept(boosts));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void getEntityKills(Consumer<Map<UUID, Map<EntityType, Integer>>> callback) {
        Map<UUID, Map<EntityType, Integer>> entityKills = new HashMap<>();
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()){
                Statement statement = connection.createStatement();
                String selectEntityKills = "SELECT * FROM " + this.getTablePrefix() + "entity_kills";
                ResultSet result = statement.executeQuery(selectEntityKills);
                while (result.next()) {
                    UUID player = UUID.fromString(result.getString("player"));
                    String typeStr = result.getString("entity_type");
                    EntityType type = typeStr.equals("PIG_ZOMBIE")
                            && ServerVersion.isServerVersionAtLeast(ServerVersion.V1_16)
                            ? EntityType.valueOf("ZOMBIFIED_PIGLIN") : EntityType.valueOf(typeStr);
                    int count = result.getInt("count");
                    if (!entityKills.containsKey(player))
                        entityKills.put(player, new HashMap<>());
                    entityKills.get(player).put(type, count);
                }
                this.sync(() -> callback.accept(entityKills));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void deleteBoost(Boosted boosted) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()){
                if (boosted instanceof BoostedPlayerImpl) {
                    String deleteBoost = "DELETE FROM " + this.getTablePrefix() + "boosted_players WHERE end_time = ?";
                    PreparedStatement statement = connection.prepareStatement(deleteBoost);
                    statement.setLong(1, boosted.getEndTime());
                    statement.executeUpdate();
                } else if (boosted instanceof BoostedSpawnerImpl) {
                    String deleteBoost = "DELETE FROM " + this.getTablePrefix() + "boosted_spawners WHERE end_time = ?";
                    PreparedStatement statement = connection.prepareStatement(deleteBoost);
                    statement.setLong(1, boosted.getEndTime());
                    statement.executeUpdate();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void deleteSpawner(PlacedSpawner spawner) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()){
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
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void getSpawners(Consumer<Map<Location, PlacedSpawnerImpl>> callback) {
        this.runAsync(() -> {
            try (Connection connection = this.databaseConnector.getConnection()){
                EpicSpawners plugin = EpicSpawners.getInstance();

                Map<Integer, PlacedSpawnerImpl> spawners = new HashMap<>();

                try (Statement statement = connection.createStatement()) {
                    String selectSpawners = "SELECT * FROM " + this.getTablePrefix() + "placed_spawners";
                    ResultSet result = statement.executeQuery(selectSpawners);
                    while (result.next()) {
                        World world = Bukkit.getWorld(result.getString("world"));

                        if (world == null)
                            continue;

                        int id = result.getInt("id");
                        int spawns = result.getInt("spawn_count");

                        String placedByStr = result.getString("placed_by");
                        UUID placedBy = placedByStr == null ? null : UUID.fromString(result.getString("placed_by"));

                        int x = result.getInt("x");
                        int y = result.getInt("y");
                        int z = result.getInt("z");
                        Location location = new Location(world, x, y, z);

                        PlacedSpawnerImpl spawner = new PlacedSpawnerImpl(location);
                        spawner.setId(id);
                        spawner.setSpawnCount(spawns);
                        spawner.setPlacedBy(placedBy);
                        spawners.put(id, spawner);
                    }
                }

                try (Statement statement = connection.createStatement()) {
                    String selectSpawnerStacks = "SELECT * FROM " + this.getTablePrefix() + "spawner_stacks";
                    ResultSet result = statement.executeQuery(selectSpawnerStacks);
                    while (result.next()) {
                        PlacedSpawnerImpl spawner = spawners.get(result.getInt("spawner_id"));
                        if (spawner == null)
                            continue;

                        String type = result.getString("data_type");
                        String tier = result.getString("tier");
                        int amount = result.getInt("amount");
                        SpawnerDataImpl data = plugin.getSpawnerManager().getSpawnerData(type);
                        if (data == null) continue;
                        SpawnerTier spawnerTier = data.getTierOrFirst(tier);
                        SpawnerStackImpl stack = new SpawnerStackImpl(spawner, spawnerTier);
                        stack.setStackSize(amount);
                        spawner.addSpawnerStack(stack);
                    }
                }

                Map<Location, PlacedSpawnerImpl> returnableSpawners = new HashMap<>();
                for (PlacedSpawnerImpl spawner : spawners.values())
                    returnableSpawners.put(spawner.getLocation(), spawner);

                this.sync(() -> callback.accept(returnableSpawners));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}

package com.songoda.epicspawners.database.migrations;

import com.songoda.core.database.DataMigration;
import com.songoda.core.database.MySQLConnector;
import com.songoda.epicspawners.EpicSpawners;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _1_InitialMigration extends DataMigration {

    public _1_InitialMigration() {
        super(1);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {
        String autoIncrement = EpicSpawners.getInstance().getDatabaseConnector() instanceof MySQLConnector ? " AUTO_INCREMENT" : "";

        // Create spawners table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "placed_spawners (" +
                    "id INTEGER PRIMARY KEY" + autoIncrement + ", " +
                    "spawn_count INTEGER NOT NULL, " +
                    "placed_by VARCHAR(36), " +
                    "world TEXT NOT NULL, " +
                    "x DOUBLE NOT NULL, " +
                    "y DOUBLE NOT NULL, " +
                    "z DOUBLE NOT NULL, " +
                    "UNIQUE (world,x,y,z) " +
                    "ON CONFLICT REPLACE" +
                    ")");
        }

        // Create spawner stacks
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "spawner_stacks (" +
                    "spawner_id INTEGER NOT NULL, " +
                    "data_type VARCHAR(100) NOT NULL," +
                    "amount INTEGER NOT NULL " +
                    ")");
        }

        // Create player boosts
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "boosted_players (" +
                    "player VARCHAR(36) NOT NULL, " +
                    "amount INTEGER NOT NULL," +
                    "end_time BIGINT NOT NULL " +
                    ")");
        }

        // Create spawner boosts
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "boosted_spawners (" +
                    "world TEXT NOT NULL, " +
                    "x DOUBLE NOT NULL, " +
                    "y DOUBLE NOT NULL, " +
                    "z DOUBLE NOT NULL, " +
                    "amount INTEGER NOT NULL," +
                    "end_time BIGINT NOT NULL " +
                    ")");
        }

        // Create entity kills
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "entity_kills (" +
                    "player VARCHAR(36) NOT NULL, " +
                    "entity_type VARCHAR(100) NOT NULL, " +
                    "count DOUBLE NOT NULL " +
                    ")");
        }
    }

}

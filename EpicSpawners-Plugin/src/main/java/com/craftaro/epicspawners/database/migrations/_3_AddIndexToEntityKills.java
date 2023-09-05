package com.craftaro.epicspawners.database.migrations;

import com.craftaro.core.database.DataMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _3_AddIndexToEntityKills extends DataMigration {

    public _3_AddIndexToEntityKills() {
        super(3);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            //add "player" field as a primary key
            statement.execute("ALTER TABLE " + tablePrefix + "entity_kills PRIMARY KEY (player)");
        } catch (Exception ignored) {}
    }
}

package com.craftaro.epicspawners.database.migrations;

import com.craftaro.core.database.DataMigration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _2_AddTiers extends DataMigration {
    public _2_AddTiers() {
        super(2);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + tablePrefix + "spawner_stacks ADD COLUMN IF NOT EXISTS tier VARCHAR(100) DEFAULT 'Tier_1' NOT NULL");
        }
    }
}

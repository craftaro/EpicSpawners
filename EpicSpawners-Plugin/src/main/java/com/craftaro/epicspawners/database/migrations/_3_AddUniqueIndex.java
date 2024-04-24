package com.craftaro.epicspawners.database.migrations;

import com.craftaro.core.database.DataMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class _3_AddUniqueIndex extends DataMigration {

    public _3_AddUniqueIndex() {
        super(3);
    }

    @Override
    public void migrate(Connection connection, String tablePrefix) throws SQLException {
        //Delete duplicate entries from previous versions
        try (Statement statement = connection.createStatement()) {
            //Delete duplicate data before adding unique index
            //MariaDB and H2
            statement.execute("DELETE FROM " + tablePrefix + "entity_kills WHERE count < (SELECT MAX(count) FROM " + tablePrefix + "entity_kills AS t2 WHERE t2.player = " + tablePrefix + "entity_kills.player AND t2.entity_type = " + tablePrefix + "entity_kills.entity_type)");
        } catch (Exception e) {
            //MySQL
            try (PreparedStatement statement = connection.prepareStatement("CREATE TEMPORARY TABLE temp_table AS SELECT player, entity_type, MAX(count) as max_count FROM " + tablePrefix + "entity_kills GROUP BY player, entity_type")) {
                statement.execute();
            } catch (Exception ignored) {
                //Table already exists
            }

            try (PreparedStatement statement = connection.prepareStatement("DELETE e FROM " + tablePrefix + "entity_kills e INNER JOIN temp_table t ON e.player = t.player AND e.entity_type = t.entity_type WHERE e.count < t.max_count")) {
                statement.execute();
            } catch (Exception ignored) {
                //Table already exists
            }

            try (PreparedStatement statement = connection.prepareStatement("DROP TEMPORARY TABLE temp_table")) {
                statement.execute();
            } catch (Exception ignored) {
                //Table already exists
            }
        }

        try (Statement statement = connection.createStatement()) {
            //MySQL and MariaDB
            statement.execute("ALTER TABLE " + tablePrefix + "entity_kills ADD UNIQUE INDEX player_uuid_entity_type (player, entity_type)");
        } catch (Exception ignored) {
            //H2
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE " + tablePrefix + "entity_kills ADD CONSTRAINT IF NOT EXISTS player_uuid_entity_type UNIQUE (player, entity_type)");
            } catch (Exception ignored2) {
                //Index already exists in the MySQL or MariaDB
            }
        }
    }
}

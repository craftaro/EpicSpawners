package com.songoda.epicspawners.utils;

import com.songoda.epicspawners.EpicSpawnersPlugin;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a MySQL database source which directly connects to, queries and
 * executes statements towards the database found at the constructed location.
 * Operations performed on this object are done async (with the help of the
 * {@link CompletableFuture} API) and uses a connection pool as to not have to
 * constantly connect to and disconnect from the database
 */
public class MySQLDatabase {

    private final EpicSpawnersPlugin instance;

    private Connection connection;

    /**
     * Construct a new instance of a MySQLDatabase given the specified database
     * credentials file. The file should be under the following format:
     * <p>
     * <code>
     * host:127.0.0.1<br>
     * user:database_username<br>
     * password:database_password
     * </code>
     *
     * @param instance            an instance of the plugin
     */
    public MySQLDatabase(EpicSpawnersPlugin instance) {
        this.instance = instance;
        try {
            Class.forName("com.mysql.jdbc.Driver");

            String url = "jdbc:mysql://" + instance.getConfig().getString("Database.IP") + ":" + instance.getConfig().getString("Database.Port") + "/" + instance.getConfig().getString("Database.Database Name");
            this.connection = DriverManager.getConnection(url, instance.getConfig().getString("Database.Username"), instance.getConfig().getString("Database.Password"));

            //ToDo: This is sloppy
            connection.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS `spawners` (\n" +
                    "\t`location` TEXT NULL,\n" +
                    "\t`stacks` TEXT NULL,\n" +
                    "\t`spawns` INT NULL,\n" +
                    "\t`placedby` TEXT NULL\n" +
                    ")");

            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `boosts` (\n" +
                    "\t`endtime` TEXT NULL,\n" +
                    "\t`boosttype` TEXT NULL,\n" +
                    "\t`data` TEXT NULL,\n" +
                    "\t`amount` INT NULL\n" +
                    ")");

            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `players` (\n" +
                    "\t`uuid` TEXT NULL,\n" +
                    "\t`entitykills` TEXT NULL\n" +
                    ")");

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Database connection failed.");
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
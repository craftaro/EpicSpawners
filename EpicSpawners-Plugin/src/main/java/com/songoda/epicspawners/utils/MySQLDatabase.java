package com.songoda.epicspawners.utils;

import com.songoda.epicspawners.EpicSpawnersPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDatabase {

    private final EpicSpawnersPlugin instance;

    private Connection connection;

    public MySQLDatabase(EpicSpawnersPlugin instance) {
        this.instance = instance;
        try {
            Class.forName("com.mysql.jdbc.Driver");

            String url = "jdbc:mysql://" + instance.getConfig().getString("Database.IP") + ":" + instance.getConfig().getString("Database.Port") + "/" + instance.getConfig().getString("Database.Database Name") + "?autoReconnect=true&useSSL=false";
            this.connection = DriverManager.getConnection(url, instance.getConfig().getString("Database.Username"), instance.getConfig().getString("Database.Password"));

            createTables();

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Database connection failed.");
        }
    }

    private void createTables() {
        try {
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `spawners` (\n" +
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

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
package com.songoda.epicspawners.utils;

import com.songoda.epicspawners.EpicSpawners;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDatabase {

    private final EpicSpawners plugin;

    private Connection connection;

    public MySQLDatabase(EpicSpawners plugin) {
        this.plugin = plugin;
        try {
            Class.forName("com.mysql.jdbc.Driver");

            String url = "jdbc:mysql://" + plugin.getConfig().getString("Database.IP") + ":" + plugin.getConfig().getString("Database.Port") + "/" + plugin.getConfig().getString("Database.Database Name") + "?autoReconnect=true&useSSL=false";
            this.connection = DriverManager.getConnection(url, plugin.getConfig().getString("Database.Username"), plugin.getConfig().getString("Database.Password"));

            createTables();

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Database connection failed.");
        }
    }

    private void createTables() {
        try {
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `" + plugin.getConfig().getString("Database.Prefix") + "spawners` (\n" +
                    "\t`location` TEXT NULL,\n" +
                    "\t`stacks` TEXT NULL,\n" +
                    "\t`spawns` INT NULL,\n" +
                    "\t`placedby` TEXT NULL\n" +
                    ")");

            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `" + plugin.getConfig().getString("Database.Prefix") + "boosts` (\n" +
                    "\t`endtime` TEXT NULL,\n" +
                    "\t`boosttype` TEXT NULL,\n" +
                    "\t`data` TEXT NULL,\n" +
                    "\t`amount` INT NULL\n" +
                    ")");

            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS `" + plugin.getConfig().getString("Database.Prefix") + "players` (\n" +
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
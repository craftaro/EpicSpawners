package com.songoda.epicspawners.storage.types;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.storage.Storage;
import com.songoda.epicspawners.storage.StorageItem;
import com.songoda.epicspawners.storage.StorageRow;
import com.songoda.epicspawners.utils.MySQLDatabase;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageMysql extends Storage {

    private MySQLDatabase database;

    public StorageMysql(EpicSpawnersPlugin instance) {
        super(instance);
        this.database = new MySQLDatabase(instance);
    }

    @Override
    public boolean containsGroup(String group) {
        try {
            DatabaseMetaData dbm = database.getConnection().getMetaData();
            ResultSet rs = dbm.getTables(null, null, group, null);
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;

    }

    @Override
    public List<StorageRow> getRowsByGroup(String group) {
        List<StorageRow> rows = new ArrayList<>();
        try {
            ResultSet set = database.getConnection().createStatement().executeQuery(String.format("SELECT * FROM `%s`", group));
            while (set.next()) {
                Map<String, StorageItem> items = new HashMap<>();

                String key = set.getString(1);
                for (int i = 2; i <= set.getMetaData().getColumnCount(); i++) {
                    if (set.getObject(i) == null || set.getObject(i) == "") continue;
                    StorageItem item = new StorageItem(set.getObject(i));
                    items.put(set.getMetaData().getColumnName(i), item);
                }
                StorageRow row = new StorageRow(key, items);
                rows.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    @Override
    public void clearFile() {
        try {
            database.getConnection().createStatement().execute("TRUNCATE `spawners`");
            database.getConnection().createStatement().execute("TRUNCATE `boosts`");
            database.getConnection().createStatement().execute("TRUNCATE `players`");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveItem(String group, StorageItem... items) {
        try {
            StringBuilder sql = new StringBuilder(String.format("INSERT INTO `%s`", group));

            sql.append(" (");

            for (StorageItem item : items) {
                if (item == null || item.asObject() == null) continue;
                sql.append(String.format("`%s`, ", item.getKey()));
            }

            sql = new StringBuilder(sql.substring(0, sql.length() - 2));

            sql.append(") VALUES (");

            for (StorageItem item : items) {
                if (item == null || item.asObject() == null) continue;
                sql.append(String.format("'%s', ", item.asObject().toString()));
            }

            sql = new StringBuilder(sql.substring(0, sql.length() - 2));

            sql.append(");");

            database.getConnection().createStatement().execute(sql.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeConnection() {
        try {
            database.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


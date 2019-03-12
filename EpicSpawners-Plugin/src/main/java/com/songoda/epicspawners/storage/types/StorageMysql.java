package com.songoda.epicspawners.storage.types;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.storage.Storage;
import com.songoda.epicspawners.storage.StorageItem;
import com.songoda.epicspawners.storage.StorageRow;
import com.songoda.epicspawners.utils.MySQLDatabase;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StorageMysql extends Storage {

    private static Map<String, StorageItem[]> toSave = new HashMap<>();
    private static Map<String, StorageItem[]> lastSave = null;
    private MySQLDatabase database;

    public StorageMysql(EpicSpawnersPlugin instance) {
        super(instance);
        this.database = new MySQLDatabase(instance);
    }

    @Override
    public boolean containsGroup(String group) {
        try {
            DatabaseMetaData dbm = database.getConnection().getMetaData();
            ResultSet rs = dbm.getTables(null, null, instance.getConfig().getString("Database.Prefix") + group, null);
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
            ResultSet set = database.getConnection().createStatement().executeQuery(String.format("SELECT * FROM `" + instance.getConfig().getString("Database.Prefix") + "%s`", group));
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
    public void prepareSaveItem(String group, StorageItem... items) {
        toSave.put(group + "]" + items[0].asObject().toString(), items);
    }

    @Override
    public void doSave() {
        this.updateData(instance);

        if (lastSave == null)
            lastSave = new HashMap<>(toSave);

        if (toSave.isEmpty()) return;
        Map<String, StorageItem[]> nextSave = new HashMap<>(toSave);

        this.makeBackup();
        this.save();

        toSave.clear();
        lastSave.clear();
        lastSave.putAll(nextSave);
    }

    @Override
    public void save() {
        try {
            Statement stmt = database.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            last:
            for (Map.Entry<String, StorageItem[]> last : lastSave.entrySet()) {
                String lastKey = last.getKey().split("]")[0];
                String lastValue = last.getValue()[0].asObject().toString();

                for (Map.Entry<String, StorageItem[]> to : toSave.entrySet()) {
                    String toKey = to.getKey().split("]")[0];
                    if (!toKey.equals(lastKey)
                            || !to.getValue()[0].asObject().equals(lastValue)
                            || to.getValue().length != last.getValue().length)
                        continue;
                    toSave.remove(to.getKey());
                    for (int i = 0; i < to.getValue().length; i ++) {
                        if (!to.getValue()[i].asObject().toString()
                                .equals(last.getValue()[i].asObject().toString())) {
                            //Update
                            StorageItem[] items = to.getValue();
                            StringBuilder sql = new StringBuilder(String.format("UPDATE `" + instance.getConfig().getString("Database.Prefix") + "%s`", toKey));

                            sql.append(" SET");

                            for (StorageItem item : items) {
                                if (item == null || item.asObject() == null) continue;
                                String key = item.getKey().split("]")[0];
                                sql.append(String.format("`%s` = '%s', ", key, item.asObject().toString()));
                            }

                            sql = new StringBuilder(sql.substring(0, sql.length() - 2));

                            sql.append(String.format(" WHERE `%s`='%s'", last.getValue()[0].getKey(), last.getValue()[0].asObject().toString()));

                            stmt.addBatch(sql.toString());

                            continue last;
                        }
                    }
                    // Already up to date.

                    continue last;
                }
                //Was not found delete.
                StringBuilder sql = new StringBuilder(String.format("DELETE FROM `" + instance.getConfig().getString("Database.Prefix") + "%s`", lastKey));
                sql.append(String.format(" WHERE `%s`='%s'", last.getValue()[0].getKey(), last.getValue()[0].asObject().toString()));
                stmt.addBatch(sql.toString());

            }

            for (Map.Entry<String, StorageItem[]> to : toSave.entrySet()) {
                String toKey = to.getKey().split("]")[0];
                //Add
                StorageItem[] items = to.getValue();
                StringBuilder sql = new StringBuilder(String.format("INSERT INTO `" + instance.getConfig().getString("Database.Prefix") + "%s`", toKey));

                sql.append(" (");

                for (StorageItem item : items) {
                    if (item == null || item.asObject() == null) continue;
                    String key = item.getKey().split("]")[0];
                    sql.append(String.format("`%s`, ", key));
                }

                sql = new StringBuilder(sql.substring(0, sql.length() - 2));

                sql.append(") VALUES (");

                for (StorageItem item : items) {
                    if (item == null || item.asObject() == null) continue;
                    sql.append(String.format("'%s', ", item.asObject().toString()));
                }

                sql = new StringBuilder(sql.substring(0, sql.length() - 2));

                sql.append(");");

                stmt.addBatch(sql.toString());
            }

            stmt.executeBatch();

            toSave.clear();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void makeBackup() {

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


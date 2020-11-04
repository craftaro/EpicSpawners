package com.songoda.epicspawners.tiers.storage;

import com.songoda.core.database.DataManagerAbstract;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.tiers.models.TierMob;
import com.songoda.epicspawners.tiers.models.TierType;
import org.bukkit.plugin.Plugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

/**
 * This class handles the SQL manager
 * This is my first time using SQL so
 * bare with me
 *
 * Class made by CodePunisher with <3
 */
public class TierSQLManager extends DataManagerAbstract
{
    private final EpicSpawners plugin;

    public TierSQLManager(DatabaseConnector databaseConnector, Plugin plugin) {
        super(databaseConnector, plugin);
        this.plugin = EpicSpawners.getInstance();
    }

    /** Creating table with columns (if it doesn't exist) */
    public void createTierMobTable() {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String creatTable = "CREATE TABLE IF NOT EXISTS " + this.getTablePrefix() + "tier_mobs (uuid varchar(255), mob_type varchar(255), level int)";

            try (PreparedStatement statement = connection.prepareStatement(creatTable)) {
                statement.executeUpdate();
            }
        }));
    }

    /**
     * Clearing tables
     *
     * This feature is used when the
     * "enabled" option is set to false
     * or when the plugin enables
     * and the option is set to "true"
     */
    public void removeTableValues() {
        this.async(() -> this.databaseConnector.connect(connection -> {
            String removeValues = "DELETE FROM " + this.getTablePrefix() + "tier_mobs";
            try (PreparedStatement statement = connection.prepareStatement(removeValues)) {
                statement.executeUpdate();
            }
        }));
    }

    /**
     * This method stores a tiermob
     * in the SQL storage async
     *
     * I'm not a fan of doing it this way
     * this could potentially happen constantly
     * I tried to only do this for all tier mobs
     * on disable, but your onPluginDisable you
     * implemented won't allow me to run a task
     *
     * I considered using persistent data containers
     * for this, but then I realized, this has to
     * work for 1.8 as well, and was like.... fuck
     *
     * Could always just not store tier mobs, I mean
     * because like, why does this even REALLY need
     * to happen? I'm mostly just adding this just
     * incase you're wanting to keep it
     */
    public void addTierMob(UUID uuid, TierMob tierMob) {
        this.async(() -> this.databaseConnector.connect(connection -> {
            // Adding mobs yo
            String addMob = "INSERT INTO " + this.getTablePrefix() + "tier_mobs (uuid, mob_type, level) VALUES (?, ?, ?)";

            String stringUUID = uuid.toString();
            String mobType = tierMob.getTierType().getEntityType();
            int level = tierMob.getSpawnerLevel();

            try (PreparedStatement statement = connection.prepareStatement(addMob)) {
                statement.setString(1, stringUUID);
                statement.setString(2, mobType);
                statement.setInt(3, level);
                statement.executeUpdate();
            }
        }));
    }

    /**
     * Getting all tier mobs and storing the objects
     * Then I'm clearing the data right after
     *
     * THIS SHOULD ONLY RUN ON ENABLE
     */
    public void getTierMobs() {
        this.async(() -> this.databaseConnector.connect(connection -> {
            // Only if feature is enabled
            if (plugin.getTierYMLManager().isEnabled()) {
                try (Statement statement = connection.createStatement()) {
                    String selectTierMobs = "SELECT * FROM " + this.getTablePrefix() + "tier_mobs";
                    ResultSet result = statement.executeQuery(selectTierMobs);

                    while (result.next()) {
                        UUID uuid = UUID.fromString(result.getString("uuid"));
                        String mobType = result.getString("mob_type");
                        int spawnerLevel = result.getInt("level");
                        TierType tierType = plugin.getTierDataManager().getTierType(mobType);
                        TierMob tierMob = new TierMob(tierType, spawnerLevel);
                        plugin.getTierDataManager().addTierMob(uuid, tierMob, false);
                    }
                }


                // Removes table values
                removeTableValues();
            }
        }));
    }
}

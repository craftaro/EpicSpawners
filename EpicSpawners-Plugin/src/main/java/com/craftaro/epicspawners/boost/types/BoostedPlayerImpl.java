package com.craftaro.epicspawners.boost.types;

import com.craftaro.core.database.Data;
import com.craftaro.epicspawners.api.boosts.types.BoostedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BoostedPlayerImpl extends BoostedImpl implements BoostedPlayer {
    private final UUID player;

    /**
     * Default constructor used for database loading.
     */
    public BoostedPlayerImpl() {
        super(0, 0);
        this.player = null;
    }

    public BoostedPlayerImpl(UUID player, int amtBoosted, long endTime) {
        super(amtBoosted, endTime);
        this.player = player;
    }

    public BoostedPlayerImpl(Player player, int amtBoosted, long endTime) {
        super(amtBoosted, endTime);
        this.player = player.getUniqueId();
    }

    @Override
    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(player);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("player", player.toString());
        map.put("amount", getAmountBoosted());
        map.put("end_time", getEndTime());
        return map;
    }

    @Override
    public Data deserialize(Map<String, Object> map) {
        return new BoostedPlayerImpl(UUID.fromString((String) map.get("player")), (int) map.get("amount"), (long) map.get("end_time"));
    }

    @Override
    public String getTableName() {
        return "boosted_players";
    }
}

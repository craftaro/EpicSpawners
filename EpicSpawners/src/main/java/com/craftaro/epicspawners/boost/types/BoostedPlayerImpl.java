package com.craftaro.epicspawners.boost.types;

import com.craftaro.epicspawners.api.boosts.types.BoostedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BoostedPlayerImpl extends BoostedImpl implements BoostedPlayer {

    private final UUID player;

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
}

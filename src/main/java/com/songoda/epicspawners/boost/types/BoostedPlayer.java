package com.songoda.epicspawners.boost.types;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BoostedPlayer extends Boosted {

    private final UUID player;

    public BoostedPlayer(UUID player, int amtBoosted, long endTime) {
        super(amtBoosted, endTime);
        this.player = player;
    }

    public BoostedPlayer(Player player, int amtBoosted, long endTime) {
        super(amtBoosted, endTime);
        this.player = player.getUniqueId();
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(player);
    }
}

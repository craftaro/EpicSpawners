package com.craftaro.epicspawners.api.boosts.types;

import org.bukkit.OfflinePlayer;

public interface BoostedPlayer extends Boosted {
    OfflinePlayer getPlayer();
}

package com.craftaro.epicspawners.api.boosts.types;

import com.craftaro.core.database.Data;
import org.bukkit.OfflinePlayer;

public interface BoostedPlayer extends Boosted {

    OfflinePlayer getPlayer();
}

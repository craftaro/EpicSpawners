package com.songoda.epicspawners.economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface Economy {

    boolean hasBalance(OfflinePlayer player, double cost);

    boolean withdrawBalance(OfflinePlayer player, double cost);

    boolean deposit(OfflinePlayer player, double amount);
}

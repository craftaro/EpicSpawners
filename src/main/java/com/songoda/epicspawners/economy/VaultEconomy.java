package com.songoda.epicspawners.economy;

import com.songoda.epicspawners.EpicSpawners;
import org.bukkit.entity.Player;

public class VaultEconomy implements Economy {

    private final EpicSpawners plugin;

    private final net.milkbowl.vault.economy.Economy vault;

    public VaultEconomy(EpicSpawners plugin) {
        this.plugin = plugin;

        this.vault = plugin.getServer().getServicesManager().
                getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();
    }

    @Override
    public boolean hasBalance(Player player, double cost) {
        return vault.has(player, cost);
    }

    @Override
    public boolean withdrawBalance(Player player, double cost) {
        return vault.withdrawPlayer(player, cost).transactionSuccess();
    }
}

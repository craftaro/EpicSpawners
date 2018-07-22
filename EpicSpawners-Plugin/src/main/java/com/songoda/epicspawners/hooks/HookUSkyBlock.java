package com.songoda.epicspawners.hooks;

import com.songoda.epicspawners.api.utils.ClaimableProtectionPluginHook;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;

public class HookUSkyBlock implements ClaimableProtectionPluginHook {

    private final uSkyBlockAPI uSkyblock;

    public HookUSkyBlock() {
        this.uSkyblock = (uSkyBlockAPI) Bukkit.getPluginManager().getPlugin("USkyBlock");
    }

    @Override
    public JavaPlugin getPlugin() { // uSkyBlockAPI is also an instance of JavaPlugin
        return (JavaPlugin) uSkyblock;
    }

    @Override
    public boolean canBuild(Player player, Location location) {
        return uSkyblock.getIslandInfo(location).getOnlineMembers().contains(player) || uSkyblock.getIslandInfo(location).isLeader(player);
    }

    @Override
    public boolean isInClaim(Location location, String id) {
        return uSkyblock.getIslandInfo(location).getLeader().equals(id);
    }

    @Override
    public String getClaimID(String name) {
        return null;
    }

}
package com.songoda.epicspawners.hooks;

import com.songoda.epicspawners.api.utils.HookType;
import com.songoda.epicspawners.api.utils.ProtectionPluginHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.talabrek.ultimateskyblock.api.uSkyBlockAPI;

public class HookUSkyBlock implements ProtectionPluginHook {

    private final uSkyBlockAPI uSkyblock;

    public HookUSkyBlock() {
        this.uSkyblock = (uSkyBlockAPI) Bukkit.getPluginManager().getPlugin("USkyBlock");
    }

    @Override
    public JavaPlugin getPlugin() { // uSkyBlockAPI is also an instance of JavaPlugin
        return (JavaPlugin) uSkyblock;
    }

    @Override
    public HookType getHookType() {
        return HookType.ISLAND;
    }

    @Override
    public boolean canBuild(Player player, Location location) {
        return uSkyblock.getIslandInfo(location).getOnlineMembers().contains(player) || uSkyblock.getIslandInfo(location).isLeader(player);
    }

    @Override
    public boolean isInClaim(Location location) {
        return uSkyblock.getIslandInfo(location) != null;
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
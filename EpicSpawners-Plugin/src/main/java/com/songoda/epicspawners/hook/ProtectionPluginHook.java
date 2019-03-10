package com.songoda.epicspawners.hook;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public interface ProtectionPluginHook {

    JavaPlugin getPlugin();

    HookType getHookType();

    boolean canBuild(Player player, Location location);

    default boolean canBuild(Player player, Block block) {
        return block != null && canBuild(player, block.getLocation());
    }

    boolean isInClaim(Location location);

    boolean isInClaim(Location location, String id);

    String getClaimID(String name);
}
package com.songoda.epicspawners.hooks;

import com.songoda.epicspawners.api.utils.ClaimableProtectionPluginHook;
import me.goodandevil.skyblock.SkyBlock;
import me.goodandevil.skyblock.island.Island;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HookSkyBlockEarth implements ClaimableProtectionPluginHook {

    private final SkyBlock skyblock;

    public HookSkyBlockEarth() {
        this.skyblock = SkyBlock.getInstance();
    }

    @Override
    public JavaPlugin getPlugin() {
        return SkyBlock.getInstance();
    }

    @Override
    public boolean canBuild(Player player, Location location) {
        Island island = skyblock.getIslandManager().getIslandAtLocation(location);
        if (island == null) return true;

        UUID owner = island.getOwnerUUID();
        UUID playerUUID = player.getUniqueId();
        if (owner == null || owner.equals(playerUUID)) return true;

        Set<UUID> teamMembers = island.getCoopPlayers();
        if (teamMembers.contains(playerUUID)) return true;

        List<Island> coopIslands = skyblock.getIslandManager().getCoopIslands(player);
        for (Island is : coopIslands) {
            if (is.getOwnerUUID().equals(playerUUID)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isInClaim(Location location, String id) {
        return skyblock.getIslandManager().getIslandAtLocation(location).getOwnerUUID().toString().equals(id);
    }

    @Override
    public String getClaimID(String name) {
        return null;
    }

}
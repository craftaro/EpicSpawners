package com.songoda.epicspawners.hooks;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.songoda.epicspawners.api.utils.ClaimableProtectionPluginHook;
import com.wasteofplastic.askyblock.ASkyBlock;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HookASkyBlock implements ClaimableProtectionPluginHook {

    private final ASkyBlockAPI skyblock;

    public HookASkyBlock() {
        this.skyblock = ASkyBlockAPI.getInstance();
    }

    @Override
    public JavaPlugin getPlugin() {
        return ASkyBlock.getPlugin();
    }

    @Override
    public boolean canBuild(Player player, Location location) {
        Island island = skyblock.getIslandAt(location);
        if (island == null) return true;

        UUID owner = island.getOwner();
        UUID playerUUID = player.getUniqueId();
        if (owner == null || owner.equals(playerUUID)) return true;

        List<UUID> teamMembers = skyblock.getTeamMembers(owner);
        if (teamMembers.contains(playerUUID)) return true;

        Set<Location> coopIslands = skyblock.getCoopIslands(player);
        for (Location islandLocation : coopIslands) {
            if (skyblock.getIslandAt(islandLocation).getOwner().equals(playerUUID)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isInClaim(Location location, String id) {
        return skyblock.getOwner(location).toString().equals(id);
    }

    @Override
    public String getClaimID(String name) {
        return null;
    }

}
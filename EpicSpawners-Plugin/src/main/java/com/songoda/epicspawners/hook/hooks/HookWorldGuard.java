package com.songoda.epicspawners.hook.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.songoda.epicspawners.hook.HookType;
import com.songoda.epicspawners.hook.ProtectionPluginHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HookWorldGuard implements ProtectionPluginHook {

    private final WorldGuard worldGuard;

    public HookWorldGuard() {
        this.worldGuard = WorldGuard.getInstance();
    }

    @Override
    public JavaPlugin getPlugin() {
        return WorldGuardPlugin.inst();
    }

    @Override
    public HookType getHookType() {
        return HookType.REGULAR;
    }

    @Override
    public boolean canBuild(Player player, Location location) {
        RegionQuery q = worldGuard.getPlatform().getRegionContainer().createQuery();
        ApplicableRegionSet ars = q.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
        return ars.testState(WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD);
    }

    @Override
    public boolean isInClaim(Location location) {
        return true;
    }

    @Override
    public boolean isInClaim(Location location, String id) {
        return false;
    }

    @Override
    public String getClaimID(String name) {
        return null;
    }

}
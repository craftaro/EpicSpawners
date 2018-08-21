package com.songoda.epicspawners.hooks;

import com.songoda.epicspawners.api.utils.ProtectionPluginHook;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HookGriefPrevention implements ProtectionPluginHook {

    private final GriefPrevention griefPrevention;

    public HookGriefPrevention() {
        this.griefPrevention = GriefPrevention.instance;
    }

    @Override
    public JavaPlugin getPlugin() {
        return griefPrevention;
    }

    @Override
    public boolean canBuild(Player player, Location location) {
        Claim claim = griefPrevention.dataStore.getClaimAt(location, false, null);
        return claim != null && claim.allowBuild(player, Material.STONE) == null;
    }

}
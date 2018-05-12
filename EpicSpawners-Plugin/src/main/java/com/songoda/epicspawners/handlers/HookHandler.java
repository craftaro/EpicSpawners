package com.songoda.epicspawners.handlers;

import com.songoda.arconix.api.utils.ConfigWrapper;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.hooks.*;
import com.songoda.epicspawners.utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by songoda on 3/17/2017.
 */
public class HookHandler {

    public Hook FactionsHook = null, RedProtectHook = null, ASkyBlockHook = null, USkyBlockHook = null,
            WorldGuardHook = null, GriefPreventionHook = null, PlotSquaredHook = null, KingdomsHook = null,
            TownyHook = null;

    public ConfigWrapper hooksFile = new ConfigWrapper(EpicSpawnersPlugin.getInstance(), "", "hooks.yml");

    public HookHandler() {
    }

    public void hook() {
        try {
            hooksFile.createNewFile("Loading hooks File", EpicSpawnersPlugin.getInstance().getDescription().getName() + " hooks File");

            new FactionsHook();
            new RedProtectHook();
            new GriefPreventionHook();
            new ASkyBlockHook();
            new USkyBlockHook();
            new WorldGuardHook();
            new PlotSquaredHook();
            new KingdomsHook();
            new TownyHook();

            hooksFile.getConfig().options().copyDefaults(true);
            hooksFile.saveConfig();
        } catch (Exception e) {
            Debugger.runReport(e);
        }

    }

    public boolean isInFaction(String name, Location l) {
        if (FactionsHook != null) {
            return FactionsHook.isInClaim(name, l);
        }
        return false;
    }

    public String getFactionId(String name) {
        if (FactionsHook != null) {
            return FactionsHook.getClaimId(name);
        }
        return null;
    }

    public boolean isInTown(String name, Location l) {
        if (TownyHook != null) {
            return TownyHook.isInClaim(name, l);
        }
        return false;
    }

    public String getTownId(String name) {
        if (TownyHook != null) {
            return TownyHook.getClaimId(name);
        }
        return null;
    }

    public boolean isInIsland(String name, Location l) {
        if (USkyBlockHook != null)
            return USkyBlockHook.isInClaim(name, l);
        else if (ASkyBlockHook != null)
            return ASkyBlockHook.isInClaim(name, l);
        else
            return false;
    }

    public String getIslandId(String name) {
        try {
            return Bukkit.getOfflinePlayer(name).getUniqueId().toString();
        } catch (Exception ignore) {
        }
        return null;
    }

    public boolean canBuild(Player p, Location l) {
        boolean result = true;
        if (WorldGuardHook != null)
            result = WorldGuardHook.canBuild(p, l);
        if (RedProtectHook != null && result)
            result = RedProtectHook.canBuild(p, l);
        if (FactionsHook != null && result)
            result = FactionsHook.canBuild(p, l);
        if (ASkyBlockHook != null && result)
            result = ASkyBlockHook.canBuild(p, l);
        if (USkyBlockHook != null && result)
            result = USkyBlockHook.canBuild(p, l);
        if (GriefPreventionHook != null && result) {
            result = GriefPreventionHook.canBuild(p, l);
        }
        if (PlotSquaredHook != null && result)
            result = PlotSquaredHook.canBuild(p, l);
        if (KingdomsHook != null && result)
            result = KingdomsHook.canBuild(p, l);
        if (TownyHook != null && result)
            result = TownyHook.canBuild(p, l);
        return result;
    }
}

package com.songoda.epicspawners.Hooks;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Utils.Debugger;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by songoda on 3/17/2017.
 */
public class TownyHook extends Hook {

    public TownyHook() {
        super("Towny");
        EpicSpawners plugin = EpicSpawners.pl();
        if (isEnabled())
            plugin.hooks.TownyHook = this;
    }

    @Override
    public boolean canBuild(Player p, Location location) {
        try {
            if (hasBypass(p) || TownyUniverse.isWilderness(location.getBlock())) return true;
            if (!TownyUniverse.getTownBlock(location).hasTown()) return true;

            Resident r = TownyUniverse.getDataSource().getResident(p.getName());
            return r.hasTown() && TownyUniverse.getTownName(location).equals(r.getTown().getName());
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return true;
    }

    @Override
    public boolean isInClaim(String id, Location location) {
        try {
            return !TownyUniverse.isWilderness(location.getBlock())
                    && TownyUniverse.getTownBlock(location).getTown().getUID() == Integer.parseInt(id);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return false;
    }

    @Override
    public String getClaimId(String name) {
        try {
            return TownyUniverse.getDataSource().getTown(name).getUID().toString();
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return null;
    }
}

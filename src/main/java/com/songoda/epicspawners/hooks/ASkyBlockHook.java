package com.songoda.epicspawners.hooks;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.utils.Debugger;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by songoda on 3/17/2017.
 */
public class ASkyBlockHook extends Hook {

    private ASkyBlockAPI as;

    public ASkyBlockHook() {
        super("ASkyblock");
        if (isEnabled()) {
            as = ASkyBlockAPI.getInstance();
            EpicSpawners plugin = EpicSpawners.getInstance();
            plugin.getHookHandler().ASkyBlockHook = this;
        }
    }

    @Override
    public boolean canBuild(Player p, Location location) {
        try {
            if (hasBypass(p) || as.getIslandAt(location) == null) return true;

            UUID owner = as.getOwner(location);
            List<UUID> list = as.getTeamMembers(owner);
            Set<Location> list2 = as.getCoopIslands(p);

            if (owner == null) return true;

            for (UUID uuid : list) {
                if (uuid.equals(p.getUniqueId())) {
                    return true;
                }
            }

            for (Location loc : list2) {
                if (as.getIslandAt(location).getOwner().equals(as.getIslandAt(loc).getOwner())) {
                    return true;
                }
            }

            return owner.equals(p.getUniqueId());
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return true;
    }

    @Override
    public boolean isInClaim(String uuid, Location location) {
        return as.getOwner(location).toString().equals(uuid);
    }
}
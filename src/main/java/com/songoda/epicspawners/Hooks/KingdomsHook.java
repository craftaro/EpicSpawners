package com.songoda.epicspawners.Hooks;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.Utils.Debugger;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.kingdoms.constants.land.Land;
import org.kingdoms.constants.land.SimpleChunkLocation;
import org.kingdoms.constants.player.OfflineKingdomPlayer;
import org.kingdoms.manager.game.GameManagement;

/**
 * Created by songoda on 3/17/2017.
 */
public class KingdomsHook extends Hook {

    public KingdomsHook() {
        super("Kingdoms");
        EpicSpawners plugin = EpicSpawners.pl();
        if (isEnabled())
            plugin.hooks.KingdomsHook = this;
    }

    @Override
    public boolean canBuild(Player p, Location location) {
        try {
            if (hasBypass(p)) return true;

            OfflineKingdomPlayer pl = GameManagement.getPlayerManager().getOfflineKingdomPlayer(p);
            if (pl.getKingdomPlayer().getKingdom() == null) return true;

            SimpleChunkLocation chunkLocation = new SimpleChunkLocation(location.getWorld().getName(), location.getChunk().getX(), location.getChunk().getZ());
            Land land = GameManagement.getLandManager().getOrLoadLand(chunkLocation);
            String owner = land.getOwner();

            return pl.getKingdomPlayer().getKingdom().getKingdomName().equals(owner) || owner == null;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return true;
    }

}

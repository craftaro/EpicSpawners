package com.songoda.epicspawners.hooks;

import com.songoda.epicspawners.api.utils.HookType;
import com.songoda.epicspawners.api.utils.ProtectionPluginHook;
import me.markeh.factionsframework.FactionsFramework;
import me.markeh.factionsframework.entities.FPlayer;
import me.markeh.factionsframework.entities.FPlayers;
import me.markeh.factionsframework.entities.Faction;
import me.markeh.factionsframework.entities.Factions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HookFactions implements ProtectionPluginHook {

    private final FactionsFramework factions;

    public HookFactions() {
        this.factions = FactionsFramework.get();
    }

    @Override
    public JavaPlugin getPlugin() {
        return factions;
    }

    @Override
    public HookType getHookType() {
        return HookType.FACTION;
    }

    @Override
    public boolean canBuild(Player player, Location location) {
        FPlayer fPlayer = FPlayers.getBySender(player);
        Faction faction = Factions.getFactionAt(location);

        return faction.isNone() || fPlayer.getFaction().equals(faction);
    }

    @Override
    public boolean isInClaim(Location location) {
        return !Factions.getFactionAt(location).isNone();
    }

    @Override
    public boolean isInClaim(Location location, String id) {
        return Factions.getFactionAt(location).getId().equals(id);
    }

    @Override
    public String getClaimID(String name) {
        Faction faction = Factions.getByName(name, "");
        return (faction != null) ? faction.getId() : null;
    }

}
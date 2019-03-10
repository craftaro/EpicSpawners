package com.songoda.epicspawners.hooks;

import com.github.intellectualsites.plotsquared.api.PlotAPI;
import com.github.intellectualsites.plotsquared.bukkit.BukkitMain;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.songoda.epicspawners.api.utils.HookType;
import com.songoda.epicspawners.api.utils.ProtectionPluginHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HookPlotSquared implements ProtectionPluginHook {

    private final PlotAPI plotSquared;

    public HookPlotSquared() {
        this.plotSquared = new PlotAPI();
    }

    @Override
    public JavaPlugin getPlugin() { // BukkitMain? Really?
        return JavaPlugin.getPlugin(BukkitMain.class);
    }

    @Override
    public HookType getHookType() {
        return HookType.REGULAR;
    }

    @Override
    public boolean canBuild(Player player, Location location) {
        com.github.intellectualsites.plotsquared.plot.object.Location plotLocation =
                new com.github.intellectualsites.plotsquared.plot.object.Location(location.getWorld().getName(),
                        location.getBlockX(), location.getBlockY(), location.getBlockZ());

        Plot plot = plotLocation.getPlot();

        return plot != null
                && plot.getOwners().contains(player.getUniqueId())
                && plot.getMembers().contains(player.getUniqueId());
    }

    @Override
    public boolean isInClaim(Location location) {
        com.github.intellectualsites.plotsquared.plot.object.Location plotLocation =
                new com.github.intellectualsites.plotsquared.plot.object.Location(location.getWorld().getName(),
                        location.getBlockX(), location.getBlockY(), location.getBlockZ());

        Plot plot = plotLocation.getPlot();
        return plot != null;
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
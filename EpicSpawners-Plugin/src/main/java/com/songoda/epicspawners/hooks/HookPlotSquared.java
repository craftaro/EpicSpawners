package com.songoda.epicspawners.hooks;

import com.github.intellectualsites.plotsquared.api.PlotAPI;
import com.github.intellectualsites.plotsquared.bukkit.BukkitMain;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
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
    public boolean canBuild(Player player, Location location) {
        com.github.intellectualsites.plotsquared.plot.object.Location plotLocation =
                new com.github.intellectualsites.plotsquared.plot.object.Location(location.getWorld().getName(),
                        location.getBlockX(), location.getBlockY(), location.getBlockZ());

        PlotArea plotArea = plotSquared.getPlotSquared().getPlotAreaAbs(plotLocation);

        return plotArea != null && plotArea.contains(plotLocation)
                && plotArea.getPlot(plotLocation).getOwners().contains(player.getUniqueId())
                &&  plotArea.getPlot(plotLocation).getMembers().contains(player.getUniqueId());
    }

}
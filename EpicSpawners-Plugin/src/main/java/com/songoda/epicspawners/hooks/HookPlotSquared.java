package com.songoda.epicspawners.hooks;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.plotsquared.bukkit.BukkitMain;
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
        return plotSquared.getPlot(location) != null && plotSquared.isInPlot(player)
            && plotSquared.getPlot(location) == plotSquared.getPlot(player);
    }

}
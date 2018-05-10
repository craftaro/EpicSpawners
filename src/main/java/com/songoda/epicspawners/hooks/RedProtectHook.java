package com.songoda.epicspawners.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.utils.Debugger;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by songoda on 3/17/2017.
 */
public class RedProtectHook extends Hook {

    public RedProtectHook() {
        super("RedProtect");
        EpicSpawners plugin = EpicSpawners.getInstance();
        if (isEnabled())
            plugin.getHookHandler().RedProtectHook = this;
    }

    @Override
    public boolean canBuild(Player p, Location location) {
        try {
            RedProtectAPI rpAPI = RedProtect.get().getAPI();
            return hasBypass(p) || (rpAPI.getRegion(location) != null && rpAPI.getRegion(location).canBuild(p));
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return true;
    }
}
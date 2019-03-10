package com.songoda.epicspawners.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import com.songoda.epicspawners.api.utils.HookType;
import com.songoda.epicspawners.api.utils.ProtectionPluginHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class HookRedProtect implements ProtectionPluginHook {

    private final RedProtect redProtect;

    public HookRedProtect() {
        this.redProtect = RedProtect.get();
    }

    @Override
    public JavaPlugin getPlugin() {
        return redProtect;
    }

    @Override
    public HookType getHookType() {
        return HookType.REGULAR;
    }

    @Override
    public boolean canBuild(Player player, Location location) {
        RedProtectAPI api = redProtect.getAPI();
        Region region = api.getRegion(location);

        return region != null && region.canBuild(player);
    }

    @Override
    public boolean isInClaim(Location location) {
        RedProtectAPI api = redProtect.getAPI();
        Region region = api.getRegion(location);
        return region != null;
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
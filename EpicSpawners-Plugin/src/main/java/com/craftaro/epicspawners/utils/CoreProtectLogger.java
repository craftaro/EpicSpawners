package com.craftaro.epicspawners.utils;

import net.coreprotect.CoreProtectAPI;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class CoreProtectLogger {

    private static CoreProtectAPI coreProtectAPI;

    private static boolean isCoreProtectEnabled() {
        return CoreProtectLogger.coreProtectAPI != null;
    }

    public static void init(CoreProtectAPI coreProtectAPI) {
        CoreProtectLogger.coreProtectAPI = coreProtectAPI;
    }

    public static void logRemoval(String playerName, Block block) {
        if (!isCoreProtectEnabled()) {
            return;
        }
        coreProtectAPI.logRemoval(playerName, block.getLocation(), block.getType(), block.getBlockData());
    }

    public static void logInteraction(String playerName, Location location) {
        if (!isCoreProtectEnabled()) {
            return;
        }
        coreProtectAPI.logInteraction(playerName, location);
    }
}

package com.songoda.epicspawners.listeners;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.References;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by songoda on 3/13/2017.
 */
public class PlayerJoinListeners implements Listener {

    private EpicSpawners plugin;

    public PlayerJoinListeners(EpicSpawners plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
        if (player.isOp() && plugin.getConfig().getBoolean("Main.Display Helpful Tips For Operators")) {
            if (plugin.getServer().getPluginManager().getPlugin("Factions") != null && plugin.getServer().getPluginManager().getPlugin("FactionsFramework") == null) {
                    player.sendMessage("");
                    player.sendMessage(Methods.formatText(References.getPrefix() + "&7Here's the deal,"));
                    player.sendMessage(Methods.formatText("&7I cannot give you full support for Factions out of the box."));
                    player.sendMessage(Methods.formatText("&7Things will work without it but if you wan't a flawless"));
                    player.sendMessage(Methods.formatText("&7experience you need to download"));
                    player.sendMessage(Methods.formatText("&7&6https://www.spigotmc.org/resources/54337/&7."));
                    player.sendMessage(Methods.formatText("&7If you don't care and don't want to see this message again"));
                    player.sendMessage(Methods.formatText("&7turn &6Helpful-Tips &7off in the config."));
                    player.sendMessage("");
                }
        }
    }
}
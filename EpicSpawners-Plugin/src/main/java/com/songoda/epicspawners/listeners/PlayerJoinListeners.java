package com.songoda.epicspawners.listeners;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.utils.Debugger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by songoda on 3/13/2017.
 */
public class PlayerJoinListeners implements Listener {

    private EpicSpawnersPlugin instance;

    public PlayerJoinListeners(EpicSpawnersPlugin instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            Player player = event.getPlayer();
            if (player.isOp() && instance.getConfig().getBoolean("Main.Display Helpful Tips For Operators")) {
                if (instance.getServer().getPluginManager().getPlugin("Factions") != null && instance.getServer().getPluginManager().getPlugin("FactionsFramework") == null) {
                    player.sendMessage("");
                    player.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&7Here's the deal,"));
                    player.sendMessage(TextComponent.formatText("&7I cannot give you full support for Factions out of the box."));
                    player.sendMessage(TextComponent.formatText("&7Things will work without it but if you wan't a flawless"));
                    player.sendMessage(TextComponent.formatText("&7experience you need to download"));
                    player.sendMessage(TextComponent.formatText("&7&6https://www.spigotmc.org/resources/54337/&7."));
                    player.sendMessage(TextComponent.formatText("&7If you don't care and don't want to see this message again"));
                    player.sendMessage(TextComponent.formatText("&7turn &6Helpful-Tips &7off in the config."));
                    player.sendMessage("");
                }
            }
        } catch (Exception ee) {
            Debugger.runReport(ee);
        }
    }
}
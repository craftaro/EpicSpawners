package com.songoda.epicspawners.listeners;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.utils.Debugger;
import org.bukkit.Rotation;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by songoda on 3/13/2017.
 */
public class TestListeners implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerEntityInteract(PlayerInteractAtEntityEvent e) {
        try {
            if (e.getRightClicked() instanceof ItemFrame) {
                if (EpicSpawnersPlugin.getInstance().dataFile.getConfig().getString("data.entityshop") != null) {
                    String uuid = e.getRightClicked().getUniqueId().toString();
                    if (EpicSpawnersPlugin.getInstance().dataFile.getConfig().getString("data.entityshop." + uuid) != null) {
                        ((ItemFrame) e.getRightClicked()).setRotation(Rotation.CLOCKWISE_45);
                        EpicSpawnersPlugin.getInstance().getShop().show(EpicSpawnersPlugin.getInstance().getSpawnerManager().getSpawnerData(EpicSpawnersPlugin.getInstance().dataFile.getConfig().getString("data.entityshop." + uuid).toLowerCase()), 1, e.getPlayer());
                    }
                }
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        try {
            Player p = e.getPlayer();
            if (p.isOp() && EpicSpawnersPlugin.getInstance().getConfig().getBoolean("Main.Display Helpful Tips For Operators")) {
                if (EpicSpawnersPlugin.getInstance().getServer().getPluginManager().getPlugin("Factions") != null && EpicSpawnersPlugin.getInstance().getHookHandler().FactionsHook == null) {
                    p.sendMessage("");
                    p.sendMessage(TextComponent.formatText(EpicSpawnersPlugin.getInstance().references.getPrefix() + "&7Here's the deal,"));
                    p.sendMessage(TextComponent.formatText("&7I cannot give you full support for Factions out of the box."));
                    p.sendMessage(TextComponent.formatText("&7Things will work without it but if you wan't a flawless"));
                    p.sendMessage(TextComponent.formatText("&7experience you need to download"));
                    p.sendMessage(TextComponent.formatText("&7&6https://www.spigotmc.org/resources/54337/&7."));
                    p.sendMessage(TextComponent.formatText("&7If you don't care and don't want to see this message again"));
                    p.sendMessage(TextComponent.formatText("&7turn &6Helpful-Tips &7off in the config."));
                    p.sendMessage("");
                }
            }
        } catch (Exception ee) {
            Debugger.runReport(ee);
        }
    }
}
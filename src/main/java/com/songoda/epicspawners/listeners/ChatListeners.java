package com.songoda.epicspawners.listeners;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Created by songoda on 2/25/2017.
 */
public class ChatListeners implements Listener {

    @EventHandler
    public void chatListeners(AsyncPlayerChatEvent e) {
        try {
            if (!e.isCancelled()) {
                if (EpicSpawners.getInstance().chatEditing.containsKey(e.getPlayer())) {
                    switch (EpicSpawners.getInstance().chatEditing.get(e.getPlayer())) {
                        case "destroy":
                            EpicSpawners.getInstance().editor.destroyFinal(e.getPlayer(), e.getMessage());
                            break;
                        case "name":
                            EpicSpawners.getInstance().editor.saveSpawnerName(e.getPlayer(), e.getMessage());
                            break;
                        case "tick":
                            EpicSpawners.getInstance().editor.saveChatEdit(e.getPlayer(), Integer.parseInt(e.getMessage()));
                            break;
                        case "addEntity":
                            EpicSpawners.getInstance().isEntityInstanceSaved = true;
                            EpicSpawners.getInstance().editor.addEntity(e.getPlayer(), e.getMessage());
                            break;
                        case "Shop-Price":
                            if (Arconix.pl().getApi().doMath().isNumeric(e.getMessage())) {
                                EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(e.getPlayer()))) + ".Shop-Price", Double.parseDouble(e.getMessage()));
                            } else {
                                e.getPlayer().sendMessage(Arconix.pl().getApi().format().formatText("&CYou must enter a number."));
                            }
                            EpicSpawners.getInstance().editor.basicSettings(e.getPlayer());
                            break;
                        case "Custom-ECO-Cost":
                            if (Arconix.pl().getApi().doMath().isNumeric(e.getMessage())) {
                                EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(e.getPlayer()))) + ".Custom-ECO-Cost", Double.parseDouble(e.getMessage()));
                            } else {
                                e.getPlayer().sendMessage(Arconix.pl().getApi().format().formatText("&CYou must enter a number."));
                            }
                            EpicSpawners.getInstance().editor.basicSettings(e.getPlayer());
                            break;
                        case "Custom-XP-Cost":
                            if (Arconix.pl().getApi().doMath().isNumeric(e.getMessage())) {
                                EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(e.getPlayer()))) + ".Custom-XP-Cost", Integer.parseInt(e.getMessage()));
                            } else {
                                e.getPlayer().sendMessage(Arconix.pl().getApi().format().formatText("&CYou must enter a number."));
                            }
                            EpicSpawners.getInstance().editor.basicSettings(e.getPlayer());
                            break;
                        case "Command":
                            String msg = e.getMessage();
                            e.getPlayer().sendMessage(Arconix.pl().getApi().format().formatText(EpicSpawners.getInstance().references.getPrefix() + "&8Command &5" + msg + "&8 saved to your inventory."));
                            EpicSpawners.getInstance().editor.addCommand(e.getPlayer(), e.getMessage());
                            break;
                        case "CustomGoal":
                            if (Arconix.pl().getApi().doMath().isNumeric(e.getMessage())) {
                                EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(e.getPlayer()))) + ".CustomGoal", Integer.parseInt(e.getMessage()));
                            } else {
                                e.getPlayer().sendMessage(Arconix.pl().getApi().format().formatText("&CYou must enter a number."));
                            }
                            EpicSpawners.getInstance().editor.basicSettings(e.getPlayer());
                            break;
                        case "Pickup-cost":
                            if (Arconix.pl().getApi().doMath().isNumeric(e.getMessage())) {
                                EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(e.getPlayer()))) + ".Pickup-cost", Double.parseDouble(e.getMessage()));
                            } else {
                                e.getPlayer().sendMessage(Arconix.pl().getApi().format().formatText("&CYou must enter a number."));
                            }
                            EpicSpawners.getInstance().editor.basicSettings(e.getPlayer());
                            break;
                        case "spawnLimit":
                            if (Arconix.pl().getApi().doMath().isNumeric(e.getMessage())) {
                                EpicSpawners.getInstance().spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(EpicSpawners.getInstance().editor.getType(EpicSpawners.getInstance().editing.get(e.getPlayer()))) + ".commandSpawnLimit", Double.parseDouble(e.getMessage()));
                                EpicSpawners.getInstance().editor.editor(e.getPlayer(), "Command");
                            } else {
                                e.getPlayer().sendMessage(Arconix.pl().getApi().format().formatText("&CYou must enter a number."));
                            }
                            EpicSpawners.getInstance().editor.basicSettings(e.getPlayer());
                            break;
                    }
                    EpicSpawners.getInstance().chatEditing.remove(e.getPlayer());
                    e.setCancelled(true);
                }
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}

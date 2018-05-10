package com.songoda.epicspawners.listeners;

import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.spawners.editor.EditingMenu;
import com.songoda.epicspawners.spawners.object.SpawnerData;
import com.songoda.epicspawners.utils.Debugger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Created by songoda on 2/25/2017.
 */
public class ChatListeners implements Listener {

    private EpicSpawners instance;

    public ChatListeners(EpicSpawners instance) {
        this.instance = instance;
    }

    @EventHandler
    public void chatListeners(AsyncPlayerChatEvent e) {
        try {
            if (!e.isCancelled()) {
                if (instance.chatEditing.containsKey(e.getPlayer())) {

                    switch (instance.chatEditing.get(e.getPlayer())) { // ToDo: This should use an Enum. & possibly prefix these things so we know where they are used.
                        case "destroy":
                            instance.getSpawnerEditor().destroyFinal(e.getPlayer(), e.getMessage());
                            break;
                        case "name":
                            instance.getSpawnerEditor().saveSpawnerName(e.getPlayer(), e.getMessage());
                            break;
                        case "addEntity":
                            instance.getSpawnerEditor().addEntity(e.getPlayer(), e.getMessage());
                        case "Shop-Price":
                            SpawnerData spawnerData = instance.getSpawnerEditor().getType(instance.getSpawnerEditor().getEditingData(e.getPlayer()).getSpawnerSlot());
                            if (Arconix.pl().getApi().doMath().isNumeric(e.getMessage())) {
                                spawnerData.setShopPrice(Double.parseDouble(e.getMessage()));
                            } else {
                                e.getPlayer().sendMessage(Arconix.pl().getApi().format().formatText("&CYou must enter a number."));
                            }
                            instance.getSpawnerEditor().basicSettings(e.getPlayer());
                        case "Tick-Rate":
                            instance.getSpawnerEditor().getType(instance.getSpawnerEditor().getEditingData(e.getPlayer()).getSpawnerSlot()).setTickRate(e.getMessage().trim());
                            instance.getSpawnerEditor().basicSettings(e.getPlayer());
                            break;
                        case "Custom-ECO-Cost":
                            if (Arconix.pl().getApi().doMath().isNumeric(e.getMessage())) {
                                instance.getSpawnerEditor().getType(instance.getSpawnerEditor().getEditingData(e.getPlayer()).getSpawnerSlot()).setUpgradeCostEconomy(Double.parseDouble(e.getMessage()));
                            } else {
                                e.getPlayer().sendMessage(Arconix.pl().getApi().format().formatText("&CYou must enter a number."));
                            }
                            instance.getSpawnerEditor().basicSettings(e.getPlayer());
                        case "Custom-XP-Cost":
                            if (Arconix.pl().getApi().doMath().isNumeric(e.getMessage())) {
                                instance.getSpawnerEditor().getType(instance.getSpawnerEditor().getEditingData(e.getPlayer()).getSpawnerSlot()).setUpgradeCostExperience(Integer.parseInt(e.getMessage()));
                            } else {
                                e.getPlayer().sendMessage(Arconix.pl().getApi().format().formatText("&CYou must enter a number."));
                            }
                            instance.getSpawnerEditor().basicSettings(e.getPlayer());
                        case "Command":
                            String msg = e.getMessage();
                            e.getPlayer().sendMessage(Arconix.pl().getApi().format().formatText(instance.references.getPrefix() + "&8Command &5" + msg + "&8 saved to your inventory."));
                            instance.getSpawnerEditor().addCommand(e.getPlayer(), e.getMessage());
                        case "CustomGoal":
                            if (Arconix.pl().getApi().doMath().isNumeric(e.getMessage())) {
                                instance.getSpawnerEditor().getType(instance.getSpawnerEditor().getEditingData(e.getPlayer()).getSpawnerSlot()).setKillGoal(Integer.parseInt(e.getMessage()));
                            } else {
                                e.getPlayer().sendMessage(Arconix.pl().getApi().format().formatText("&CYou must enter a number."));
                            }
                            instance.getSpawnerEditor().basicSettings(e.getPlayer());
                        case "Pickup-cost":
                            if (Arconix.pl().getApi().doMath().isNumeric(e.getMessage())) {
                                instance.getSpawnerEditor().getType(instance.getSpawnerEditor().getEditingData(e.getPlayer()).getSpawnerSlot()).setPickupCost(Double.parseDouble(e.getMessage()));
                            } else {
                                e.getPlayer().sendMessage(Arconix.pl().getApi().format().formatText("&CYou must enter a number."));
                            }
                            instance.getSpawnerEditor().basicSettings(e.getPlayer());
                        case "spawnLimit":
                            //SpawnerData spawnerData = instance.getSpawnerEditor().getType(instance.getSpawnerEditor().getEditingData(e.getPlayer()).getSpawnerSlot());
                            if (Arconix.pl().getApi().doMath().isNumeric(e.getMessage())) { //ToDo: Make this work.
                                //instance.spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(instance.getSpawnerEditor().getType(instance.editing.get(e.getPlayer()))) + ".commandSpawnLimit", Double.parseDouble(e.getMessage()));
                                instance.getSpawnerEditor().editor(e.getPlayer(), EditingMenu.COMMAND);
                            } else {
                                e.getPlayer().sendMessage(Arconix.pl().getApi().format().formatText("&CYou must enter a number."));
                            }
                            instance.getSpawnerEditor().basicSettings(e.getPlayer());
                    }
                    instance.chatEditing.remove(e.getPlayer());
                    e.setCancelled(true);
                }
            }
        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }
}
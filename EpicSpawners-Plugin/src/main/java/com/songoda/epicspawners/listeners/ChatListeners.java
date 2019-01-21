package com.songoda.epicspawners.listeners;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.References;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.editor.EditingMenu;
import com.songoda.epicspawners.utils.Debugger;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by songoda on 2/25/2017.
 */
public class ChatListeners implements Listener {

    private HashMap<UUID, EditingType> inEditor = new HashMap<>();
    private EpicSpawnersPlugin instance;

    public ChatListeners(EpicSpawnersPlugin instance) {
        this.instance = instance;
    }

    @EventHandler(ignoreCancelled = true)
    public void chatListeners(AsyncPlayerChatEvent e) {
        try {
            if (!inEditor.containsKey(e.getPlayer().getUniqueId())) return;

            Player player = e.getPlayer();

            switch (inEditor.get(player.getUniqueId())) {
                case DESTROY:
                    instance.getSpawnerEditor().destroyFinal(player, e.getMessage());
                    break;
                case NAME:
                    instance.getSpawnerEditor().saveSpawnerName(player, e.getMessage());
                    break;
                case ADD_ENTITY:
                    try {
                        EntityType.valueOf(e.getMessage().toUpperCase());
                        instance.getSpawnerEditor().addEntity(player, e.getMessage().toUpperCase());
                    } catch (Exception ex) {
                        player.sendMessage("That is not a correct EntityType. Please try again..");
                    }
                    break;
                case SHOP_PRICE:
                    SpawnerData spawnerData = instance.getSpawnerEditor().getEditingData(player).getSpawnerEditing();
                    if (Methods.isInt(e.getMessage())) {
                        spawnerData.setShopPrice(Double.parseDouble(e.getMessage()));
                    } else {
                        player.sendMessage(Methods.formatText("&CYou must enter a number."));
                    }
                    instance.getSpawnerEditor().basicSettings(player);
                    break;
                case TICK_RATE:
                    instance.getSpawnerEditor().getEditingData(player).getSpawnerEditing().setTickRate(e.getMessage().trim());
                    instance.getSpawnerEditor().basicSettings(player);
                    break;
                case CUSTOM_ECO_COST:
                    if (Methods.isInt(e.getMessage())) {
                        instance.getSpawnerEditor().getEditingData(player).getSpawnerEditing().setUpgradeCostEconomy(Double.parseDouble(e.getMessage()));
                    } else {
                        player.sendMessage(Methods.formatText("&CYou must enter a number."));
                    }
                    instance.getSpawnerEditor().basicSettings(player);
                    break;
                case CUSTOM_XP_COST:
                    if (Methods.isInt(e.getMessage())) {
                        instance.getSpawnerEditor().getEditingData(player).getSpawnerEditing().setUpgradeCostExperience(Integer.parseInt(e.getMessage()));
                    } else {
                        player.sendMessage(Methods.formatText("&CYou must enter a number."));
                    }
                    instance.getSpawnerEditor().basicSettings(player);
                    break;
                case COMMAND:
                    String msg = e.getMessage();
                    player.sendMessage(Methods.formatText(References.getPrefix() + "&8Command &5" + msg + "&8 saved to your inventory."));
                    instance.getSpawnerEditor().addCommand(player, e.getMessage());
                    break;
                case CUSTOM_GOAL:
                    if (Methods.isInt(e.getMessage())) {
                        instance.getSpawnerEditor().getEditingData(player).getSpawnerEditing().setKillGoal(Integer.parseInt(e.getMessage()));
                    } else {
                        player.sendMessage(Methods.formatText("&CYou must enter a number."));
                    }
                    instance.getSpawnerEditor().basicSettings(player);
                    break;
                case PICKUP_COST:
                    if (Methods.isInt(e.getMessage())) {
                        instance.getSpawnerEditor().getEditingData(player).getSpawnerEditing().setPickupCost(Double.parseDouble(e.getMessage()));
                    } else {
                        player.sendMessage(Methods.formatText("&CYou must enter a number."));
                    }
                    instance.getSpawnerEditor().basicSettings(player);
                    break;
                case SPAWN_LIMIT:
                    //SpawnerData spawnerData = instance.getSpawnerEditor().getType(instance.getSpawnerEditor().getEditingData(player).getSpawnerSlot());
                    if (Methods.isInt(e.getMessage())) { //ToDo: Make this work.
                        //instance.spawnerFile.getConfig().set("Entities." + Methods.getTypeFromString(instance.getSpawnerEditor().getType(instance.editing.get(player))) + ".commandSpawnLimit", Double.parseDouble(e.getMessage()));
                        instance.getSpawnerEditor().editor(player, EditingMenu.COMMAND);
                    } else {
                        player.sendMessage(Methods.formatText("&CYou must enter a number."));
                    }
                    instance.getSpawnerEditor().basicSettings(player);
                    break;
            }
            inEditor.remove(player.getUniqueId());
            e.setCancelled(true);


        } catch (Exception ex) {
            Debugger.runReport(ex);
        }
    }

    public void addToEditor(Player player, EditingType type) {
        inEditor.put(player.getUniqueId(), type);
    }

    public enum EditingType {DESTROY, NAME, ADD_ENTITY, SHOP_PRICE, TICK_RATE, CUSTOM_ECO_COST, CUSTOM_XP_COST, COMMAND, CUSTOM_GOAL, PICKUP_COST, SPAWN_LIMIT}
}
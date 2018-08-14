package com.songoda.epicspawners.command.commands;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.command.AbstractCommand;
import org.bukkit.command.CommandSender;

public class CommandEpicSpawners extends AbstractCommand {

    public CommandEpicSpawners() {
        super("EpicSpawners", null);
    }

    @Override
    protected boolean runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args) {
        sender.sendMessage("");
        sender.sendMessage(TextComponent.formatText("&f>>&m------------&6&l EpicSpawners Help &f&m------------&f<<"));
        sender.sendMessage(TextComponent.formatText("                   &7" + instance.getDescription().getVersion() + " Created by &5&l&oBrianna"));

        sender.sendMessage(TextComponent.formatText("&6/EpicSpawners&7 - Displays this page."));
        sender.sendMessage(TextComponent.formatText("&6/SpawnerShop&7 - Opens the spawner shop."));
        sender.sendMessage(TextComponent.formatText("&6/spawnerstats&7 - Allows a player to overview their current EpicSpawners stats and see how many kills they have left to get a specific spawner drop."));
        if (sender.hasPermission("epicspawners.admin")) {
            sender.sendMessage(TextComponent.formatText("&6/es editor&7 - Opens the spawner editor."));
            sender.sendMessage(TextComponent.formatText("&6/es change <Type>&7 - Changes the entity for the spawner you are looking at."));
            sender.sendMessage(TextComponent.formatText("&6/es give [player/all] [spawnertype/random] [multiplier] [amount]&7 - Gives an operator the ability to spawn a spawner of his or her choice."));
            sender.sendMessage(TextComponent.formatText("&6/es settings&7 - Edit the EpicSpawners Settings."));
            sender.sendMessage(TextComponent.formatText("&6/es boost <p:player, f:faction, t:town, i:islandOwner> <amount> [m:minute, h:hour, d:day, y:year]&7 - This allows you to boost the amount of spawns that are got from placed spawners."));
        }
        sender.sendMessage("");

        return false;
    }
}

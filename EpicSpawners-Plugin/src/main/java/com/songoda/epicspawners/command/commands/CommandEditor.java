package com.songoda.epicspawners.command.commands;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandEditor extends AbstractCommand {

    public CommandEditor(AbstractCommand abstractCommand) {
        super("editor", "epicspawners.admin", abstractCommand);
    }

    @Override
    protected boolean runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command.");
            return true;
        }

        instance.getSpawnerEditor().openSpawnerSelector((Player) sender, 1);
        return true;
    }
}

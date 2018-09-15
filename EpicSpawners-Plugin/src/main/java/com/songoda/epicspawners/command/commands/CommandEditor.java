package com.songoda.epicspawners.command.commands;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandEditor extends AbstractCommand {

    public CommandEditor(AbstractCommand abstractCommand) {
        super("editor", abstractCommand, true);
    }

    @Override
    protected ReturnType runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args) {
        instance.getSpawnerEditor().openSpawnerSelector((Player) sender, 1);
        return ReturnType.SUCCESS;
    }

    @Override
    public String getPermissionNode() {
        return "epicspawners.admin";
    }

    @Override
    public String getSyntax() {
        return "/es editor";
    }

    @Override
    public String getDescription() {
        return "Opens the spawner editor.";
    }
}

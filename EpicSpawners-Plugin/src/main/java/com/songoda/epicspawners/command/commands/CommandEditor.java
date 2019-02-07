package com.songoda.epicspawners.command.commands;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandEditor extends AbstractCommand {

    public CommandEditor(AbstractCommand abstractCommand) {
        super(abstractCommand, true,"editor");
    }

    @Override
    protected ReturnType runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args) {
        instance.getSpawnerEditor().openSpawnerSelector((Player) sender, 1);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(EpicSpawnersPlugin instance, CommandSender sender, String... args) {
        return null;
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

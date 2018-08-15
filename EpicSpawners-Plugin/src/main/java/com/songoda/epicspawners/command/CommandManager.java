package com.songoda.epicspawners.command;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.command.commands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements CommandExecutor {

    private EpicSpawnersPlugin instance;

    private List<AbstractCommand> commands = new ArrayList<>();

    public CommandManager(EpicSpawnersPlugin instance) {
        this.instance = instance;

        AbstractCommand commandEpicSpawners = addCommand(new CommandEpicSpawners());

        addCommand(new CommandGive(commandEpicSpawners));
        addCommand(new CommandReload(commandEpicSpawners));
        addCommand(new CommandEditor(commandEpicSpawners));
        addCommand(new CommandGive(commandEpicSpawners));
        addCommand(new CommandChange(commandEpicSpawners));
        addCommand(new CommandSettings(commandEpicSpawners));
        addCommand(new CommandSpawnerShop());
        addCommand(new CommandSpawnerStats());
    }

    private AbstractCommand addCommand(AbstractCommand abstractCommand) {
        commands.add(abstractCommand);
        return abstractCommand;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        for (AbstractCommand abstractCommand : commands) {
            if (abstractCommand.getCommand().equalsIgnoreCase(command.getName())) {
                if (strings.length == 0) {
                    processPerms(abstractCommand, commandSender, strings);
                    return true;
                }
            } else if (strings.length != 0 && abstractCommand.getParent() != null && abstractCommand.getParent().getCommand().equalsIgnoreCase(command.getName())) {
                String cmd = strings[0];
                if (cmd.equalsIgnoreCase(abstractCommand.getCommand())) {
                    processPerms(abstractCommand, commandSender, strings);
                    return true;
                }
            }
        }
        commandSender.sendMessage(instance.references.getPrefix() + TextComponent.formatText("&7The command you entered does not exist or is spelt incorrectly."));
        return true;
    }

    private void processPerms(AbstractCommand command, CommandSender sender, String[] strings) {
        if (command.getPermissionNode() == null || sender.hasPermission(command.getPermissionNode())) {
            command.runCommand(instance, sender, strings);
            return;
        }
        sender.sendMessage(instance.references.getPrefix() + instance.getLocale().getMessage("event.general.nopermission"));

    }
}

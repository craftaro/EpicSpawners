package com.songoda.epicspawners.command.commands;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.References;
import com.songoda.epicspawners.command.AbstractCommand;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandReload extends AbstractCommand {

    public CommandReload(AbstractCommand parent) {
        super(parent, false, "reload");
    }

    @Override
    protected ReturnType runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args) {
        instance.reload();
        sender.sendMessage(Methods.formatText(References.getPrefix() + "&7Configuration and Language files reloaded."));
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
        return "/es reload";
    }

    @Override
    public String getDescription() {
        return "Reload the Configuration and Language files.";
    }
}

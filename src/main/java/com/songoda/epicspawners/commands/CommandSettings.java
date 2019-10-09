package com.songoda.epicspawners.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.configuration.editor.ConfigEditorGui;
import com.songoda.epicspawners.EpicSpawners;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandSettings extends AbstractCommand {

    private final EpicSpawners plugin;

    public CommandSettings(EpicSpawners plugin) {
        super(true, "Settings");
        this.plugin = plugin;
    }

    @Override
    protected AbstractCommand.ReturnType runCommand(CommandSender sender, String... args) {
        plugin.getGuiManager().showGUI((Player) sender, new ConfigEditorGui(plugin, null, "UltimateClaims Settings Manager", plugin.getCoreConfig()));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicspawners.admin";
    }

    @Override
    public String getSyntax() {
        return "/es settings";
    }

    @Override
    public String getDescription() {
        return "Edit the EpicSpawners Settings.";
    }
}

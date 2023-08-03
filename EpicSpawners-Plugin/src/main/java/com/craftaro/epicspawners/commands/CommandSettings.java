package com.craftaro.epicspawners.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.core.configuration.editor.PluginConfigGui;
import com.craftaro.epicspawners.EpicSpawners;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CommandSettings extends AbstractCommand {
    private final EpicSpawners plugin;

    public CommandSettings(EpicSpawners plugin) {
        super(CommandType.PLAYER_ONLY, "settings");
        this.plugin = plugin;
    }

    @Override
    protected AbstractCommand.ReturnType runCommand(CommandSender sender, String... args) {
        plugin.getGuiManager().showGUI((Player) sender, new PluginConfigGui(plugin));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return Collections.emptyList();
    }

    @Override
    public String getPermissionNode() {
        return "epicspawners.admin";
    }

    @Override
    public String getSyntax() {
        return "settings";
    }

    @Override
    public String getDescription() {
        return "Edit the EpicSpawners Settings.";
    }
}

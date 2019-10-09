package com.songoda.epicspawners.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.gui.GUIEditorSelector;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandEditor extends AbstractCommand {

    private final EpicSpawners plugin;

    public CommandEditor(EpicSpawners plugin) {
        super(true, "editor");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        new GUIEditorSelector(plugin, (Player) sender);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicspawners.admin.editor";
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

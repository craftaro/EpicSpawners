package com.songoda.epicspawners.command.commands;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.command.AbstractCommand;
import com.songoda.epicspawners.gui.GUIEditorSelector;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandEditor extends AbstractCommand {

    public CommandEditor(AbstractCommand abstractCommand) {
        super(abstractCommand, true,"editor");
    }

    @Override
    protected ReturnType runCommand(EpicSpawners instance, CommandSender sender, String... args) {
        new GUIEditorSelector(instance, (Player)sender);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(EpicSpawners instance, CommandSender sender, String... args) {
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

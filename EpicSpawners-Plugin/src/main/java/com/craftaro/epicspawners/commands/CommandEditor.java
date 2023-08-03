package com.craftaro.epicspawners.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.gui.EditorSelectorGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CommandEditor extends AbstractCommand {
    private final EpicSpawners plugin;

    public CommandEditor(EpicSpawners plugin) {
        super(CommandType.PLAYER_ONLY, "editor");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        plugin.getGuiManager().showGUI((Player) sender, new EditorSelectorGui(plugin, (Player) sender));
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return Collections.emptyList();
    }

    @Override
    public String getPermissionNode() {
        return "epicspawners.admin.editor";
    }

    @Override
    public String getSyntax() {
        return "editor";
    }

    @Override
    public String getDescription() {
        return "Opens the spawner editor.";
    }
}

package com.songoda.epicspawners.tiers;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.tiers.gui.GUIEditorTierSelector;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CommandTierEditor extends AbstractCommand
{
    private final EpicSpawners plugin;

    public CommandTierEditor(EpicSpawners plugin) {
        super(true, "tierseditor");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        new GUIEditorTierSelector(plugin, (Player) sender);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return Collections.emptyList();
    }

    @Override
    public String getPermissionNode() {
        return "epicspawners.admin.tierseditor";
    }

    @Override
    public String getSyntax() {
        return "tierseditor";
    }

    @Override
    public String getDescription() {
        return "Opens the tiers editor.";
    }
}

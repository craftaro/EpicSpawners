package com.craftaro.epicspawners.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.epicspawners.EpicSpawners;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CommandReload extends AbstractCommand {
    private final EpicSpawners plugin;

    public CommandReload(EpicSpawners plugin) {
        super(CommandType.CONSOLE_OK, "reload");
        this.plugin = plugin;
    }

    @Override
    protected AbstractCommand.ReturnType runCommand(CommandSender sender, String... args) {
        plugin.reloadConfig();
        plugin.getLocale().getMessage("&7Configuration and Language files reloaded.").sendPrefixedMessage(sender);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return Collections.emptyList();
    }

    @Override
    public String getPermissionNode() {
        return "epicspawners.admin.reload";
    }

    @Override
    public String getSyntax() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reload the Configuration and Language files.";
    }
}

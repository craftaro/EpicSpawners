package com.songoda.epicspawners.command.commands;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSettings extends AbstractCommand {

    public CommandSettings(AbstractCommand parent) {
        super("settings", "epicspawners.admin", parent, true);
    }

    @Override
    protected boolean runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args) {
        instance.getSettingsManager().openSettingsManager((Player)sender);
        return false;
    }
}

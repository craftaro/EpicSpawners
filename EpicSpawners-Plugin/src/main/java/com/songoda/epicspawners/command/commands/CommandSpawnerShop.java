package com.songoda.epicspawners.command.commands;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSpawnerShop extends AbstractCommand {

    public CommandSpawnerShop() {
        super("spawnershop", "epicspawners.openshop", null, true);
    }

    @Override
    protected boolean runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args) {
        instance.getShop().open((Player) sender, 1);
        return false;
    }
}

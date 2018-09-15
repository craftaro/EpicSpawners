package com.songoda.epicspawners.command.commands;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.command.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSpawnerShop extends AbstractCommand {

    public CommandSpawnerShop() {
        super("spawnershop", null, true);
    }

    @Override
    protected ReturnType runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args) {
        instance.getShop().open((Player) sender, 1);
        return ReturnType.SUCCESS;
    }

    @Override
    public String getPermissionNode() {
        return "epicspawners.openshop";
    }

    @Override
    public String getSyntax() {
        return "/SpawnerShop";
    }

    @Override
    public String getDescription() {
        return "Opens the spawner shop.";
    }
}

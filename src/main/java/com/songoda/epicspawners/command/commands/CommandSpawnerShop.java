package com.songoda.epicspawners.command.commands;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.command.AbstractCommand;
import com.songoda.epicspawners.gui.GUISpawnerShop;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandSpawnerShop extends AbstractCommand {

    public CommandSpawnerShop() {
        super(null, true, "spawnershop");
    }

    @Override
    protected ReturnType runCommand(EpicSpawnersPlugin plugin, CommandSender sender, String... args) {
        new GUISpawnerShop(plugin, (Player) sender);
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(EpicSpawnersPlugin instance, CommandSender sender, String... args) {
        return null;
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

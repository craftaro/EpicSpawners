package com.songoda.epicspawners.command.commands;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.command.AbstractCommand;
import com.songoda.epicspawners.gui.GUISpawnerStats;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandSpawnerStats extends AbstractCommand {

    public CommandSpawnerStats() {
        super(null, true, "spawnerstats");
    }

    @Override
    protected ReturnType runCommand(EpicSpawners instance, CommandSender sender, String... args) {
        Player player = (Player) sender;

        if (instance.getPlayerActionManager().getPlayerAction(player).getEntityKills().size() == 0) {
            instance.getLocale().getMessage("interface.spawnerstats.nokills").sendPrefixedMessage(player);
            return AbstractCommand.ReturnType.SUCCESS;
        }


        new GUISpawnerStats(instance, player);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(EpicSpawners instance, CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicspawners.stats";
    }

    @Override
    public String getSyntax() {
        return "/SpawnerStats";
    }

    @Override
    public String getDescription() {
        return "This allows you to boost the amount of spawns that are got from placed spawners.";
    }
}

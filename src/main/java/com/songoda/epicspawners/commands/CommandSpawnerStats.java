package com.songoda.epicspawners.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.gui.GUISpawnerStats;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CommandSpawnerStats extends AbstractCommand {

    private final EpicSpawners plugin;

    public CommandSpawnerStats(EpicSpawners plugin) {
        super(true, "spawnerstats");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = (Player) sender;

        if (plugin.getPlayerDataManager().getPlayerData(player).getEntityKills().size() == 0) {
            plugin.getLocale().getMessage("interface.spawnerstats.nokills").sendPrefixedMessage(player);
            return AbstractCommand.ReturnType.SUCCESS;
        }


        new GUISpawnerStats(plugin, player);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return Collections.emptyList();
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

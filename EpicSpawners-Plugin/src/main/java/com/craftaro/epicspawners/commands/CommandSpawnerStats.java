package com.craftaro.epicspawners.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.gui.SpawnerStatsGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CommandSpawnerStats extends AbstractCommand {
    private final EpicSpawners plugin;

    public CommandSpawnerStats(EpicSpawners plugin) {
        super(CommandType.PLAYER_ONLY, "spawnerstats");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        Player player = (Player) sender;

        if (this.plugin.getPlayerDataManager().getPlayerData(player).getEntityKills().isEmpty()) {
            this.plugin.getLocale().getMessage("interface.spawnerstats.nokills").sendPrefixedMessage(player);
            return AbstractCommand.ReturnType.SUCCESS;
        }

        this.plugin.getGuiManager().showGUI(player, new SpawnerStatsGui(this.plugin, player));

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

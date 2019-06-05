package com.songoda.epicspawners.command.commands;

import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.References;
import com.songoda.epicspawners.boost.BoostData;
import com.songoda.epicspawners.boost.BoostType;
import com.songoda.epicspawners.command.AbstractCommand;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandBoost extends AbstractCommand {

    public CommandBoost(AbstractCommand parent) {
        super(parent, false, "boost");
    }

    @Override
    protected ReturnType runCommand(EpicSpawners instance, CommandSender sender, String... args) {
        if (args.length < 3) {
            sender.sendMessage(References.getPrefix() + Methods.formatText("&7Syntax error..."));
            return ReturnType.SYNTAX_ERROR;
        }
        if (!Methods.isInt(args[2])) {
            sender.sendMessage(Methods.formatText(References.getPrefix() + "&6" + args[2] + " &7is not a number..."));
            return ReturnType.SYNTAX_ERROR;
        }

        long duration = 0L;

        if (args.length > 3) {
            for (int i = 1; i < args.length; i++) {
                String line = args[i];
                long time = Methods.parseTime(line);
                duration += time;

            }
        }

        Player player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            sender.sendMessage(Methods.formatText(References.getPrefix() + "&cThat player does not exist or is not online..."));
            return ReturnType.FAILURE;
        }

        BoostData boostData = new BoostData(BoostType.PLAYER, Integer.parseInt(args[2]), duration == 0L ? Long.MAX_VALUE : System.currentTimeMillis() + duration, player.getUniqueId());
        instance.getBoostManager().addBoostToSpawner(boostData);
        sender.sendMessage(Methods.formatText(References.getPrefix() + player.getName() + "&7 has been given a spawner boost of &6" + args[2] + "&7 for " + Methods.makeReadable(duration) + "&7."));

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(EpicSpawners instance, CommandSender sender, String... args) {
        if (args.length == 2) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return players;
        } else if (args.length == 3) {
            return Arrays.asList("1", "2", "3", "4", "5");
        } else if (args.length == 4) {
            return Arrays.asList("1m", "2h", "3d", "4d");
        }
        return null;
    }
    @Override
    public String getPermissionNode() {
        return "epicspawners.admin.boost";
    }

    @Override
    public String getSyntax() {
        return "/es boost <player> <amount> [duration]";
    }

    @Override
    public String getDescription() {
        return "This allows you to boost the amount of spawns that are got from placed spawners.";
    }
}

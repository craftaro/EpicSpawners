package com.songoda.epicspawners.command.commands;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.api.methods.math.AMath;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.boost.BoostData;
import com.songoda.epicspawners.boost.BoostType;
import com.songoda.epicspawners.command.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Calendar;
import java.util.Date;

public class CommandBoost extends AbstractCommand {

    public CommandBoost(AbstractCommand parent) {
        super("boost", "epicspawners.admin", parent, false);
    }

    @Override
    protected boolean runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args) {
        if (args.length < 3) {
            sender.sendMessage(instance.getReferences().getPrefix() + TextComponent.formatText("&7Syntax error..."));
            return false;
        }
        if (!args[1].contains("p:") && !args[1].contains("player:") &&
                !args[1].contains("f:") && !args[1].contains("faction:") &&
                !args[1].contains("t:") && !args[1].contains("town:") &&
                !args[1].contains("i:") && !args[1].contains("island:")) {
            sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&6" + args[1] + " &7this is incorrect"));
            return false;
        }
        String[] arr = (args[1]).split(":");
        if (!AMath.isInt(args[2])) {
            sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&6" + args[2] + " &7is not a number..."));
            return false;
        }

        Calendar c = Calendar.getInstance();
        Date currentDate = new Date();
        c.setTime(currentDate);

        String response = " &6" + arr[1] + "&7 has been given a spawner boost of &6" + args[2];

        if (args.length > 3) {
            if (args[3].contains("m:")) {
                String[] arr2 = (args[3]).split(":");
                c.add(Calendar.MINUTE, Integer.parseInt(arr2[1]));
                response += " &7for &6" + arr2[1] + " minutes&7.";
            } else if (args[3].contains("h:")) {
                String[] arr2 = (args[3]).split(":");
                c.add(Calendar.HOUR, Integer.parseInt(arr2[1]));
                response += " &7for &6" + arr2[1] + " hours&7.";
            } else if (args[3].contains("d:")) {
                String[] arr2 = (args[3]).split(":");
                c.add(Calendar.HOUR, Integer.parseInt(arr2[1]) * 24);
                response += " &7for &6" + arr2[1] + " days&7.";
            } else if (args[3].contains("y:")) {
                String[] arr2 = (args[3]).split(":");
                c.add(Calendar.YEAR, Integer.parseInt(arr2[1]));
                response += " &7for &6" + arr2[1] + " years&7.";
            } else {
                sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&7" + args[3] + " &7is invalid."));
                return false;
            }
        } else {
            c.add(Calendar.YEAR, 10);
            response += "&6.";
        }

        String start = "&7";

        BoostType boostType = null;

        Object boostObject = null;

        if (arr[0].equalsIgnoreCase("p") || arr[0].equalsIgnoreCase("player")) {
            if (Bukkit.getOfflinePlayer(arr[1]) == null) {
                sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&cThat player does not exist..."));
            } else {
                start += "The player";
                boostType = BoostType.PLAYER;
                boostObject = Bukkit.getOfflinePlayer(arr[1]).getUniqueId().toString();
            }
        } else if (arr[0].equalsIgnoreCase("f") || arr[0].equalsIgnoreCase("faction")) {
            if (instance.getFactionId(arr[1]) == null) {
                sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&cThat faction does not exist..."));
                return false;
            }

            start += "The faction";
            boostType = BoostType.FACTION;
            boostObject = instance.getFactionId(arr[1]);
        } else if (arr[0].equalsIgnoreCase("t") || arr[0].equalsIgnoreCase("town")) {
            if (instance.getTownId(arr[1]) == null) {
                sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&cThat town does not exist..."));
                return false;
            }

            start += "The town";
            boostType = BoostType.TOWN;
            boostObject = instance.getTownId(arr[1]);
        } else if (arr[0].equalsIgnoreCase("i") || arr[0].equalsIgnoreCase("island")) {
            if (instance.getIslandId(arr[1]) == null) {
                sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&cThat island does not exist..."));
                return false;
            }

            start += "The island";
            boostType = BoostType.ISLAND;
            boostObject = instance.getIslandId(arr[1]);
        }

        if (boostType == null || boostObject == null) {
            sender.sendMessage("Syntax error.");
            return false;
        }

        BoostData boostData = new BoostData(boostType, Integer.parseInt(args[2]), c.getTime().getTime(), boostObject);
        instance.getBoostManager().addBoostToSpawner(boostData);
        sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + start + response));

        return true;
    }
}

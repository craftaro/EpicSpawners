package com.songoda.epicspawners.command.commands;

import com.google.common.collect.Iterables;
import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.api.methods.math.AMath;
import com.songoda.epicspawners.EpicSpawnersPlugin;
import com.songoda.epicspawners.api.spawner.SpawnerData;
import com.songoda.epicspawners.command.AbstractCommand;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Random;

public class CommandGive extends AbstractCommand {

    public CommandGive(AbstractCommand abstractCommand) {
        super("give", "epicspawners.admin", abstractCommand, false);
    }

    @Override
    protected boolean runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args) {
        if (args.length <= 3 && args.length != 6) {
            sender.sendMessage(instance.getReferences().getPrefix() + TextComponent.formatText("&7Syntax error..."));
            return true;
        }
        if (Bukkit.getPlayerExact(args[1]) == null && !args[1].toLowerCase().equals("all")) {
            sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&cThat username does not exist, or the user is not online!"));
            return true;
        }
        int multi = 1;

        SpawnerData data = null;
        for (SpawnerData spawnerData : instance.getSpawnerManager().getAllSpawnerData()) {
            String input = args[2].toUpperCase().replace("_", "").replace(" ", "");
            String compare = spawnerData.getIdentifyingName().toUpperCase().replace("_", "").replace(" ", "");
            if (input.equals(compare))
                data = spawnerData;
        }

        if (data == null && !args[2].equalsIgnoreCase("random")) {
            sender.sendMessage(instance.getReferences().getPrefix() + TextComponent.formatText(instance.getReferences().getPrefix() + "&7The entity Type &6" + args[2] + " &7does not exist. Try one of these:"));
            StringBuilder list = new StringBuilder();

            for (SpawnerData spawnerData : instance.getSpawnerManager().getAllSpawnerData()) {
                list.append(spawnerData.getIdentifyingName().toUpperCase().replace(" ", "_")).append("&7, &6");
            }
            sender.sendMessage(TextComponent.formatText("&6" + list));
        } else {
            if (args[2].equalsIgnoreCase("random")) {
                Collection<SpawnerData> list = instance.getSpawnerManager().getAllSpawnerData();
                Random rand = new Random();
                data = Iterables.get(list, rand.nextInt(list.size()));
            }
            if (args.length == 4) {
                if (!AMath.isInt(args[3])) {
                    sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&6" + args[3] + "&7 is not a number."));
                    return true;
                }
                int amt = Integer.parseInt(args[3]);
                ItemStack spawnerItem = data.toItemStack(Integer.parseInt(args[3]));
                if (args[1].toLowerCase().equals("all")) {
                    for (Player pl : Bukkit.getOnlinePlayers()) {
                        pl.getInventory().addItem(spawnerItem);
                        pl.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.give.success", amt, Methods.compileName(data, multi, false))));
                    }
                } else {
                    Player pl = Bukkit.getPlayerExact(args[1]);
                    pl.getInventory().addItem(spawnerItem);
                    pl.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.give.success", amt, Methods.compileName(data, multi, false))));

                }
            } else {
                if (!AMath.isInt(args[3])) {
                    sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&6" + args[3] + "&7 is not a number."));
                    return true;
                }
                if (!AMath.isInt(args[4])) {
                    sender.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + "&6" + args[4] + "&7 is not a number."));
                    return true;
                }
                int amt = Integer.parseInt(args[3]);
                multi = Integer.parseInt(args[4]);
                ItemStack spawnerItem = data.toItemStack(amt, multi);
                if (args[1].toLowerCase().equals("all")) {
                    for (Player pl : Bukkit.getOnlinePlayers()) {
                        pl.getInventory().addItem(spawnerItem);
                        pl.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.give.success", amt, Methods.compileName(data, multi, false))));
                    }
                } else {
                    Player pl = Bukkit.getPlayerExact(args[1]);
                    pl.getInventory().addItem(spawnerItem);
                    pl.sendMessage(TextComponent.formatText(instance.getReferences().getPrefix() + instance.getLocale().getMessage("command.give.success", amt, Methods.compileName(data, multi, false))));

                }
            }
        }
        return true;
    }
}

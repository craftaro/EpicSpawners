package com.songoda.epicspawners.command.commands;

import com.google.common.collect.Iterables;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.References;
import com.songoda.epicspawners.command.AbstractCommand;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.utils.Methods;
import com.songoda.epicspawners.utils.settings.Setting;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CommandGive extends AbstractCommand {

    public CommandGive(AbstractCommand abstractCommand) {
        super(abstractCommand, false, "give");
    }

    @Override
    protected ReturnType runCommand(EpicSpawners instance, CommandSender sender, String... args) {
        if (args.length <= 3 && args.length != 6) {
            return ReturnType.SYNTAX_ERROR;
        }
        if (Bukkit.getPlayerExact(args[1]) == null && !args[1].toLowerCase().equals("all")) {
            sender.sendMessage(Methods.formatText(References.getPrefix() + "&cThat username does not exist, or the user is not online!"));
            return ReturnType.FAILURE;
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
            sender.sendMessage(Methods.formatText(References.getPrefix() + "&7The entity Type &6" + args[2] + " &7does not exist. Try one of these:"));
            StringBuilder list = new StringBuilder();

            for (SpawnerData spawnerData : instance.getSpawnerManager().getAllSpawnerData()) {
                list.append(spawnerData.getIdentifyingName().toUpperCase().replace(" ", "_")).append("&7, &6");
            }
            sender.sendMessage(Methods.formatText("&6" + list));
            return ReturnType.FAILURE;
        }
        if (args[2].equalsIgnoreCase("random")) {
            Collection<SpawnerData> list = instance.getSpawnerManager().getAllEnabledSpawnerData();
            Random rand = new Random();
            data = Iterables.get(list, rand.nextInt(list.size()));
        }
        if (args.length == 4) {
            if (!Methods.isInt(args[3])) {
                sender.sendMessage(Methods.formatText(References.getPrefix() + "&6" + args[3] + "&7 is not a number."));
                return ReturnType.SYNTAX_ERROR;
            }
            int amt = Integer.parseInt(args[3]);
            ItemStack spawnerItem = data.toItemStack(Integer.parseInt(args[3]));
            if (args[1].toLowerCase().equals("all")) {
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    pl.getInventory().addItem(spawnerItem);
                    pl.sendMessage(Methods.formatText(References.getPrefix() + instance.getLocale().getMessage("command.give.success", amt, Methods.compileName(data, multi, false))));
                }
            } else {
                Player pl = Bukkit.getPlayerExact(args[1]);
                pl.getInventory().addItem(spawnerItem);
                pl.sendMessage(Methods.formatText(References.getPrefix() + instance.getLocale().getMessage("command.give.success", amt, Methods.compileName(data, multi, false))));

            }
            return ReturnType.FAILURE;
        }
        if (!Methods.isInt(args[3])) {
            sender.sendMessage(Methods.formatText(References.getPrefix() + "&6" + args[3] + "&7 is not a number."));
            return ReturnType.FAILURE;
        }
        if (!Methods.isInt(args[4])) {
            sender.sendMessage(Methods.formatText(References.getPrefix() + "&6" + args[4] + "&7 is not a number."));
            return ReturnType.FAILURE;
        }
        int amt = Integer.parseInt(args[3]);

        multi = Integer.parseInt(args[4]);
        ItemStack spawnerItem = data.toItemStack(amt, multi);

        if (multi > Setting.SPAWNERS_MAX.getInt()) {
            sender.sendMessage(Methods.formatText(References.getPrefix() + "&7 The multiplier &6" + multi + "&7 is above this spawner types maximum stack size."));
            return ReturnType.FAILURE;
        }

        if (args[1].toLowerCase().equals("all")) {
            for (Player pl : Bukkit.getOnlinePlayers()) {
                pl.getInventory().addItem(spawnerItem);
                pl.sendMessage(Methods.formatText(References.getPrefix() + instance.getLocale().getMessage("command.give.success", amt, Methods.compileName(data, multi, false))));
            }
        } else {
            Player pl = Bukkit.getPlayerExact(args[1]);
            pl.getInventory().addItem(spawnerItem);
            pl.sendMessage(Methods.formatText(References.getPrefix() + instance.getLocale().getMessage("command.give.success", amt, Methods.compileName(data, multi, false))));

        }
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(EpicSpawners instance, CommandSender sender, String... args) {
        if (args.length == 2) {
            List<String> players = new ArrayList<>();
            players.add("All");
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return players;
        } else if (args.length == 3) {
            List<String> spawners = new ArrayList<>();
            spawners.add("Random");
            for (SpawnerData spawnerData : instance.getSpawnerManager().getAllSpawnerData()) {
                spawners.add(spawnerData.getIdentifyingName().replace( " ", "_"));
            }
            return spawners;
        } else if (args.length == 4) {
            List<String> values = new ArrayList<>();
            for (int i = 1; i <= Setting.SPAWNERS_MAX.getInt(); i++) {
                values.add(String.valueOf(i));
            }
            return values;
        } else if (args.length == 5) {
            return Arrays.asList("1", "2", "3", "4", "5");
        }
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epicspawners.admin.give";
    }

    @Override
    public String getSyntax() {
        return "/es give [player/all] [spawnertype/random] [amount] [stack-size]";
    }

    @Override
    public String getDescription() {
        return "Gives an operator the ability to spawn a spawner of his or her choice.";
    }
}

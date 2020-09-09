package com.songoda.epicspawners.commands;

import com.google.common.collect.Iterables;
import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.utils.TextUtils;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.settings.Settings;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CommandGive extends AbstractCommand {

    private final EpicSpawners plugin;

    public CommandGive(EpicSpawners plugin) {
        super(false, "give");
        this.plugin = plugin;
    }

    @Override
    protected AbstractCommand.ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length <= 2 || args.length > 4) {
            return ReturnType.SYNTAX_ERROR;
        }
        if (Bukkit.getPlayerExact(args[0]) == null && !args[0].toLowerCase().equals("all")) {
            plugin.getLocale().newMessage("&cThat username does not exist, or the user is not online!").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }
        int multi = 1;

        SpawnerData data = null;
        for (SpawnerData spawnerData : plugin.getSpawnerManager().getAllSpawnerData()) {
            String input = args[1].toUpperCase().replace("_", "").replace(" ", "");
            String compare = spawnerData.getIdentifyingName().toUpperCase().replace("_", "").replace(" ", "");
            if (input.equals(compare))
                data = spawnerData;
        }

        if (data == null && !args[1].equalsIgnoreCase("random")) {
            plugin.getLocale().newMessage("&7The entity Type &6" + args[1] + " &7does not exist. Try one of these:").sendPrefixedMessage(sender);
            StringBuilder list = new StringBuilder();

            for (SpawnerData spawnerData : plugin.getSpawnerManager().getAllSpawnerData()) {
                list.append(spawnerData.getIdentifyingName().toUpperCase().replace(" ", "_")).append("&7, &6");
            }
            sender.sendMessage(TextUtils.formatText("&6" + list));
            return ReturnType.FAILURE;
        }
        if (args[1].equalsIgnoreCase("random")) {
            Collection<SpawnerData> list = plugin.getSpawnerManager().getAllEnabledSpawnerData();
            Random rand = new Random();
            data = Iterables.get(list, rand.nextInt(list.size()));
        }
        if (args.length == 3) {
            if (!Methods.isInt(args[2])) {
                plugin.getLocale().newMessage("&6" + args[2] + "&7 is not a number.").sendPrefixedMessage(sender);
                return ReturnType.SYNTAX_ERROR;
            }
            int amt = Integer.parseInt(args[2]);
            ItemStack spawnerItem = data.toItemStack(Integer.parseInt(args[2]));
            if (args[0].toLowerCase().equals("all")) {
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    Map<Integer, ItemStack> overflow = pl.getInventory().addItem(spawnerItem);
                    for (ItemStack item : overflow.values())
                        pl.getWorld().dropItemNaturally(pl.getLocation(), item);
                    plugin.getLocale().getMessage("command.give.success").processPlaceholder("amount", amt).processPlaceholder("type", data.getCompiledDisplayName(multi)).sendPrefixedMessage(pl);
                }
            } else {
                Player pl = Bukkit.getPlayerExact(args[0]);
                Map<Integer, ItemStack> overflow = pl.getInventory().addItem(spawnerItem);
                for (ItemStack item : overflow.values())
                    pl.getWorld().dropItemNaturally(pl.getLocation(), item);
                plugin.getLocale().getMessage("command.give.success").processPlaceholder("amount", amt).processPlaceholder("type", data.getCompiledDisplayName(multi)).sendPrefixedMessage(pl);

            }
            return ReturnType.FAILURE;
        }
        if (!Methods.isInt(args[2])) {
            plugin.getLocale().newMessage("&6" + args[2] + "&7 is not a number.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }
        if (!Methods.isInt(args[3])) {
            plugin.getLocale().newMessage("&6" + args[3] + "&7 is not a number.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }
        int amt = Integer.parseInt(args[2]);

        multi = Integer.parseInt(args[3]);
        ItemStack spawnerItem = data.toItemStack(amt, Math.max(0, multi));

        if (multi > Settings.SPAWNERS_MAX.getInt()) {
            plugin.getLocale().newMessage("&7 The multiplier &6" + multi + "&7 is above this spawner types maximum stack size.").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        if (args[0].toLowerCase().equals("all")) {
            for (Player pl : Bukkit.getOnlinePlayers()) {
                pl.getInventory().addItem(spawnerItem);
                plugin.getLocale().getMessage("command.give.success").processPlaceholder("amount", amt).processPlaceholder("type", data.getCompiledDisplayName(multi)).sendPrefixedMessage(pl);
            }
        } else {
            Player pl = Bukkit.getPlayerExact(args[0]);
            pl.getInventory().addItem(spawnerItem);
            plugin.getLocale().getMessage("command.give.success").processPlaceholder("amount", amt).processPlaceholder("type", data.getCompiledDisplayName(multi)).sendPrefixedMessage(pl);

        }
        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (args.length == 1) {
            List<String> players = Methods.convertToList(Bukkit.getOnlinePlayers(), Player::getName);
            players.add("All");
            return players;
        } else if (args.length == 2) {
            List<String> spawners = Methods.convertToList(plugin.getSpawnerManager().getAllSpawnerData(), (spawnerData) -> spawnerData.getIdentifyingName().replace(" ", "_"));
            spawners.add("random");
            return spawners;
        } else if (args.length == 3) {
            int max = Settings.SPAWNERS_MAX.getInt();
            List<String> values;
            ;

            if (max <= 0) {
                values = new ArrayList<>(1);
                values.add(String.valueOf(1));
            } else {
                values = new ArrayList<>(max);
                for (int i = 1; i <= max; i++) {
                    values.add(String.valueOf(i));
                }
            }

            return values;
        } else if (args.length == 4) {
            return Arrays.asList("1", "2", "3", "4", "5");
        }
        return Collections.emptyList();
    }

    @Override
    public String getPermissionNode() {
        return "epicspawners.admin.give";
    }

    @Override
    public String getSyntax() {
        return "give [player/all] [spawnertype/random] [amount] [stack-size]";
    }

    @Override
    public String getDescription() {
        return "Gives an operator the ability to spawn a spawner of his or her choice.";
    }
}

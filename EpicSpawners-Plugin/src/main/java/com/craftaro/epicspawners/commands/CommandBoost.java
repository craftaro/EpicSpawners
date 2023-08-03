package com.craftaro.epicspawners.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.core.utils.NumberUtils;
import com.craftaro.core.utils.TimeUtils;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.boost.types.BoostedPlayerImpl;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandBoost extends AbstractCommand {
    private final EpicSpawners plugin;

    public CommandBoost(EpicSpawners plugin) {
        super(CommandType.CONSOLE_OK, "boost");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length < 2) {
            plugin.getLocale().newMessage("&7Syntax error...").sendPrefixedMessage(sender);
            return ReturnType.SYNTAX_ERROR;
        }
        if (!NumberUtils.isInt(args[1])) {
            plugin.getLocale().newMessage("&6" + args[1] + " &7is not a number...").sendPrefixedMessage(sender);
            return ReturnType.SYNTAX_ERROR;
        }

        long duration = 0L;

        if (args.length > 2) {
            for (String line : args) {
                long time = TimeUtils.parseTime(line);
                duration += time;

            }
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            plugin.getLocale().newMessage("&cThat player does not exist or is not online...").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        BoostedPlayerImpl boost = new BoostedPlayerImpl(player, Integer.parseInt(args[1]), duration == 0L ? Long.MAX_VALUE : System.currentTimeMillis() + duration);
        plugin.getBoostManager().addBoost(boost);
        plugin.getDataManager().save(boost, "player", player.getUniqueId().toString());
        plugin.getLocale()
                .newMessage("&6" + player.getName() + "&7 has been given a spawner boost of &6" + args[1] + "&7" + (duration == 0L ? "" : (" for " + TimeUtils.makeReadable(duration))) + "&7.")
                .sendPrefixedMessage(sender);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
        } else if (args.length == 2) {
            return Arrays.asList("1", "2", "3", "4", "5");
        } else if (args.length == 3) {
            return Arrays.asList("1m", "2h", "3d", "4d");
        }
        return Collections.emptyList();
    }

    @Override
    public String getPermissionNode() {
        return "epicspawners.admin.boost";
    }

    @Override
    public String getSyntax() {
        return "boost <player> <amount> [duration]";
    }

    @Override
    public String getDescription() {
        return "This allows you to boost the amount of spawns that are got from placed spawners.";
    }
}

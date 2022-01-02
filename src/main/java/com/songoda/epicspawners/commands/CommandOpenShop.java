package com.songoda.epicspawners.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.gui.SpawnerShopGui;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandOpenShop extends AbstractCommand {

    private final EpicSpawners plugin;

    public CommandOpenShop(EpicSpawners plugin) {
        super(false, "openshop");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length < 1 || args.length > 2) {
            return ReturnType.SYNTAX_ERROR;
        }

        if (Bukkit.getPlayerExact(args[0]) == null && !args[0].equalsIgnoreCase("all")) {
            plugin.getLocale().newMessage("&cThat username does not exist, or the user is not online!").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        this.openShop(args[0]);
        return ReturnType.SUCCESS;
    }

    private void openShop(String who) {
        if (who.equalsIgnoreCase("all")) {
            for (Player pl : Bukkit.getOnlinePlayers()) {
                plugin.getGuiManager().showGUI(pl, new SpawnerShopGui(plugin, pl));
            }
        } else {
            Player pl = Bukkit.getPlayerExact(who);
            plugin.getGuiManager().showGUI(pl, new SpawnerShopGui(plugin, pl));
        }
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        if (args.length == 1) {
            List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            players.add("All");
            return players;
        }
        return Collections.emptyList();
    }

    @Override
    public String getPermissionNode() {
        return "epicspawners.admin.openshop";
    }

    @Override
    public String getSyntax() {
        return "openshop [player/all]";
    }

    @Override
    public String getDescription() {
        return "Gives an operator the ability to open the spawner shop for someone.";
    }
}

package com.craftaro.epicspawners.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.epicspawners.EpicSpawners;
import com.craftaro.epicspawners.gui.SpawnerShopGui;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandOpenShop extends AbstractCommand {
    private final EpicSpawners plugin;

    public CommandOpenShop(EpicSpawners plugin) {
        super(CommandType.CONSOLE_OK, "openshop");

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
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                String name = player.getName();
                players.add(name);
            }

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

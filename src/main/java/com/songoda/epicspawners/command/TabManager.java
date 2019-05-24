package com.songoda.epicspawners.command;

import com.songoda.epicspawners.EpicSpawners;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class TabManager implements TabCompleter {

    private final CommandManager commandManager;

    TabManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] strings) {
        for (AbstractCommand abstractCommand : commandManager.getCommands()) {
            if (abstractCommand.getCommand() != null && abstractCommand.getCommand().equalsIgnoreCase(command.getName().toLowerCase())) {
                if (strings.length == 1) {
                    List<String> subs = new ArrayList<>();
                    for (AbstractCommand ac : commandManager.getCommands()) {
                        if (ac.getSubCommand() == null) continue;
                        subs.addAll(ac.getSubCommand());
                    }
                    subs.removeIf(s -> !s.toLowerCase().startsWith(strings[0].toLowerCase()));
                    return subs;
                }
            } else if (strings.length != 0 && abstractCommand.getParent() != null && abstractCommand.getParent().getCommand().equalsIgnoreCase(command.getName().toLowerCase())) {
                String cmd = strings[0];
                String cmd2 = strings.length >= 2 ? String.join(" ", strings[0], strings[1]) : null;
                for (String cmds : abstractCommand.getSubCommand()) {
                    if (cmd.equalsIgnoreCase(cmds) || (cmd2 != null && cmd2.equalsIgnoreCase(cmds))) {
                        List<String> list = abstractCommand.onTab(EpicSpawners.getInstance(), sender, strings);
                        String str = strings[strings.length - 1];
                        if (list != null && str != null && str.length() >= 1) {
                            try {
                                list.removeIf(s -> !s.toLowerCase().startsWith(str.toLowerCase()));
                            } catch (UnsupportedOperationException ignored) {}
                        }
                        return list;
                    }
                }
            }
        }
        return null;
    }
}

package com.songoda.epicspawners.command;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class AbstractCommand {

    private AbstractCommand parent;

    private String command;

    protected AbstractCommand(String command, AbstractCommand parent) {
        this.command = command;
        this.parent = parent;
    }

    public AbstractCommand getParent() {
        return parent;
    }


    public String getCommand() {
        return command;
    }

    protected abstract boolean runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args);
}

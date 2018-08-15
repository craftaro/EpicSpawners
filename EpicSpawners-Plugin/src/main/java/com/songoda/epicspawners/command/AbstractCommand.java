package com.songoda.epicspawners.command;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import org.bukkit.command.CommandSender;

public abstract class AbstractCommand {

    private final AbstractCommand parent;

    private final String permissionNode;

    private final String command;

    protected AbstractCommand(String command, String permissionNode, AbstractCommand parent) {
        this.command = command;
        this.parent = parent;
        this.permissionNode = permissionNode;
    }

    public AbstractCommand getParent() {
        return parent;
    }


    public String getCommand() {
        return command;
    }

    public String getPermissionNode() {
        return permissionNode;
    }

    protected abstract boolean runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args);
}

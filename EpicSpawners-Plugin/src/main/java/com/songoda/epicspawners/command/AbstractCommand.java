package com.songoda.epicspawners.command;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import org.bukkit.command.CommandSender;

public abstract class AbstractCommand {

    private final AbstractCommand parent;

    private final String permissionNode;

    private final String command;

    private final boolean noConsole;

    protected AbstractCommand(String command, String permissionNode, AbstractCommand parent, boolean noConsole) {
        this.command = command;
        this.parent = parent;
        this.permissionNode = permissionNode;
        this.noConsole = noConsole;
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

    public boolean isNoConsole() {
        return noConsole;
    }

    protected abstract boolean runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args);
}

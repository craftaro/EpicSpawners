package com.songoda.epicspawners.command;

import com.songoda.epicspawners.EpicSpawnersPlugin;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractCommand {

    private final AbstractCommand parent;
    private final boolean noConsole;
    private String command;

    private List<String> subCommand = new ArrayList<>();

    protected AbstractCommand(AbstractCommand parent, boolean noConsole, String... command) {
        if (parent != null) {
            this.subCommand = Arrays.asList(command);
        } else {
            this.command = Arrays.asList(command).get(0);
        }
        this.parent = parent;
        this.noConsole = noConsole;
    }

    public AbstractCommand getParent() {
        return parent;
    }

    public String getCommand() {
        return command;
    }

    public List<String> getSubCommand() {
        return subCommand;
    }

    public void addSubCommand(String command) {
        subCommand.add(command);
    }

    protected abstract ReturnType runCommand(EpicSpawnersPlugin instance, CommandSender sender, String... args);

    protected abstract List<String> onTab(EpicSpawnersPlugin instance, CommandSender sender, String... args);

    public abstract String getPermissionNode();

    public abstract String getSyntax();

    public abstract String getDescription();

    public boolean isNoConsole() {
        return noConsole;
    }

    public enum ReturnType {SUCCESS, FAILURE, SYNTAX_ERROR}
}
